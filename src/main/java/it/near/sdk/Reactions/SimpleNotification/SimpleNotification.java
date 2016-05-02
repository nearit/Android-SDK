package it.near.sdk.Reactions.SimpleNotification;

import android.os.Parcel;
import android.os.Parcelable;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Reactions.PollNotification.PollNotification;

/**
 * @author cattaneostefano
 */
public class SimpleNotification extends Resource implements Parcelable {

    @SerializeName("text")
    String text;
    @SerializeName("updated_at")
    String updated_at;

    public SimpleNotification() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getText());
        dest.writeString(getId());
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new SimpleNotification(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new SimpleNotification[size];
        }
    };

    public SimpleNotification(Parcel in) {
        setText(in.readString());
        setId(in.readString());
    }
}