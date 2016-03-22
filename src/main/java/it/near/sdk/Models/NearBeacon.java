package it.near.sdk.Models;

import at.rags.morpheus.Annotations.SerializeName;
import at.rags.morpheus.Resource;

/**
 * Created by cattaneostefano on 22/03/16.
 */
public class NearBeacon extends Resource {

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

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public String getProximity_uuid() {
        return proximity_uuid;
    }

    public void setProximity_uuid(String proximity_uuid) {
        this.proximity_uuid = proximity_uuid;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isLike(org.altbeacon.beacon.Beacon beacon) {
        return beacon.getId1().toString().equals(this.getProximity_uuid())
                && beacon.getId2().toInt() == this.getMajor()
                && beacon.getId3().toInt() == this.getMinor();
    }



    public static int distanceToProximity(double distance) {

        if (distance<=0)
            return 3;

        else if (distance<=0.3)
            return 1;

        else if (distance<=3)
            return 2;

        else if (distance<3)
            return 3;

        return 3;
    }
}
