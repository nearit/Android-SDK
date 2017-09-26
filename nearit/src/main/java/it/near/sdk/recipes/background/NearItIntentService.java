package it.near.sdk.recipes.background;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
            sendSimpleNotification(this, intent);
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            NearItBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Send a simple notification, and also tracks the recipe as notified.
     *
     * @param intent the intent from the receiver.
     */
    public static void sendSimpleNotification(Context context, @NonNull Intent intent) {
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

    private static Intent getAutoTrackingTargetIntent(Context context, @NonNull Intent intent) {
        return new Intent(context, AutoTrackingReceiver.class)
            .putExtras(intent.getExtras());
    }

    protected static void sendNotifiedTracking(@NonNull Intent intent) {
        TrackingInfo trackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO);
        NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS);
    }

    private static int imgResFromIntent(@NonNull Intent intent) {
        GlobalConfig globalConfig = NearItManager.getInstance().globalConfig;
        String action = intent.getStringExtra(NearItIntentConstants.ACTION);
        if (action.equals(NearItManager.PUSH_MESSAGE_ACTION)) {
            return fetchPushNotification(globalConfig);
        } else if (action.equals(NearItManager.GEO_MESSAGE_ACTION)) {
            return fetchProximityNotification(globalConfig);
        } else
            return fetchProximityNotification(globalConfig);
    }

    private static int fetchProximityNotification(GlobalConfig globalConfig) {
        int imgRes = globalConfig.getProximityNotificationIcon();
        if (imgRes != GlobalConfig.DEFAULT_EMPTY_NOTIFICATION) {
            return imgRes;
        } else {
            return DEFAULT_GEO_NOTIFICATION_ICON;
        }
    }

    private static int fetchPushNotification(GlobalConfig globalConfig) {
        int imgRes = globalConfig.getPushNotificationIcon();
        if (imgRes != GlobalConfig.DEFAULT_EMPTY_NOTIFICATION) {
            return imgRes;
        } else {
            return DEFAULT_PUSH_NOTIFICATION_ICON;
        }
    }

    private static int uniqueNotificationCode() {
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

    @Nullable
    public static Intent getIntent(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent().setAction(action).setPackage(context.getPackageName());
        List<ResolveInfo> resolverInfo = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA);
        if (resolverInfo.size() < 1) {
            return null;
        }
        return intent;
    }

    @Nullable
    public static String getResolverInfo(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent().setAction(action).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA);
        if (resolveInfos.size() < 1) return null;
        return resolveInfos.get(0).serviceInfo.name;
    }
}
