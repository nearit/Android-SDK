package it.near.sdk.reactions;

import android.os.Parcelable;

/**
 * Created by cattaneostefano on 28/11/2016.
 */

public interface ContentFetchListener {
    void onContentFetched(Parcelable content, boolean cached);
    void onContentFetchError(String error);
}
