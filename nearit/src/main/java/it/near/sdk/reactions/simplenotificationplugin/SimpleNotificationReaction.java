package it.near.sdk.reactions.simplenotificationplugin;

import it.near.sdk.reactions.Reaction;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;

public class SimpleNotificationReaction extends Reaction {
    public static final String PLUGIN_NAME = "simple-notification";
    private static final String SHOW_SIMPLE_NOTIFICATION_ACTION_NAME = "simple_notification";

    public SimpleNotificationReaction(NearNotifier nearNotifier) {
        super(nearNotifier);
    }

    @Override
    public void refreshConfig() {
        // intentionally left empty
    }

    @Override
    public String getReactionPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe, TrackingInfo trackingInfo) {
        switch (reaction_action) {
            case SHOW_SIMPLE_NOTIFICATION_ACTION_NAME:
                if (recipe.isForegroundRecipe()) {
                    nearNotifier.deliverForegroundReaction(SimpleNotification.fromRecipe(recipe), recipe, trackingInfo);
                } else {
                    nearNotifier.deliverBackgroundReaction(SimpleNotification.fromRecipe(recipe), trackingInfo, recipe.getNotificationBody(), getReactionPluginName());
                }
                break;
        }
    }

    @Override
    public void handlePushReaction(Recipe recipe, ReactionBundle reaction_bundle) {
        nearNotifier.deliverBackgroundPushReaction(SimpleNotification.fromRecipe(recipe), recipe.getId(), recipe.getNotificationBody(), getReactionPluginName());
    }

    @Override
    public void handlePushReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleId) {
        nearNotifier.deliverBackgroundPushReaction(SimpleNotification.fromNotificationText(notificationText), recipeId, notificationText, getReactionPluginName());
    }

    @Override
    public boolean handlePushBundledReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleString) {
        nearNotifier.deliverBackgroundPushReaction(SimpleNotification.fromNotificationText(notificationText), recipeId, notificationText, getReactionPluginName());
        return true;
    }
}
