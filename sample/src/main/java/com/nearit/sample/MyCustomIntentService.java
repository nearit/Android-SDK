package com.nearit.sample;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.poll.Poll;
import it.near.sdk.reactions.simplenotification.SimpleNotification;
import it.near.sdk.recipes.background.NearItIntentService;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

/**
 * This is the manifest element for the IntentService
 * <service android:name=".MyCustomIntentService"
 * android:exported="false"/>
 */

public class MyCustomIntentService extends NearItIntentService implements CoreContentsListener {


    private final String TAG = getClass().getName();

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            sendSimpleNotification(intent);
            NearUtils.parseCoreContents(intent, this);
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            MyCustomBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    // handle the content specifically for different content types in these callback methods
    @Override
    public void gotPollNotification(@Nullable Intent intent, Poll notification, String recipeId) {
        Log.d(TAG, "gotPollNotification");
    }

    @Override
    public void gotContentNotification(@Nullable Intent intent, Content notification, String recipeId) {
        Log.d(TAG, "gotContentNotification");
    }

    @Override
    public void gotCouponNotification(@Nullable Intent intent, Coupon notification, String recipeId) {
        Log.d(TAG, "gotCouponNotification");
    }

    @Override
    public void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, String recipeId) {
        Log.d(TAG, "gotCustomJsonNotification");
    }

    @Override
    public void gotSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, String recipeId) {
        Log.d(TAG, "gotSimpleNotification");
    }

    @Override
    public void gotFeedbackNotification(@Nullable Intent intent, Feedback s_notif, String recipeId) {
        Log.d(TAG, "gotFeedbackNotification");
    }
}
