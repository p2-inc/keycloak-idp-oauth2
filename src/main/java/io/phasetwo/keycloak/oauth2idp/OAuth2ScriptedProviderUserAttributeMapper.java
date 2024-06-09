package io.phasetwo.keycloak.oauth2idp;

import com.google.auto.service.AutoService;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.IdentityProviderMapper;

@AutoService(IdentityProviderMapper.class)
public class OAuth2ScriptedProviderUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    public static final String PROVIDER_ID = "oauth2-user-attribute-mapper";
    private static final String[] cp = new String[]{OAuth2IdentityProviderFactory.PROVIDER_ID};


    public OAuth2ScriptedProviderUserAttributeMapper() {
        super();

        // A trick to change the help text and label from Social provider
        getConfigProperties()
                .stream()
                .filter(config -> config.getName().equals("jsonField"))
                .forEach(config -> {
                    config.setLabel("Mapping context JSON Field Path");
                    config.setHelpText("Path of field in OAuthProvider provider User Profile JSON data to get value from. You can use dot notation for nesting . A array index can be accessed with the dot notation as well. Eg. 'contact.address.0.country'.");
                });
    }


    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Import user profile information if it exists in provider `mappingContext` JSON into the specified user attribute.";
    }
}