package it.near.sdk.Beacons.BeaconForest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Representation of a Beacon. In this plugin a beacon will represent a region.
 * Beacons of the Forest Manager are organized in a tree-like structure, therefore a beacon can have children.
 *
 * @author cattaneostefano
 */
public class Beacon extends Resource {

    @SerializedName("uuid")
    String uuid;
    @SerializedName("minor")
    int minor;
    @SerializedName("major")
    int major;
    @SerializedName("name")
    String name;
    @Relationship("children")
    private List<Beacon> children;

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

    public List<Beacon> getChildren() {
        return children;
    }

    public void setChildren(List<Beacon> children) {
        this.children = children;
    }
}
