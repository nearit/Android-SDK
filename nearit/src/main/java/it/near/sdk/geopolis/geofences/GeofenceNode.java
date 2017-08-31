package it.near.sdk.geopolis.geofences;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.Geofence;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.geopolis.Node;

/**
 * Created by cattaneostefano on 21/09/16.
 */

public class GeofenceNode extends Node implements Parcelable {
    private static final int LOITERING_DELAY = 30000;
    @SerializedName("latitude")
    public Number latitude;

    @SerializedName("longitude")
    public Number longitude;

    @SerializedName("radius")
    public Number radius;

    public GeofenceNode() {
        super();
    }


    public Geofence toGeofence() {
        return new Geofence.Builder()
                .setRequestId(getId())
                .setCircularRegion(latitude.doubleValue(), longitude.doubleValue(), radius.intValue())
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
        dest.writeDouble(latitude.doubleValue());
        dest.writeDouble(longitude.doubleValue());
        dest.writeDouble(radius.doubleValue());
        dest.writeString(getId());
    }

    protected GeofenceNode(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        radius = in.readDouble();
        setId(in.readString());
    }

    public static final Creator<GeofenceNode> CREATOR = new Creator<GeofenceNode>() {
        @Override
        public GeofenceNode createFromParcel(Parcel in) {
            return new GeofenceNode(in);
        }

        @Override
        public GeofenceNode[] newArray(int size) {
            return new GeofenceNode[size];
        }
    };

    public static List<Geofence> toGeofences(List<GeofenceNode> geofenceNodes) {
        List<Geofence> geofences = new ArrayList<>();
        for (GeofenceNode geofenceNode : geofenceNodes) {
            geofences.add(geofenceNode.toGeofence());
        }
        return geofences;
    }

    /**
     * Exclusion strategy for gson serializing, since there are circular references in the structure
     *
     * @return
     */
    public static ExclusionStrategy getExclusionStrategy() {
        return new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getName().equals("parent") || f.getName().equals("children");
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
    }
}
