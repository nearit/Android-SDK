package it.near.sdk.reactions.content;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.recipes.models.ReactionBundle;

/**
 * @author cattaneostefano
 */
public class Content extends ReactionBundle implements Parcelable {
    @SerializedName("content")
    public String content;
    @SerializedName("video_link")
    public String video_link;
    @SerializedName("updated_at")
    public String updated_at;
    @SerializedName("images_ids")
    public List<String> images_id;
    @Relationship("images")
    public List<Image> images;
    @Relationship("audio")
    public Audio audio;
    @Relationship("upload")
    public Upload upload;

    private List<ImageSet> images_links;

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

    public Audio getAudio() {
        return audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
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
        dest.writeParcelable(getAudio(), flags);
        dest.writeParcelable(getUpload(), flags);
    }

    // Creator
    public static final Parcelable.Creator<Content> CREATOR = new Parcelable.Creator<Content>() {
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
        setId(in.readString());
        List<ImageSet> list = new ArrayList<ImageSet>();
        in.readTypedList(list, ImageSet.CREATOR);
        setImages_links(list);
        setAudio((Audio) in.readParcelable(Audio.class.getClassLoader()));
        setUpload((Upload) in.readParcelable(Upload.class.getClassLoader()));
    }

    public boolean hasContentToInclude() {
        return audio != null ||
                (images != null && images.size() > 0) ||
                upload != null;
    }
}
