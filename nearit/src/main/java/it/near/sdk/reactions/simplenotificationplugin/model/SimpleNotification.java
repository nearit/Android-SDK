package it.near.sdk.reactions.simplenotificationplugin.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by cattaneostefano on 07/10/2016.
 */

public class SimpleNotification implements Parcelable {

    private String notificationMessage;
    private String notificationTitle;

    public SimpleNotification(String notificationMessage, @Nullable String notificationTitle) {
        this.notificationMessage = notificationMessage;
        this.notificationTitle = notificationTitle;
    }

    protected SimpleNotification(Parcel in) {
        notificationMessage = in.readString();
        notificationTitle = in.readString();
    }

    public static final Creator<SimpleNotification> CREATOR = new Creator<SimpleNotification>() {
        @Override
        public SimpleNotification createFromParcel(Parcel in) {
            return new SimpleNotification(in);
        }

        @Override
        public SimpleNotification[] newArray(int size) {
            return new SimpleNotification[size];
        }
    };

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(notificationMessage);
        dest.writeString(notificationTitle);
    }
}
