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
If you experience delays or inconsistent behavior from recipes that are supposed to trigger from small geofences (< 100mt), try to enlarge your geofence.

Be aware that, if you are trying to repeat the experience of entering a geofence, without physically leaving it, to test some recipe, you can try to switch location services off and on again to repeat the geofence event.
If you are changing recipes frequently and want to try a change immediately, you can swipe the app and reopen it. The SDK will check if the recipes have changed and will sync their content.

The NearIT platform has a separate set of trackings for location entities and recipes interactions. Look for trackings in the analytics section and check your geofence or beacon region detection count. This way, you can know if you are being detected. If that is the case, there might be something blocking your recipe. For example:

- The recipe has a cooldown period and not enough time has passed
- The recipe is not scheduled for this moment
- The recipe carries a coupon and your user has already received it. 
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



