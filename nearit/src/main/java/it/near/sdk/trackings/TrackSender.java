package it.near.sdk.trackings;

import cz.msebera.android.httpclient.client.HttpClient;

public class TrackSender {

    private final HttpClient httpClient;

    public TrackSender(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void sendTrack(TrackRequest request, RequestListener listener) {
        // TODO implementation
        listener.onSuccess();
    }

    public interface RequestListener {
        void onSuccess();

        void onFailure(int statusCode);
    }
}
