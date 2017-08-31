# Migration guide

This document helps you migrate from the 2.1.x version to the 2.2.x version.

In version 2.2.x the NearIT manager instance can be obtained with a static method, so you don't have to hold the instance yourself. 
If you are doing an explicit initialization of the library in the `onCreate` method of your `Application` class, you **MUST** remove it.
Initialization is now done automatically, in the same way the [Firebase library does it](https://firebase.googleblog.com/2016/12/how-does-firebase-initialize-on-android.html).

The api key must now be included in you manifest file, in a `<meta-data>` tag:
```xml
<application ... >
    <meta-data
       android:name="near_api_key"
       android:value="<your-near-api-key>" />
</application>
```

To access the NearIT instance you can start using the method:
```java
NearItManager.getInstance();
```

Every content that gets delivered to you (by intent or with a callback method), has the notification message included as a new public field:
```java
String notificationMessage = feedbackFromNear.notificationMessage
```
The recipeId is no longer delivered to the app, now all tracking is done with a `TrackingInfo` object:
```java
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.ENGAGED_STATUS);
```
Consequently, the callback for the proximity listener interface has different arguments:
```java
@Override
public void foregroundEvent(Parcelable content, TrackingInfo trackingInfo) {
    // handle the event
    // To extract the content and to have it automatically casted to the appropriate object type
    NearUtils.parseCoreContents(content, trackingInfo, coreContentListener);
}
```
Also, callbacks for the content parser follow the same change. For example:
```java
@Override
public void gotCustomJSONNotification(CustomJSON customJson, TrackingInfo trackingInfo) {
    // handle the content
}
```

## Automatic trackings
If you are using the built-in notification builder, now you get both automatic *notified* and *engaged* trackings, respectively, when the notification is shown and when it's tapped on.
If you are customizing the notification, you can still use manual trackings:
```java
// to extract the TrackingInfo from the intent:
// TrackingInfo trackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO);
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.ENGAGED_STATUS);
```
