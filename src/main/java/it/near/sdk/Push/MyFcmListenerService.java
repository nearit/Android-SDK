package it.near.sdk.Push;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import it.near.sdk.GlobalState;
import it.near.sdk.Recipes.RecipesManager;

/**
 * Service that receives push notification.
 *
 * @author cattaneostefano
 */
public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmListenerService";

    /**
     * Called when message is received.
     *
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "From: " + message.getFrom());
        Log.d(TAG, "Message: " + message);

        Map data = message.getData();
        String recipe_id = (String) data.get("recipe_id");
        String push_id = (String) data.get("push_id");

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

    private RecipesManager getRecipesManager() { return GlobalState.getInstance(getApplicationContext()).getRecipesManager(); }
    // [END receive_message]


}

