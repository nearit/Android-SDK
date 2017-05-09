package it.near.sdk.push;



import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import it.near.sdk.GlobalState;
import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.RecipesManager;

/**
 * Service that receives push notification.
 *
 * @author cattaneostefano
 */
public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmListenerService";

    /**
     * Called when message is received.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {
        NearLog.d(TAG, "From: " + message.getFrom());
        NearLog.d(TAG, "Message: " + message);

        Map data = message.getData();
        String recipe_id = (String) data.get("recipe_id");
        String push_id = (String) data.get("push_id");

        getRecipesManager().processRecipe(recipe_id);

        // [START_EXCLUDE]
        /*
          Production applications would usually process the message here.
          Eg: - Syncing with server.
              - Store message in local database.
              - Update UI.
         */

        /*
          In some cases it may be useful to show a notification indicating to the user
          that a message was received.
         */
        // sendNotification(message);
        // [END_EXCLUDE]
    }

    private RecipesManager getRecipesManager() {
        return RecipesManager.getInstance();
    }
    // [END receive_message]

}

