# Box OAuth2 Identity Provider

## Contents

## Overview
This documentation is dedicated to the use of "OAuth2" Identity provider. This section describes the SSO setup for a Box developer account.

## Prerequisite

Create a new Box developer [account](https://support.box.com/hc/en-us/articles/360043697274-Managing-developer-sandboxes-for-Box-admins).

## Setup

1. Add OAuth2 Identity Provider
2. Populate `clientId` and `clientSecret` with the values obtained from the [Box developer account](https://developer.box.com/guides/authentication/oauth2/).
3. Add `https://account.box.com/api/oauth2/authorize` to Authorization URL.
4. Add `https://api.box.com/oauth2/token` to Token URL.
5. In the `Advanced Settings` section add the `root_readonly` scope.

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
var response = SimpleHttp.doGet("https://api.box.com/2.0/users/me", session)
                         .header("authorization", "Bearer "+accessToken)
                         .asString();
                         
/**
parse response into a JS object
*/
var identity = JSON.parse(response);

/**
Create the standard profile for user.
*/          
      
var profile = new BrokeredUserProfile();
profile.setEmail(identity.login);
profile.setUsername(identity.id);
profile.setFirstName(identity.name);
profile.setLastName(identity.name);


/**
return profile
*/   
profile;
```