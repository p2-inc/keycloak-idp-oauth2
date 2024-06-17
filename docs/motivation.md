# OAuth 2.0 “Identity Provider” for Keycloak

## What’s the difference between OAuth 2.0 and OIDC?
OAuth 2.0 and OpenID Connect (OIDC) are related but distinct protocols used for authentication and authorization in the context of web and mobile applications. While they share similarities, they serve different purposes:

### OAuth 2.0 (Open Authorization 2.0):
- OAuth 2.0 is an authorization framework that enables third-party applications to obtain limited access to a user's protected resources without exposing their credentials.
- It is primarily focused on delegated access, allowing applications to obtain an access token representing specific permissions from a resource owner (typically a user) after they grant consent.
- OAuth 2.0 defines different grant types (such as Authorization Code, Implicit, Client Credentials, etc.) that dictate how access tokens are obtained based on the use case and client type.
- It does not inherently provide identity information about the user but focuses on granting access to resources (like APIs) after obtaining permission.

### OpenID Connect (OIDC):
- OpenID Connect is an identity layer built on top of OAuth 2.0 and provides authentication as well as information about the user (claims) in the form of an ID token.
- OIDC extends OAuth 2.0 by adding an identity layer, allowing clients to obtain information about the authenticated user.
- It defines an ID token, which is a JSON Web Token (JWT) containing identity information such as user ID, name, email, and other claims. This token allows the client application to authenticate the user and access their identity information.
- OIDC uses OAuth 2.0 flows (such as the Authorization Code flow) but adds an ID token to provide user authentication and additional user information.

In summary, OAuth 2.0 is primarily an authorization framework that focuses on granting access tokens for limited access to resources, while OpenID Connect is an authentication layer that extends OAuth 2.0 to provide user authentication and identity information in the form of ID tokens. Often, OIDC is used in conjunction with OAuth 2.0 to handle both authentication and authorization in modern web and mobile applications.

## Why do we need an OAuth 2.0 “Identity Provider”?
Users of Keycloak have the ability to create Identity Providers that are configured with the following parameters
- “Hidden on the Login Page”
- “Account Linking Only”
- “Store tokens” and “Store tokens readable”

This is often used by developers to retrieve access tokens for APIs that have OIDC-compliant authentication flows. Following an account linking flow, the external IdP tokens can be retrieved from Keycloak (https://www.keycloak.org/docs/latest/server_admin/index.html#retrieving-external-idp-tokens) and used with the external API.
However, this is not possible with OAuth 2.0 providers in Keycloak, despite this being a more common mechanism of authenticating and authorizing APIs.

## But some of Keycloak’s “Social Identity Providers” use OAuth 2.0?
Correct. However, the specific social providers that Keycloak has selected implement some kind of ability to retrieve user and identity information from the provider. Through a custom implementation of those methods, Keycloak can retrieve identity information it would have otherwise taken from a standard OIDC interaction. 

## How?
Implement a standard OAuth 2.0 Identity Provider that overrides the identity portions of the `OIDCIdentityProvider` class to allow retrieval of identity information by script. This would be implemented by persisting a custom script as a configuration value of the identity provider, and executing the script (js using Nashorn) with a limited scope once the authorization code flow has been executed by the user, and the IdP has returned information sufficient to authorize retrieval of identity information.

