package it.near.sdk.communication;

import android.content.Context;


import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.logging.NearLog;

/**
 * Contains a static method to send trackings
 *
 * @author cattaneostefano
 */
public class NearNetworkUtil {
    public static final String TAG = "NearNetworkUtil";
    public static final String INCLUDE_PARAMETER = "include";

    /**
     * Send tracking information to the back-end.
     *
     * @param context the app context.
     * @param url     the tracking url.
     * @param body    the HTTP request body.
     */
    public static void sendTrack(Context context, String url, String body) {
        NearAsyncHttpClient httpClient = new NearAsyncHttpClient(context);
        try {
            httpClient.post(context, url, body, new NearJsonHttpResponseHandler() {
                @Override
                public void setUsePoolThread(boolean pool) {
                    super.setUsePoolThread(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "Tracking data sent.");
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Tracking data not sent. Error: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            NearLog.d(TAG, "Data error");
        }
    }

    /**
     * Send tracking information to the back-end. Since every component sends tracking data to different endpoints, the url is a parameter.
     *
     * @param context the application context.
     * @param url     the tracking url.
     * @param body    the HHTP request body.
     * @param handler the response handler.
     */
    public static void sendTrack(Context context, String url, String body, NearJsonHttpResponseHandler handler) throws UnsupportedEncodingException, AuthenticationException {
        NearAsyncHttpClient httpClient = new NearAsyncHttpClient(context);
        httpClient.post(context, url, body, handler);
    }

}
