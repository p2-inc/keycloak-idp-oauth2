> :rocket: **Try it for free** in the new Phase
> Two [keycloak managed service](https://phasetwo.io/?utm_source=github&utm_medium=readme&utm_campaign=keycloak-idp-oauth2).
> See the [announcement and demo video](https://phasetwo.io/blog/self-service/) for more information.

# keycloak-idp-oauth2

Keycloak scripted OAuth2 identity provider implementation.

This extension is used in the [Phase Two](https://phasetwo.io) cloud offering, and is released here as part of its
commitment to making its [core extensions](https://phasetwo.io/docs/introduction/open-source) open source. Please
consult the [license](COPYING) for information regarding use.

## Quick start

The easiest way to get started is our [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=tags).
Documentation and examples for using it are in the [phasetwo-containers](https://github.com/p2-inc/phasetwo-containers)
repo. The most recent version of this extension is included.

## OAuth2 Scripted identity provider

A standard OAuth 2.0 Identity Provider that retrieves identity information by script.

## Installation

1. Build the jar:

```
mvn clean install
```

2. Copy the jar produced in `target/` to your `providers` directory (for Quarkus) or `standalone/deployments`
   directory (for legacy) and rebuild/restart keycloak.

## Implementation Notes

The identity provider is persisting a script as a configuration value. This script is executed by
the [Nashorn scripting engine]("https://www.oracle.com/technical-resources/articles/java/jf14-nashorn.html").
The script will be executed once the authorization code flow has been executed by the user. This will retrieve the
identity information required for creating a keycloak user.

When writing the script be aware of `Nashorn` has some limitations. It supports ECMAScript 5.1 and some ECMAScript 6
features.

The script must return a `BrokeredUserProfile` otherwise the Identity provider will throw an error.

```java
public class BrokeredUserProfile {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    private Object mappingContext;
}
```

The `username` field of the BrokeredUserProfile is mandatory.

In order to use an identity provider mapper the `mappingContext` must be populated with a JSON object containing the information required for the mapping logic.  
A example can be seen in the Amazon identity provider [script](./docs/amazon-authenticator.md#identity-script).  
There is no need to add the `mappingContext` if no mapping is required after obtaining the identity information.

## Mappers

### Attribute importer mapper

Import user profile information if it exists in provider `mappingContext` JSON into the specified user attribute.

![attribute-mapper](docs/assets/attribute-mapper.png)

## Demo

See the following example provider configurations:

1. [Amazon](./docs/amazon-authenticator.md)
2. [Discord](./docs/discord-authenticator.md)
3. [Dropbox](./docs/dropbox-authenticator.md)
4. [Github](./docs/github-authenticator.md)
5. [Box](./docs/box-authenticator.md)
6. [Stripe Connect](./docs/stripe-connect-authenticator.md)

---

All documentation, source code and other files in this repository are Copyright 2024 Phase Two, Inc.

