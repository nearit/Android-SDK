package it.near.sdk.push;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.RecipesManager;

/**
 * Service that receives push notification.
 *
 * @author cattaneostefano
 */
public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmListenerService";
    private static final String RECIPE_ID = "recipe_id";
    private static final String REACTION_PLUGIN_ID = "reaction_plugin_id";
    private static final String REACTION_ACTION_ID = "reaction_action_id";
    private static final String REACTION_BUNDLE_ID = "reaction_bundle_id";
    private static final String REACTION_BUNDLE = "reaction_bundle";
    private static final String NOTIFICATION = "notification";
    private static final String NOTIFICATION_BODY = "body";

    /**
     * Called when message is received.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {
        NearLog.d(TAG, "From: " + message.getFrom());
        NearLog.d(TAG, "Message: " + message);

        Map data = message.getData();
        String recipe_id = (String) data.get(RECIPE_ID);

        try {

            if (data.containsKey(REACTION_PLUGIN_ID) &&
                    data.containsKey(REACTION_ACTION_ID) &&
                    data.containsKey(REACTION_BUNDLE_ID) &&
                    data.containsKey(NOTIFICATION)) {
                String reactionPluginId = (String) data.get(REACTION_PLUGIN_ID);
                String reactionActionId = (String) data.get(REACTION_ACTION_ID);
                String reactionBundleId = (String) data.get(REACTION_BUNDLE_ID);
                JSONObject notification = new JSONObject((String) data.get(NOTIFICATION));
                String notificationText = (String) notification.get(NOTIFICATION_BODY);
                if (data.containsKey(REACTION_BUNDLE)) {
                    String reactionBundleString = (String) data.get(REACTION_BUNDLE);
                    boolean success = getRecipesManager().processReactionBundle(
                            recipe_id, notificationText,
                            reactionPluginId, reactionActionId,
                            reactionBundleString
                    );
                    if (!success) {
                        getRecipesManager().processRecipe(recipe_id, notificationText, reactionPluginId, reactionActionId, reactionBundleId);
                    }
                } else {
                    getRecipesManager().processRecipe(recipe_id, notificationText, reactionPluginId, reactionActionId, reactionBundleId);
                }
            } else {
                getRecipesManager().processRecipe(recipe_id);
            }


        } catch (JSONException e) {
            getRecipesManager().processRecipe(recipe_id);
        }


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

