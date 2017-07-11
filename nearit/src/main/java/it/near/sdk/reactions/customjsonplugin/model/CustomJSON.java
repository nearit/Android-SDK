package it.near.sdk.reactions.customjsonplugin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.recipes.models.ReactionBundle;

/**
 * @author cattaneostefano.
 */
public class CustomJSON extends ReactionBundle implements Parcelable {

    @SerializedName("content")
    public HashMap<String, Object> content;

    public CustomJSON() {
    }

    protected CustomJSON(Parcel in) {
        setId(in.readString());
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
        dest.writeString(getId());
        dest.writeSerializable(content);
    }
}
