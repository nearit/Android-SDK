# NearIT Android SDK #

NearIT allows to engage app users by sending **context-aware targeted content**.

[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://developer.android.com/about/dashboards/index.html#Platform) [![license](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)
[ ![Download](https://api.bintray.com/packages/catt-stefano/maven/it.near.sdk.core/images/download.svg)](https://bintray.com/catt-stefano/maven/it.near.sdk.core/_latestVersion)
[![Build Status](https://travis-ci.org/nearit/Android-SDK.svg?branch=master)](https://travis-ci.org/nearit/Android-SDK)
[![Documentation Status](https://readthedocs.org/projects/nearit-android/badge/?version=latest)](http://nearit-android.readthedocs.io/en/latest/?badge=latest)

## Recipes

NearIT allows to manage apps by defining “recipes”. Those are simple rules made of 3 ingredients:
- **WHO**: define the target users
- **WHAT**: define what action NearIT should do
- **TRIGGER**: define when the action should be triggered

## How it works

[**NearIT web interface**](https://go.nearit.com/) allows you to configure all the features, in a snap.
Once the settings are configured, **everyone** - even people without technical skills - can manage context-aware mobile contents.

**NearIT SDK** synchronize with servers and behave accordingly to the settings and the recipes. Any content will be delivered at the right time, you just need to handle its presentation.

## Features
* User Segmentation
* Beacon monitoring and ranging
* Geofence monitoring
* Notifications and in-app content
* Analytics

## Installation

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java

dependencies {
    compile 'it.near.sdk.core:nearit:2.1.12'
}
```

In the *onCreate* method of your Application class, initialize a *NearItManager* object, passing the API key as a String


```java
 @Override
    public void onCreate() {
        super.onCreate();
        nearItManager = new NearItManager(this, getResources().getString(R.string.nearit_api_key));
        // calling this method on the Application onCreate is MANDATORY
        nearItManager.initLifecycleMethods(this);
    }
```

## Integration guide

For information on how to integrate all NearIT feautures in your app, visit the [documentation website](http://nearit-android.readthedocs.io/en/latest/?badge=latest)
