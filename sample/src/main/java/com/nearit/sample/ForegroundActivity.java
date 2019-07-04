package com.nearit.sample;

import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;

import it.near.sdk.NearItManager;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.foreground.ProximityListener;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.ContentsListener;
import it.near.sdk.utils.NearUtils;

public class ForegroundActivity extends AppCompatActivity implements ProximityListener, ContentsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground);

        NearItManager.getInstance().addProximityListener(this);
    }

    @Override
    public void foregroundEvent(Parcelable content, TrackingInfo trackingInfo) {
        // You will be notified of any foreground content in this method
        // To have the Parcelable object casted appropriately you can use the utility method
        NearUtils.parseContents(content, trackingInfo, this);

    }

    @Override
    public void gotContentNotification(Content notification, TrackingInfo trackingInfo) {

    }

    @Override
    public void gotCouponNotification(Coupon notification, TrackingInfo trackingInfo) {

    }

    @Override
    public void gotCustomJSONNotification(CustomJSON notification, TrackingInfo trackingInfo) {

    }

    @Override
    public void gotSimpleNotification(SimpleNotification s_notif, TrackingInfo trackingInfo) {

    }

    @Override
    public void gotFeedbackNotification(Feedback s_notif, TrackingInfo trackingInfo) {

    }
}
