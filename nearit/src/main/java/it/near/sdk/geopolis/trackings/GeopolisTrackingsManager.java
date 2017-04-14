package it.near.sdk.geopolis.trackings;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import it.near.sdk.communication.NearAsyncHttpClient;

import static it.near.sdk.utils.NearUtils.checkNotNull;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class GeopolisTrackingsManager {

    private final NearAsyncHttpClient nearAsyncHttpClient;
    private final SharedPreferences sp;

    public GeopolisTrackingsManager(@NonNull NearAsyncHttpClient nearAsyncHttpClient,
                                    @NonNull SharedPreferences sp) {
        this.nearAsyncHttpClient = checkNotNull(nearAsyncHttpClient);
        this.sp = checkNotNull(sp);
    }
}
