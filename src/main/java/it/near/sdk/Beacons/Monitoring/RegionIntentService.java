package it.near.sdk.Beacons.Monitoring;

import android.app.IntentService;
import android.content.Intent;

import it.near.sdk.GlobalConfig;
import it.near.sdk.R;
import it.near.sdk.Reactions.ContentNotification.ContentNotification;
import it.near.sdk.Reactions.CoreContentsListener;
import it.near.sdk.Reactions.PollNotification.PollNotification;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;
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
public class RegionIntentService extends IntentService {

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

        String trigger = intent.getStringExtra("trigger-source");
        String source = intent.getStringExtra("content-source");

        // create simple intent to open app launcher
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        targetIntent.putExtras(intent.getExtras());
        // sends system notification
        NearNotification.send(this, GlobalConfig.getInstance(this).getNotificationImage(), trigger, source, targetIntent, NOTIFICATION_ID);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        RegionBroadcastReceiver.completeWakefulIntent(intent);
    }


    /**
     * Utility method for extensions of this class. It notifies the listener if the intent contains a recognized core content.
     * @param intent The intent to analyze.
     * @param listener Contains a callback method for each content type.
     * @return true if the content was recognized as core and passed to a callback method, false if it wasn't.
     */
    protected boolean parseCoreContents(Intent intent, CoreContentsListener listener) {

        String content_source = intent.getExtras().getString("content-source");
        String content_type = intent.getExtras().getString("content-type");

        String trigger_source = intent.getExtras().getString("trigger-source");
        String trigger_type = intent.getExtras().getString("trigger-type");
        String trigger_item = intent.getExtras().getString("trigger-item");

        SimpleNotification s_notif;
        ContentNotification c_notif;
        PollNotification p_notif;

        boolean coreContent = false;
        switch (content_source) {
            case "simple-notification" :
                s_notif = (SimpleNotification) intent.getParcelableExtra("content");
                listener.gotSimpleNotification(intent, s_notif, content_source, content_type, trigger_source, trigger_type, trigger_item);
                coreContent = true;
                break;
            case "content-notification" :
                c_notif = (ContentNotification) intent.getParcelableExtra("content");
                listener.getContentNotification(intent, c_notif, content_source, content_type, trigger_source, trigger_type, trigger_item);
                coreContent = true;
                break;
            case "poll-notification" :
                p_notif = (PollNotification) intent.getParcelableExtra("content");
                listener.getPollNotification(intent, p_notif, content_source, content_type, trigger_source, trigger_type, trigger_item);
                coreContent = true;
                break;
        }
        return coreContent;
    }


}
