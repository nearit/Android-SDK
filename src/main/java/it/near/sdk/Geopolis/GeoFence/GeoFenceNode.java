package it.near.sdk.Geopolis.GeoFence;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.Geofence;
import com.google.gson.annotations.SerializedName;

import it.near.sdk.Geopolis.Node;

/**
 * Created by cattaneostefano on 21/09/16.
 */

public class GeoFenceNode extends Node implements Parcelable{
    private static final int LOITERING_DELAY = 30000;
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

    public Geofence toGeofence(){
        return new Geofence.Builder()
                .setRequestId(getId())
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setLoiteringDelay(LOITERING_DELAY)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(radius);
    }

    protected GeoFenceNode(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        radius = in.readInt();
    }

    public static final Creator<GeoFenceNode> CREATOR = new Creator<GeoFenceNode>() {
        @Override
        public GeoFenceNode createFromParcel(Parcel in) {
            return new GeoFenceNode(in);
        }

        @Override
        public GeoFenceNode[] newArray(int size) {
            return new GeoFenceNode[size];
        }
    };

}
