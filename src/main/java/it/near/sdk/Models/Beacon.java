package it.near.sdk.Models;

import at.rags.morpheus.Annotations.SerializeName;
import at.rags.morpheus.Resource;

/**
 * Created by cattaneostefano on 15/03/16.
 */

public class Beacon extends Resource {

    @SerializeName("name")
    String name;
    @SerializeName("major")
    Integer major;
    @SerializeName("proximity_uuid")
    String proximity_uuid;
    @SerializeName("range")
    Integer range;
    @SerializeName("minor")
    Integer minor;
    @SerializeName("color")
    String color;

    public String getName() {
        return name;
    }

    public Integer getMajor() {
        return major;
    }

    public String getProximity_uuid() {
        return proximity_uuid;
    }

    public Integer getRange() {
        return range;
    }

    public Integer getMinor() {
        return minor;
    }

    public String getColor() {
        return color;
    }

    public boolean isLike(org.altbeacon.beacon.Beacon beacon) {
        return beacon.getId1().toString().equals(this.getProximity_uuid())
                && beacon.getId2().toInt() == this.getMajor()
                && beacon.getId3().toInt() == this.getMinor();
    }
}


