package it.near.sdk.Reactions.ContentNotification;

import java.io.Serializable;

/**
 * Created by cattaneostefano on 31/03/16.
 */
public class ImageSet implements Serializable{
    String fullSize;
    String bigSize;
    String smallSize;

    public ImageSet() {
    }

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
}
