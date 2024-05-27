# Dropbox OAuth2 Identity Provider

## Contents

## Overview
This documentation is dedicated to the use of "OAuth2" Identity provider. This section describes the SSO setup for a Dropbox developer account.

## Prerequisite

Create a new Dropbox developer account.

## Known-issue

OAuth2 identity provider cannot use [users/get_current_account]("https://www.dropbox.com/developers/documentation/http/documentation#users-get_current_account) API endpoint for Dropbox integration.
The `SimpleHttp` JAVA component cannot perform a `doPost` call with empty body. 

E.q:
Add scope: account_info.read
Do the following HTTP call:

```
var response = SimpleHttp.doPost("https://api.dropboxapi.com/2/users/get_current_account", session)
                         .header("Authorization", "Bearer " + accessToken)
                         .json({}) /**SimpleHttp deosn't support POST call without a body */
                         .asString();
```
Response: 

``
Error in call to API function "users/get_current_account": request body: expected null, got value
``

Use the [openid/userinfo]("https://www.dropbox.com/developers/documentation/http/documentation#openid-userinfo") for obtaining the user profile information.

## Setup

1. Add OAuth2 Identity Provider
2. Populate `clientId` and `clientSecret` with the values obtained from the Amazon developer account.
3. Add `https://www.dropbox.com/oauth2/authorize` to Authorization URL.
4. Add `https://api.dropboxapi.com/oauth2/token` to Token URL.
5. In the `Advanced Settings` section add the `openid profile email` scope.

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
var response = SimpleHttp.doPost("https://api.dropboxapi.com/2/openid/userinfo", session)
                         .header("Authorization", "Bearer " + accessToken)
                         .json({}) /**SimpleHttp deosn't support POST call without a body */
                         .asString();
                         
/**
parse response into a JS object
*/
var identity = JSON.parse(response);
/**
Create the standard profile for user.
*/                      
var profile = new BrokeredUserProfile();
profile.setUsername(identity.sub);
profile.setFirstName(identity.given_name);
profile.setLastName(identity.family_name);
profile.setEmail(identity.email);

/**
return profile
*/   
profile;
```

## Dropbox Teams // Future improvement


In order to create a custom logic for organizations or groups based on Dropbox you can use [teams]("https://www.dropbox.com/developers/documentation/http/documentation#users-get_account").

E.q:
Add scope: sharing.read
The teams list can be obtained by performing the following HTTP call:

```
var response = SimpleHttp.doPost("https://api.dropboxapi.com/2/users/get_account", session)
                         .header("Authorization", "Bearer " + accessToken)
                         .json({"account_id": "$accountId"})
                         .asString();
```