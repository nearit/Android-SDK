package it.near.sdk.utils;

import android.content.Intent;
import android.support.annotation.Nullable;

import it.near.sdk.reactions.content.Content;
import it.near.sdk.reactions.coupon.Coupon;
import it.near.sdk.reactions.customjson.CustomJSON;
import it.near.sdk.reactions.feedback.Feedback;
import it.near.sdk.reactions.poll.Poll;
import it.near.sdk.reactions.simplenotification.SimpleNotification;

/**
 * Interface for being notified of core content types.
 *
 * @author cattaneostefano
 */
public interface CoreContentsListener {
    void gotPollNotification(@Nullable Intent intent, Poll notification, String recipeId);
    void gotContentNotification(@Nullable Intent intent, Content notification, String recipeId);
    void gotCouponNotification(@Nullable Intent intent, Coupon notification, String recipeId);
    void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, String recipeId);
    void gotSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, String recipeId);
    void gotFeedbackNotification(@Nullable Intent intent, Feedback s_notif, String recipeId);
}
