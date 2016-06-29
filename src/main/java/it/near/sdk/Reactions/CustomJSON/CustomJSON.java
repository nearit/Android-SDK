package it.near.sdk.Reactions.CustomJSON;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano.
 */
public class CustomJSON extends Resource implements Parcelable{

    @SerializedName("content")
    HashMap<String, Object> content;

    public CustomJSON() {
    }


    protected CustomJSON(Parcel in) {
        content = (HashMap<String, Object>) in.readSerializable();
    }

    public static final Creator<CustomJSON> CREATOR = new Creator<CustomJSON>() {
        @Override
        public CustomJSON createFromParcel(Parcel in) {
            return new CustomJSON(in);
        }

        @Override
        public CustomJSON[] newArray(int size) {
            return new CustomJSON[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(content);
    }
}
