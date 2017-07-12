package it.near.sdk.recipes.models;

import android.os.Parcelable;

import it.near.sdk.morpheusnear.Resource;

/**
 * @author cattaneostefano.
 */
public abstract class ReactionBundle extends Resource implements Parcelable {
    public ReactionBundle() {
    }

    public boolean hasContentToInclude() {
        return false;
    }

}
