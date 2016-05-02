package it.near.sdk.Communication;

import android.content.Context;
import android.os.Build;
import android.support.multidex.BuildConfig;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.Utils.NearUtils;
import it.near.sdk.Utils.ULog;

/**
 * Class with static method to register an app installation to our Near APIs.
 *
 * @author cattaneostefano
 */
public class NearInstallation {

    private static final String INSTALLATION_RES_TYPE = "installations";
    private static final String PLATFORM = "platform";
    private static final String ANDROID = "android";
    private static final String PLATFORM_VERSION = "platform_version";
    private static final String SDK_VERSION = "sdk_version";
    private static final String DEVICE_IDENTIFIER = "device_identifier";
    private static final String APP_ID = "app_id";
    private static final String TAG = "NearInstallation";

    /**
     * Registers a new installation to the server. It uses a POST request if an installationId is not present (new installation),
     * or a PUT if an installationId is already present.
     * The installationId is a back-end concept and does not correspond with the device token.
     *
     * @param context the app context
     */
    public static void registerInstallation(final Context context){
        // get the local installation id
        String installationId = GlobalConfig.getInstance(context).getInstallationId();
        try {
            // build a JSON api request body with or without the id, depending wheter the installID is null or not
            String installBody = getInstallationBody(context, installationId);
            // with the same criteria, we decide the type of request to do
            int method = installationId == null ? Request.Method.POST : Request.Method.PUT;
            String subPath = installationId == null ? "" : "/" + installationId;
            GlobalState.getInstance(context).getRequestQueue().add(
                    new CustomJsonRequest(context, method, Constants.API.INSTALLATIONS_PATH + subPath, installBody, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ULog.d(TAG , "Installation data sent");
                            // If the registration is correct, we save the installationId locally
                            try {
                                String installationId = response.getJSONObject("data").getString("id");
                                GlobalConfig.getInstance(context).setInstallationId(installationId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ULog.d(TAG, "Installation datat sending error: " + error.toString());
                        }
                    }));

        } catch (JSONException e) {
            ULog.d(TAG, "Unable to send installation data");
            e.printStackTrace();
        }
    }

    /**
     * Return a JSONapi formatted installation object with the proper attributes.
     *
     * @param context the app context.
     * @param id installation id. It can be null and in that case will not be set.
     * @return The JSONapi string of the installation object.
     * @throws JSONException
     */
    private static String getInstallationBody(Context context, String id) throws JSONException {
        HashMap<String, Object> attributeMap = new HashMap<>();
        // Set platform to "android"
        attributeMap.put(PLATFORM, ANDROID);
        // Set platform version to the Android API level of the device
        attributeMap.put(PLATFORM_VERSION, String.valueOf(Build.VERSION.SDK_INT));
        // set SDK version
        attributeMap.put(SDK_VERSION, it.near.sdk.BuildConfig.VERSION_NAME);
        // Set device token (for GCM)
        attributeMap.put(DEVICE_IDENTIFIER, GlobalConfig.getInstance(context).getDeviceToken());
        // Set app ID (as defined by our APIs)
        attributeMap.put(APP_ID, GlobalConfig.getInstance(context).getAppId());
        return NearUtils.toJsonAPI(INSTALLATION_RES_TYPE, id, attributeMap);
    }

}
