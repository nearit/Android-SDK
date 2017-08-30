package it.near.sdk.recipes.models;

import android.os.Parcel;
import android.os.Parcelable;

import it.near.sdk.morpheusnear.Resource;

/**
 * @author cattaneostefano.
 */
public class ReactionBundle extends Resource implements Parcelable {
    public String notificationMessage;

    public ReactionBundle() {
    }

    public ReactionBundle(Parcel in) {
        notificationMessage = in.readString();
    }

    public static final Creator<ReactionBundle> CREATOR = new Creator<ReactionBundle>() {
        @Override
        public ReactionBundle createFromParcel(Parcel in) {
            return new ReactionBundle(in);
        }

        @Override
        public ReactionBundle[] newArray(int size) {
            return new ReactionBundle[size];
        }
    };

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(notificationMessage);
    }

    public boolean hasContentToInclude() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
