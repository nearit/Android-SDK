package it.near.sdk.Communication;

import android.content.Context;
import android.os.Looper;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalState;
import it.near.sdk.Utils.ULog;

/**
 * Contains a static method to send trackings
 * @author cattaneostefano
 */
public class NearNetworkUtil {
    public static final String TAG = "NearNetworkUtil";


    /**
     * Send tracking information to the back-end.
     * @param context the app context.
     * @param url the tracking url.
     * @param body the HTTP request body.
     */
    public static void sendTrack(Context context, String url, String body){
        // TODO not tested
        NearAsyncHttpClient httpClient = new NearAsyncHttpClient();
        try {
            httpClient.nearPost(context, url, body, new JsonHttpResponseHandler(){
                @Override
                public void setUsePoolThread(boolean pool) {
                    super.setUsePoolThread(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ULog.d(TAG, "Tracking data sent.");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    ULog.d(TAG, "Tracking data not sent. Error: " + statusCode);
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
/*
        GlobalState.getInstance(context).getRequestQueue().add(
                new CustomJsonRequest(context, Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, "Tracking data sent.");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ULog.d(TAG, "Tracking data not sent. Error: " + error.toString());
                    }
                })
        );
*/
    }

    /**
     * Send tracking information to the back-end. Since every component sends tracking data to different endpoints, the url is a parameter.
     * @param context the application context.
     * @param url the tracking url.
     * @param body the HHTP request body.
     * @param handler the response handler.
     */
    public static void sendTrack (Context context, String url, String body, JsonHttpResponseHandler handler) throws UnsupportedEncodingException, AuthenticationException {
        NearAsyncHttpClient httpClient = new NearAsyncHttpClient();
        httpClient.nearPost(context, url, body, handler);
    }

}
