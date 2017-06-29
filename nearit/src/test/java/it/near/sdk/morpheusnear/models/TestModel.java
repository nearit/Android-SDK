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
    public Number double_value;
    @SerializedName("int_value")
    public Number int_value;

    public TestModel() {
    }

}
