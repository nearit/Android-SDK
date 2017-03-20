package it.near.sdk.morpheusnear.models;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.morpheusnear.Resource;

/**
 * Created by cattaneostefano on 06/03/2017.
 */

public class TestChildModel extends Resource {
    @SerializedName("favourite_child")
    public boolean isFavoChild;

    public TestChildModel() {
    }

    public boolean getIsFavoChild() {
        return isFavoChild;
    }
}
