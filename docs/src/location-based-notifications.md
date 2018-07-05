# Location Based Notifications

The SDK automatically includes the permission for location access in its manifest (necessary for beacon and geofence monitoring). When targeting API level 23+, please ask for and verify the presence of **ACCESS_FINE_LOCATION** permissions at runtime.

After you asked for the proper permissions:

<div class="code-java">
// call this after you are given the proper permission for scanning (ACCESS_FINE_LOCATION)
NearItManager.getInstance().startRadar();
// to stop the radar call the method nearItManager.stopRadar()
</div>
<div class="code-kotlin">
// call this after you are given the proper permission for scanning (ACCESS_FINE_LOCATION)
NearItManager.getInstance().startRadar()
// to stop the radar call the method nearItManager.stopRadar()
</div>

The SDK creates a system notification for every location-based recipe. On the notification tap, your launcher activity will start.
To learn how to deal with in-app content once the user taps on the notification, see this [section](in-app-content.md).

If you want to customize your notifications, see this [section](custom-bkg-notification.md).

___
**WARNING**: If you experience build or runtime problems with google play services or firebase components, make sure to include the following versions of these dependencies in your app:
```xml
compile "com.google.firebase:firebase-messaging:@@firebase_messaging_version@@"
compile "com.google.firebase:firebase-core:@@firebase_core_version@@"
compile "com.google.android.gms:play-services-location:@@play_services_location@@"
```
Conflicting play services version may result in compile-time and run-time errors.
