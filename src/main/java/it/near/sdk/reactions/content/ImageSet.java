package it.near.sdk.Reactions.Content;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author cattaneostefano
 */
public class ImageSet implements Parcelable{
    String fullSize;
    String smallSize;

    public ImageSet() {
    }

    protected ImageSet(Parcel in) {
        fullSize = in.readString();
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
        dest.writeString(smallSize);
    }

    // generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageSet imageSet = (ImageSet) o;

        if (fullSize != null ? !fullSize.equals(imageSet.fullSize) : imageSet.fullSize != null)
            return false;
        return smallSize != null ? smallSize.equals(imageSet.smallSize) : imageSet.smallSize == null;

    }

    @Override
    public int hashCode() {
        int result = fullSize != null ? fullSize.hashCode() : 0;
        result = 31 * result + (smallSize != null ? smallSize.hashCode() : 0);
        return result;
    }
}
