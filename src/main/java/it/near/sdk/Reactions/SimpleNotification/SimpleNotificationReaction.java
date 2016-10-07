package it.near.sdk.Reactions.SimpleNotification;

import android.content.Context;

import java.util.ArrayList;

import it.near.sdk.Reactions.Reaction;
import it.near.sdk.Recipes.Models.ReactionBundle;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Recipes.NearNotifier;

/**
 * Created by cattaneostefano on 07/10/2016.
 */

public class SimpleNotificationReaction extends Reaction {
    private static final String SHOW_SIMPLE_NOTIFICATION_ACTION_NAME = "simple_notification";
    public static final String PLUGIN_NAME = "simple-notification";

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
        switch (reaction_action){
            case SHOW_SIMPLE_NOTIFICATION_ACTION_NAME:
                showContent(recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(Recipe recipe, String push_id, ReactionBundle reaction_bundle) {
        SimpleNotification simpleNotification = new SimpleNotification(recipe.getNotificationBody(), recipe.getNotificationTitle());
        nearNotifier.deliverBackgroundPushReaction(simpleNotification, recipe, push_id);
    }

    private void showContent(Recipe recipe) {
        SimpleNotification simpleNotification = new SimpleNotification(recipe.getNotificationBody(), recipe.getNotificationTitle());
        nearNotifier.deliverBackgroundReaction(simpleNotification, recipe);
    }



    @Override
    public void handleEvaluatedReaction(Recipe recipe, String bundle_id) {
        // This is not supposed to be called, a simple notification, right now is not going to be attached to a recipe needing evaluation
        showContent(recipe);
    }
}
