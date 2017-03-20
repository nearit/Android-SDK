package it.near.sdk.Reactions.CustomJSON;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Recipes.Models.ReactionBundle;

/**
 * @author cattaneostefano.
 */
public class CustomJSON extends ReactionBundle implements Parcelable{

    @SerializedName("content")
    public HashMap<String, Object> content;

    public CustomJSON() {
    }

    public HashMap<String, Object> getContent() {
        return content;
    }

    public void setContent(HashMap<String, Object> content) {
        this.content = content;
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
