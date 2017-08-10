package it.near.sdk.recipes;

import android.os.Parcelable;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;

public interface NearNotifier {
    /**
     * Deliver a reaction in the background. It might come from a cached recipe, or an online evaluated recipe.
     *
     * @param parcelable the generic parcelable reaction object.
     */
    void deliverBackgroundReaction(Parcelable parcelable, TrackingInfo trackingInfo, String notificationText, String reactionPlugin);

    /**
     * Deliver a reaction in the background coming from a push.
     *
     * @param parcelable the generic parcelable reaction object.
     */
    void deliverBackgroundPushReaction(Parcelable parcelable, String recipeId, String notificationText, String reactionPlugin);

    /**
     * Deliver a reaction for a foreground-only recipe e.g. ranging recipe.
     *
     * @param parcelable the generic parcelable reaction object.
     * @param recipe     the recipe object.
     */
    void deliverForegroundReaction(Parcelable parcelable, Recipe recipe, TrackingInfo trackingInfo);
}
