package it.near.sdk.operation;

import android.content.Context;
import android.net.Uri;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearInstallation;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.logging.NearLog;
import it.near.sdk.utils.NearJsonAPIUtils;

/**
 * Class containing methods to create a new profile and to add values to the profile for user segmentation.
 *
 * @author cattaneostefano.
 */
public class NearItUserProfile {

    private static final String PLUGIN_NAME = "congrego";
    private static final String PROFILE_RES_TYPE = "profiles";
    private static final String DATA_POINTS_RES_TYPE = "data_points";
    private static final String TAG = "NearItUserProfile";
    private static NearAsyncHttpClient httpClient = new NearAsyncHttpClient();

    /**
     * Set the profileId of the user using this app installation. This string usually comes from the authentication service for the app.
     *
     * @param context   the application context.
     * @param profileId the profileId string.
     */
    public static void setProfileId(Context context, String profileId) {
        GlobalConfig.getInstance(context).setProfileId(profileId);
        NearInstallation.registerInstallation(context);
    }

    /**
     * Get the cached profileId. Might be null.
     *
     * @param context the application context.
     * @return the cached profileId.
     */
    public static String getProfileId(Context context) {
        return GlobalConfig.getInstance(context).getProfileId();
    }

    /**
     * Reset the profileId. After this is called, the get will return null.
     *
     * @param context the application context.
     */
    public static void resetProfileId(Context context) {
        GlobalConfig.getInstance(context).setProfileId(null);
        NearInstallation.registerInstallation(context);
    }

    /**
     * Create a new profile and saves the profile identifier internally. After a profile is created, it's possible to add properties to it.
     *
     * @param context  the application context.
     * @param listener interface for success or failure on profile creation.
     */
    public static void createNewProfile(final Context context, final ProfileCreationListener listener) {
        String profileId = GlobalConfig.getInstance(context).getProfileId();
        if (profileId != null) {
            // profile already created
            NearInstallation.registerInstallation(context);
            listener.onProfileCreated(false, profileId);
            return;
        }

        String requestBody = null;
        try {
            requestBody = buildProfileCreationRequestBody(context);
        } catch (JSONException e) {
            listener.onProfileCreationError("Can't compute request body");
            return;
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE).build();

        try {
            httpClient.nearPost(context, url.toString(), requestBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "got profile: " + response.toString());

                    String profileId = null;
                    try {
                        profileId = response.getJSONObject("data").getString("id");
                        GlobalConfig.getInstance(context).setProfileId(profileId);
                        // update the installation with the profile id
                        NearInstallation.registerInstallation(context);
                        GlobalState.getInstance(context).getRecipesManager().refreshConfig();
                        listener.onProfileCreated(true, profileId);
                    } catch (JSONException e) {
                        listener.onProfileCreationError("unknown server format");
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "profile erro: " + statusCode);
                    listener.onProfileCreationError("network error: " + statusCode);
                }

            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onProfileCreationError("error: impossible to make a request");
        }
    }

    private static String buildProfileCreationRequestBody(Context context) throws JSONException {
        String appId = GlobalConfig.getInstance(context).getAppId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("app_id", appId);
        return NearJsonAPIUtils.toJsonAPI("profiles", map);
    }

    /**
     * Create or update user data, a key/value couple used in profile segmentation.
     *
     * @param context  the application context.
     * @param key      the name of the data field.
     * @param value    the value of the data field for the current user.
     * @param listener interface for success or failure on property creation.
     */
    public static void setUserData(final Context context, String key, String value, final UserDataNotifier listener) {
        String profileId = GlobalConfig.getInstance(context).getProfileId();
        if (profileId == null) {
            listener.onDataNotSetError("Profile didn't exists");
            NearItUserProfile.createNewProfile(context, new ProfileCreationListener() {
                @Override
                public void onProfileCreated(boolean created, String profileId) {
                    // TODO replay method call?
                }

                @Override
                public void onProfileCreationError(String error) {

                }
            });
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("value", value);
        String reqBody = null;
        try {
            reqBody = NearJsonAPIUtils.toJsonAPI("data_points", map);
        } catch (JSONException e) {
            listener.onDataNotSetError("Request creation error");
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE)
                .appendPath(profileId)
                .appendPath(DATA_POINTS_RES_TYPE).build();
        //TODO not tested
        try {
            httpClient.nearPost(context, url.toString(), reqBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "datapoint created: " + response.toString());
                    GlobalState.getInstance(context).getRecipesManager().refreshConfig();
                    listener.onDataCreated();
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    listener.onDataNotSetError("network error: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onDataNotSetError("error: impossible to send requests");
        }
    }

    /**
     * Create or update multiple user data, key/value couples used in profile segmentation.
     *
     * @param context   the application context.
     * @param valuesMap map fo key values profile data.
     * @param listener  interface for success or failure on properties creation.
     */
    public static void setBatchUserData(final Context context, HashMap<String, String> valuesMap, final UserDataNotifier listener) {
        String profileId = GlobalConfig.getInstance(context).getProfileId();
        if (profileId == null) {
            listener.onDataNotSetError("Profile didn't exists");
            NearItUserProfile.createNewProfile(context, new ProfileCreationListener() {
                @Override
                public void onProfileCreated(boolean created, String profileId) {

                }

                @Override
                public void onProfileCreationError(String error) {

                }
            });
            return;
        }

        ArrayList<HashMap<String, Object>> maps = new ArrayList<>();
        for (Map.Entry<String, String> entry : valuesMap.entrySet()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("key", entry.getKey());
            map.put("value", entry.getValue());
            maps.add(map);
        }
        String reqBody = null;
        try {
            reqBody = NearJsonAPIUtils.toJsonAPI("data_points", maps);
        } catch (JSONException e) {
            listener.onDataNotSetError("Request creatin error");
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE)
                .appendPath(profileId)
                .appendPath(DATA_POINTS_RES_TYPE).build();

        // TODO not tested
        try {
            httpClient.nearPost(context, url.toString(), reqBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "datapoint created: " + response.toString());
                    GlobalState.getInstance(context).getRecipesManager().refreshConfig();
                    listener.onDataCreated();
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    listener.onDataNotSetError("network error: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onDataNotSetError("error: impossible to send request");
        }
    }
}
