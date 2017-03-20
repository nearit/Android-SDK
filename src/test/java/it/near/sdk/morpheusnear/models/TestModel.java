package it.near.sdk.morpheusnear.models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.morpheusnear.Resource;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class TestModel extends Resource {
    @SerializedName("content")
    public String content;

    public TestModel() {
    }

    public String getContent() {
        return content;
    }
}
