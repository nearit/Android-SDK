package com.nearit.sample;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.near.sdk.NearItManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;

import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

public class ForegroundActivity extends AppCompatActivity implements ProximityListener, CoreContentsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground);

        NearItManager.getInstance(this).addProximityListener(this);

    }


    @Override
    public void foregroundEvent(Parcelable content, Recipe recipe, TrackingInfo trackingInfo) {
        // You will be notified of any foreground content in this method
        // To have the Parcelable object casted appropriately you can use the utility method
        NearUtils.parseCoreContents(content, recipe, trackingInfo, this);

    }

    @Override
    public void gotContentNotification(@Nullable Intent intent, Content notification, TrackingInfo trackingInfo, String notificationMessage) {

    }

    @Override
    public void gotCouponNotification(@Nullable Intent intent, Coupon notification, TrackingInfo trackingInfo, String notificationMessage) {

    }

    @Override
    public void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, TrackingInfo trackingInfo, String notificationMessage) {

    }

    @Override
    public void gotSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, TrackingInfo trackingInfo, String notificationMessage) {

    }

    @Override
    public void gotFeedbackNotification(@Nullable Intent intent, Feedback s_notif, TrackingInfo trackingInfo, String notificationMessage) {

    }
}
