package it.near.sdk.Realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import it.near.sdk.Models.NearBeacon;

/**
 * Created by alessandrocolleoni on 23/03/16.
 *
 * Create a BeaconRealm object, used to persist on Realm (device persistence)
 *
 */
public class BeaconRealm extends RealmObject {

    @PrimaryKey
    String id;

    String name;

    Integer major;

    String proximity_uuid;

    Integer range;

    Integer minor;

    String color;

    public BeaconRealm() {}

    /**
     * Initialize a BeaconRealm with data's from a beacon model
     * @param beacon
     */
    public BeaconRealm(NearBeacon beacon) {

        id = beacon.getId();
        name = beacon.getName();
        major = beacon.getMajor();
        proximity_uuid = beacon.getProximity_uuid();
        range = beacon.getRange();
        minor = beacon.getMinor();
        color = beacon.getColor();

    }

    /**
     * Convert the BeaconRealm to a Beacon model
     * @return converted beacon
     */
    public NearBeacon convertToModel() {

        NearBeacon beacon = new NearBeacon();

        beacon.setId(id);
        beacon.setName(name);
        beacon.setMajor(major);
        beacon.setProximity_uuid(proximity_uuid);
        beacon.setRange(range);
        beacon.setMinor(minor);
        beacon.setColor(color);

        return beacon;

    }

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
}
