package it.near.sdk.communication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.logging.NearLog;
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
    private static final String PROFILE_ID = "profile_id";

    private NearInstallationRequestQueue requestQueue;
    private Context context;

    private final GlobalConfig globalConfig;

    public NearInstallation(Context context, NearAsyncHttpClient httpClient, GlobalConfig globalConfig) {
        this.context = context;
        this.globalConfig = globalConfig;
        requestQueue = new NearInstallationRequestQueue(httpClient, globalConfig);
    }

    /**
     * Registers a new installation to the server. It uses a POST request if an installationId is not present (new installation),
     * or a PUT if an installationId is already present.
     * The installationId is a back-end concept and does not correspond with the device token.
     *
     */
    public void refreshInstallation() {
        // get the local installation id
        String installationId = globalConfig.getInstallationId();
        try {
            // build a JSON api request body with or without the id, depending whether the installID is null or not
            String installBody = getInstallationBody(context, globalConfig, installationId);
            // with the same criteria, we decide the type of request to do
            requestQueue.registerInstallation(installBody);
        } catch (JSONException e) {
            NearLog.d(TAG, "Unable to send installation data");
        }
    }

    private String getInstallationBody(Context context, GlobalConfig globalConfig, String id) throws JSONException {
        HashMap<String, Object> attributeMap = new HashMap<>();
        // Set platform to "android"
        attributeMap.put(PLATFORM, ANDROID);
        // Set platform version to the Android API level of the device
        attributeMap.put(PLATFORM_VERSION, String.valueOf(Build.VERSION.SDK_INT));
        // set SDK version
        attributeMap.put(SDK_VERSION, it.near.sdk.BuildConfig.VERSION_NAME);
        // Set device token (for GCM)
        attributeMap.put(DEVICE_IDENTIFIER, getDeviceToken(globalConfig));
        // Set app ID (as defined by our APIs)
        attributeMap.put(APP_ID, globalConfig.getAppId());
        // Set the profile if I have it.
        attributeMap.put(PROFILE_ID, globalConfig.getProfileId());
        // Set bluetooth availability
        attributeMap.put(BLUETOOTH, getBluetoothStatus());
        // Set location permission
        attributeMap.put(LOCATION, getLocationPermissionStatus(context));
        return NearJsonAPIUtils.toJsonAPI(INSTALLATION_RES_TYPE, id, attributeMap);
    }

    private static boolean getLocationPermissionStatus(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean getBluetoothStatus() {
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

    private static String getDeviceToken(GlobalConfig globalConfig) {
        String token = globalConfig.getDeviceToken();
        if (token == null) {
            try {
                String firebaseToken = FirebaseInstanceId.getInstance().getToken();
                if (firebaseToken != null) {
                    token = firebaseToken;
                    globalConfig.setDeviceToken(token);
                }
            } catch (IllegalStateException e) {
                NearLog.e(TAG, "We can't get your firebase instance. Near push notification might not work");
            }
        }
        return token;
    }
}
