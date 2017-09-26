package com.nearit.sample;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.background.NearItBroadcastReceiver;
import it.near.sdk.recipes.background.NearItIntentService;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

/**
 * This is the manifest element for the IntentService
 * <service android:name=".MyCustomIntentService"
 * android:exported="false">
 *  <intent-filter>
 *      <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
 *      <category android:name="android.intent.category.DEFAULT" />
 *  </intent-filter>
 *  <intent-filter>
 *      <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
 *      <category android:name="android.intent.category.DEFAULT" />
 *  </intent-filter>
 * </service>
 */

public class MyCustomIntentService extends NearItIntentService implements CoreContentsListener {


    private final String TAG = getClass().getName();

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            sendSimpleNotification(intent);
            NearUtils.parseCoreContents(intent, this);
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            NearItBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    @Override
    public void gotContentNotification(Content notification, TrackingInfo trackingInfo) {
        Log.d(TAG, "gotContentNotification");
    }

    @Override
    public void gotCouponNotification(Coupon notification, TrackingInfo trackingInfo) {
        Log.d(TAG, "gotCouponNotification");
    }

    @Override
    public void gotCustomJSONNotification(CustomJSON notification, TrackingInfo trackingInfo) {
        Log.d(TAG, "gotCustomJsonNotification");
    }

    @Override
    public void gotSimpleNotification(SimpleNotification s_notif, TrackingInfo trackingInfo) {
        Log.d(TAG, "gotSimpleNotification");
    }

    @Override
    public void gotFeedbackNotification(Feedback s_notif, TrackingInfo trackingInfo) {
        Log.d(TAG, "gotFeedbackNotification");
    }
}
