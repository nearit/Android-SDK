# NearIT Android SDK #

NearIT allows to engage app users by sending **context-aware targeted content**.

[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://developer.android.com/about/dashboards/index.html#Platform) [![license](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)
[![Download](https://api.bintray.com/packages/nearit/NearIT-Android-SDK/it.near.sdk%3Anearit/images/download.svg) ](https://bintray.com/nearit/NearIT-Android-SDK/it.near.sdk%3Anearit/_latestVersion)
![CircleCI](https://circleci.com/bb/Synapsev2/android-sdk.svg?style=svg&circle-token=db61dcef36cb607df966d415992533076b8927bc)
[![Documentation Status](https://img.shields.io/badge/docs-visit-green.svg)](https://docs.nearit.com/android/installation/)

## Recipes

NearIT allows to manage apps by defining *recipes*. Those are simple rules made of 3 ingredients:
- **WHO**: define the target users
- **WHAT**: define what action NearIT should do
- **TRIGGER**: define when the action should be triggered

## How it works

[**NearIT web interface**](https://go.nearit.com/) allows you to configure all the features, in a snap.
Once the settings are configured, **everyone** - even people without technical skills - can manage context-aware mobile contents.

**NearIT SDK** synchronize with servers and behave accordingly to the settings and the recipes. Any content will be delivered at the right time, you just need to handle its presentation.

## Installation

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java
dependencies {
    compile 'it.near.sdk:nearit:2.10.8'
}
```

In AndroidManifest.xml, add the following element as a child of the `<application>` element, by inserting it just before the closing `</application>` tag:

```xml
<meta-data
       android:name="near_api_key"
       android:value="<your-near-api-key>" />
```
then, re-build your application.

In your app, you can access the NearItManager instance with 
```java
NearItManager.getInstance()
```

## Integration guide

For information on how to integrate all NearIT features in your app, follow the [integration guide](https://docs.nearit.com/android/installation/)


## Migration

If you are upgrading your NearIt dependency from 2.1.x follow this [guide](docs/migration/migration-2.2.md)
