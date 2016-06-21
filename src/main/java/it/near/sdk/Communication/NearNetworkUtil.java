package it.near.sdk.Communication;

import android.content.Context;

import org.json.JSONObject;

import it.near.sdk.GlobalState;
import it.near.sdk.Utils.ULog;

/**
 * Contains a static method to send trackings
 * @author cattaneostefano
 */
public class NearNetworkUtil {
    public static final String TAG = "NearNetworkUtil";

    // TODO queue for trackings

    /**
     * Send tracking information to the back-end. Since every component sends tracking data to different endpoints, the url is a parameter.
     * @param context the app context.
     * @param url the tracking url.
     * @param body the HTTP request body.
     */
    public static void sendTrack(Context context, String url, String body){
        // TODO ewfjrbghregfr
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

}
