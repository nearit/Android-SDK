package it.near.sdk.Models;

import at.rags.morpheus.Annotations.SerializeName;
import at.rags.morpheus.Resource;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class Matching extends Resource {

    @SerializeName("content_id")
    String content_id;
    @SerializeName("app_id")
    String app_id;
    @SerializeName("beacon_id")
    String beacon_id;
    @SerializeName("active")
    Boolean active;

}
