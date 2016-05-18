package it.near.sdk.Operation;

import android.app.DownloadManager;
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
 * Class containing methods to create a new profile and to add values to the profile for user segmentation.
 * @author cattaneostefano.
 */
public class UserProfile {

    private static final String PLUGIN_NAME = "congrego";
    private static final String PROFILE_RES_TYPE = "profiles";
    private static final String DATA_POINTS_RES_TYPE = "data_points";
    private static final String TAG = "UserProfile";

    /**
     * Create a new profile and saves the profile identifier internally. After a profile is created, it's possible to add properties to it.
     * @param context the application context.
     * @param listener interface for success or failure on profile creation.
     */
    public static void CreateNewProfile(Context context, final ProfileCreationListener listener){
        String requestBody = null;
        try {
            requestBody = buildProfileCreationRequestBody(context);
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onProfileCreationError("Can't compute request body");
            return;
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE).build();

        GlobalState.getInstance(context).getRequestQueue().add(new CustomJsonRequest(
                context,
                Request.Method.POST,
                url.toString(),
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, "got profile: " + response.toString());
                        // TODO what to do with profile response
                        listener.onProfileCreated();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onProfileCreationError("volley network error: " + error.toString());
                    }
                }

        ));

    }

    private static String buildProfileCreationRequestBody(Context context) throws JSONException {
        String appId = GlobalConfig.getInstance(context).getAppId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("app_id", appId);
        String reqBody = NearUtils.toJsonAPI("profiles", map);
        return reqBody;
    }

    /**
     * Create or update a new data point, a key/value couple representing a property used in profile segmentation.
     * @param context the application context.
     * @param key the name of the property.
     * @param value the value of the property for the current user.
     * @param listener interface for success or failure on property creation.
     */
    public static void SetDataPoint(Context context, String key, String value, final DataPointNotifier listener){
        String profileId = GlobalConfig.getInstance(context).getProfileId();
        if (profileId == null) {
            listener.onDataPointNotSetError("Profile didn't exists");
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("value", value);
        String reqBody= null;
        try {
            reqBody = NearUtils.toJsonAPI("data_points", map);
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onDataPointNotSetError("Request creation error");
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE)
                .appendPath(profileId)
                .appendPath(DATA_POINTS_RES_TYPE).build();

        GlobalState.getInstance(context).getRequestQueue().add(new CustomJsonRequest(
                context,
                Request.Method.POST,
                url.toString(),
                reqBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ULog.d(TAG, "datapoint created: " + response.toString());
                        listener.onDataPointCreated();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onDataPointNotSetError("volley network error: " + error.toString());
                    }
                }
        ));

    }

}
