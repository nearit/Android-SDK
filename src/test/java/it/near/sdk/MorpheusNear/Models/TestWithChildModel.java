package it.near.sdk.MorpheusNear.Models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 06/03/2017.
 */

public class TestWithChildModel extends Resource {
    @SerializedName("content")
    String content;
    @Relationship("child")
    TestChildModel child;

    public TestWithChildModel() {
    }

    public String getContent() {
        return content;
    }

    public TestChildModel getChild() {
        return child;
    }
}
