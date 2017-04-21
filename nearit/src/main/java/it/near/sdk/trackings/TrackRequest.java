package it.near.sdk.trackings;

public class TrackRequest {

    final String url;
    final String body;

    public TrackRequest(String url, String body) {
        this.url = url;
        this.body = body;
    }
}
