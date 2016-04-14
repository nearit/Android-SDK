package it.near.sdk.Reactions.ContentNotification;

import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class ContentNotification extends Resource {
    @SerializeName("text")
    String text;
    @SerializeName("content")
    String content;
    @SerializeName("video_link")
    String video_link;
    @SerializeName("app_id")
    String app_id;
    @SerializeName("owner_id")
    String owner_id;
    @SerializeName("images_ids")
    List<String> images_id;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideo_link() {
        return video_link;
    }

    public void setVideo_link(String video_link) {
        this.video_link = video_link;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public List<String> getImages_id() {
        return images_id;
    }

    public void setImages_id(List<String> images_id) {
        this.images_id = images_id;
    }
}
