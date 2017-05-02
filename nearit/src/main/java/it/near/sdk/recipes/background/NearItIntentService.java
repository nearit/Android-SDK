package it.near.sdk.recipes.background;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import org.json.JSONException;

import it.near.sdk.GlobalConfig;
import it.near.sdk.NearItManager;
import it.near.sdk.R;
import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.RecipesManager;
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
                notificationCodeFromIntent(intent)
        );
    }

    private Intent getLauncherTargetIntent(@NonNull Intent intent) {
        return getPackageManager()
                .getLaunchIntentForPackage(this.getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtras(intent.getExtras());
    }

    private void sendNotifiedTracking(@NonNull Intent intent) {
        String recipeId = intent.getStringExtra(NearItIntentConstants.RECIPE_ID);
        try {
            RecipesManager recipesManager = RecipesManager.getInstance();
            if (recipesManager != null) {
                recipesManager.sendTracking(recipeId, Recipe.NOTIFIED_STATUS);
            }
        } catch (JSONException e) {
            NearLog.d(TAG, "Invalid track body");
        }
    }

    private int imgResFromIntent(@NonNull Intent intent) {
        if (intent.getAction().equals(NearItManager.PUSH_MESSAGE_ACTION)) {
            return fetchPushNotification();
        } else if (intent.getAction().equals(NearItManager.GEO_MESSAGE_ACTION)) {
            return fetchProximityNotification();
        } else
            return fetchProximityNotification();
    }

    private int fetchProximityNotification() {
        int imgRes = GlobalConfig.getInstance(this).getProximityNotificationIcon();
        if (imgRes != GlobalConfig.DEFAULT_EMPTY_NOTIFICATION) {
            return imgRes;
        } else {
            return DEFAULT_GEO_NOTIFICATION_ICON;
        }
    }

    private int fetchPushNotification() {
        int imgRes = GlobalConfig.getInstance(this).getPushNotificationIcon();
        if (imgRes != GlobalConfig.DEFAULT_EMPTY_NOTIFICATION) {
            return imgRes;
        } else {
            return DEFAULT_PUSH_NOTIFICATION_ICON;
        }
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
