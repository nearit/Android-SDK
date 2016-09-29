package it.near.sdk.Geopolis.Beacons;

import com.google.gson.annotations.SerializedName;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.UUID;

import it.near.sdk.Geopolis.Node;

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

    public String getProximityUUID() {
        return proximityUUID;
    }

    public void setProximityUUID(String proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public static Region toAltRegion(BeaconNode beaconNode) throws NullPointerException{
        Region region = new Region(beaconNode.getIdentifier(),
                                Identifier.fromUuid(UUID.fromString(beaconNode.getProximityUUID())),
                                beaconNode.getMajor() != 0 ? Identifier.fromInt(beaconNode.getMajor()) : null,
                                beaconNode.getMinor() != 0 ? Identifier.fromInt(beaconNode.getMinor()) : null);
        return region;
    }

}
