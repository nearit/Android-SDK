package it.near.sdk.morpheusnear.models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.morpheusnear.Resource;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class TestModel extends Resource {
    @SerializedName("content")
    public String content;
    @SerializedName("double_value")
    public Double double_value;
    @SerializedName("int_value")
    public Integer int_value;

    public TestModel() {
    }

    public String getContent() {
        return content;
    }

    public Double getDouble_value() {
        return double_value;
    }

    public Integer getInt_value() {
        return int_value;
    }
}
