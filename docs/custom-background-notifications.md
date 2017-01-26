
If you need a different approach for handling background delivered content, other than having a notification every time a recipe is triggered, you need to subclass 2 classes (a BroadcastReceiver and an IntentService) and properly add them in your manifest. See the Android samples repository for an implementation of this scenario (including how to track a notified recipe). Here's a snippet of the manifest:

```xml
<!-- region messages -->
<!-- This will extend RegionIntentService -->
<service android:name=".MyRegionIntentService" />

<!-- Region related messages -->
<!--NearItBroadcastReceiver-->
<receiver android:name=".MyRegionBroadcastReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```
