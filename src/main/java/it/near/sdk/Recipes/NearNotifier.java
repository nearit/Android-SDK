package it.near.sdk.Recipes;

import android.os.Parcelable;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * @author cattaneostefano
 */
public interface NearNotifier {
    public abstract void deliverBackgroundRegionReaction(Parcelable parcelable, Recipe recipe);
    public abstract void deliverBackgroundPushReaction(Parcelable parcelable, Recipe recipe);
}
