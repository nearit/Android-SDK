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
To handle complex use cases, you can write your own JobIntentService by subclassing the built-in one.
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
//  super.sendSimpleNotification(this, intent);
//  this method also sends the proper tracking information to our servers.<br>
//  If you want to completely customize the user experience, you should implement your logic here.
//  You may want to use this method:
//  NearUtils.parseContents(intent, contentListener); // to get casted content in the listener callback methods<br>
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
//  NearUtils.parseContents(intent, contentListener) // to get casted content in the listener callback methods<br>
//  IMPORTANT
//  If you are overriding the default notification mechanism, remember to track the recipe as notified with:
//  val trackingInfo: TrackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO)
//  NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS)
}
</div>

Then add your custom JobIntentService to the manifest
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

## Custom ranging notifications
Beacon interaction (beacon ranging) is a peculiar trigger that only works when your app is in the foreground.<br>
NearIT Android SDK will automatically show heads-up notifications.

If you need to disable the default behaviour, call this method in the **onCreate** method of your application: 
<div class="code-java">
{
    @Override
    public void onCreate() {
        super.onCreate();
        NearItManager.getInstance().disableDefaultRangingNotifications();
        // ...
    }
}
</div>
<div class="code-kotlin">
{
    override fun onCreate() {
        super.onCreate()
        NearItManager.getInstance().disableDefaultRangingNotifications()
    }
}
</div>

    
And if you want to receive ranging contents and handle them manually, set a **proximity listener** with the method:
<div class="code-java">
{
    //  ...
    NearItManager.getInstance().addProximityListener(this);
    // remember to remove the listener when the object is being destroyed with 
    // NearItManager.getInstance().removeProximityListener(this);
    //  ...
}
@Override
public void foregroundEvent(Parcelable content, TrackingInfo trackingInfo) {
    // handle the event
    // To extract the content and to have it automatically casted to the appropriate object type
    NearUtils.parseCoreContents(content, trackingInfo, coreContentListener);
}
</div>
<div class="code-kotlin">
{
    //  ...
    NearItManager.getInstance().addProximityListener(this)
    // remember to remove the listener when the object is being destroyed with 
    // NearItManager.getInstance().removeProximityListener(this)
    //  ...
}
override fun foregroundEvent(content: Parcelable, trackingInfo: TrackingInfo) {
    // handle the event
    // To extract the content and to have it automatically casted to the appropriate object type
    NearUtils.parseCoreContents(content, trackingInfo, coreContentListener)
}
</div>


**Warning:** In this situation you will need to write the code for **Trackings** and to eventually show an **In-app notification**.
