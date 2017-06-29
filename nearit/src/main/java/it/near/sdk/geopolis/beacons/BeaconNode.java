package it.near.sdk.geopolis.beacons;

import com.google.gson.annotations.SerializedName;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.UUID;

import it.near.sdk.geopolis.Node;

public class BeaconNode extends Node {
    @SerializedName("proximity_uuid")
    public String proximityUUID;

    @SerializedName("major")
    public Integer major;

    @SerializedName("minor")
    public Integer minor;

    public BeaconNode() {
    }

    public static Region toAltRegion(BeaconNode beaconNode) throws NullPointerException {
        if (beaconNode.identifier == null || beaconNode.minor != null)
            throw new NullPointerException();
        return new Region(beaconNode.getId(),
                Identifier.fromUuid(UUID.fromString(beaconNode.proximityUUID)),
                beaconNode.major != null ? Identifier.fromInt(beaconNode.major) : null,
                beaconNode.minor != null ? Identifier.fromInt(beaconNode.minor) : null);
    }

    public static boolean isBeacon(Node node) {
        return node instanceof BeaconNode &&
                ((BeaconNode) node).minor != null &&
                ((BeaconNode) node).major != null;
    }
}
