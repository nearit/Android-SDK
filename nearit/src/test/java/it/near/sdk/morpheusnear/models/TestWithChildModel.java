package it.near.sdk.morpheusnear.models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.morpheusnear.Resource;

/**
 * Created by cattaneostefano on 06/03/2017.
 */

public class TestWithChildModel extends Resource {
    @SerializedName("content")
    public String content;
    @Relationship("child")
    public TestChildModel child;

    public TestWithChildModel() {
    }
}
