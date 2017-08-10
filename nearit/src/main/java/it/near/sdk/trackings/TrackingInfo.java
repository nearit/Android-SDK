package it.near.sdk.trackings;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class TrackingInfo implements Parcelable {
    public String recipeId;
    public HashMap<String, String> metadata;

    public TrackingInfo() {
    }

    public TrackingInfo(Parcel in) {
        recipeId = in.readString();
        metadata = (HashMap<String, String>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(recipeId);
        dest.writeSerializable(metadata);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackingInfo> CREATOR = new Creator<TrackingInfo>() {
        @Override
        public TrackingInfo createFromParcel(Parcel in) {
            return new TrackingInfo(in);
        }

        @Override
        public TrackingInfo[] newArray(int size) {
            return new TrackingInfo[size];
        }
    };
}
