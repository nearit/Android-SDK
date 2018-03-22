## As soon as I add the nearit dependency my app crashes at startup.

Be sure to include the near api key in your app manifest. Pay attention to put the meta declaration inside the `<application>` element. Example:
```xml
<application ... >
    <meta-data
        android:name="near_api_key"
        android:value="XXXXXXXXXXXX" />
</application>
```

<br>
## Location based notifications are not working

Make sure that you are starting the NearIT radar **after** you are granted `FINE_LOCATION` permissions from your users. 
If you experience delays or inconsistent behavior from recipes that are supposed to trigger from small geofences (< 100mt), try enlarging your geofence.

Be aware that, if you are trying to repeat the experience of entering a geofence, without physically leaving it, to test some recipe, you won't normally be able to do it. On the other hand, you can try to switch location services off and on again to repeat the geofence event.
If you are changing recipes frequently and want to try a change immediately, you can swipe the app and reopen it. The SDK will check if the recipes have changed and will sync their content.

The NearIT platform has a separate set of trackings for location entities and recipes interactions. Look for trackings in the analytics section and check your geofence or beacon region detection count. This way, you can know if you are being detected. If that is the case, there might be something blocking your recipe. For example:

- The recipe has a cooldown period and not enough time has passed
- The recipe is not scheduled for this moment
- The recipe carries a coupon and your user has already received it.
- You are not in the target of users for that recipe. 
<br>
## Beacons notifications are not working
 
Note: Beacon detection only works on devices running Android 4.3+ with Bluetooth LE enabled.
Make sure that your beacon is properly configured, and that the iBeacon package is enabled. If your NearIT app has more than one region in the panel, make sure that those regions are not overlapping in signal. For every beacon on the platform, also make sure that you don’t have more than one physical beacons with the same settings deployed in your space.
<br>
## Push notifications are not working

Check your application log as soon as the app starts. If you see this log: 
```
We can't get your firebase instance. Near push notification might not work
``` 
it means Firebase is not configured properly. 
The most common reasons are:

- The `google-services.json` file is missing or is in the wrong folder.
- The `google-services.json` file is malformed.
- The `google-services.json` file is referencing a different app. Please check that the app package name in the file is the same as your app.

If you are not seeing the error log, yet you are not receiving push notifications make sure that the FCM api key is actually set, inside the "Push notification" section in the platform. [Documentation](push-notifications.md).

If you just switched your debug build between different firebase projects or NearIT apps, try to wipe the app data and start the app again.
<br>
## Cooldown rules are not applied

Cooldown rules are only applied if the proper trackings are correctly observed. Specifically, cooldown filters are based on the `Notified` recipe trackings. If you are not customizing the notification creation, trackings are automatic.
If you are customizing background notifications you should track the recipe as `Notified` just before you launch your notifications. 
If you are customizing foreground notifications with a `ProximityListener`, you should track the recipe as `Notified` just before you show the user any indication that a content is available.

## After switching between environments, some features stopped working

When you switch between environments (NearIT apps or Firebase projects) your installation might be in an inconsistent state. Even uninstalling and re-installing the app will not help restoring a stable state.
To wipe all application data, you must go into the application settings and clear the storage.

To enter the app settings, go into the device setting list screen and search for an 'Applications' setting. Depending on the OS Version and on manufacturer customizations, this section might have a slightly different name ('Manage applications', 'Apps', ...). From the list, select your app and select the 'Storage' settings. In there you should have 2 options: 'clear data' and 'clear cache'. Please select 'clear data' and re-open your app.

## Multiple push providers

NearIT can achieve a fast push integration through the automatic inclusion of two receivers in your final app. 
Those two receivers listen for two events: when the device is granted a device token, and when the device receives contents via a push notification.
Because of the [Android manifest merging](https://developer.android.com/studio/build/manifest-merge.html), only one receiver for each specific action can be included in your app final manifest file. 

Depending on existing features in your app, you might already have those two receivers defined. In this case, only your receivers will be notified of the events they are subscribed to.
You can maintain the NearIT push functionality by passing those two event through our SDK.
In your receiver for the device token generation, in your `onTokenRefresh()` method, simply call 
```java
NearInstanceIDListenerService.sendRegistrationToServer(yourDeviceToken);
```
In the receiver for the push messages, in your `onMessageReceived()` method, simply call
```java
NearFcmListenerService.processRemoteMessage(yourRemoteMessage);
```

If you are using another push provider, that also automatically includes those two receivers, you will have that provider and the NearIT SDK competing over the inclusion in the final manifest.
You can read about how to give one of the two modules the priority on the manifest merging [documentation](https://developer.android.com/studio/build/manifest-merge.html).
Remember that, after a build, you can check the final manifest file, with those conflict already resolved in your app module `build/intermediates/manifests/full/debug/` directory (as of gradle 3.0.1). 

If you need both providers push implementation to work, you will have to define the two receivers in your app, thus avoiding any manifest competition. Then, in the receivers callbacks, you can pass the events to the proper library implementations (see above)
 
