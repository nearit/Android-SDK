package it.near.sdk.Push;

import android.content.Intent;

import it.near.sdk.Reactions.Content.Content;
import it.near.sdk.Utils.CoreContentsListener;
import it.near.sdk.Reactions.Coupon.Coupon;
import it.near.sdk.Reactions.CustomJSON.CustomJSON;
import it.near.sdk.Reactions.Feedback.Feedback;
import it.near.sdk.Reactions.Poll.Poll;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;
import it.near.sdk.Utils.BaseIntentService;
import it.near.sdk.Utils.NearUtils;

/**
 * @author cattaneostefano.
 */
public class FcmIntentService extends BaseIntentService implements CoreContentsListener {

    private static String TAG = "FcmIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NearUtils.parseCoreContents(intent, this);
        FcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public void gotPollNotification(Intent intent, Poll notification, String recipeId) {
        sendSimpleNotification(intent);
    }

    @Override
    public void gotContentNotification(Intent intent, Content notification, String recipeId) {
        sendSimpleNotification(intent);
    }

    @Override
    public void gotCouponNotification(Intent intent, Coupon notification, String recipeId) {
        sendSimpleNotification(intent);
    }

    @Override
    public void gotCustomJSONNotification(Intent intent, CustomJSON notification, String recipeId) {
        // TODO this was a custom json, implementation may vary, it's usually overriden
    }

    @Override
    public void gotSimpleNotification(Intent intent, SimpleNotification s_notif, String recipeId) {
        sendSimpleNotification(intent);
    }

    @Override
    public void gotFeedbackNotification(Intent intent, Feedback s_notif, String recipeId) {
        sendSimpleNotification(intent);
    }

}
