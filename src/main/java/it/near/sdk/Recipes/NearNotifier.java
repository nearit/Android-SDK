package it.near.sdk.Recipes;

import android.os.Parcelable;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * @author cattaneostefano
 */
public interface NearNotifier {
    public abstract void deliverReaction(Parcelable parcelable, Recipe recipe);
}
