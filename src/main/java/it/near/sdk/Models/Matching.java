package it.near.sdk.Models;

import at.rags.morpheus.Annotations.SerializeName;
import at.rags.morpheus.Resource;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class Matching extends Resource {

    @SerializeName("content_id")
    String content_id;
    @SerializeName("app_id")
    String app_id;
    @SerializeName("beacon_id")
    String beacon_id;
    @SerializeName("active")
    Boolean active;

    public String getContent_id() {
        return content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getBeacon_id() {
        return beacon_id;
    }

    public void setBeacon_id(String beacon_id) {
        this.beacon_id = beacon_id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
