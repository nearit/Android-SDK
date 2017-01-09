# NearIT Android SDK #

NearIT allows to engage app users by sending **context-aware targeted content**.

[ ![Download](https://api.bintray.com/packages/catt-stefano/maven/it.near.sdk.core/images/download.svg) ](https://bintray.com/catt-stefano/maven/it.near.sdk.core/_latestVersion)

## Recipes ##

NearIT allows users to control apps by defining “recipes”, simple rules made of 3 ingredients:
- **WHO**: define the target users
- **WHAT**: define what action NearIT should do
- **TRIGGER**: define when the action should be triggered

## How it works ##

NearIT web interface allows you to configure all the features, in a snap.
Once the settings are configured, everyone - even people without technical skills - can manage app content and send context-aware notifications, coupons and surveys.

NearIT SDK synchronize with servers and behave accordingly to the settings and the recipes. Any content will be delivered at the right time, you will only need to handle its presentation.

## Features ##
* **User Segmentation**: choose the target of your content. Profiling can be done also using external data sources and users’ past behaviour.
* **Beacon**: manage your beacon fleet and send location-based content.
* **Geofence**: engage users in a specific location with relevant content.
* **Notifications and in-app content**: send content to engage your users with your mobile app. 
* **Analytics**: analyze in real time the results of the sent campaigns.

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
[Custom background notifications](docs/custom-background-notifications.md)

[Custom Push Notification](docs/custom-push-notification.md)

[Using Pro-Guard?](docs/proguard.md)

[Javadocs](https://www.nearit.com/android-sdk-api/)
