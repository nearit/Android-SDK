package it.near.sdk.Reactions.ContentNotification;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 18/04/16.
 */
public class Image extends Resource {
    @SerializeName("image")
    HashMap<String, Object> image;

    public HashMap<String, Object> getImage() {
        return image;
    }

    public void setImage(HashMap<String, Object> image) {
        this.image = image;
    }
}
