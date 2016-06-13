package it.near.sdk.Reactions.Content;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author cattaneostefano
 */
public class ImageSet implements Parcelable{
    String fullSize;
    String bigSize;
    String smallSize;

    public ImageSet() {
    }

    protected ImageSet(Parcel in) {
        fullSize = in.readString();
        bigSize = in.readString();
        smallSize = in.readString();
    }

    public static final Creator<ImageSet> CREATOR = new Creator<ImageSet>() {
        @Override
        public ImageSet createFromParcel(Parcel in) {
            return new ImageSet(in);
        }

        @Override
        public ImageSet[] newArray(int size) {
            return new ImageSet[size];
        }
    };

    public String getFullSize() {
        return fullSize;
    }

    public void setFullSize(String fullSize) {
        this.fullSize = fullSize;
    }

    public String getBigSize() {
        return bigSize;
    }

    public void setBigSize(String bigSize) {
        this.bigSize = bigSize;
    }

    public String getSmallSize() {
        return smallSize;
    }

    public void setSmallSize(String smallSize) {
        this.smallSize = smallSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullSize);
        dest.writeString(bigSize);
        dest.writeString(smallSize);
    }
}
