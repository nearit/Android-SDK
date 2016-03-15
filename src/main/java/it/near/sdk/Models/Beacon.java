package it.near.sdk.Models;

import at.rags.morpheus.Annotations.SerializeName;
import at.rags.morpheus.Resource;

/**
 * Created by cattaneostefano on 15/03/16.
 */

public class Beacon extends Resource {
    @SerializeName("name")
    String name;
    @SerializeName("major")
    String major;
}


