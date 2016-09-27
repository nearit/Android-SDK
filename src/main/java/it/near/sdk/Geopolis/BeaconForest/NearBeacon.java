package it.near.sdk.Geopolis.BeaconForest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Representation of a NearBeacon. In this plugin a beacon will represent a region.
 * Beacons of the Forest Manager are organized in a tree-like structure, therefore a beacon can have children.
 *
 * @author cattaneostefano
 */
public class NearBeacon extends Resource {

    @SerializedName("uuid")
    String uuid;
    @SerializedName("minor")
    int minor;
    @SerializedName("major")
    int major;
    @SerializedName("name")
    String name;
    @Relationship("children")
    private List<NearBeacon> children;

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

    public List<NearBeacon> getChildren() {
        return children;
    }

    public void setChildren(List<NearBeacon> children) {
        this.children = children;
    }
}
