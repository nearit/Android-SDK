package it.near.sdk.operation;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
    private static final String TAG = "NearItUserProfile";

    private final GlobalConfig globalConfig;
    private final NearAsyncHttpClient httpClient;
    private final UserDataBackOff userDataBackOff;

    private ProfileIdUpdateListener profileIdUpdateListener;
    private ProfileFetchListener profileFetchListener;

    private boolean profileCreationBusy = false;

    public NearItUserProfile(GlobalConfig globalConfig, NearAsyncHttpClient httpClient, UserDataBackOff userDataBackOff) {
        this.globalConfig = globalConfig;
        this.httpClient = httpClient;
        this.userDataBackOff = userDataBackOff;
    }

    public void setProfileIdUpdateListener(ProfileIdUpdateListener profileIdUpdateListener) {
        this.profileIdUpdateListener = profileIdUpdateListener;
    }

    public void setProfileDataUpdateListener(ProfileDataUpdateListener profileDataUpdateListener) {
        userDataBackOff.setProfileDataUpdateListener(profileDataUpdateListener);
    }

    public void setProfileId(String profileId) {
        globalConfig.setProfileId(profileId);
        notifyListener();
    }

    private void notifyListener() {
        if (profileIdUpdateListener != null) {
            profileIdUpdateListener.onProfileIdUpdated();
        }
    }

    @Deprecated
    @Nullable
    public String getProfileId() {
        return globalConfig.getProfileId();
    }

    public void getProfileId(Context context, final ProfileFetchListener listener) {
        String profileId = globalConfig.getProfileId();
        if (profileId != null) {
            listener.onProfileId(profileId);
        } else {
            if (profileCreationBusy) {
                //  if a creation is already in progress, notify this listener too when ready
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

            //  notify the listener
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

                        userDataBackOff.sendDataPoints();

                        //  notifies to NearItManager
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
                    NearLog.d(TAG, "profile error: " + statusCode);
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
     * @param key   the name of the data field.
     * @param value the value of the data field for the current user.
     */
    public void setUserData(String key, String value) {
        userDataBackOff.setUserData(key, value);
    }

    /**
     * Create or update multiple user data, key/value couples used in profile segmentation.
     *
     * @param valuesMap map fo key values profile data.
     */
    public void setBatchUserData(Map<String, String> valuesMap) {
        userDataBackOff.setBatchUserData(valuesMap);
    }

    public static NearItUserProfile obtain(GlobalConfig globalConfig, Context context) {
        UserDataCacheManager userDataCacheManager = UserDataCacheManager.obtain(context);
        UserDataBackOff userDataBackOff =  UserDataBackOff.obtain(userDataCacheManager, globalConfig, context);
        return new NearItUserProfile(globalConfig, new NearAsyncHttpClient(context),userDataBackOff);
    }

    public interface ProfileFetchListener {
        void onProfileId(String profileId);
        void onError(String error);
    }
}
