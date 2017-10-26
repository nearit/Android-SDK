package it.near.sdk.operation;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.utils.NearJsonAPIUtils;

public class NearItUserDataAPI {

    private static final String TAG = "NearItUserDataAPI";
    private static final String PLUGIN_NAME = "congrego";
    private static final String PROFILE_RES_TYPE = "profiles";
    private static final String DATA_POINTS_RES_TYPE = "data_points";

    private final GlobalConfig globalConfig;
    private final NearAsyncHttpClient httpClient;

    public NearItUserDataAPI(GlobalConfig globalConfig, NearAsyncHttpClient httpClient) {
        this.globalConfig = globalConfig;
        this.httpClient = httpClient;
    }

    public void sendDataPoints(@NonNull final HashMap<String, String> userData, final UserDataSendListener listener) {
        String profileId = globalConfig.getProfileId();
        if (!userData.isEmpty()) {
            if (profileId != null) {
                ArrayList<HashMap<String, Object>> maps = new ArrayList<>();
                for (Map.Entry<String, String> entry : userData.entrySet()) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("key", entry.getKey());
                    map.put("value", entry.getValue());
                    maps.add(map);
                }
                String reqBody;
                try {
                    reqBody = NearJsonAPIUtils.toJsonAPI("data_points", maps);
                } catch (JSONException e) {
                    NearLog.d(TAG, "Error creating userdata request");
                    listener.onSendingFailure();
                    return;
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
                            listener.onSendingSuccess(userData);
                        }

                        @Override
                        public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                            NearLog.d(TAG, "datapoint not created, network error: " + statusCode);
                            listener.onSendingFailure();
                        }
                    });
                } catch (AuthenticationException | UnsupportedEncodingException e) {
                    NearLog.d(TAG, "error: impossible to send requests");
                }
            }
        }
    }

    public static NearItUserDataAPI obtain(GlobalConfig globalConfig, Context context) {
        return new NearItUserDataAPI(globalConfig, new NearAsyncHttpClient(context));
    }

    interface UserDataSendListener {
        void onSendingSuccess(HashMap<String, String> sentData);

        void onSendingFailure();
    }
}
