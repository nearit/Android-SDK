package it.near.sdk.push;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import it.near.sdk.NearItManager;
import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.RecipeReactionHandler;
import it.near.sdk.utils.FormatDecoder;

/**
 * Service that receives push notification.
 *
 * @author cattaneostefano
 */
public class NearFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "NearFcmListenerService";

    /**
     * Called when message is received.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {
        NearLog.d(TAG, "From: " + message.getFrom());
        NearLog.d(TAG, "Message: " + message);

        processRemoteMessage(message);
    }

    public static void processRemoteMessage(RemoteMessage remoteMessage) {
        new PushProcessor(
                getRecipesReactionHandler(),
                new FormatDecoder()
        ).processPush(remoteMessage.getData());
    }

    private static RecipeReactionHandler getRecipesReactionHandler() {
        return NearItManager.getInstance().getRecipesReactionHandler();
    }
    // [END receive_message]

}

