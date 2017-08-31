package it.near.sdk.utils;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.trackings.TrackingInfo;

/**
 * Interface for being notified of core content types.
 *
 * @author cattaneostefano
 */
public interface CoreContentsListener {

    void gotContentNotification(Content notification, TrackingInfo trackingInfo);

    void gotCouponNotification(Coupon notification, TrackingInfo trackingInfo);

    void gotCustomJSONNotification(CustomJSON notification, TrackingInfo trackingInfo);

    void gotSimpleNotification(SimpleNotification s_notif, TrackingInfo trackingInfo);

    void gotFeedbackNotification(Feedback s_notif, TrackingInfo trackingInfo);
}
