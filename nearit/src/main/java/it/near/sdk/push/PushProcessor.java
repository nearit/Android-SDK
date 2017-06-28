package it.near.sdk.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.zip.DataFormatException;

import it.near.sdk.reactions.simplenotification.SimpleNotificationReaction;
import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.utils.FormatDecoder;

class PushProcessor {

    private final RecipesManager recipesManager;
    private final FormatDecoder formatDecoder;

    static final String REACTION_PLUGIN_ID = "reaction_plugin_id";
    static final String REACTION_ACTION_ID = "reaction_action_id";
    static final String REACTION_BUNDLE_ID = "reaction_bundle_id";
    static final String RECIPE_ID = "recipe_id";
    static final String NOTIFICATION = "notification";
    static final String REACTION_BUNDLE = "reaction_bundle";
    static final String NOTIFICATION_BODY = "body";

    PushProcessor(RecipesManager recipesManager, FormatDecoder formatDecoder) {
        this.recipesManager = recipesManager;
        this.formatDecoder = formatDecoder;
    }

    boolean processPush(Map pushData) {
        if (!pushData.containsKey(RECIPE_ID)) return false;
        String recipeId = (String) pushData.get(RECIPE_ID);

        normalizeSimpleNotificationContent(pushData);

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

                    } catch (UnsupportedEncodingException | IllegalArgumentException | DataFormatException e) {
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

    private void normalizeSimpleNotificationContent(Map pushData) {
        if (pushData.containsKey(REACTION_PLUGIN_ID) &&
                pushData.get(REACTION_PLUGIN_ID).equals(SimpleNotificationReaction.PLUGIN_NAME)) {
            pushData.put(REACTION_BUNDLE_ID, "dummy");
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

    private String decodeCompressedBundle(Map pushData) throws DataFormatException, UnsupportedEncodingException {
        String compressed = (String) pushData.get(REACTION_BUNDLE);
        byte[] bytes = formatDecoder.decodeBase64(compressed);
        return formatDecoder.decompressZLIB(bytes);
    }
}
