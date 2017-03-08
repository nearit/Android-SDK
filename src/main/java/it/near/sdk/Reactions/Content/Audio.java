package it.near.sdk.Reactions.Content;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 02/03/2017.
 */

public class Audio extends Resource implements Parcelable {
    @SerializedName("audio")
    public HashMap<String, Object> audio;

    public Audio() {
    }

    public HashMap<String, Object> getAudio() {
        return audio;
    }

    public void setAudio(HashMap<String, Object> audio) {
        this.audio = audio;
    }

    public String getUrl() {
        return (String) audio.get("url");
    }

    protected Audio(Parcel in) {
        setId(in.readString());
        audio = (HashMap<String, Object>) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeSerializable(audio);
    }

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

}
