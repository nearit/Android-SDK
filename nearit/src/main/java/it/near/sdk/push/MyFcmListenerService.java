package it.near.sdk.push;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import it.near.sdk.NearItManager;
import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.utils.FormatDecoder;

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

        new PushProcessor(
                getRecipesManager(),
                new FormatDecoder()
        ).processPush(message.getData());

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
        return NearItManager.getInstance(this).getRecipesManager();
    }
    // [END receive_message]

}

