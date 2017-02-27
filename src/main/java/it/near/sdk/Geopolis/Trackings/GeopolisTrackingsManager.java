package it.near.sdk.Geopolis.Trackings;

import android.content.SharedPreferences;

import it.near.sdk.Communication.NearAsyncHttpClient;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class GeopolisTrackingsManager {

    private final NearAsyncHttpClient nearAsyncHttpClient;
    private final SharedPreferences sp;

    public GeopolisTrackingsManager(NearAsyncHttpClient nearAsyncHttpClient, SharedPreferences sp) {
        this.nearAsyncHttpClient = nearAsyncHttpClient;
        this.sp = sp;
    }



}
