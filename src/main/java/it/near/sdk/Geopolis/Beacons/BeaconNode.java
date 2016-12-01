package it.near.sdk.Geopolis.Beacons;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.gson.annotations.SerializedName;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.apache.commons.lang3.ObjectUtils;

import java.util.IllegalFormatException;
import java.util.UUID;

import it.near.sdk.Geopolis.Node;

/**
 * Created by cattaneostefano on 21/09/16.
 */

public class BeaconNode extends Node {
    @SerializedName("proximity_uuid")
    String proximityUUID;

    @SerializedName("major")
    Integer major;

    @SerializedName("minor")
    Integer minor;

    public BeaconNode() {
    }

    public String getProximityUUID() {
        return proximityUUID;
    }

    public void setProximityUUID(String proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public static Region toAltRegion(BeaconNode beaconNode) throws NullPointerException {
        if (beaconNode.getIdentifier() == null || beaconNode.getMinor() != null) throw new NullPointerException();
        Region region = new Region(beaconNode.getId(),
                                Identifier.fromUuid(UUID.fromString(beaconNode.getProximityUUID())),
                                beaconNode.getMajor() != null ? Identifier.fromInt(beaconNode.getMajor()) : null,
                                beaconNode.getMinor() != null ? Identifier.fromInt(beaconNode.getMinor()) : null);
        return region;
    }

    public static boolean isBeacon(Node node) {
        return node instanceof BeaconNode &&
                ((BeaconNode) node).getMinor() != null &&
                ((BeaconNode) node).getMajor() != null;
    }
}
