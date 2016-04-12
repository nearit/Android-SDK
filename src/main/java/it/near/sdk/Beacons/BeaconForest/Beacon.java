package it.near.sdk.Beacons.BeaconForest;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 12/04/16.
 */
public class Beacon extends Resource {

    @SerializeName("uuid")
    String uuid;
    @SerializeName("minor")
    int minor;
    @SerializeName("major")
    int major;
    @SerializeName("name")
    String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
