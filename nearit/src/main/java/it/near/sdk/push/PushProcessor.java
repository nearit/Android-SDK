package it.near.sdk.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import it.near.sdk.recipes.RecipesManager;

public class PushProcessor {

    private final RecipesManager recipesManager;

    private static final String REACTION_PLUGIN_ID = "reaction_plugin_id";
    private static final String REACTION_ACTION_ID = "reaction_action_id";
    private static final String REACTION_BUNDLE_ID = "reaction_bundle_id";
    private static final String RECIPE_ID = "recipe_id";
    private static final String NOTIFICATION = "notification";
    private static final String REACTION_BUNDLE = "reaction_bundle";
    private static final String NOTIFICATION_BODY = "body";

    public PushProcessor(RecipesManager recipesManager) {
        this.recipesManager = recipesManager;
    }

    public boolean processPush(Map pushData) {

        if (pushHasReactionInfo(pushData)) {
            String reactionPluginId = (String) pushData.get(REACTION_PLUGIN_ID);
            String reactionActionId = (String) pushData.get(REACTION_ACTION_ID);
            String reactionBundleId = (String) pushData.get(REACTION_BUNDLE_ID);
            JSONObject notification = null;
            try {
                notification = new JSONObject((String) pushData.get(NOTIFICATION));
                String notificationText = (String) notification.get(NOTIFICATION_BODY);

                if (pushHasReactionBundle(pushData)) {

                } else {
                    
                }

            } catch (JSONException e) {
                oldProcessRequest(pushData);
            }
        } else {
            oldProcessRequest(pushData);
        }
    }

    private void oldProcessRequest(Map pushData) {
        if (!pushData.containsKey(RECIPE_ID)) return;
        recipesManager.processRecipe((String) pushData.get(RECIPE_ID));
    }

    private boolean pushHasReactionBundle(Map pushData) {
        return pushData.containsKey(REACTION_BUNDLE);
    }

    private boolean pushHasReactionInfo(Map pushData) {
        return pushData.containsKey(REACTION_PLUGIN_ID) &&
                pushData.containsKey(REACTION_ACTION_ID) &&
                pushData.containsKey(REACTION_BUNDLE_ID) &&
                pushHasNotification(pushData);
    }

    private boolean pushHasNotification(Map pushData) {
        if (!pushData.containsKey(NOTIFICATION))
            return false;
        try {
            JSONObject notification = new JSONObject((String) pushData.get(NOTIFICATION));
            return notification.has(NOTIFICATION_BODY);
        } catch (JSONException e) {
            return false;
        }
    }
}
