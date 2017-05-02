# Custom Background Notification

To receive background notifications, you can add our built-in receiver as shown in the [Enable Triggers](enable-triggers.md) section. The receiver shows a system notification, with the provided texts and a pre-set system icon. There's no time-limit or special condition to be met to show the notification so you will always get it. To handle complex use cases, you can write your own receivers by subclassing the built-in one.

## Custom Receiver and Service

Let's look at the built-in receiver manifest declaration:
```xml
<!-- built in background receiver -->
<receiver
    android:name="it.near.sdk.recipes.background.NearItBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    <intent-filter>
        <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```
This receiver, along with a built-in service (not to be declared in the app manifest), creates the background functionality.
By extending NearItBroadcastReceiver and NearItIntentService you can customize the app background behavior.

In the `onReceive` method of your custom receiver, start your custom service:
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

In the `onHandleIntent` of the custom IntentService:
```java
@Override
protected void onHandleIntent(Intent intent) {
  /*
  Do whatever you want with the intent, like setting a cooldown or filter events

  to notify it to the user with a system notification, call 
  super.sendSimpleNotification(intent);
  this method also sends the proper tracking information to our servers.
  If you want to completely customize the user experience, you should implement your logic here.
  You may use this method:
  NearUtils.parseCoreContents(intent, coreContentListener); // to get casted content in the listener callback methods

  IMPORTANT
  If you are overriding the default notification mechanism, remember to track the recipe as notified with:
  String recipeId = intent.getStringExtra(NearItIntentConstants.RECIPE_ID);
  try {
      nearItManager.getRecipesManager().sendTracking(recipeId, Recipe.NOTIFIED_STATUS);
  } catch (JSONException e) {
      
  }
  */

  // always end this method with
  MyCustomBroadcastReciever.completeWakefulIntent(intent);
}
```

Then replace the custom broadcast receiver and add the custom intent service to the manifest
```xml
<service android:name=".MyCustomIntentService"
            android:exported="false" />

<receiver
    android:name=".MyCustomBroadcastReciever"
    android:exported="false">
    <!-- Add both intent filters to deal with both trigger notification in the same way -->
    <intent-filter>
        <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    <intent-filter>
            <action android:name="it.near.sdk.permission.REGION_MESSAGE" />
            <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

By combination of receivers and intent filters, you can customize only one kind of background notifications, or use 2 different receivers for the 2 cases.
Remember that you can always programmatically check the action of an intent, inside `onReceive` in the receiver and inside `onHandleIntent` in the intent service with `intent.getAction()`.
