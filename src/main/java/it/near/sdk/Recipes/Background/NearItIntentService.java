package it.near.sdk.Recipes.Background;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

import it.near.sdk.GlobalConfig;
import it.near.sdk.NearItManager;
import it.near.sdk.R;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearItIntentConstants;
import it.near.sdk.Utils.NearNotification;

/**
 * @author cattaneostefano.
 */
public class NearItIntentService extends IntentService {

    public static final int REGION_NOTIFICATION_ID = 1;
    public static final int PUSH_NOTIFICATION_ID = 2;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NearItIntentService() {
        super(NearItIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // we send the notification about the content intent
        if (intent != null) {
            sendSimpleNotification(intent);
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        NearItBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Send a simple notification, and also tracks the recipe as notified.
     * @param intent the intent from the receiver.
     */
    protected void sendSimpleNotification(@NonNull Intent intent){
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        targetIntent.putExtras(intent.getExtras());
        String notif_title = intent.getStringExtra(NearItIntentConstants.NOTIF_TITLE);
        String notifText = intent.getStringExtra(NearItIntentConstants.NOTIF_BODY);
        if (notif_title == null) {
            notif_title = getApplicationInfo().loadLabel(getPackageManager()).toString();
        }
        String recipeId = intent.getStringExtra(NearItIntentConstants.RECIPE_ID);
        try {
            RecipesManager.sendTracking(getApplicationContext(), recipeId, Recipe.NOTIFIED_STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NearNotification.send(this, imgResFromIntent(intent), notif_title, notifText, targetIntent, notificationCodeFromIntent(intent));

    }

    private int imgResFromIntent (@NonNull Intent intent) {
        if (intent.getAction().equals(NearItManager.PUSH_MESSAGE_ACTION)){
            return R.drawable.ic_send_white_24dp;
        } else if (intent.getAction().equals(NearItManager.GEO_MESSAGE_ACTION)){
            return GlobalConfig.getInstance(this).getNotificationImage();
        }
        return GlobalConfig.getInstance(this).getNotificationImage();
    }

    private int notificationCodeFromIntent(@NonNull Intent intent) {
        switch (intent.getAction()){
            case NearItManager.PUSH_MESSAGE_ACTION:
                return PUSH_NOTIFICATION_ID;
            case NearItManager.GEO_MESSAGE_ACTION:
                return REGION_NOTIFICATION_ID;
            default:
                return REGION_NOTIFICATION_ID;
        }
    }

}
