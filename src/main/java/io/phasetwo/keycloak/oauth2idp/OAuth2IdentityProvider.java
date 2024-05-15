package io.phasetwo.keycloak.oauth2idp;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ScriptModel;
import org.keycloak.scripting.EvaluatableScriptAdapter;
import org.keycloak.scripting.ScriptingProvider;

import java.util.Optional;

@JBossLog
public class OAuth2IdentityProvider extends OIDCIdentityProvider {

    public static final String DEFAULT_IDENTITY_SCRIPT = "script";
    private final String scriptCode;

    public OAuth2IdentityProvider(KeycloakSession session,
                                  OAuth2IdentityProviderConfig config) {
        super(session, config);
        scriptCode = Optional.ofNullable(config.getIdentityScript()).orElse(DEFAULT_IDENTITY_SCRIPT);
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        var scriptSource = getScriptCode();
        var realm = session.getContext().getRealm();
        ScriptingProvider scripting = session.getProvider(ScriptingProvider.class);
        ScriptModel scriptModel = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, "oauth2-identity-script", scriptSource, null);

        EvaluatableScriptAdapter script = scripting.prepareEvaluatableScript(scriptModel);
        Object attributeValue;
        try {
            attributeValue = script.eval((bindings) -> {
                bindings.put("accessToken", accessToken);
                bindings.put("realm", realm);
            });
            //If the result is an array or is iterable, get all values


        } catch (Exception ex) {
            log.error("Error during execution of ProtocolMapper script", ex);
            //fallback logic
        }

        BrokeredIdentityContext user = new BrokeredIdentityContext("test");

        return user;
    }

    private String getScriptCode() {
        return scriptCode;
    }
}