package it.near.sdk.recipes;

import android.os.Parcelable;

import it.near.sdk.recipes.models.Recipe;

public interface NearNotifier {
    /**
     * Deliver a reaction in the background. It might come from a cached recipe, or an online evaluated recipe.
     *
     * @param parcelable the generic parcelable reaction object.
     */
    void deliverBackgroundReaction(Parcelable parcelable, String recipeId, String notificationText, String reactionPlugin,String reactionAction);

    /**
     * Deliver a reaction in the background coming from a push.
     *
     * @param parcelable the generic parcelable reaction object.
     * @param push_id    the push message identifier, used for tracking.
     */
    void deliverBackgroundPushReaction(Parcelable parcelable, String push_id, String recipeId, String notificationText, String reactionPlugin,String reactionAction);

    /**
     * Deliver a reaction for a foreground-only recipe e.g. ranging recipe.
     *
     * @param parcelable the generic parcelable reaction object.
     * @param recipe     the recipe object.
     */
    void deliverForegroundReaction(Parcelable parcelable, Recipe recipe);
}
