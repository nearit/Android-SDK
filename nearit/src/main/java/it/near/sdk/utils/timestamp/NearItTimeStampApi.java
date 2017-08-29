package it.near.sdk.utils.timestamp;

import android.net.Uri;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.utils.NearJsonAPIUtils;

public class NearItTimeStampApi {

    private static final String TIMESTAMP_PATH = "timestamps";

    private final NearAsyncHttpClient httpClient;
    private final Morpheus morpheus;
    private final GlobalConfig globalConfig;

    public NearItTimeStampApi(NearAsyncHttpClient httpClient, Morpheus morpheus, GlobalConfig globalConfig) {
        this.httpClient = httpClient;
        this.morpheus = morpheus;
        this.globalConfig = globalConfig;
    }

    void fetchTimeStamps(final TimeStampListener listener) {
        if (globalConfig.getAppId() == null) {
            listener.onError("No app id");
            return;
        }

        Uri url = Uri.parse(TIMESTAMP_PATH)
                .buildUpon()
                .appendPath(globalConfig.getAppId())
                .build();

        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    List<CacheTimestamp> timestamps = NearJsonAPIUtils.parseList(morpheus, response, CacheTimestamp.class);
                    listener.onSuccess(timestamps);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    listener.onError("Server error");
                }
            });
        } catch (AuthenticationException e) {
            listener.onError("Auth fails");
        }
    }

    public interface TimeStampListener {
        void onSuccess(List<CacheTimestamp> timestamps);
        void onError(String message);
    }

    public static Morpheus buildMorpheus() {
        Morpheus morpheus = new Morpheus();
        morpheus.getFactory().getDeserializer().registerResourceClass("timestamps", CacheTimestamp.class);
        return morpheus;
    }
}
