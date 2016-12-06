package it.near.sdk.Utils;

import android.content.Intent;
import android.support.annotation.Nullable;

import it.near.sdk.Reactions.Content.Content;
import it.near.sdk.Reactions.Coupon.Coupon;
import it.near.sdk.Reactions.CustomJSON.CustomJSON;
import it.near.sdk.Reactions.Feedback.Feedback;
import it.near.sdk.Reactions.Poll.Poll;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;

/**
 * Interface for being notified of core content types.
 *
 * @author cattaneostefano
 */
public interface CoreContentsListener {
    void getPollNotification(@Nullable Intent intent, Poll notification, String recipeId);
    void getContentNotification(@Nullable Intent intent, Content notification, String recipeId);
    void getCouponNotification(@Nullable Intent intent, Coupon notification, String recipeId);
    void getCustomJSONNotification(@Nullable Intent intent, CustomJSON notification, String recipeId);
    void getSimpleNotification(@Nullable Intent intent, SimpleNotification s_notif, String recipeId);
    void getFeedbackNotification(@Nullable Intent intent, Feedback s_notif, String recipeId);
}
