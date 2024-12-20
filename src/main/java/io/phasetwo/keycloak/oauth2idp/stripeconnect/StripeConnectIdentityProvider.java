package io.phasetwo.keycloak.oauth2idp.stripeconnect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.phasetwo.keycloak.oauth2idp.model.BrokeredUserProfile;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ExchangeExternalToken;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ScriptModel;
import org.keycloak.scripting.EvaluatableScriptAdapter;
import org.keycloak.scripting.ScriptingProvider;
import org.keycloak.utils.StringUtil;

@JBossLog
public class StripeConnectIdentityProvider
    extends AbstractOAuth2IdentityProvider<StripeConnectScriptedProviderConfig>
    implements ExchangeExternalToken {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static final String DEFAULT_IDENTITY_SCRIPT = "script";
  private final String scriptCode;

  public StripeConnectIdentityProvider(
      KeycloakSession session, StripeConnectScriptedProviderConfig config) {
    super(session, config);
    scriptCode = Optional.ofNullable(config.getIdentityScript()).orElse(DEFAULT_IDENTITY_SCRIPT);
  }

  public BrokeredIdentityContext getFederatedIdentity(String response) {
    String accessToken =
        this.extractTokenFromResponse(response, this.getAccessTokenResponseParameter());
    var stripeUserId = this.extractTokenFromResponse(response, "stripe_user_id");

    if (accessToken == null) {
      throw new IdentityBrokerException(
          "No access token available in OAuth server response: " + response);
    } else {
      BrokeredIdentityContext context = doGetFederatedIdentity(accessToken, stripeUserId);
      context.getContextData().put("FEDERATED_ACCESS_TOKEN", accessToken);
      return context;
    }
  }

  protected BrokeredIdentityContext doGetFederatedIdentity(
      String accessToken, String stripeUserId) {
    var scriptSource = getScriptCode();
    var realm = session.getContext().getRealm();
    ScriptingProvider scripting = session.getProvider(ScriptingProvider.class);
    ScriptModel scriptModel =
        scripting.createScript(
            realm.getId(),
            ScriptModel.TEXT_JAVASCRIPT,
            "stripe-identity-script",
            scriptSource,
            null);

    EvaluatableScriptAdapter script = scripting.prepareEvaluatableScript(scriptModel);
    Object response = null;
    try {
      response =
          script.eval(
              (bindings) -> {
                bindings.put("realm", realm);
                bindings.put("accessToken", accessToken);
                bindings.put("stripe_user_id", stripeUserId);
                bindings.put("session", session);
              });

    } catch (Exception ex) {
      log.error("Error during execution of Identity script", ex);
      throw new IdentityBrokerException("Could not obtain user profile from script.", ex);
    }

    if (!(response instanceof BrokeredUserProfile profile)) {
      throw new IdentityBrokerException("Script response must be instance of BrokeredUserProfile.");
    }

    logger.tracef("profile retrieved from identity script: %s", profile);

    JsonNode node = mapper.convertValue(profile, JsonNode.class);
    return extractIdentityFromProfile(null, node);
  }

  @Override
  protected BrokeredIdentityContext extractIdentityFromProfile(
      EventBuilder event, JsonNode profile) {
    var username = getJsonProperty(profile, "username");
    if (StringUtil.isNullOrEmpty(username)) {
      throw new IdentityBrokerException("BrokeredUserProfile username must not be null.");
    }

    BrokeredIdentityContext user = new BrokeredIdentityContext(username, getConfig());

    user.setUsername(username);
    user.setLastName(getJsonProperty(profile, "lastName"));
    user.setFirstName(getJsonProperty(profile, "firstName"));
    user.setEmail(getJsonProperty(profile, "email"));
    user.setIdp(this);

    var mappingContext = profile.get("mappingContext");
    if (mappingContext != null) {
      AbstractJsonUserAttributeMapper.storeUserProfileForMapper(
          user, mappingContext, getConfig().getAlias());
    }

    return user;
  }

  @Override
  protected String getDefaultScopes() {
    return ""; // no default scope
  }

  private String getScriptCode() {
    return scriptCode;
  }
}
