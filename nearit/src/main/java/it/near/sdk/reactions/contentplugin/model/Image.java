package it.near.sdk.reactions.contentplugin.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;

import it.near.sdk.morpheusnear.Resource;

/**
 * @author cattaneostefano
 */
public class Image extends Resource {
    @SerializedName("image")
    public HashMap<String, Object> image;

    public Image() {
    }

    public HashMap<String, Object> getImage() {
        return image;
    }

    public void setImage(HashMap<String, Object> image) {
        this.image = image;
    }

    public ImageSet toImageSet() {
        ImageSet imageSet = new ImageSet();
        imageSet.setFullSize((String) image.get("url"));
        imageSet.setSmallSize(((LinkedTreeMap<String, Object>) image.get("square_300")).get("url").toString());
        return imageSet;
    }

}
