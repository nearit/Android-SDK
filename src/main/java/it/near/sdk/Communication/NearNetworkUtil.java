package it.near.sdk.Communication;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import it.near.sdk.GlobalState;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano
 */
public class NearNetworkUtil {
    public static final String TAG = "NearNetworkUtil";

    public static void sendTrack(Context context, String url, String body){
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
    }

}
