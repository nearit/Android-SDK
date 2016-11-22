package it.near.sdk.Reactions.Content;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Recipes.Models.ReactionBundle;

/**
 * @author cattaneostefano
 */
public class Content extends ReactionBundle implements Parcelable {
    @SerializedName("content")
    String content;
    @SerializedName("video_link")
    String video_link;
    @SerializedName("updated_at")
    String updated_at;
    @SerializedName("images_ids")
    List<String> images_id;
    @Relationship("images")
    List<Image> images;

    List<ImageSet> images_links;

    public Content() {
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
        dest.writeString(content);
        dest.writeString(video_link);
        dest.writeString(updated_at);
        dest.writeString(getId());
        dest.writeTypedList(getImages_links());
    }

    // Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

    public Content(Parcel in) {
        setContent(in.readString());
        setVideo_link(in.readString());
        setUpdated_at(in.readString());
        /*List<String> list = null;
        in.readList(list, List.class.getClassLoader());
        setImages_id(list);*/
        setId(in.readString());
        List<ImageSet> list = new ArrayList<ImageSet>();
        in.readTypedList(list, ImageSet.CREATOR);
        setImages_links(list);
    }

    public boolean isSimpleNotification() {
        return content == null &&
                images_links.size() == 0 &&
                video_link == null;
    }
}
