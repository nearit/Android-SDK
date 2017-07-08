package it.near.sdk.reactions.contentplugin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.recipes.models.ReactionBundle;

public class Content extends ReactionBundle implements Parcelable {
    @SerializedName("content")
    public String contentString;
    @SerializedName("video_link")
    public String video_link;
    @SerializedName("updated_at")
    public String updated_at;
    @Relationship("images")
    public List<Image> images;
    @Relationship("audio")
    public Audio audio;
    @Relationship("upload")
    public Upload upload;

    private List<ImageSet> images_links;

    public Content() {
    }

    public List<ImageSet> getImages_links() {
        return images_links;
    }

    public void setImages_links(List<ImageSet> images_links) {
        this.images_links = images_links;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contentString);
        dest.writeString(video_link);
        dest.writeString(updated_at);
        dest.writeString(getId());
        dest.writeTypedList(getImages_links());
        dest.writeParcelable(audio, flags);
        dest.writeParcelable(upload, flags);
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
        contentString = in.readString();
        video_link = in.readString();
        updated_at = in.readString();
        setId(in.readString());
        List<ImageSet> list = new ArrayList<ImageSet>();
        in.readTypedList(list, ImageSet.CREATOR);
        setImages_links(list);
        audio = in.readParcelable(Audio.class.getClassLoader());
        upload = in.readParcelable(Upload.class.getClassLoader());
    }

    public boolean hasContentToInclude() {
        return audio != null ||
                (images != null && images.size() > 0) ||
                upload != null;
    }
}
