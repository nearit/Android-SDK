package it.near.sdk.Push;

import android.app.IntentService;
import android.content.Intent;

import it.near.sdk.GlobalConfig;
import it.near.sdk.Reactions.ContentNotification.ContentNotification;
import it.near.sdk.Reactions.CoreContentsListener;
import it.near.sdk.Reactions.PollNotification.PollNotification;
import it.near.sdk.Utils.BaseIntentService;
import it.near.sdk.Utils.NearNotification;

/**
 * @author cattaneostefano.
 */
public class GcmIntentService extends BaseIntentService implements CoreContentsListener {

    private static String TAG = "GcmIntentService";
    private static final int PUSH_NOTIFICATION_ID = 2;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        parseCoreContents(intent, this);
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public void getPollNotification(Intent intent, PollNotification notification, String notifText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle) {
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        targetIntent.putExtras(intent.getExtras());
        String notif_title = intent.getStringExtra("notif_title");
        if (notif_title == null) {
            notif_title = getApplicationInfo().loadLabel(getPackageManager()).toString();
        }
        NearNotification.send(this, GlobalConfig.getInstance(this).getNotificationImage(), notif_title, notifText, targetIntent, PUSH_NOTIFICATION_ID);
    }

    @Override
    public void getContentNotification(Intent intent, ContentNotification notification, String notifText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle) {
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        targetIntent.putExtras(intent.getExtras());
        String notif_title = intent.getStringExtra("notif_title");
        if (notif_title == null) {
            notif_title = getApplicationInfo().loadLabel(getPackageManager()).toString();
        }
        NearNotification.send(this, GlobalConfig.getInstance(this).getNotificationImage(), notif_title, notifText, targetIntent, PUSH_NOTIFICATION_ID);

    }

}
