# Customize Notifications

## Styling options
You can set your own icon for the location-based notifications and the push notifications with the methods:
<div class="code-java">
NearItManager.getInstance().setProximityNotificationIcon(R.drawable.ic_my_location_notification);
NearItManager.getInstance().setPushNotificationIcon(R.drawable.ic_my_push_notification);
</div>
<div class="code-kotlin">
NearItManager.getInstance().setProximityNotificationIcon(R.drawable.ic_my_location_notification)
NearItManager.getInstance().setPushNotificationIcon(R.drawable.ic_my_push_notification)
</div>

## Custom Service
To handle complex use cases, you can write your own IntentService by subclassing the built-in one.
Let's look at the built-in IntentService manifest declaration:
```xml
<!-- built in service -->
<service
    android:name=".recipes.background.NearBackgroundJobIntentService"
    android:exported="false"
    android:permission="android.permission.BIND_JOB_SERVICE" />
```
This JobIntentService (not to be declared in the app manifest), creates the background notification functionality.
By extending NearBackgroundJobIntentService you can customize the app background behavior.

In the `onHandleWork` of the custom IntentService:
<div class="code-java">
@Override
protected void onHandleWork(@NonNull Intent intent) { 
//  Do whatever you want with the intent, but be aware that, depending on the target,
//  this might be executed in a jobservice and background limitations apply.<br>
//  To notify it to the user with a system notification, call 
//  super.sendSimpleNotification(intent);
//  this method also sends the proper tracking information to our servers.<br>
//  If you want to completely customize the user experience, you should implement your logic here.
//  You may want to use this method:
//  NearUtils.parseCoreContents(intent, coreContentListener); // to get casted content in the listener callback methods<br>
//  IMPORTANT
//  If you are overriding the default notification mechanism, remember to track the recipe as notified with:
//  TrackingInfo trackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO);
//  NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS);
}
</div>
<div class="code-kotlin">
override fun onHandleWork(intent: Intent) {
//  Do whatever you want with the intent, but be aware that, depending on the target,
//  this might be executed in a jobservice and background limitations apply.<br>
//  To notify it to the user with a system notification, call 
//  super.sendSimpleNotification(intent)
//  this method also sends the proper tracking information to our servers.<br>
//  If you want to completely customize the user experience, you should implement your logic here.
//  You may want to use this method:
//  NearUtils.parseCoreContents(intent, coreContentListener) // to get casted content in the listener callback methods<br>
//  IMPORTANT
//  If you are overriding the default notification mechanism, remember to track the recipe as notified with:
//  val trackingInfo: TrackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO)
//  NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS)
}
</div>

Then add your custom IntentService to the manifest
```xml
<service android:name=".MyCustomJobIntentService"
            android:exported="false"
            android:permission="android.permission.BIND_JOB_SERVICE">
    <intent-filter>
        <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    <intent-filter>
        <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</service>
```

By combination of intent filters, you can customize only one kind of background notifications, or use 2 different intent services for the 2 situations.
