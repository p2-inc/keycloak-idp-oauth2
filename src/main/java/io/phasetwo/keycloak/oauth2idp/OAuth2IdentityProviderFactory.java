package io.phasetwo.keycloak.oauth2idp;

import com.google.auto.service.AutoService;
import java.util.List;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

@AutoService(IdentityProviderFactory.class)
public class OAuth2IdentityProviderFactory
    extends AbstractIdentityProviderFactory<OAuth2IdentityProvider> {
  public static final String PROVIDER_ID = "oauth2";

  @Override
  public String getName() {
    return "OAuth2 Identity provider";
  }

  @Override
  public OAuth2IdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
    return new OAuth2IdentityProvider(session, new OAuth2ScriptedProviderConfig(model));
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
