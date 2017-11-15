package it.near.sdk.operation;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Map;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.NearAsyncHttpClient;

/**
 * Class containing methods to create a new profile and to add values to the profile for user segmentation.
 *
 * @author cattaneostefano.
 */
public class NearItUserProfile {

    public static final String OPTED_OUT_PROFILE_ID = "Opted_Out_Profile";

    private final GlobalConfig globalConfig;
    private final UserDataBackOff userDataBackOff;
    private final NearItUserProfileAPI userProfileAPI;

    private ProfileIdUpdateListener profileIdUpdateListener;

    private boolean optedOut;


    public NearItUserProfile(GlobalConfig globalConfig, UserDataBackOff userDataBackOff, NearItUserProfileAPI userProfileAPI) {
        this.globalConfig = globalConfig;
        this.userDataBackOff = userDataBackOff;
        this.userProfileAPI = userProfileAPI;
        optedOut = globalConfig.getOptOut();
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
        if (globalConfig.getOptOut()) {
            return OPTED_OUT_PROFILE_ID;
        } else {
            return globalConfig.getProfileId();
        }
    }

    public void getProfileId(Context context, final ProfileFetchListener listener) {
        if (globalConfig.getOptOut()) {
            listener.onProfileId(OPTED_OUT_PROFILE_ID);
        } else {
            userProfileAPI.getProfileId(listener);
        }
    }

    /**
     * Create a new profile and saves the profile identifier internally. After a profile is created, it's possible to add properties to it.
     *
     * @param context  the application context.
     * @param listener interface for success or failure on profile creation.
     */
    public void createNewProfile(final Context context, final ProfileCreationListener listener) {
        userProfileAPI.createNewProfile(listener);
    }

    /**
     * Create or update user data, a key/value couple used in profile segmentation.
     *
     * @param key   the name of the data field.
     * @param value the value of the data field for the current user.
     */
    public void setUserData(String key, String value) {
        if(!globalConfig.getOptOut()) {
            userDataBackOff.setUserData(key, value);
        }
    }

    /**
     * Create or update multiple user data, key/value couples used in profile segmentation.
     *
     * @param valuesMap map fo key values profile data.
     */
    public void setBatchUserData(Map<String, String> valuesMap) {
        if(!globalConfig.getOptOut()) {
            userDataBackOff.setBatchUserData(valuesMap);
        }
    }

    public static NearItUserProfile obtain(GlobalConfig globalConfig, Context context) {
        UserDataCacheManager userDataCacheManager = UserDataCacheManager.obtain(context);
        UserDataBackOff userDataBackOff = UserDataBackOff.obtain(userDataCacheManager, globalConfig, context);
        NearItUserProfileAPI nearItUserProfileAPI = new NearItUserProfileAPI(userDataBackOff, new NearAsyncHttpClient(context, globalConfig), globalConfig);
        return new NearItUserProfile(globalConfig, userDataBackOff, nearItUserProfileAPI);
    }

    public void onOptOut() {
        optedOut = true;
        userDataBackOff.onOptOut();
    }

    public interface ProfileFetchListener {
        void onProfileId(String profileId);
        void onError(String error);
    }
}
