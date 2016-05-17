package it.near.sdk.Utils;

import android.app.IntentService;
import android.content.Intent;

import it.near.sdk.Reactions.ContentNotification.ContentNotification;
import it.near.sdk.Reactions.CoreContentsListener;
import it.near.sdk.Reactions.PollNotification.PollNotification;

/**
 * @author cattaneostefano.
 */
public abstract class BaseIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BaseIntentService(String name) {
        super(name);
    }

    /**
     * Utility method for extensions of this class. It notifies the listener if the intent contains a recognized core content.
     * @param intent The intent to analyze.
     * @param listener Contains a callback method for each content type.
     * @return true if the content was recognized as core and passed to a callback method, false if it wasn't.
     */
    protected boolean parseCoreContents(Intent intent, CoreContentsListener listener) {

        String reaction_plugin = intent.getExtras().getString("reaction-plugin");
        String reaction_action = intent.getExtras().getString("reaction-action");
        String notif_body = intent.getExtras().getString("notif_body");

        String pulse_plugin = intent.getExtras().getString("pulse-plugin");
        String pulse_action = intent.getExtras().getString("pulse-action");
        String pulse_bundle = intent.getExtras().getString("pulse-bundle");

        ContentNotification c_notif;
        PollNotification p_notif;

        boolean coreContent = false;
        if (reaction_plugin == null) return false;
        switch (reaction_plugin) {
            case "content-notification" :
                c_notif = (ContentNotification) intent.getParcelableExtra("content");
                if (c_notif.isSimpleNotification()){
                    listener.gotSimpleNotification(intent, notif_body, reaction_plugin, reaction_action, pulse_plugin, pulse_action, pulse_bundle);
                } else {
                    listener.getContentNotification(intent, c_notif, notif_body, reaction_plugin, reaction_action, pulse_plugin, pulse_action, pulse_bundle);
                }
                coreContent = true;
                break;
            case "poll-notification" :
                p_notif = (PollNotification) intent.getParcelableExtra("content");
                listener.getPollNotification(intent, p_notif, notif_body, reaction_plugin, reaction_action, pulse_plugin, pulse_action, pulse_bundle);
                coreContent = true;
                break;
        }
        return coreContent;
    }
}
