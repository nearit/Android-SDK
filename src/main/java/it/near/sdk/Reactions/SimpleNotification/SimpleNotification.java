package it.near.sdk.Reactions.SimpleNotification;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class SimpleNotification extends Resource {

    @SerializeName("text")
    String text;
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}