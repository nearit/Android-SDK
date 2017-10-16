package it.near.sdk.operation;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.utils.NearJsonAPIUtils;

/**
 * Created by Federico Boschini on 16/10/17.
 */

public class NearItUserDataAPI {

    private static final String TAG = "NearItUserDataAPI";
    private static final String PLUGIN_NAME = "congrego";
    private static final String PROFILE_RES_TYPE = "profiles";
    private static final String DATA_POINTS_RES_TYPE = "data_points";

    private final GlobalConfig globalConfig;
    private final NearAsyncHttpClient httpClient;

    private boolean isBusy = false;

    public NearItUserDataAPI(GlobalConfig globalConfig, NearAsyncHttpClient httpClient) {
        this.globalConfig = globalConfig;
        this.httpClient = httpClient;
    }

    public boolean getIsBusy() {
        return this.isBusy;
    }

    public void sendDataPoints(HashMap<String, Object> userData, final UserDataSendListener listener) {
        String profileId = globalConfig.getProfileId();
        if (profileId != null) {
            if (!isBusy) {
                isBusy = true;

                final HashMap<String, Object> userDataCopy = new HashMap<>(userData);

                String reqBody = null;
                try {
                    reqBody = NearJsonAPIUtils.toJsonAPI("data_points", userDataCopy);
                } catch (JSONException e) {
                    NearLog.d(TAG, "Error creating userdata request");
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
                            listener.onSendingSuccess(userDataCopy);
                        }

                        @Override
                        public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                            NearLog.d(TAG, "datapoint not created, network error: " + statusCode);
                            listener.onSendingFailure();
                        }
                    });
                    isBusy = false;
                } catch (AuthenticationException | UnsupportedEncodingException e) {
                    NearLog.d(TAG, "error: impossible to send requests");
                }
            }
        }
    }

    public interface UserDataSendListener {
        void onSendingSuccess(HashMap<String, Object> sentData);
        void onSendingFailure();
    }
}
