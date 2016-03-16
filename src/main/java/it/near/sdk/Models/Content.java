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

}
