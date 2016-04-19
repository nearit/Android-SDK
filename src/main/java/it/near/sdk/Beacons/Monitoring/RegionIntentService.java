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

        targetIntent.putExtras(intent.getExtras());
        NearNotification.send(this, GlobalConfig.getInstance(this).getNotificationImage(), trigger, source, targetIntent, NOTIFICATION_ID);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        RegionBroadcastReceiver.completeWakefulIntent(intent);
    }


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
