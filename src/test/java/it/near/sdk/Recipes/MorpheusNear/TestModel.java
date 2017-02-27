package it.near.sdk.Recipes.MorpheusNear;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class TestModel extends Resource {
    @SerializedName("content")
    String content;

    public TestModel() {
    }

    public String getContent() {
        return content;
    }
}
