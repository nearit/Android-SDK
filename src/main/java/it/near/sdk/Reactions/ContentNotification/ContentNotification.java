package it.near.sdk.Reactions.ContentNotification;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class ContentNotification extends Resource implements Parcelable {
    @SerializeName("text")
    String text;
    @SerializeName("content")
    String content;
    @SerializeName("video_link")
    String video_link;
    @SerializeName("updated_at")
    String updated_at;
    @SerializeName("images_ids")
    List<String> images_id;
    @Relationship("image")
    List<Image> images;

    List<ImageSet> images_links;

    public ContentNotification() {
    }

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

    public List<String> getImages_id() {
        return images_id;
    }

    public void setImages_id(List<String> images_id) {
        this.images_id = images_id;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public List<ImageSet> getImages_links() {
        return images_links;
    }

    public void setImages_links(List<ImageSet> images_links) {
        this.images_links = images_links;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getText());
        dest.writeString(getContent());
        dest.writeString(getVideo_link());
        dest.writeString(getUpdated_at());
        dest.writeList(getImages_id());
        dest.writeString(getId());
    }

    // Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ContentNotification createFromParcel(Parcel in) {
            return new ContentNotification(in);
        }

        public ContentNotification[] newArray(int size) {
            return new ContentNotification[size];
        }
    };

    public ContentNotification(Parcel in) {
        setText(in.readString());
        setContent(in.readString());
        setVideo_link(in.readString());
        setUpdated_at(in.readString());
        List<String> list = null;
        in.readList(list, List.class.getClassLoader());
        setImages_id(list);
        setId(in.readString());
    }
}
