package it.near.sdk.Geopolis.BeaconForest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cattaneostefano on 21/09/16.
 */

public class GeoFenceNode extends Node {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("radius")
    private int radius;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
