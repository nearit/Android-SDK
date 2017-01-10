# Custom background notification

In both location-based and push-driven notifications, you can add our built-in receiver as shown in the [Enable Triggers](enable-triggers.md) section. Those receivers always show a system notification, with the provided texts and a pre-set system icon (that can be overridden). There's no time-limit or condition to met to show the notification. To handle complex use cases, you can write your own receivers by subclassing the built-in ones.

## Custom receiver

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
By writing your own BroadcastReceiver you can fully customize your app background behaviour. 

Just like with region messages you can add your own receiver for the push notification to have total control of your app behaviour.
And these components to your manifest (see the Android samples repository for an implementation of this scenario).
```xml
<service android:name=".MyFcmIntentService"
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
Also, you need to omit this receiver from your manifest
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
We futhermore suggest to create an unique receiver for both region messages and push messages if their custom behavior matches. Just add both intent filters inside the receiver and deal with those messages in the same receiver. Check the intent action string to find out the source of the intent.

WARNING: If you are using some gms play services in your app and experience malfunctioning, please be sure to use the 9.6.1 version of the gms dependency you are pulling in your app.
