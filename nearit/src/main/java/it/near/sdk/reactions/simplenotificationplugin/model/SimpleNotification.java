package it.near.sdk.reactions.simplenotificationplugin.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;

public class SimpleNotification extends ReactionBundle implements Parcelable {

    public String notificationMessage;
    private String notificationTitle;

    public SimpleNotification(String notificationMessage, @Nullable String notificationTitle) {
        this.notificationMessage = notificationMessage;
        this.notificationTitle = notificationTitle;
    }

    protected SimpleNotification(Parcel in) {
        super(in);
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

    @Deprecated
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
        super.writeToParcel(dest, flags);
        dest.writeString(notificationMessage);
        dest.writeString(notificationTitle);
    }

    public static SimpleNotification fromNotificationText(String notificationText) {
        return new SimpleNotification(notificationText, null);
    }

    public static SimpleNotification fromRecipe(Recipe recipe) {
        return new SimpleNotification(recipe.getNotificationBody(), recipe.getNotificationTitle());
    }
}
