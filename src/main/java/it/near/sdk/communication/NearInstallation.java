package it.near.sdk.communication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.utils.NearJsonAPIUtils;

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
    private static final String BLUETOOTH = "bluetooth";
    private static final String LOCATION = "location";
    private static final String TAG = "NearInstallation";
    public static final String PLUGIN_RESOURCES = "plugin_resources";
    private static final String PROFILE_ID = "profile_id";

    private static NearAsyncHttpClient httpClient = new NearAsyncHttpClient();

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
            try {
                registerOrEditInstallation(context, installationId, installBody, new NearJsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG , "Installation data sent");
                        // If the registration is correct, we save the installationId locally
                        try {
                            String installationId = response.getJSONObject("data").getString("id");
                            GlobalConfig.getInstance(context).setInstallationId(installationId);
                        } catch (JSONException e) {
                            Log.d(TAG, "Data format error");
                        }
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        Log.d(TAG, "Installation datat sending error: " + statusCode + " " + responseString);
                    }
                });
            } catch (UnsupportedEncodingException | AuthenticationException e) {
                Log.d(TAG, "Data error");
            }

        } catch (JSONException e) {
            Log.d(TAG, "Unable to send installation data");
        }
    }


    private static void registerOrEditInstallation(Context context, String installationId, String installBody, AsyncHttpResponseHandler jsonHttpResponseHandler) throws UnsupportedEncodingException, AuthenticationException {
        if (installationId == null){
            httpClient.nearPost(context, Constants.API.INSTALLATIONS_PATH, installBody, jsonHttpResponseHandler );
        } else {
            String subPath = "/" + installationId;
            httpClient.nearPut(context, Constants.API.INSTALLATIONS_PATH + subPath, installBody, jsonHttpResponseHandler);
        }
    }

    /**
     * Return a JSONapi formatted installation object with the proper attributes.
     *
     * @param context the app context.
     * @param id      installation id. It can be null and in that case will not be set.
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
        // Set the profile if I have it.
        attributeMap.put(PROFILE_ID, GlobalConfig.getInstance(context).getProfileId());
        // Set bluetooth availability
        attributeMap.put(BLUETOOTH, getBluetoothStatus());
        // Set location permission
        attributeMap.put(LOCATION, ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        return NearJsonAPIUtils.toJsonAPI(INSTALLATION_RES_TYPE, id, attributeMap);
    }

    public static boolean getBluetoothStatus() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                return true;
            }
        }
        return false;
    }
}
