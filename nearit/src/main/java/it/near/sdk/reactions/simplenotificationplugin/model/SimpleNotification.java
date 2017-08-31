package it.near.sdk.reactions.simplenotificationplugin.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;

public class SimpleNotification extends ReactionBundle implements Parcelable {

    public String message;
    private String title;

    public SimpleNotification(String notificationMessage, @Nullable String notificationTitle) {
        this.message = notificationMessage;
        this.title = notificationTitle;
    }

    protected SimpleNotification(Parcel in) {
        super(in);
        message = in.readString();
        title = in.readString();
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
        return message;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.message = notificationMessage;
    }

    @Deprecated
    public String getNotificationTitle() {
        return title;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.title = notificationTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(message);
        dest.writeString(title);
    }

    public static SimpleNotification fromNotificationText(String notificationText) {
        SimpleNotification simpleNotification = new SimpleNotification(notificationText, null);
        simpleNotification.notificationMessage = notificationText;
        return simpleNotification;
    }

    public static SimpleNotification fromRecipe(Recipe recipe) {
        SimpleNotification simpleNotification = new SimpleNotification(recipe.getNotificationBody(), recipe.getNotificationTitle());
        simpleNotification.notificationMessage = recipe.getNotificationBody();
        return simpleNotification;
    }
}
