# Custom background notification

In both location-based and push-driven notifications, you can add our built-in receiver as shown in the [Enable Triggers](enable-triggers.md) section. Those receivers show a system notification, with the provided texts and a pre-set system icon (that can be overridden). There's no time-limit or special condition to be met to show the notification so you will always get it. To handle complex use cases, you can write your own receivers by subclassing the built-in ones.

## Custom receiver and service

Let's look at the built-in push receiver manifest declaration:
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
This receiver, along with a built-in service (not to be declared in the app manifest), creates the background functionality.
By extending FcmBroadcastReceiver and FcmIntentService you can customize the app background behaviour.

In the `onReceive` method of your custom receiver, start your custom service
```java
@Override
public void onReceive(Context context, Intent intent) {
  // Explicitly specify that MyCustomIntentService will handle the intent.
  ComponentName comp = new ComponentName(context.getPackageName(),
    MyCustomIntentService.class.getName());

  // Start the service, keeping the device awake while it is launching.
  startWakefulService(context, (intent.setComponent(comp)));
}
```

In the `onHandleIntent` of the custom IntentService
```java
@Override
protected void onHandleIntent(Intent intent) {
  /*
  Do whatever you want with the intent, like setting a cooldown or filter events

  IMPORTANT
  Since you are overriding the default notification mechanism, remember to track the recipe as notified with:
  String recipeId = intent.getStringExtra(NearItIntentConstants.RECIPE_ID);
  try {
      Recipe.sendTracking(getApplicationContext(), recipeId, Recipe.NOTIFIED_STATUS);
  } catch (JSONException e) {
      e.printStackTrace();
  }

  There is an utility method to automatically process known content types and calls the CoreContentsListener callback methods.
  parseCoreContents(Intent intent, CoreContentsListener listener);

  There is an utility method for creating notifications
  NearNotification.send(context, GlobalConfig.getInstance(this).getNotificationImage(), notificationTitle, notificationText, targetIntent, NOTIFICATION_ID);
  or you can create your own, that's probably why you are here anyway
  */

  // always end this method with
  FcmBroadcastReceiver.completeWakefulIntent(intent);
}
```

Then add (or replace) the custom broadcast receiver and add the custom intent service to the manifest
```xml
<service android:name=".MyCustomIntentService"
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

With the same technique, you can extend the behaviour for the location based background functionalities, just replace FcmBroadcastReceiver with RegionBroadcastReceiver and FcmIntentService with RegionIntentService. Finally use this intent filter for this custom receiver
```xml
<receiver
    android:name=".MyRegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.REGION_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

## Optimal customization
We furthermore suggest to create an unique receiver for both region messages and push messages if their custom behavior matches. This way you can define a single way to handle background behaviour regardless of the event source, since the concept of a recipe already takes care of the event-action coupling.
In the manifest declaration for your custom broadcast receiver, add both intent filters and you will catch both location-based and push-driven notifications.
```xml
<receiver
    android:name=".MyBackgroundBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.REGION_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    <intent-filter>
        <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```
You should now deal with all messages in the same receiver. If you want to know the source of the event, just look the intent action string with `intent.getAction()`.

WARNING: If you are using some gms play services in your app and experience malfunctioning, please be sure to use the 10.0.1 version of the gms dependency you are pulling in your app.
