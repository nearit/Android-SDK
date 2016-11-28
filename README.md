# NearIt Android SDK #

This is the NearIt Android SDK. With this component you can integrate the NearIt services into your app to engage with your users.

## Features ##

* NearIt services integration
* Beacon detection with app in the foreground.
* Content delivery
* Beacon and geofence monitoring with app in the background.
* Different types of contents.
* Push Notification
* User Segmentation


## Behaviour ##

The SDK will start monitoring the regions configured in the CMS of your app. Any content will be delivered through a notification that will call your launcher app and carry some extras.
To implement a custom background behaviour look in the advanced topics section.

## Getting started ##

To start using the SDK, include this in your app *build.gradle*

```java

dependencies {
    compile 'it.near.sdk.core:nearitsdk:2.0.1'
}
```

In the *onCreate* method of your Application class, initialize a *NearItManager* object, passing the API key as a String


```java
 @Override
    public void onCreate() {
        super.onCreate();
        nearItManager = new NearItManager(this, getResources().getString(R.string.api_key));
    }

```

When you want to start the radar for geofences and beacons call this method

```java
    // call this when you are given the proper permission for scanning (ACCESS_FINE_LOCATION)
    nearItManager.startRadar()
    // to stop call this method nearItManager.stopRadar()
```

The SDK automatically includes the permission for location access in its manifest (necessary for beacon and geofence monitoring). When targeting API level 23+, please ask for and verify the presence of ACCESS_FINE_LOCATION permissions at runtime.

## Foreground updates ##

To receive foreground contents (e.g. ranging recipes) set a proximity listener with the method
```java
{
    ...
    nearItManager.addProximityListener(this);
    // remember to remove the listener when the object is being destroyed with 
    // nearItManager.removeProximityListener(this);
    ...
}

@Override
public void foregroundEvent(Parcelable content, Recipe recipe) {
    // handle the event
    // if you show the notification to the user track the recipe as notified with
    // Recipe.sendTracking(getApplicationContext(), recipe.getId(), Recipe.NOTIFIED_STATUS);
    // when the user interacts with the content, track the event with
    // Recipe.sendTracking(getApplicationContext(), recipe.getId(), Recipe.ENGAGED_STATUS);
}   
```

## Built-in region background receivers ##

If you want to be notified when a user enters a region (bluetooth or geofence) using the built-in background region notifications put this in your app manifest. 
Any content will be delivered through a notification that will call your launcher app and carry some extras.
```xml
<!-- built in region receivers -->
<receiver android:name="it.near.sdk.Geopolis.Background.RegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.GEO_MESSAGE"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</receiver>
```
You can set your own icon for the notifications with the method *setNotificationImage(int imgRes)* of the *NearItManager*

Recipes tracks themselves as received, but you need to track the tap event, by calling
```java
Recipe.sendTracking(getApplicationContext(), recipeId, Recipe.ENGAGED_STATUS);
```

Recipes either deliver content in the background or in the foreground but not both. Check this table to see how you will be notified.

| Type of trigger                  | Delivery           |
|----------------------------------|--------------------|
| Push (immediate or scheduled)    | Background intent  |
| Enter and Exit on geofences      | Background intent  |
| Enter and Exit on beacon regions | Background intent  |
| Enter in a specific beacon range | Proximity listener |

If you want to customize the behavior of background notification see [this page](docs/custom-background-notifications.md)

### Answer Polls ###

To answer a poll add this to your code
```java
// answer can either be 1 or 2, poll is the poll object.
nearItManager.sendEvent(new PollEvent(poll, answer);
// if you don't hold the poll object use this constructor
nearItManager.sendEvent(new PollEvent(pollId, answer, recipeId));
```

### Give feedback ###

To send a rating to a feedback
```java
// rating must be an integer between 0 and 5, and you can set a comment string.
nearItManager.sendEvent(new FeedbackEvent(feedback, rating, "Awesome"));
// if you don't hold the feedback object use this constructor
nearItManager.sendEvent(new FeedbackEvent(feedbackId, rating, "Nice", recipeId));
```

## Enable Push Notifications ##

NearIt offers a default push reception and visualization. It shows a system notification with the notification message.
When a user taps on a notification, it starts your app launcher and passes the intent with all the necessary information about the push, including the reaction bundle (the content to display) just like the region notifications.

To enable push notification, set up a firebase project and follow the official instruction to integrate it into an app. [If you need help follow those steps](docs/firebase.md)
Enter the cloud messaging server key into the CMS. Push notification only work if a profile is created [How to create a profile](docs/user-profilation.md).

To receive the system notification of a push recipe, add this receiver in the *application* tag of your app *manifest*
```xml
<application ...>
...
    <receiver
         android:name="it.near.sdk.Push.FcmBroadcastReceiver"
         android:exported="false">
         <intent-filter>
                <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
                <category android:name="android.intent.category.DEFAULT" />
         </intent-filter>
    </receiver>
</application>
```

Every push notification tracks itself as received when the SDK receives it.
If you want to track notification taps, simply do
```java
// the recipeId will be included in the extras bundle of the intent with the key IntentConstants.RECIPE_ID
Recipe.sendTracking(getApplicationContext(), recipeId, Recipe.ENGAGED_STATUS);
```

[Custom Push Notification](docs/custom-push-notification.md)

## Other resources ##

[Custom background notifications](docs/custom-background-notifications.md)

[Custom Push Notification](docs/custom-push-notification.md)

[User Profilation](docs/user-profilation.md)
