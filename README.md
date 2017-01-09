# NearIt Android SDK #

This is the NearIt Android SDK. With this component you can integrate the NearIt services into your app to engage with your users.

## Features ##

* NearIt services integration
* Beacon detection with app in the foreground.
* Content delivery
* Beacon and geofence monitoring with app in the background.
* Different types of contents.
* Push Notifications
* User Segmentation


## Behaviour ##

The SDK will synchronize with our servers and behave accordingly to the CMS settings and the recipes. Any content from triggered recipes will be delivered to your app.

## Getting started ##

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
