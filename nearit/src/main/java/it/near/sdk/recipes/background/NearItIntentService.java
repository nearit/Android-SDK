package it.near.sdk.recipes.background;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

import it.near.sdk.GlobalConfig;
import it.near.sdk.NearItManager;
import it.near.sdk.R;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearItIntentConstants;
import it.near.sdk.utils.NearNotification;

public class NearItIntentService extends IntentService {

    public static final int REGION_NOTIFICATION_ID = 1;
    public static final int PUSH_NOTIFICATION_ID = 2;
    private static final String TAG = "NearITIntentService";
    private static final int DEFAULT_GEO_NOTIFICATION_ICON = R.drawable.icon_geo_default_24dp;
    private static final int DEFAULT_PUSH_NOTIFICATION_ICON = R.drawable.icon_push_default_24dp;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NearItIntentService() {
        super(NearItIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Create the notification about the content inside the intent
        if (intent != null) {
            sendSimpleNotification(intent);
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            NearItBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Send a simple notification, and also tracks the recipe as notified.
     *
     * @param intent the intent from the receiver.
     */
    protected void sendSimpleNotification(@NonNull Intent intent) {

        String notifText = intent.getStringExtra(NearItIntentConstants.NOTIF_BODY);
        String notifTitle = intent.getStringExtra(NearItIntentConstants.NOTIF_TITLE);
        if (notifTitle == null) {
            notifTitle = getApplicationInfo().loadLabel(getPackageManager()).toString();
        }

        sendNotifiedTracking(intent);

        NearNotification.send(this,
                imgResFromIntent(intent),
                notifTitle,
                notifText,
                getLauncherTargetIntent(intent),
                uniqueNotificationCode()
        );
    }

    private Intent getLauncherTargetIntent(@NonNull Intent intent) {
        return getPackageManager()
                .getLaunchIntentForPackage(this.getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtras(intent.getExtras());
    }

    protected void sendNotifiedTracking(@NonNull Intent intent) {
        TrackingInfo trackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO);
        NearItManager.getInstance(this).sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS);
    }

    private int imgResFromIntent(@NonNull Intent intent) {
        GlobalConfig globalConfig = NearItManager.getInstance(this).globalConfig;
        if (intent.getAction().equals(NearItManager.PUSH_MESSAGE_ACTION)) {
            return fetchPushNotification(globalConfig);
        } else if (intent.getAction().equals(NearItManager.GEO_MESSAGE_ACTION)) {
            return fetchProximityNotification(globalConfig);
        } else
            return fetchProximityNotification(globalConfig);
    }

    private int fetchProximityNotification(GlobalConfig globalConfig) {
        int imgRes = globalConfig.getProximityNotificationIcon();
        if (imgRes != GlobalConfig.DEFAULT_EMPTY_NOTIFICATION) {
            return imgRes;
        } else {
            return DEFAULT_GEO_NOTIFICATION_ICON;
        }
    }

    private int fetchPushNotification(GlobalConfig globalConfig) {
        int imgRes = globalConfig.getPushNotificationIcon();
        if (imgRes != GlobalConfig.DEFAULT_EMPTY_NOTIFICATION) {
            return imgRes;
        } else {
            return DEFAULT_PUSH_NOTIFICATION_ICON;
        }
    }

    private int uniqueNotificationCode() {
        return (int) Calendar.getInstance().getTimeInMillis();
    }

    private int notificationCodeFromIntent(@NonNull Intent intent) {
        switch (intent.getAction()) {
            case NearItManager.PUSH_MESSAGE_ACTION:
                return PUSH_NOTIFICATION_ID;
            case NearItManager.GEO_MESSAGE_ACTION:
                return REGION_NOTIFICATION_ID;
            default:
                return REGION_NOTIFICATION_ID;
        }
    }
}
