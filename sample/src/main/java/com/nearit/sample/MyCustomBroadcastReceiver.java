package com.nearit.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import it.near.sdk.recipes.background.NearItBroadcastReceiver;

/**
 * This is the manifest element for this receiver
 *  <receiver
 *    android:name=".MyCustomBroadcastReceiver"
 *    android:exported="false">
 *      <intent-filter>
 *          <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
 *          <category android:name="android.intent.category.DEFAULT" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
 *          <category android:name="android.intent.category.DEFAULT" />
 *      </intent-filter>
 *  </receiver>
 */

public class MyCustomBroadcastReceiver extends NearItBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that MyLocationIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), MyCustomIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
