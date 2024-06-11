/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.phasetwo.keycloak.oauth2idp.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.service.AutoService;
import io.phasetwo.keycloak.oauth2idp.OAuth2IdentityProviderFactory;
import io.phasetwo.keycloak.oauth2idp.stripeconnect.StripeConnectIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.broker.saml.mappers.UsernameTemplateMapper.Target;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.TARGET;
import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.TARGETS;
import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.TRANSFORMERS;
import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.getTarget;

@AutoService(IdentityProviderMapper.class)
public class OAuth2UsernameTemplateMapper extends AbstractIdentityProviderMapper {

    public static final String[] COMPATIBLE_PROVIDERS = {
            OAuth2IdentityProviderFactory.PROVIDER_ID,
            StripeConnectIdentityProviderFactory.PROVIDER_ID
    };

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

    public static final String TEMPLATE = "template";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(TEMPLATE);
        property.setLabel("Template");
        property.setHelpText("Template to use to format the username to import.  Substitutions are enclosed in ${}.  For example: '${ALIAS}.${CLAIM.sub}'.  ALIAS is the provider alias.  CLAIM.<NAME> references mapping context claim. \n"
          + "The substitution can be converted to upper or lower case by appending |uppercase or |lowercase to the substituted value, e.g. '${CLAIM.sub | lowercase}");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("${ALIAS}.${CLAIM.id}");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(TARGET);
        property.setLabel("Target");
        property.setHelpText("Destination field for the mapper. LOCAL (default) means that the changes are applied to the username stored in local database upon user import. BROKER_ID and BROKER_USERNAME means that the changes are stored into the ID or username used for federation user lookup, respectively.");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(TARGETS);
        property.setDefaultValue(Target.LOCAL.toString());
        configProperties.add(property);
    }

    public static final String PROVIDER_ID = "oauth-username-idp-mapper";

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Preprocessor";
    }

    @Override
    public String getDisplayType() {
        return "Username Template Importer";
    }

    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        // preprocessFederatedIdentity gets called anyways, so we only need to set the username if necessary.
        // However, we don't want to set the username when the email is used as username
        if (getTarget(mapperModel.getConfig().get(TARGET)) == Target.LOCAL && !realm.isRegistrationEmailAsUsername()) {
            user.setUsername(context.getModelUsername());
        }
    }

    private static final Pattern SUBSTITUTION = Pattern.compile("\\$\\{([^}]+?)(?:\\s*\\|\\s*(\\S+)\\s*)?\\}");

    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setUserNameFromTemplate(mapperModel, context);
    }

    private void setUserNameFromTemplate(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String template = mapperModel.getConfig().get(TEMPLATE);
        Matcher m = SUBSTITUTION.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String variable = m.group(1);
            UnaryOperator<String> transformer = Optional.ofNullable(m.group(2)).map(TRANSFORMERS::get).orElse(UnaryOperator.identity());

            if (variable.equals("ALIAS")) {
                m.appendReplacement(sb, transformer.apply(context.getIdpConfig().getAlias()));
            } else if (variable.equals("UUID")) {
                m.appendReplacement(sb, transformer.apply(KeycloakModelUtils.generateId()));
            } else if (variable.startsWith("CLAIM.")) {
                var claim = variable.substring("CLAIM.".length());
                var profileJsonNode = (JsonNode)context.getContextData().get("UserInfo");
                var value = AbstractJsonUserAttributeMapper.getJsonValue(profileJsonNode, claim);
                if (value == null) {
                    value = "";
                } else if (value instanceof Collection && ((Collection<?>) value).size() == 1) {
                    // In case the value is list with single value, it might be preferred to avoid converting whole collection toString, but rather use value like "foo" instead of "[foo]"
                    value = ((Collection<?>) value).iterator().next();
                }
                m.appendReplacement(sb, transformer.apply(value.toString()));
            } else {
                m.appendReplacement(sb, m.group(1));
            }

        }
        m.appendTail(sb);

        Target t = getTarget(mapperModel.getConfig().get(TARGET));
        t.set(context, sb.toString());
    }

    @Override
    public String getHelpText() {
        return "Format the username to import.";
    }
}
