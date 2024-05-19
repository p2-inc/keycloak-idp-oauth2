package io.phasetwo.keycloak.oauth2idp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ExchangeExternalToken;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ScriptModel;
import org.keycloak.scripting.EvaluatableScriptAdapter;
import org.keycloak.scripting.ScriptingProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@JBossLog
public class OAuth2IdentityProvider extends AbstractOAuth2IdentityProvider<OAuth2ScriptedProviderConfig> implements ExchangeExternalToken {

    private static ObjectMapper mapper = new ObjectMapper();

    public static final String DEFAULT_IDENTITY_SCRIPT = "script";
    private final String scriptCode;

    public OAuth2IdentityProvider(KeycloakSession session,
                                  OAuth2ScriptedProviderConfig config) {
        super(session, config);
        scriptCode = Optional.ofNullable(config.getIdentityScript()).orElse(DEFAULT_IDENTITY_SCRIPT);
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        var scriptSource = getScriptCode();
        var realm = session.getContext().getRealm();
        ScriptingProvider scripting = session.getProvider(ScriptingProvider.class);
        ScriptModel scriptModel = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, "oauth2-identity-script", scriptSource, null);

        //We can bind a function
        BiFunction<String, HashMap<String, String>, JsonNode> httpGet = (url, requestParams) -> {
            try {
                return SimpleHttp.doGet(url, session)
                        //will add parameters: headers, queryParams, body from requestParams
                        .param("access_token", accessToken)
                        .asJson();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        BiFunction<String, HashMap<String, String>, JsonNode> httpPost = (url, requestParams) -> {
            try {
                return SimpleHttp.doPost(url, session)
                       //will add parameters: headers, queryParams, body from requestParams
                        .asJson();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        EvaluatableScriptAdapter script = scripting.prepareEvaluatableScript(scriptModel);
        Object response = null;
        try {
            response = script.eval((bindings) -> {
                bindings.put("realm", realm);
                bindings.put("httpGet", httpGet);
                bindings.put("httpPost", httpPost);
            });


        } catch (Exception ex) {
            log.error("Error during execution of ProtocolMapper script", ex);
            //fallback logic
        }

        JsonNode profile = (JsonNode) response;
        logger.tracef("profile retrieved from github: %s", profile);

        return extractIdentityFromProfile(null, profile);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        //We need to return a standard Map which populates the user profile
        //smth like {
        //      {username: "test"},
        //      {lastname: "test"},
        //      {firstname: "firstname"},
        //      {email: "firstname"},
        //      {attributes: {..}}
        //}
        var username = getJsonProperty(profile, "user_id");
        BrokeredIdentityContext user = new BrokeredIdentityContext(username);

        user.setUsername(username);
        user.setFirstName(getJsonProperty(profile, getJsonProperty(profile, "name")));
        user.setEmail(getJsonProperty(profile, "email"));
        user.setIdpConfig(getConfig());
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }


    @Override
    protected String getDefaultScopes() {
        return "";
    }

    private String getScriptCode() {
        return scriptCode;
    }
}