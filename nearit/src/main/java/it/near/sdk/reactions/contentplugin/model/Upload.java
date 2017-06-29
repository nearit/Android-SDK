package it.near.sdk.reactions.contentplugin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.morpheusnear.Resource;

/**
 * Created by cattaneostefano on 02/03/2017.
 */

public class Upload extends Resource implements Parcelable {
    @SerializedName("upload")
    public HashMap<String, Object> uploadMap;

    public Upload() {
    }

    public String getUrl() {
        return (String) uploadMap.get("url");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeSerializable(uploadMap);
    }

    protected Upload(Parcel in) {
        setId(in.readString());
        uploadMap = (HashMap<String, Object>) in.readSerializable();
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
