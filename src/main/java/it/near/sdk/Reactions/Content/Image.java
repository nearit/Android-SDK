package it.near.sdk.Reactions.Content;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano
 */
public class Image extends Resource {
    @SerializedName("image")
    HashMap<String, Object> image;

    public Image() {
    }

    public HashMap<String, Object> getImage() {
        return image;
    }

    public void setImage(HashMap<String, Object> image) {
        this.image = image;
    }

}
