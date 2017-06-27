package it.near.sdk.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.zip.DataFormatException;

import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.utils.FormatDecoder;

class PushProcessor {

    private final RecipesManager recipesManager;
    private final FormatDecoder formatDecoder;

    private static final String REACTION_PLUGIN_ID = "reaction_plugin_id";
    private static final String REACTION_ACTION_ID = "reaction_action_id";
    private static final String REACTION_BUNDLE_ID = "reaction_bundle_id";
    private static final String RECIPE_ID = "recipe_id";
    private static final String NOTIFICATION = "notification";
    private static final String REACTION_BUNDLE = "reaction_bundle";
    private static final String NOTIFICATION_BODY = "body";

    PushProcessor(RecipesManager recipesManager, FormatDecoder formatDecoder) {
        this.recipesManager = recipesManager;
        this.formatDecoder = formatDecoder;
    }

    boolean processPush(Map pushData) {
        if (!pushData.containsKey(RECIPE_ID)) return false;
        String recipeId = (String) pushData.get(RECIPE_ID);

        if (pushHasReactionInfo(pushData)) {
            String reactionPluginId = (String) pushData.get(REACTION_PLUGIN_ID);
            String reactionActionId = (String) pushData.get(REACTION_ACTION_ID);
            String reactionBundleId = (String) pushData.get(REACTION_BUNDLE_ID);
            JSONObject notification = null;
            try {
                notification = new JSONObject((String) pushData.get(NOTIFICATION));
                String notificationText = (String) notification.get(NOTIFICATION_BODY);

                if (pushHasReactionBundle(pushData)) {

                    try {
                        String reactionBundleString = decodeCompressedBundle(pushData);
                        boolean success = recipesManager.processReactionBundle(recipeId, notificationText, reactionPluginId, reactionActionId, reactionBundleString);
                        if (!success) {
                            return processFromBundleId(recipeId, notificationText, reactionPluginId, reactionActionId, reactionBundleId);
                        } else {
                            return true;
                        }

                    } catch (IOException | IllegalArgumentException | DataFormatException e) {
                        return processFromBundleId(recipeId, notificationText, reactionPluginId, reactionActionId, reactionBundleId);
                    }

                } else {
                    return processFromBundleId(recipeId, notificationText, reactionPluginId, reactionActionId, reactionBundleId);
                }

            } catch (JSONException e) {
                return oldProcessRequest(recipeId);
            }
        } else {
            return oldProcessRequest(recipeId);
        }
    }


    private boolean oldProcessRequest(String recipeId) {
        recipesManager.processRecipe(recipeId);
        return true;
    }

    private boolean processFromBundleId(String recipeId, String notificationText, String reactionPluginId, String reactionActionId, String reactionBundleId) {
        recipesManager.processRecipe(recipeId, notificationText, reactionPluginId, reactionActionId, reactionBundleId);
        return true;
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

    private String decodeCompressedBundle(Map pushData) throws IOException, DataFormatException {
        String compressed = (String) pushData.get(REACTION_BUNDLE);
        byte[] bytes = formatDecoder.decodeBae64(compressed);
        return formatDecoder.decompressZLIB(bytes);
    }
}
