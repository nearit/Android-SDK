package it.near.sdk.Reactions;

import android.content.Intent;

import it.near.sdk.Reactions.ContentNotification.ContentNotification;
import it.near.sdk.Reactions.PollNotification.PollNotification;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;

/**
 * @author cattaneostefano
 */
public interface CoreContentsListener {
    public abstract void getPollNotification(Intent intent, PollNotification notification, String content_source, String content_type, String trigger_source, String trigger_type, String trigger_item);
    public abstract void getContentNotification(Intent intent,ContentNotification notification, String content_source, String content_type, String trigger_source, String trigger_type, String trigger_item);
    public abstract void gotSimpleNotification(Intent intent,SimpleNotification notification, String content_source, String content_type, String trigger_source, String trigger_type, String trigger_item);
}
