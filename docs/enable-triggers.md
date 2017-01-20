# Enable triggers #

Based on what recipe triggers you want to use, some setup will be necessary.

## Location based Triggers ##

When you want to start the radar for geofences and beacons call this method

```java
// call this when you are given the proper permission for scanning (ACCESS_FINE_LOCATION)
nearItManager.startRadar()
// to stop the radar call the method nearItManager.stopRadar()
```

The SDK automatically includes the permission for location access in its manifest (necessary for beacon and geofence monitoring). When targeting API level 23+, please ask for and verify the presence of ACCESS_FINE_LOCATION permissions at runtime.

To enable location based notification, add this in your app manifest application element.
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
To learn how to deal with in-app content see this [section](handle-content.md).
You can use your own receiver for custom notification handling. See this [section](custom-bkg-notification.md).

You can set your own icon for the location-based notifications with the method *setNotificationImage(int imgRes)* of *NearItManager*

## Enable Push Notifications ##

To enable push notification, set up a firebase project and follow the official instructions to integrate it into an app. [If you need help follow those steps.](firebase.md)
Enter the cloud messaging firebase server key into the appropriate CMS section.

To enable push driven notification, add this in your app manifest application element
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
To learn how to deal with in-app content see this [section](handle-content.md).
You can use your own receiver for custom notification handling. See this [section](custom-bkg-notification.md).

WARNING: If you are using some gms play services in your app and experience runtime malfunctioning, please be sure to use the 10.0.1 version of the gms dependency you are pulling in your app. Conflicting play services version may result in compile-time and run-time errors.
