package it.near.sdk.MorpheusNear.Models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

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

    public String getContent() {
        return content;
    }

    public TestChildModel getChild() {
        return child;
    }
}
