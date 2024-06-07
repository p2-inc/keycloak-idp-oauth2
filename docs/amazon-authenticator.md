# Amazon OAuth2 Identity Provider

## Contents

## Overview
This documentation is dedicated to the use of "OAuth2" Identity provider. This section describes the SSO setup for an Amazon developer account.

## Prerequisite

Create a new Amazon developer [account](https://developer.amazon.com/).

## Setup

1. Add OAuth2 Identity Provider
2. Populate `clientId` and `clientSecret` with the values obtained from the [Amazon developer account](https://developer.amazon.com/settings/console/securityprofile/overview.html).
3. Add `https://www.amazon.com/ap/oa` to Authorization URL.
4. Add `https://api.amazon.com/auth/o2/token` to Token URL.
5. In the `Advanced Settings` section add the `profile` scope.

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
var response = SimpleHttp.doGet("https://api.amazon.com/user/profile", session)
                         .param("access_token", accessToken)
                         .asString();
                         
/**
parse response into a JS object
*/
var identity = JSON.parse(response);

/**
Create the standard profile for user.
*/                      
var profile = new BrokeredUserProfile();
profile.setEmail(identity.email);
profile.setUsername(identity.user_id);
profile.setFirstName(identity.name);
profile.setLastName(identity.name);

/**
return profile
*/   
profile;
```