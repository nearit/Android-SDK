package it.near.sdk.Geopolis.Ranging;

import android.os.Parcelable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    void enterBeaconRange(Parcelable content, Recipe recipe);

}
