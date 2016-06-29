package it.near.sdk.Reactions.Coupon;


import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano.
 */
public class Coupon extends Resource {
    @SerializedName("name")
    String name;
    @SerializedName("description")
    String description;
    @SerializedName("value")
    String value;
    @SerializedName("expires_at")
    String expires_at;
    @SerializedName("icon_id")
    String icon_id;

    public Coupon() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public String getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(String icon_id) {
        this.icon_id = icon_id;
    }
}
