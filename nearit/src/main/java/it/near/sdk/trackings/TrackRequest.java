package it.near.sdk.trackings;

import org.json.JSONException;
import org.json.JSONObject;


public class TrackRequest {

    static final String KEY_URL = "url";
    static final String KEY_BODY = "body";
    final String url;
    final String body;

    public TrackRequest(String url, String body) {
        this.url = url;
        this.body = body;
    }

    public JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_URL, this.url);
            jsonObject.put(KEY_BODY, this.body);
        } catch (JSONException ignored) {
        }
        return jsonObject;
    }

    public static TrackRequest fromJsonObject(JSONObject json) throws JSONException {
        String url = json.getString(KEY_URL);
        String body = json.getString(KEY_BODY);
        return new TrackRequest(url, body);
    }
}
