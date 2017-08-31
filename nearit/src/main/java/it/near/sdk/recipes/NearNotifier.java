package it.near.sdk.recipes;

import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;

public interface NearNotifier {
    /**
     * Deliver a reaction in the background. It might come from a cached recipe, or an online evaluated recipe.
     *
     * @param parcelable the generic parcelable reaction object.
     */
    void deliverBackgroundReaction(ReactionBundle parcelable, TrackingInfo trackingInfo);

    /**
     * Deliver a reaction in the background coming from a push.
     *
     * @param parcelable the generic parcelable reaction object.
     */
    void deliverBackgroundPushReaction(ReactionBundle parcelable, TrackingInfo trackingInfo);

    /**
     * Deliver a reaction for a foreground-only recipe e.g. ranging recipe.
     *
     * @param parcelable the generic parcelable reaction object.
     * @param recipe     the recipe object.
     */
    void deliverForegroundReaction(ReactionBundle parcelable, Recipe recipe, TrackingInfo trackingInfo);
}
