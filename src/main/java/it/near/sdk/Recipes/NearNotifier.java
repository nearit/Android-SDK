package it.near.sdk.Recipes;

import android.os.Parcelable;

/**
 * Created by cattaneostefano on 24/03/16.
 */
public interface NearNotifier {
    public abstract void deliverReaction(Parcelable parcelable, Recipe recipe);
}
