package it.near.sdk.Reactions;

import android.content.Intent;

import it.near.sdk.Reactions.Content.Content;
import it.near.sdk.Reactions.Coupon.Coupon;
import it.near.sdk.Reactions.Poll.Poll;

/**
 * Interface for being notified of core content types.
 *
 * @author cattaneostefano
 */
public interface CoreContentsListener {
    public abstract void getPollNotification(Intent intent, Poll notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
    public abstract void getContentNotification(Intent intent, Content notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
    public abstract void getCouponNotification(Intent intent, Coupon notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
}
