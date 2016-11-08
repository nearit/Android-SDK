
If you need a different approach for notifying region enter, other than having a notification at every instance of this event, you need to subclass 2 classes (a BroadcastReceiver and an IntentService) and properly add them in your manifest. See the Android samples repository for an implementation of this scenario (including how to track a notified recipe). Here's a snippet of the manifest:

```xml
<!-- region messages -->
<service android:name=".MyRegionIntentService" />

<!-- Region related messages -->
<receiver android:name=".MyRegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```
