package it.near.sdk.geopolis.beacons.ranging;

import android.os.Parcelable;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;

/**
 * @author cattaneostefano
 */
public interface ProximityListener {

    void foregroundEvent(Parcelable content, Recipe recipe, TrackingInfo trackingInfo);
}
