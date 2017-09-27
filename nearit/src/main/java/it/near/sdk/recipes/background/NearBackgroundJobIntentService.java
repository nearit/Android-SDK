package it.near.sdk.recipes.background;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import it.near.sdk.GlobalConfig;
import it.near.sdk.NearItManager;
import it.near.sdk.R;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearItIntentConstants;
import it.near.sdk.utils.NearNotification;

import static it.near.sdk.utils.NearItIntentConstants.ACTION;

public class NearBackgroundJobIntentService extends JobIntentService {

    private static final int JOB_ID = 778899;

    private static final int DEFAULT_GEO_NOTIFICATION_ICON = R.drawable.icon_geo_default_24dp;
    private static final int DEFAULT_PUSH_NOTIFICATION_ICON = R.drawable.icon_push_default_24dp;

    public static void enqueueWork(Context context, Intent work) {
        String name = getResolverInfo(context, work.getStringExtra(ACTION));
        if (name == null) {
            enqueueWork(context, NearBackgroundJobIntentService.class, JOB_ID, work);
        } else {
            try {
                Class t = Class.forName(name);
                enqueueWork(context, t, JOB_ID, work);
            } catch (Throwable ignored) {
                enqueueWork(context, NearBackgroundJobIntentService.class, JOB_ID, work);
            }
        }
    }

    @Nullable
    public static String getResolverInfo(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent().setAction(action).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA);
        if (resolveInfos.size() < 1) return null;
        return resolveInfos.get(0).serviceInfo.name;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.e("JIS", "jis running");
        // Create the notification about the content inside the intent
        if (intent != null) {
            sendSimpleNotification(this, intent);
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // NearItBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Send a simple notification, and also tracks the recipe as notified.
     *
     * @param intent the intent from the receiver.
     */
    public void sendSimpleNotification(Context context, @NonNull Intent intent) {
        ReactionBundle content = intent.getParcelableExtra(NearItIntentConstants.CONTENT);
        String notifText = content.notificationMessage;

        String notifTitle = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();

        sendNotifiedTracking(intent);

        NearNotification.send(context,
                imgResFromIntent(intent),
                notifTitle,
                notifText,
                getAutoTrackingTargetIntent(context, intent),
                uniqueNotificationCode()
        );
    }

    protected void sendNotifiedTracking(@NonNull Intent intent) {
        TrackingInfo trackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO);
        NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS);
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

    private int imgResFromIntent(@NonNull Intent intent) {
        GlobalConfig globalConfig = NearItManager.getInstance().globalConfig;
        String action = intent.getStringExtra(NearItIntentConstants.ACTION);
        if (action.equals(NearItManager.PUSH_MESSAGE_ACTION)) {
            return fetchPushNotification(globalConfig);
        } else if (action.equals(NearItManager.GEO_MESSAGE_ACTION)) {
            return fetchProximityNotification(globalConfig);
        } else
            return fetchProximityNotification(globalConfig);
    }

    private Intent getAutoTrackingTargetIntent(Context context, @NonNull Intent intent) {
        return new Intent(context, AutoTrackingReceiver.class)
                .putExtras(intent.getExtras());
    }

    private static int uniqueNotificationCode() {
        return (int) Calendar.getInstance().getTimeInMillis();
    }
}
