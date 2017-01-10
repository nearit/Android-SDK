# NearIT Android SDK #

NearIT allows to engage app users by sending **context-aware targeted content**.

[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://developer.android.com/about/dashboards/index.html#Platform) [![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/nearit/Android-SDK/blob/master/LICENSE)
[![Download](https://api.bintray.com/packages/catt-stefano/maven/it.near.sdk.core/images/download.svg)](https://bintray.com/catt-stefano/maven/it.near.sdk.core/_latestVersion)
[![Documentation Status](https://readthedocs.org/projects/nearit-android/badge/?version=latest)](http://nearit-android.readthedocs.io/en/latest/?badge=latest)

## Recipes ##

NearIT allows users to control apps by defining “recipes”.Those are simple rules made of 3 ingredients:

- **WHO**: define the target users

- **WHAT**: define what action NearIT should do

- **TRIGGER**: define when the action should be triggered

## How it works ##

The NearIT web interface allows you to configure all the features, in a snap.
Once the settings are configured, everyone - even people without technical skills - can manage app content and send context-aware notifications, coupons and surveys.

The NearIT SDK synchronize with servers and behave accordingly to the settings and the recipes. Any content will be delivered at the right time, you just need to handle its presentation.

## Features ##
* **User Segmentation**: choose the target of your content. Profiling can be done also using external data sources and users’ past behaviour.
* **Beacon**: manage your beacon fleet and send location-based content.
* **Geofence**: engage users in a specific location with relevant content.
* **Notifications and in-app content**: send content to engage your users with your mobile app.
* **Analytics**: analyze the results of sent campaigns in real time.

## Installation ##

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java

dependencies {
    compile 'it.near.sdk.core:nearitsdk:2.0.5'
}
```

In the *onCreate* method of your Application class, initialize a *NearItManager* object, passing the API key as a String


```java
@Override
public void onCreate() {
  super.onCreate();
  nearItManager = new NearItManager(this, getResources().getString(R.string.nearit_api_key));
}
```

[Enabling triggers](docs/enable-triggers.md)

[Handle recipe content](docs/handle-content.md)

[User Profilation](docs/user-profilation.md)

## Other resources ##

[Custom Push Notification](docs/custom-push-notification.md)

[Using Pro-Guard?](docs/proguard.md)

[Javadocs](https://www.nearit.com/android-sdk-api/)
