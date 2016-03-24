package it.near.sdk.Models;

import java.util.List;

import at.rags.morpheus.Annotations.SerializeName;
import at.rags.morpheus.Resource;

/**
 * Created by cattaneostefano on 16/03/16.
 */
public class Content extends Resource{

    @SerializeName("title")
    String title;
    @SerializeName("short_description")
    String shortDescription;
    @SerializeName("long_description")
    String longDescription;
    @SerializeName("trashed")
    Boolean trashed;
    @SerializeName("photo_ids")
    List<String> photoIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Boolean getTrashed() {
        return trashed;
    }

    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }

    public List<String> getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(List<String> photoIds) {
        this.photoIds = photoIds;
    }
}
