package it.near.sdk.morpheusnear.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

public class TestWithChildrenModel extends Resource {

    @SerializedName("content")
    public String content;
    @Relationship("children")
    public List<TestChildModel> children;

    public TestWithChildrenModel() {
    }

    public String getContent() {
        return content;
    }

    public List<TestChildModel> getChildren() {
        return children;
    }
}
