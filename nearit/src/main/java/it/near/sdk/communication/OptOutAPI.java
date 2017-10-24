package it.near.sdk.communication;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.logging.NearLog;
import it.near.sdk.operation.NearItUserProfile;

/**
 * @author federico.boschini
 */

public class OptOutAPI {

    private static final String TAG = "OptOutAPI";
    private static final String PLUGIN_NAME = "congrego";
    private static final String PROFILE_RES_TYPE = "profiles";
    private static final String OPTOUT_RES_TYPE = "optout";

    private final NearAsyncHttpClient httpClient;
    private final GlobalConfig globalConfig;

    public OptOutAPI(NearAsyncHttpClient httpClient, GlobalConfig globalConfig) {
        this.httpClient = httpClient;
        this.globalConfig = globalConfig;
    }

    public void optOut(@NonNull final OptOutNotifier optOutNotifier) {

        String profileId = globalConfig.getProfileId();

        if (profileId == null) {
            optOutNotifier.onFailure("profile id is null, can't opt-out");
            return;
        }

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PLUGIN_NAME)
                .appendPath(PROFILE_RES_TYPE)
                .appendPath(profileId)
                .appendPath(OPTOUT_RES_TYPE).build();

        try {
            httpClient.nearDelete(url.toString(), new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "Profile opted out: " + response.toString());
                    optOutNotifier.onSuccess();
                    globalConfig.setOptOut();
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    optOutNotifier.onFailure("network error: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            optOutNotifier.onFailure("error: impossible to send request");
        }

    }

}
