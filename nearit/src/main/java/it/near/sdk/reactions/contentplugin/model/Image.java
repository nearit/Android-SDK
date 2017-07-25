package it.near.sdk.reactions.contentplugin.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import it.near.sdk.morpheusnear.Resource;

/**
 * @author cattaneostefano
 */
public class Image extends Resource {
    @SerializedName("image")
    public HashMap<String, Object> imageMap;

    public Image() {
    }

    public ImageSet toImageSet() throws MissingImageException {
        if (imageMap == null) throw new MissingImageException();
        ImageSet imageSet = new ImageSet();
        if (!imageMap.containsKey("url")) throw new MissingImageException();
        imageSet.setFullSize((String) imageMap.get("url"));
        if (imageMap.containsKey("square_300") && ((Map<String, Object>) imageMap.get("square_300")).containsKey("url")) {
            imageSet.setSmallSize(((Map<String, Object>) imageMap.get("square_300")).get("url").toString());
        }
        return imageSet;
    }

    public class MissingImageException extends Exception {
        public MissingImageException() {
            super("missing data");
        }
    }

}
