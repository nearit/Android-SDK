package it.near.sdk.Beacons.Monitoring;

import android.app.IntentService;
import android.content.Intent;

import it.near.sdk.GlobalConfig;
import it.near.sdk.R;
import it.near.sdk.Reactions.ContentNotification.ContentNotification;
import it.near.sdk.Reactions.CoreContentsListener;
import it.near.sdk.Reactions.PollNotification.PollNotification;
import it.near.sdk.Utils.BaseIntentService;
import it.near.sdk.Utils.NearNotification;

/**
 * IntentService that gets notified when we receive a region event. It is explicitly called by {@link RegionBroadcastReceiver}.
 * It has to be manually added to the manifest like in the sample below inside the <code>application</code> tag.
 *
 * <pre>
 *     {@code
 *          <!-- region messages -->
 *          <service android:name="it.near.sdk.Beacons.Monitoring.RegionIntentService" />
 *     }
 * </pre>
 * @author cattaneostefano
 */
public class RegionIntentService extends BaseIntentService {

    private static String TAG = "RegionIntentService";
    /**
     * Constant for the notification
     */
    public static final int NOTIFICATION_ID = 1;

    /**
     * Default constructor.
     */
    public RegionIntentService() {
        super("RegionIntentService");
    }

    /**
     * Handle the intent that started the service. The default implementation sends a system notification with the component that launched
     * the recipe and the type of content to be delivered to the app. Since those messages are intented for debug use, it is strongly raccomended
     * to extends this class and override this method, without calling super() and releasing the wake lock the same way it is done here.
     *
     * @param intent the intent to handle
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        String notif_title = intent.getStringExtra("notif_title");
        String notif_body = intent.getStringExtra("notif_body");

        // create simple intent to open app launcher
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        targetIntent.putExtras(intent.getExtras());
        if (notif_title == null) {
            notif_title = getApplicationInfo().loadLabel(getPackageManager()).toString();
        }
        // sends system notification
        NearNotification.send(this, GlobalConfig.getInstance(this).getNotificationImage(), notif_title, notif_body, targetIntent, NOTIFICATION_ID);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        RegionBroadcastReceiver.completeWakefulIntent(intent);
    }



}
