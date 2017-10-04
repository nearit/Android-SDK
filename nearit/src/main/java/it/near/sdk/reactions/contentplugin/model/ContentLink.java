package it.near.sdk.reactions.contentplugin.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ContentLink implements Parcelable {

    public static final String CTA_LABEL_KEY = "label";
    public static final String CTA_URL_KEY = "url";

    public String label;
    public String url;

    public ContentLink(String label, String url) {
        this.label = label;
        this.url = url;
    }

    protected ContentLink(Parcel in) {
        label = in.readString();
        url = in.readString();
    }

    public static final Creator<ContentLink> CREATOR = new Creator<ContentLink>() {
        @Override
        public ContentLink createFromParcel(Parcel in) {
            return new ContentLink(in);
        }

        @Override
        public ContentLink[] newArray(int size) {
            return new ContentLink[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(label);
        parcel.writeString(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentLink that = (ContentLink) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        return url != null ? url.equals(that.url) : that.url == null;

    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
