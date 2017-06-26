package it.near.sdk.reactions;

import android.content.Context;

import java.util.List;

import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.NearNotifier;
import it.near.sdk.recipes.models.Recipe;

/**
 * Superclass for plugins of type "reaction". Subclass to add the support of new "What" types and handle requests to fire contents,
 * from within the devices and form push notifications.
 *
 * @author cattaneostefano
 */
public abstract class Reaction {
    /**
     * List of supported plugin actions. Still unused.
     */
    protected List<String> supportedActions = null;
    /**
     * App context.
     */
    protected Context mContext;
    /**
     * Notifier of content to the app.
     */
    protected NearNotifier nearNotifier;

    public Reaction(Context mContext, NearNotifier nearNotifier) {
        this.mContext = mContext;
        this.nearNotifier = nearNotifier;
    }

    public List<String> getSupportedActions() {
        if (supportedActions == null) {
            buildActions();
        }
        return supportedActions;
    }

    /**
     * Method called by the recipe manager to trigger a reaction.
     *
     * @param recipe matched recipe
     */
    public void handleReaction(Recipe recipe) {
        if (!getPluginName().equals(recipe.getReaction_plugin_id())) {
            return;
        }
        handleReaction(recipe.getReaction_action().getId(), recipe.getReaction_bundle(), recipe);
    }

    /**
     * Build supported actions
     */
    public abstract void buildActions();

    /**
     * Refresh configuration from the server. Consider caching the results so you can support offline mode.
     */
    public abstract void refreshConfig();

    /**
     * @return the profile name.
     */
    public abstract String getPluginName();

    /**
     * Handle a reaction, including the call to the NearNotifier object.
     *
     * @param reaction_action the reaction anction of the recipe.
     * @param reaction_bundle the reaction bundle of the recipe.
     * @param recipe          the entire recipe object.
     */
    protected abstract void handleReaction(String reaction_action, ReactionBundle reaction_bundle, Recipe recipe);

    /**
     * Handle a reaction from a push notification, including the call to the NearNotifier object. Since this will be called after the insertion
     * of a push based recipe, it's highly unlikely that the recipe information will be cached.
     *
     * @param recipe          the recipe object.
     * @param push_id         the id of the push notification.
     * @param reaction_bundle the reaction bundle.
     */
    public abstract void handlePushReaction(Recipe recipe, String push_id, ReactionBundle reaction_bundle);

    /**
     * Handle a reaction from a push notification. This will fetch the reactionBundle if is not cached.
     *
     * @param recipeId         the recipe object.
     * @param reactionAction   the reaction action.
     */
    public abstract void handlePushReaction(String recipeId, String notificationText, String reactionAction, String reactionBundleId);
}
