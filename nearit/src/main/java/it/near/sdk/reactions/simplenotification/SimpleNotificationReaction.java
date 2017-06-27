package it.near.sdk.reactions.simplenotification;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.recipes.NearNotifier;

/**
 * Created by cattaneostefano on 07/10/2016.
 */

public class SimpleNotificationReaction extends CoreReaction {
    public static final String PLUGIN_NAME = "simple-notification";
    private static final String SHOW_SIMPLE_NOTIFICATION_ACTION_NAME = "simple_notification";
    private static final String PREFS_SUFFIX = "NearSimpleNot";
    private static final String RES_NAME = "contents";

    public SimpleNotificationReaction(Context mContext, NearNotifier nearNotifier) {
        super(mContext, nearNotifier);
    }

    @Override
    public void buildActions() {
        supportedActions = new ArrayList<String>();
        supportedActions.add(SHOW_SIMPLE_NOTIFICATION_ACTION_NAME);
    }

    @Override
    public void refreshConfig() {
        // intentionally left empty
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe) {
        switch (reaction_action) {
            case SHOW_SIMPLE_NOTIFICATION_ACTION_NAME:
                showContent(null, recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(Recipe recipe, String push_id, ReactionBundle reaction_bundle) {
        nearNotifier.deliverBackgroundPushReaction(contentFromRecipe(recipe), recipe.getId(), recipe.getNotificationBody(), getPluginName());
    }

    @Override
    public void handlePushReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleId) {
        nearNotifier.deliverBackgroundPushReaction(contentFromRecipe(notificationText), recipeId, notificationText, getPluginName());
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        nearNotifier.deliverBackgroundPushReaction(contentFromRecipe(notificationText), recipeId, notificationText, getPluginName());
        return true;
    }

    private SimpleNotification contentFromRecipe(String notificationText) {
        return new SimpleNotification(notificationText, null);
    }

    private SimpleNotification contentFromRecipe(Recipe recipe) {
        return new SimpleNotification(recipe.getNotificationBody(), recipe.getNotificationTitle());
    }

    @Override
    protected void getContent(String reaction_bundle, Recipe recipe, ContentFetchListener listener) {
        listener.onContentFetched(contentFromRecipe(recipe), false);
    }

    @Override
    public String getPrefSuffix() {
        return PREFS_SUFFIX;
    }

    @Override
    protected HashMap<String, Class> getModelHashMap() {
        return new HashMap<>();
    }

    @Override
    protected String getResTypeName() {
        return RES_NAME;
    }
}
