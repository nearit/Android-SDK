package it.near.sdk.MorpheusNear.Models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Resource;

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
