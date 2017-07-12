package it.near.sdk.geopolis;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.morpheusnear.Resource;

/**
 * Created by cattaneostefano on 21/09/16.
 */

public class Node extends Resource {
    @SerializedName("identifier")
    public String identifier;

    @SerializedName("tags")
    public List<String> tags;

    @Relationship("parent")
    public Node parent;

    @Relationship("children")
    public List<Node> children;

    public Node() {
    }
}
