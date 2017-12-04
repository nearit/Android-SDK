package com.nearit.sample_kotlin

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import it.near.sdk.NearItManager
import it.near.sdk.reactions.contentplugin.model.Content
import it.near.sdk.reactions.couponplugin.model.Coupon
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON
import it.near.sdk.reactions.feedbackplugin.model.Feedback
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification
import it.near.sdk.recipes.foreground.ProximityListener
import it.near.sdk.trackings.TrackingInfo
import it.near.sdk.utils.ContentsListener
import it.near.sdk.utils.NearUtils

class ForegroundActivity : AppCompatActivity(), ProximityListener, ContentsListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground)

        NearItManager.getInstance().addProximityListener(this)
    }

    override fun foregroundEvent(content: Parcelable?, trackingInfo: TrackingInfo?) {
        // You will be notified of any foreground content in this method
        // To have the Parcelable object casted appropriately you can use the utility method
        NearUtils.parseContents(content, trackingInfo, this)
    }

    override fun gotContentNotification(notification: Content?, trackingInfo: TrackingInfo?) {

    }

    override fun gotCouponNotification(notification: Coupon?, trackingInfo: TrackingInfo?) {

    }

    override fun gotCustomJSONNotification(notification: CustomJSON?, trackingInfo: TrackingInfo?) {

    }

    override fun gotSimpleNotification(s_notif: SimpleNotification?, trackingInfo: TrackingInfo?) {

    }

    override fun gotFeedbackNotification(s_notif: Feedback?, trackingInfo: TrackingInfo?) {

    }
}