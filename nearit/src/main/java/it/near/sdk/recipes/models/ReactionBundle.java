package it.near.sdk.recipes.models;

import android.os.Parcel;
import android.os.Parcelable;

import it.near.sdk.morpheusnear.Resource;

/**
 * @author cattaneostefano.
 */
public abstract class ReactionBundle extends Resource implements Parcelable {
    public String notificationMessage;

    public ReactionBundle() {
    }

    public ReactionBundle(Parcel in) {
        notificationMessage = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(notificationMessage);
    }

    public boolean hasContentToInclude() {
        return false;
    }

}
