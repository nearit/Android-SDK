package it.near.sdk.Geopolis.Beacons.Ranging;

import android.os.Parcelable;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    void foregroundEvent(Parcelable content, Recipe recipe);

}
