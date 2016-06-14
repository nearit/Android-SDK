# NearIt Android SDK #

This is the NearIt Android SDK. With this component you can integrate the NearIt services into your app to engage with your users.

It is currently in **beta**.

## Features ##

* NearIt services integration
* Beacon detection with app in the foreground.
* Content delivery
* Beacon monitoring with app in the background.
* Different types of contents.
* Push Notification

## Coming soon ##

* Different type of detectors
* User Segmentation

## Behaviour ##

The SDK will start monitoring the regions configured in the CMS of your app. Any content will be delivered through a notification that will call your launcher app and carry some extras.
To implement a custom background behaviour look in the advanced topics section.

## How do I get set up? ##

To start using the SDK, include this in your app *build.gradle*

```java

dependencies {
    compile 'it.near.sdk.core:nearitsdk:0.2.8'
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

## Advanced topics ##

* Region scanning is set to a scan every 60 seconds when outside your beacons range. As soon as beacons are picked up, it switches to 20 seconds.
* You can set the minimum parameter for determine the distance upon which the SDK must detect beacons recipes with the method *setThreshold(floatParam)* or the *NearItManager*. The default value is *0.5f*
* The SDK automatically includes the permission for location access in its manifest (necessary for beacon monitoring). When targeting API level 23+, please ask for and verify the presence of ACCESS_COARSE_LOCATION permissions at runtime.
* You can set your own icon for the notifications with the method *setNotificationImage(int imgRes)* of the *NearItManager*

### Built-in region background receivers ###

If you want to be notified when a user enters a region using the built-in background region notifications put this in your app manifest.
```xml
<!-- built in region receivers -->
<receiver android:name="it.near.sdk.Beacons.Monitoring.RegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.REGION_MESSAGE"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</receiver>
```

### Custom background behavior ###

If you need a different approach for notifying region enter, other than having a notification at every instance of this event, you need to subclass 2 classes (a BroadcastReceiver and an IntentService) and properly add them in your manifest. See the sample for an implementation of this scenario. Here's a snippet of the manifest:

```xml
<!-- region messages -->
<service android:name=".MyRegionIntentService" />

<!-- Region related messages -->
<receiver android:name=".MyRegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.REGION_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

### Answer Polls###

To answer a poll add this to your code
```java
// answer can either be 1 or 2
nearItManager.sendEvent(new PollEvent(poll_id, answer);
```

### Enable Push Notifications ###

NearIt offers a default push reception and visualization. It shows a system notification with the notification message.
When a user taps on a notification, it starts your app launcher and passes the intent with all the necessary information about the push, including the reaction bundle (the content to display).

To enable push notification, add this permission to your app *manifest*
```xml
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
<permission
        android:name="it.near.sampleapp.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
<uses-permission android:name="<YOUR_APP_PACKAGE_NAME>.permission.C2D_MESSAGE" />
```

Set your push senderId
```java
nearItManager.setPushSenderId("your-app-sender-id");
```

Add this receiver in the *application* tag of your app *manifest*
```xml
<application ...>
...
    <receiver
        android:name="com.google.android.gms.gcm.GcmReceiver"
        android:exported="true"
        android:permission="com.google.android.c2dm.permission.SEND" >
        <intent-filter>
            <action android:name="com.google.android.c2dm.intent.RECEIVE" />
            <category android:name="<YOUR_APP_PACKAGE_NAME>" />
        </intent-filter>
    </receiver>
</application>
```

Every push notification tracks itself as received when the SDK receives it.
If you want to track notification taps, simply do
```java
// the push_id will be included in the extras bundle of the intent
nearItManager.sendEvent(new OpenPushEvent(push_id));
```

### Custom Push Notification ###

If you need a custom handling of push notification (anything that must happens before or instead of the local notification), subclass these two classes
* GcmIntentService
* GcmBroadcastReceiver
in the same way it was done with MyRegionIntentService and MyRegionBroadcastReceiver
And add them to your manifest
```xml
<service android:name=".MyGcmIntentService"
            android:exported="false"/>

<receiver
    android:name=".MyGcmBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

### User profilation ###

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
NearItUserProfile.setBatchUserData(context, hasmap, new UserDataNotifier() {
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
