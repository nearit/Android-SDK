package it.near.sdk.utils.timestamp;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.morpheusnear.Resource;

public class CacheTimestamp extends Resource {

    public CacheTimestamp() {
    }

    @SerializedName("what")
    public String what;

    @SerializedName("time")
    public Number time;
}
