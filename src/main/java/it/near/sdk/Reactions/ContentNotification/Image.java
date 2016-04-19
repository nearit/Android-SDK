package it.near.sdk.Reactions.ContentNotification;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 18/04/16.
 */
public class Image extends Resource {
    @SerializeName("image")
    ArrayMap<String, Object> image;

    public Image() {
    }

    public ArrayMap<String, Object> getImage() {
        return image;
    }

    public void setImage(ArrayMap<String, Object> image) {
        this.image = image;
    }

}
