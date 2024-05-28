# GitHub OAuth2 Identity Provider

## Contents

## Overview
This documentation is dedicated to the use of "OAuth2" Identity provider. This section describes the SSO setup for a GitHub developer account.

## Prerequisite

Create a new GitHub [account](https://github.com/settings/developers).

## Setup

1. Add OAuth2 Identity Provider
2. Populate `clientId` and `clientSecret` with the values obtained from the [GitHub account](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps).
3. Add `https://github.com/login/oauth/authorize` to Authorization URL.
4. Add `https://github.com/login/oauth/access_token` to Token URL.
5. In the `Advanced Settings` section add the `user:email` scope.

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
var userResponse = SimpleHttp.doGet("https://api.github.com/user", session)
                         .header("Authorization", "Bearer " + accessToken)
                         .asString();
var emailResponse = SimpleHttp.doGet("https://api.github.com/user/emails", session)
                         .header("Authorization", "Bearer " + accessToken)
                         .asString();
                         
/**
parse response into a JS object
*/
var user = JSON.parse(userResponse);
var emails = JSON.parse(emailResponse);

/**
Do email find logic
*/
var primaryEmail = emails.filter(function(email){return email.primary === true}).shift();

/**
Create the standard profile for user.
*/                      
var profile = new BrokeredUserProfile();
profile.setUsername(user.login);
profile.setFirstName(user.name);
profile.setLastName(user.name);
profile.setEmail(primaryEmail.email);

/**
return profile
*/   
profile;
```