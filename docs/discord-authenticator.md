# Discord OAuth2 Identity Provider

## Contents

## Overview

This documentation is dedicated to the use of "OAuth2" Identity provider. This section describes the SSO setup for a
Discord developer account.

## Prerequisite

Create a new Discord developer [account](https://discord.com/developers).

## Setup

1. Add OAuth2 Identity Provider
2. Populate `clientId` and `clientSecret` with the values obtained from the [Discord developer account](https://discord.com/developers/docs/topics/oauth2).
3. Add `https://discord.com/oauth2/authorize` to Authorization URL.
4. Add `https://discord.com/api/oauth2/token` to Token URL.
5. In the `Advanced Settings` section add the `identify email` scope.

## Identity Script

```
/**
 * Available variables: 
 * realm - the current realm.
 * session - the current keycloakSession.
 * accessToken - the authentication session accessToken.
 */

var SimpleHttp = Java.type('org.keycloak.broker.provider.util.SimpleHttp');
var BrokeredUserProfile = Java.type('io.phasetwo.keycloak.oauth2idp.model.BrokeredUserProfile');

/**
Use SimpleHttp from Keycloak class to perform http calls to profile endpoint
*/
var response = SimpleHttp.doGet("https://discord.com/api/oauth2/@me", session)
                         .header("Authorization", "Bearer " + accessToken)
                         .asString();
                         
/**
parse response into a JS object
*/
var identity = JSON.parse(response);
/**
Create the standard profile for user.
*/                      
var profile = new BrokeredUserProfile();
profile.setUsername(identity.user.username);
profile.setFirstName(identity.user.global_name);
profile.setLastName(identity.user.global_name);

/**
return profile
*/   
profile;
```

## Guilds // Future improvement

In order to create a custom logic for organizations or groups based on guilds add the `guilds` scope to the provider Scopes setting.
The guilds list can be a obtained by performing the following HTTP call:

```
   var guilds = SimpleHttp.doGet("https://discord.com/api/users/@me/guilds", session)
                          .header("Authorization", "Bearer " + accessToken)
                          .asJson();
```