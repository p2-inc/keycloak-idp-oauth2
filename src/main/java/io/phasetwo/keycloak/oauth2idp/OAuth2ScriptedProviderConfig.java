package io.phasetwo.keycloak.oauth2idp;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class OAuth2ScriptedProviderConfig extends OAuth2IdentityProviderConfig {

    private static final String IDENTITY_SCRIPT = "identityScript";
    public static final String TOKEN_URL = "tokenUrl";
    public static final String AUTHORIZATION_URL = "authorizationUrl";

    public OAuth2ScriptedProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public OAuth2ScriptedProviderConfig() {

    }

    public String getIdentityScript() {
        return getConfig().get(IDENTITY_SCRIPT);
    }

    public void setIdentityScript(String script) {
        getConfig().put(IDENTITY_SCRIPT, script);
    }

    public void setTokenUrl(String tokenUrl) {
        super.setTokenUrl(tokenUrl);
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        super.setAuthorizationUrl(authorizationUrl);
    }

    public static List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(AUTHORIZATION_URL)
                .label("Authorize URL")
                .required(true)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property()
                .name(TOKEN_URL)
                .label("Token URL")
                .required(true)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property()
                .name(IDENTITY_SCRIPT)
                .label("Identity script")
                .helpText(
                        "Script to compute the user identity. \n" + //
                                " Available variables: \n" + //
                                " 'realm' - the current realm.\n\n" +
                                " 'session' - the current keycloakSession.\n\n" +
                                " 'accessToken' - the authentication session accessToken.\n\n" +
                                "To use: the last statement is the value returned to Java.\n" +
                                "The result will be tested if it can be iterated upon (e.g. an array or a collection).\n" +
                                " - If it is not, toString() will be called on the object to get the value of the attribute\n" +
                                " - If it is, toString() will be called on all elements to return multiple attribute values.\n"
                )
                .defaultValue("/**\n" + //
                        " * Available variables: \n" + //
                        " * realm - the current realm.\n" +
                        " * session - the current keycloakSession.\n" +
                        " * accessToken - the authentication session accessToken.\n" +
                        "*/\n\n" +
                        "/** add Java dependencies */\n" +
                        "var SimpleHttp = Java.type('org.keycloak.broker.provider.util.SimpleHttp');\n" +
                        "var BrokeredUserProfile = Java.type('io.phasetwo.keycloak.oauth2idp.model.BrokeredUserProfile');\n\n" +
                        "/** //insert your code here... */\n\n" +
                        "/** Create the user profile. */\n" +
                        " var profile = new BrokeredUserProfile();\n" +
                        " profile.setEmail(identity.email);\n" +
                        " profile.setUsername(identity.user_id);\n" +
                        " profile.setLastName(identity.name);\n\n" +
                        "/**\n" +
                        "return profile. BrokeredUserProfile is enforced by this provider.Returning anything else will throw an exception \n" +
                        "*/\n\n" +
                        "profile;"
                )
                .type(ProviderConfigProperty.SCRIPT_TYPE)
                .add()
                .build();
    }
}