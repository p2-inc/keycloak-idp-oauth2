# Active Organizations Authenticator

## Contents

## Overview
This documentation is dedicated to the use of "OAuth2" Identity provider.  

## Script

```
/**
* Available variables:
* httpGet - a function with 2 parameters: url, requestParams
* httpPost - a function with 2 parameters: url, requestParams
* realm - the current realm
  */


var HashMap = Java.type('java.util.HashMap');
var map = new HashMap(); //empty for now

var identity = httpGet("https://api.amazon.com/user/profile", map);

identity;
```