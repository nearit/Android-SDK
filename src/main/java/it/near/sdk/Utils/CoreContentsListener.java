package it.near.sdk.Utils;

import android.content.Intent;

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
    void getPollNotification(Intent intent, Poll notification, String recipeId);
    void getContentNotification(Intent intent, Content notification, String recipeId);
    void getCouponNotification(Intent intent, Coupon notification, String recipeId);
    void getCustomJSONNotification(Intent intent, CustomJSON notification, String recipeId);
    void getSimpleNotification(Intent intent, SimpleNotification s_notif, String recipeId);
    void getFeedbackNotification(Intent intent, Feedback s_notif, String recipeId);
}
