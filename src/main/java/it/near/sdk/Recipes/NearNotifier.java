package it.near.sdk.Recipes;

import android.os.Parcelable;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * @author cattaneostefano
 */
public interface NearNotifier {
    /**
     * Deliver a reaction in the background. It might come from a cached recipe, or an online evaluated recipe.
     * @param parcelable the generic parcelable reaction object.
     * @param recipe the recipe object.
     */
    void deliverBackgroundReaction(Parcelable parcelable, Recipe recipe);

    /**
     * Deliver a reaction in the background coming from a push.
     * @param parcelable the generic parcelable reaction object.
     * @param recipe the recipe object.
     * @param push_id the push message identifier, used for tracking.
     */
    void deliverBackgroundPushReaction(Parcelable parcelable, Recipe recipe, String push_id);

    /**
     * Deliver a reaction for a foreground-only recipe e.g. ranging recipe.
     * @param parcelable the generic parcelable reaction object.
     * @param recipe the recipe object.
     */
    void deliverForegroundReaction(Parcelable parcelable, Recipe recipe);
}
