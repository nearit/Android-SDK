package it.near.sdk.Reactions;

import android.content.Intent;

import it.near.sdk.Reactions.Content.Content;
import it.near.sdk.Reactions.Coupon.Coupon;
import it.near.sdk.Reactions.CustomJSON.CustomJSON;
import it.near.sdk.Reactions.Poll.Poll;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;

/**
 * Interface for being notified of core content types.
 *
 * @author cattaneostefano
 */
public interface CoreContentsListener {
    void getPollNotification(Intent intent, Poll notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
    void getContentNotification(Intent intent, Content notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
    void getCouponNotification(Intent intent, Coupon notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
    void getCustomJSONNotification(Intent intent, CustomJSON notification, String notificationText, String content_plugin, String content_action, String pulse_plugin, String pulse_action, String pulse_bundle);
    void getSimpleNotification(Intent intent, SimpleNotification s_notif, String notif_body, String reaction_plugin, String reaction_action, String pulse_plugin, String pulse_action, String pulse_bundle);
}
