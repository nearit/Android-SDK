package it.near.sdk.MorpheusNear.Models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 06/03/2017.
 */

public class TestChildModel extends Resource {
    @SerializedName("favourite_child")
    boolean isFavoChild;

    public TestChildModel() {
    }

    public boolean getIsFavoChild() {
        return isFavoChild;
    }
}
