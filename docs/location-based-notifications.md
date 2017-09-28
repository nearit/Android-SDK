# Location Based Notifications

The SDK automatically includes the permission for location access in its manifest (necessary for beacon and geofence monitoring). When targeting API level 23+, please ask for and verify the presence of ACCESS_FINE_LOCATION permissions at runtime.
If you are also manually including ACCESS_COARSE_LOCATION in your manifest please be sure to specify a `maxSdkVersion` of _22_.
```java
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"
        android:maxSdkVersion="22"/>
```

When you want to start the radar for **geofences and beacons** call this method:

```java
// call this when you are given the proper permission for scanning (ACCESS_FINE_LOCATION)
NearItManager.getInstance().startRadar()
// to stop the radar call the method nearItManager.stopRadar()
```

The SDK creates a system notification for every background recipe. On the notification tap, your launcher activity will start.
To learn how to deal with in-app content once the user taps on the notification, see this [section](in-app-content.md).

If you want to customize your notifications, see this [section](custom-bkg-notification.md).

___
**WARNING**: If you experience build or runtime problems with google play services components, make sure to include the 11.4.0 version of any gms dependency in your app. Example:
```xml
compile 'com.google.android.gms:play-services-analytics:11.4.0'
```
Conflicting play services version may result in compile-time and run-time errors.
