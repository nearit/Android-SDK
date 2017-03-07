package it.near.sdk.Reactions.Content;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 02/03/2017.
 */

public class Upload extends Resource implements Parcelable {
    @SerializedName("upload")
    HashMap<String, Object> upload;

    public Upload() {
    }

    public HashMap<String, Object> getUpload() {
        return upload;
    }

    public void setUpload(HashMap<String, Object> upload) {
        this.upload = upload;
    }

    public String getUrl() {
        return (String) upload.get("url");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeSerializable(upload);
    }

    protected Upload(Parcel in) {
        setId(in.readString());
        upload = (HashMap<String, Object>) in.readSerializable();
    }

    public static final Creator<Upload> CREATOR = new Creator<Upload>() {
        @Override
        public Upload createFromParcel(Parcel in) {
            return new Upload(in);
        }

        @Override
        public Upload[] newArray(int size) {
            return new Upload[size];
        }
    };

}
