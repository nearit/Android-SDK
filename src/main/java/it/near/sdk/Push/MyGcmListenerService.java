package it.near.sdk.Push;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import it.near.sdk.GlobalState;
import it.near.sdk.Recipes.RecipesManager;

/**
 * Service that receives push notification.
 *
 * @author cattaneostefano
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        String recipe_id = data.getString("recipe_id");
        String push_id = data.getString("push_id");

        // TODO track received push
        getPushManager().trackPush(push_id, PushManager.PUSH_RECEIVED_ACTION);

        getRecipesManager().processRecipe(recipe_id);

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // sendNotification(message);
        // [END_EXCLUDE]
    }

    private PushManager getPushManager() { return GlobalState.getInstance(getApplicationContext()).getPushManager(); }
    // [END receive_message]

    private RecipesManager getRecipesManager(){return GlobalState.getInstance(getApplicationContext()).getRecipesManager();}

}

