package it.near.sdk.Operation;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.CustomJsonRequest;
import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * @author cattaneostefano.
 */
public class UserProfile {

    private static final String PLUGIN_NAME = "congrego";
    private static final String RES_TYPE = "profiles";
    private static final String TAG = "UserProfile";

    public static void CreateNewProfile(Context context, ){

        try {
            String requestBody = getRequestBody(context);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(RES_TYPE).build();

        GlobalState.getInstance(context).getRequestQueue().add(new CustomJsonRequest(
                context,
                Request.Method.POST,
                url.toString(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, "got profile");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }

        ));

    }

    private static String getRequestBody(Context context) throws JSONException {

        String appId = GlobalConfig.getInstance(context).getAppId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("app_id", appId);
        String reqBody = NearUtils.toJsonAPI("profiles", map);

    }

}
