package it.near.sdk.Utils;

import android.app.IntentService;
import android.content.Intent;

import org.json.JSONException;

import it.near.sdk.GlobalConfig;
import it.near.sdk.NearItManager;
import it.near.sdk.R;
import it.near.sdk.Reactions.Content.Content;
import it.near.sdk.Reactions.Coupon.Coupon;
import it.near.sdk.Reactions.CustomJSON.CustomJSON;
import it.near.sdk.Reactions.Feedback.Feedback;
import it.near.sdk.Reactions.Poll.Poll;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;
import it.near.sdk.Recipes.Models.Recipe;

/**
 * @author cattaneostefano.
 */
public abstract class BaseIntentService extends IntentService {

    public static final int REGION_NOTIFICATION_ID = 1;
    public static final int PUSH_NOTIFICATION_ID = 2;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BaseIntentService(String name) {
        super(name);
    }


    protected void sendSimpleNotification(Intent intent){
        Intent targetIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        targetIntent.putExtras(intent.getExtras());
        String notif_title = intent.getStringExtra(IntentConstants.NOTIF_TITLE);
        String notifText = intent.getStringExtra(IntentConstants.NOTIF_BODY);
        if (notif_title == null) {
            notif_title = getApplicationInfo().loadLabel(getPackageManager()).toString();
        }
        String recipeId = intent.getStringExtra(IntentConstants.RECIPE_ID);
        try {
            Recipe.sendTracking(getApplicationContext(), recipeId, Recipe.NOTIFIED_STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NearNotification.send(this, imgResFromIntent(intent), notif_title, notifText, targetIntent, notificationCodeFromIntent(intent));

    }

    private int imgResFromIntent (Intent intent) {
        if (intent.getAction().equals(NearItManager.PUSH_MESSAGE_ACTION)){
            return R.drawable.ic_send_white_24dp;
        } else if (intent.getAction().equals(NearItManager.REGION_MESSAGE_ACTION)){
            return GlobalConfig.getInstance(this).getNotificationImage();
        }
        return GlobalConfig.getInstance(this).getNotificationImage();
    }

    private int notificationCodeFromIntent(Intent intent) {
        switch (intent.getAction()){
            case NearItManager.PUSH_MESSAGE_ACTION:
                return PUSH_NOTIFICATION_ID;
            case NearItManager.REGION_MESSAGE_ACTION:
                return REGION_NOTIFICATION_ID;
            default:
                return REGION_NOTIFICATION_ID;
        }
    }

}
