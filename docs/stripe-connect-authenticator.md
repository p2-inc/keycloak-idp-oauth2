# Stripe Connect Identity Provider

## Contents

## Overview
This documentation is dedicated to the use of "stripe-connect" identity provider. This section describes the OAuth setup for a Stripe Connect .

## Prerequisite

Create a new Stripe [account](https://dashboard.stripe.com).
Use the steps in the following guideline to configure OAuth login with [StripeConnect](https://docs.stripe.com/connect/oauth-reference#get-authorize).
The `test mode` should be used when testing the stripe integration.

In the Stripe connect [Onboarding options](https://dashboard.stripe.com/test/settings/connect/onboarding-options/oauth) please add the `redirect_uri` to the identity provider.

## Setup

1. Add a new Stripe Connect Identity provider
2. Populate `clientId` from Stripe connect [Onboarding options](https://dashboard.stripe.com/test/settings/connect/onboarding-options/oauth).
3. Populate `clientSecret` with the value of the [API key](https://dashboard.stripe.com/test/apikeys).
3. Add `https://connect.stripe.com/oauth/authorize` to Authorization URL.
4. Add `https://connect.stripe.com/oauth/token` to Token URL.
5. In the `Advanced Settings` section add the `read_only` scope.

## Identity Script

```
/**
 * Available variables: 
 * realm - the current realm.
 * session - the current keycloakSession.
 * accessToken - the authentication session accessToken.
 * stripe_user_id - user id obtained from Stripe.
*/

var SimpleHttp = Java.type('org.keycloak.broker.provider.util.SimpleHttp');
var BrokeredUserProfile = Java.type('io.phasetwo.keycloak.oauth2idp.model.BrokeredUserProfile');

var apiKey = "$your-stripe-api-key";

/**
Use SimpleHttp from Keycloak class to perform http calls to profile endpoint
*/
var accountResponse = SimpleHttp.doGet("https://api.stripe.com/v1/accounts/"+ stripe_user_id, session)
                         .authBasic(apiKey, "" )
                         .asString();
/**
parse response into a JS object
*/
var account = JSON.parse(accountResponse);

/**
Create the standard profile for user.
*/                      
var profile = new BrokeredUserProfile();
profile.setUsername(account.id);
profile.setEmail(account.email);

/**
return profile
*/   
profile;
```