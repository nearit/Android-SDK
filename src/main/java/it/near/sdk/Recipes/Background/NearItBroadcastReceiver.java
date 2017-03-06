package it.near.sdk.Recipes.Background;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * WakefulBroadcastReceiver to receive background messages. This receiver should be initiated in the manifest to use the default implementation
 * or extended to create a custom implementation (In this case the extended receiver it's the one to include in the app manifest).
 * The action to register in the manifest is <code>it.near.sdk.permission.GEO_MESSAGE</code> for location and <code>it.near.sdk.permission.PUSH_MESSAGE"</code> for push notifications.
 *
 * <pre>
 *     {@code
 *           <receiver android:name="it.near.sdk.Beacons.Monitoring.NearItBroadcastReceiver"
 *           android:exported="false">
 *              <intent-filter>
 *                  <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
 *                  <category android:name="android.intent.category.DEFAULT" />
 *              </intent-filter>
 *              <intent-filter>
 *                  <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
 *                  <category android:name="android.intent.category.DEFAULT" />
 *              </intent-filter>
 *           </receiver>
 *     }
 * </pre>
 *
 * @author cattaneostefano
 */
public class NearItBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "NearItBroadcastReceiver";

    /**
     * Starts @RegionIntentService in wakeful mode.
     * @param context the current context
     * @param intent the input intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Explicitly specify that RegionIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), NearItIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
