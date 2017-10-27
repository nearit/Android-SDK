package it.near.sdk.operation;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.NearItManager;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
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

    public static final String OPTED_OUT_PROFILE_ID = "Opted_Out_Profile";

    private final GlobalConfig globalConfig;
    private final NearAsyncHttpClient httpClient;
    private ProfileUpdateListener profileUpdateListener;
    private ProfileFetchListener profileFetchListener;

    private boolean profileCreationBusy = false;
    private boolean optedOut;

    public NearItUserProfile(GlobalConfig globalConfig, NearAsyncHttpClient httpClient) {
        this.globalConfig = globalConfig;
        this.httpClient = httpClient;
        optedOut = globalConfig.getOptOut();
    }

    public void setProfileUpdateListener(ProfileUpdateListener profileUpdateListener) {
        this.profileUpdateListener = profileUpdateListener;
    }

    public void setProfileId(String profileId) {
        globalConfig.setProfileId(profileId);
        notifyListener();
    }

    private void notifyListener() {
        if (profileUpdateListener != null) {
            profileUpdateListener.onProfileUpdated();
        }
    }

    @Deprecated
    @Nullable
    public String getProfileId() {
        if (optedOut) {
            return OPTED_OUT_PROFILE_ID;
        } else {
            return globalConfig.getProfileId();
        }
    }

    public void getProfileId(Context context, final ProfileFetchListener listener) {
        if (optedOut) {
            listener.onProfileId(OPTED_OUT_PROFILE_ID);
        } else {
            String profileId = globalConfig.getProfileId();
            if (profileId != null) {
                listener.onProfileId(profileId);
            } else {
                if (profileCreationBusy) {
                    profileFetchListener = listener;
                } else {
                    createNewProfile(context, new ProfileCreationListener() {
                        @Override
                        public void onProfileCreated(boolean created, String profileId) {
                            listener.onProfileId(profileId);
                        }

                        @Override
                        public void onProfileCreationError(String error) {
                            listener.onError("Couldn't create profile");
                        }
                    });
                }
            }
        }
    }

    /**
     * Create a new profile and saves the profile identifier internally. After a profile is created, it's possible to add properties to it.
     *
     * @param context  the application context.
     * @param listener interface for success or failure on profile creation.
     */
    public void createNewProfile(final Context context, final ProfileCreationListener listener) {
        profileCreationBusy = true;
        final NearItManager nearItManager = NearItManager.getInstance();
        final GlobalConfig globalConfig = nearItManager.globalConfig;
        String profileId = globalConfig.getProfileId();
        if (profileId != null) {
            // profile already created
            nearItManager.updateInstallation();
            profileCreationBusy = false;
            notifyFetchListener(profileId);
            listener.onProfileCreated(false, profileId);
            return;
        }

        String requestBody = null;
        try {
            requestBody = buildProfileCreationRequestBody(globalConfig);
        } catch (JSONException e) {
            profileCreationBusy = false;
            listener.onProfileCreationError("Can't compute request body");
            notifyFetchListener();
            return;
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE).build();

        try {
            httpClient.nearPost(url.toString(), requestBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "profile created: " + response.toString());

                    String profileId = null;
                    try {
                        profileId = response.getJSONObject("data").getString("id");
                        globalConfig.setProfileId(profileId);
                        // update the installation with the profile id
                        nearItManager.updateInstallation();
                        nearItManager.getRecipesManager().refreshConfig();
                        profileCreationBusy = false;
                        notifyFetchListener(profileId);
                        listener.onProfileCreated(true, profileId);
                    } catch (JSONException e) {
                        profileCreationBusy = false;
                        notifyFetchListener();
                        listener.onProfileCreationError("unknown server format");
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "profile erro: " + statusCode);
                    profileCreationBusy = false;
                    notifyFetchListener();
                    listener.onProfileCreationError("network error: " + statusCode);
                }

            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            profileCreationBusy = false;
            notifyFetchListener();
            listener.onProfileCreationError("error: impossible to make a request");
        }
    }

    private void notifyFetchListener(String profileId) {
        if (profileFetchListener != null) {
            profileFetchListener.onProfileId(profileId);
            profileFetchListener = null;
        }
    }

    private void notifyFetchListener() {
        if (profileFetchListener != null) {
            profileFetchListener.onError("Couldn't fetch profile");
            profileFetchListener = null;
        }
    }

    private String buildProfileCreationRequestBody(GlobalConfig globalConfig) throws JSONException {
        String appId = globalConfig.getAppId();
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
    public void setUserData(final Context context, String key, String value, final UserDataNotifier listener) {
        if (!optedOut) {
            final NearItManager nearItManager = NearItManager.getInstance();
            String profileId = nearItManager.globalConfig.getProfileId();
            if (profileId == null) {
                listener.onDataNotSetError("Profile didn't exists");
                createNewProfile(context, new ProfileCreationListener() {
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
                httpClient.nearPost(url.toString(), reqBody, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        NearLog.d(TAG, "datapoint created: " + response.toString());
                        nearItManager.getRecipesManager().refreshConfig();
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
        } else {
            NearLog.d(TAG, "User data not created because user opted-out");
        }
    }

    /**
     * Create or update multiple user data, key/value couples used in profile segmentation.
     *
     * @param context   the application context.
     * @param valuesMap map fo key values profile data.
     * @param listener  interface for success or failure on properties creation.
     */
    public void setBatchUserData(final Context context, Map<String, String> valuesMap, final UserDataNotifier listener) {
        if (!optedOut) {
            String profileId = NearItManager.getInstance().globalConfig.getProfileId();
            if (profileId == null) {
                listener.onDataNotSetError("Profile didn't exists");
                createNewProfile(context, new ProfileCreationListener() {
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

            try {
                httpClient.nearPost(url.toString(), reqBody, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        NearLog.d(TAG, "datapoint created: " + response.toString());
                        NearItManager.getInstance().getRecipesManager().refreshConfig();
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
        } else {
            NearLog.d(TAG, "User data not created because user opted-out");
        }
    }

    public void onOptOut() {
        optedOut = true;
    }

    public interface ProfileFetchListener {
        void onProfileId(String profileId);

        void onError(String error);
    }
}
