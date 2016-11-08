# NearIt Android SDK #

This is the NearIt Android SDK. With this component you can integrate the NearIt services into your app to engage with your users.

- This is a pre-release software and features can change.

## Features ##

* NearIt services integration
* Beacon detection with app in the foreground.
* Content delivery
* Beacon and geofence monitoring with app in the background.
* Different types of contents.
* Push Notification
* User Segmentation

[This is a link!](docs/firebase.md)

## Behaviour ##

The SDK will start monitoring the regions configured in the CMS of your app. Any content will be delivered through a notification that will call your launcher app and carry some extras.
To implement a custom background behaviour look in the advanced topics section.

## How do I get set up? ##

To start using the SDK, include this in your app *build.gradle*

```java

dependencies {
    compile 'it.near.sdk.core:nearitsdk:0.2.16'
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

## Advanced topics ##

* The SDK automatically includes the permission for location access in its manifest (necessary for beacon and geofence monitoring). When targeting API level 23+, please ask for and verify the presence of ACCESS_FINE_LOCATION permissions at runtime.
* You can set your own icon for the notifications with the method *setNotificationImage(int imgRes)* of the *NearItManager*

## Foreground updates ##

To receive foreground contents (e.g. ranging recipes) set a proximity listener with the method
```java
{
    ...
    nearItManager.addProximityListener(this);
    // remember to remove the listener whenthe object is being destroyed with nearItManager.removeProximityListener(this);
    ...
}

@Override
public void foregroundEvent(Parcelable content, Recipe recipe) {
    // handle the event
}   
```

## Built-in region background receivers ##

If you want to be notified when a user enters a region (bluetooth or geofence) using the built-in background region notifications put this in your app manifest. Any content will be delivered through a notification that will call your launcher app and carry some extras.
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

Recipes either deliver content in the background or in the foreground but not both. Check this table to see how you will be notified.

| Type of trigger                  | Delivery           |
|----------------------------------|--------------------|
| Push (immediate or scheduled)    | Background intent  |
| Enter and Exit on geofences      | Background intent  |
| Enter and Exit on beacon regions | Background intent  |
| Enter in a specific beacon range | Proximity listener |

### Custom background behavior ###

If you need a different approach for notifying region enter, other than having a notification at every instance of this event, you need to subclass 2 classes (a BroadcastReceiver and an IntentService) and properly add them in your manifest. See the Android samples repository for an implementation of this scenario (including how to track a notified recipe). Here's a snippet of the manifest:

```xml
<!-- region messages -->
<service android:name=".MyRegionIntentService" />

<!-- Region related messages -->
<receiver android:name=".MyRegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

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

### Track recipes ###

Recipes tracks themselves as received, but you need to track the tap event, by calling
```java
Recipe.sendTracking(getApplicationContext(), recipeId, Recipe.ENGAGED_STATUS);
```

## User profilation ##

To profile users, you need to either create a new profile in our server or pass us a profileId obtained from your authentication services in the SDK.

To register an user in our platform call the method
```java
NearItUserProfile.createNewProfile(context, new ProfileCreationListener() {
    @Override
    public void onProfileCreated() {
        // your profile was created
    }
                                            
    @Override
    public void onProfileCreationError(String error) {
        // there was an error
    }
});
```
Calling this method multiple times will results in multiple profiles being created, each time with no profilation data.

To be sure to call this method only when necessary, check if you already created a profile with this method
```java
String profileId = NearItUserProfile.getProfileId(context);
```
If the result is null, it means that no profile is associated with the app installation.

After the profile is created set user data
```java
NearItUserProfile.setUserData(context, "name", "John", new UserDataNotifier() {
    @Override
    public void onDataCreated() {
        // data was set/created                                                
    }
                                                       
    @Override
    public void onDataNotSetError(String error) {
        // there was an error                        
    }
});
```

If you have multiple data properties, set them in batch
```java
HashMap<String, String> userDataMap = new HashMap<>();
userDataMap.put("name", "John");
userDataMap.put("age", "23");           // set everything as String
userDataMap.put("saw_tutorial", "true") // even booleans, the server has all the right logic
NearItUserProfile.setBatchUserData(context, userDataMap, new UserDataNotifier() {
            @Override
            public void onDataCreated() {
                // data was set/created 
            }

            @Override
            public void onDataNotSetError(String error) {

            }
        });
```
If you try to set user data before creating a profile the error callback will be called.

If you want to set a profileId manually (if it's coming from your user management systems) use the method
```java
NearItUserProfile.setProfileId(context, profileId);
```

If you want to reset your profile use this method
```java
NearItUserProfile.resetProfileId(context)
```
Further calls to NearItUserProfile.getProfileId(context) will return null.

## Enable Push Notifications ##

NearIt offers a default push reception and visualization. It shows a system notification with the notification message.
When a user taps on a notification, it starts your app launcher and passes the intent with all the necessary information about the push, including the reaction bundle (the content to display) just like the region notifications.

To enable push notification, set up a firebase project and follow the official instruction to integrate it into an app (create a project in the firebase console, download the google-services.json file and add the proper dependencies into the project level and app level gradle file).
The NearIt SDK already has the dependency related to the FCM messaging service and has registered the proper service and receivers for handling our internal push notification resolution in its manifest. The data will be fetched directly from the config file (google-services.json).
The only extra step is to enter the cloud messaging server key into the CMS. Push notification only work if a profile is created.

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

### Custom Push Notification ###

Just like with region messages you can add your own receiver for the push notification to have total control of your app behaviour. [see above](#custom-background-behavior)
And add them to your manifest (see the Android samples repository for an implementation of this scenario). 
```xml
<service android:name=".MyFcmIntentService"
            android:exported="false"/>

<receiver
    android:name=".MyFcmBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```
Also, you need to omit this receiver from your manifest
```xml
<receiver
         android:name="it.near.sdk.Push.FcmBroadcastReceiver"
         android:exported="false">
         <intent-filter>
                <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
                <category android:name="android.intent.category.DEFAULT" />
         </intent-filter>
    </receiver>
```
We futhermore suggest to create an unique receiver for both region messages and push messages if their custom behavior matches. Just add both intent filters inside the receiver and deal with those messages in the same receiver. Check the intent action string to find out the source of the intent.

WARNING: If you are using some gms play services in your app and experience malfunctioning, please be sure to use the 9.6.1 version of the gms dependency you are pulling in your app.
