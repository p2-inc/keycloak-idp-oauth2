package io.phasetwo.keycloak.oauth2idp.stripeconnect;

import com.google.auto.service.AutoService;
import io.phasetwo.keycloak.oauth2idp.OAuth2ScriptedProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

@AutoService(IdentityProviderFactory.class)
public class StripeConnectIdentityProviderFactory extends AbstractIdentityProviderFactory<StripeConnectIdentityProvider> {
    public static final String PROVIDER_ID = "stripe-connect-provider";

    @Override
    public String getName() {
        return "Stripe Connect Identity provider";
    }

    @Override
    public StripeConnectIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new StripeConnectIdentityProvider(session, new StripeConnectScriptedProviderConfig(model));
    }

    @Override
    public OAuth2ScriptedProviderConfig createConfig() {
        return new OAuth2ScriptedProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return OAuth2ScriptedProviderConfig.getConfigProperties();
    }
}