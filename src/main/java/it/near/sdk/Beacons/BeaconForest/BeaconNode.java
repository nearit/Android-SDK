package it.near.sdk.Beacons.BeaconForest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cattaneostefano on 21/09/16.
 */

public class BeaconNode extends Node {
    @SerializedName("proximity_uuid")
    private String proximityUUID;

    @SerializedName("major")
    private int major;

    @SerializedName("minor")
    private int minor;
}
