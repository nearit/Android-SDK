package it.near.sdk.Beacons.Monitoring;

import android.app.IntentService;
import android.content.Intent;

import it.near.sdk.GlobalConfig;
import it.near.sdk.R;
import it.near.sdk.Utils.NearNotification;

/**
 * Created by cattaneostefano on 11/04/16.
 */
public class RegionIntentService extends IntentService {

    private static String TAG = "RegionIntentService";
    public static final int NOTIFICATION_ID = 1;

    /**
     * default constructor
     */
    public RegionIntentService() {
        super("RegionIntentService");
    }

    /**
     *
     * @param intent the intent to handle
     */
    @Override
    protected void onHandleIntent(Intent intent) {


        // creo notifica di sistema
        String trigger = intent.getStringExtra("trigger-source");
        String source = intent.getStringExtra("content-source");

        // creo intent per tap sulle push, apriro il launcher dell'App
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // todo set dynamic notification icon
        int iconRes = R.drawable.ic_image_black_24dp;

        NearNotification.send(this, GlobalConfig.getInstance(this).getNotificationImage(), trigger, source, targetIntent, NOTIFICATION_ID);



        // Release the wake lock provided by the WakefulBroadcastReceiver.
        RegionBroadcastReceiver.completeWakefulIntent(intent);
    }

}
