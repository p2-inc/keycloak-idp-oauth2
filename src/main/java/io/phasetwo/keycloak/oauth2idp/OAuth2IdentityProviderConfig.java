package io.phasetwo.keycloak.oauth2idp;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class OAuth2IdentityProviderConfig extends OIDCIdentityProviderConfig {

    private static final String IDENTITY_SCRIPT = "identityScript";

    public OAuth2IdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public OAuth2IdentityProviderConfig() {

    }

    public String getIdentityScript() {
        return getConfig().get(IDENTITY_SCRIPT);
    }

    public void setIdentityScript(String script) {
        getConfig().put(IDENTITY_SCRIPT, script);
    }

    public static List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(IDENTITY_SCRIPT)
                .label("Identity script")
                .helpText(
                        "Script to compute the user identity. \n" + //
                                " Available variables: \n" + //
                                " 'accessToken' - the current user third-party access token.\n" + //
                                " 'realm' - the current realm.\n\n" +
                                "To use: the last statement is the value returned to Java.\n" +
                                "The result will be tested if it can be iterated upon (e.g. an array or a collection).\n" +
                                " - If it is not, toString() will be called on the object to get the value of the attribute\n" +
                                " - If it is, toString() will be called on all elements to return multiple attribute values.\n"//
                )
                .defaultValue("/**\n" + //
                        " * Available variables: \n" + //
                        " * accessToken - the current user third-party access token\n" + //
                        " * realm - the current realm\n" + //
                        " */\n\n\n//insert your code here..." //
                )
                .type(ProviderConfigProperty.SCRIPT_TYPE)
                .add().build();
    }
}