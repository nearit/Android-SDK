package it.near.sdk.reactions.simplenotification;

import android.content.Context;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

import it.near.sdk.reactions.ContentFetchListener;
import it.near.sdk.reactions.CoreReaction;
import it.near.sdk.reactions.Reaction;
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
                showContent(null, recipe);
                break;
        }
    }

    @Override
    public void handlePushReaction(Recipe recipe, String push_id, ReactionBundle reaction_bundle) {
        nearNotifier.deliverBackgroundPushReaction(contentFromRecipe(recipe), recipe, push_id);
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
        HashMap<String, Class> map = new HashMap<>();
        return map;
    }

    @Override
    protected String getResTypeName() {
        return "contents";
    }
}
