package io.phasetwo.keycloak.oauth2idp;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class OAuth2IdentityProviderFactory extends AbstractIdentityProviderFactory<OAuth2IdentityProvider> {
    public static final String PROVIDER_ID = "oauth2";

    @Override
    public String getName() {
        return "OAuth2 Identity provider";
    }

    @Override
    public OAuth2IdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new OAuth2IdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    @Override
    public OIDCIdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return OAuth2IdentityProviderConfig.getConfigProperties();
    }
}