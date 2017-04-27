package com.nearit.sample;

import android.content.Intent;
import android.support.annotation.Nullable;

import it.near.sdk.reactions.content.Content;
import it.near.sdk.reactions.coupon.Coupon;
import it.near.sdk.reactions.customjson.CustomJSON;
import it.near.sdk.reactions.feedback.Feedback;
import it.near.sdk.reactions.poll.Poll;
import it.near.sdk.reactions.simplenotification.SimpleNotification;
import it.near.sdk.recipes.background.NearItIntentService;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

/**
 * This is the manifest element for the IntentService
 * <service android:name=".MyCustomIntentService"
 *           android:exported="false"/>
 */

public class MyCustomIntentService extends NearItIntentService implements CoreContentsListener {


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        /*
        Do whatever you want with the intent, like setting a cooldown or filter events

        There is an utility method to automatically process known content types and calls the CoreContentsListener callback methods.
        parseCoreContents(Intent intent, CoreContentsListener listener);

        IMPORTANT
        Since you are overriding the default notification mechanism, remember to track the recipe as notified with:
        String recipeId = intent.getStringExtra("recipe_id");
        try {
            nearItManager.getRecipesManager().sendTracking(recipeId, Recipe.NOTIFIED_STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        There is an utility method for creating notifications
        NearNotification.send(context, GlobalConfig.getInstance(this).getProximityNotificationIcon(), notificationTitle, notificationText, targetIntent, NOTIFICATION_ID);
        */
        NearUtils.parseCoreContents(intent, this);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        MyCustomBroadcastReceiver.completeWakefulIntent(intent);
    }


    // handle the content specifically for different content types in these callback methods
    @Override
    public void gotPollNotification(@Nullable Intent intent, Poll notification, String recipeId) {

    }

    @Override
    public void gotContentNotification(@Nullable Intent intent, Content notification, String recipeId) {

    }

    @Override
    public void gotCouponNotification(@Nullable Intent intent, Coupon notification, String recipeId) {

    }

    @Override
    public void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, String recipeId) {

    }

    @Override
    public void gotSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, String recipeId) {

    }

    @Override
    public void gotFeedbackNotification(@Nullable Intent intent, Feedback s_notif, String recipeId) {

    }
}
