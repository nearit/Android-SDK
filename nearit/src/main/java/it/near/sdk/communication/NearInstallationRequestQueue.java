package it.near.sdk.communication;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentLinkedQueue;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.logging.NearLog;

import static it.near.sdk.utils.NearUtils.checkNotNull;

public class NearInstallationRequestQueue {

    private static final String TAG = "NearInstallationRequestQueue";
    private final NearAsyncHttpClient httpClient;
    private final Context context;
    private final GlobalConfig globalConfig;

    private ConcurrentLinkedQueue<InstallationRequest> installationRequests;

    public NearInstallationRequestQueue(NearAsyncHttpClient httpClient, Context context, GlobalConfig globalConfig) {
        this.httpClient = checkNotNull(httpClient);
        this.context = checkNotNull(context);
        this.globalConfig = checkNotNull(globalConfig);
    }

    public void registerInstallation(String installationBody, JsonHttpResponseHandler jsonHttpResponseHandler) {
        InstallationRequest installationRequest = new InstallationRequest(installationBody, new NearJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                NearLog.d(TAG, "Installation data sent");
                // If the registration is correct, we save the installationId locally
                try {
                    String installationId = response.getJSONObject("data").getString("id");
                    globalConfig.setInstallationId(installationId);
                } catch (JSONException e) {
                    NearLog.d(TAG, "Data format error");
                }
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                super.onFailureUnique(statusCode, headers, throwable, responseString);
            }
        });
        installationRequests.add(installationRequest);
        startConsumingQueue();
    }

    private void startConsumingQueue() {
        while (installationRequests.peek() != null) {
            InstallationRequest installationRequest = installationRequests.poll();
            registerOrEditInstallation(installationRequest);
        }
    }

    private void registerOrEditInstallation(InstallationRequest installationRequest) {
        String installationId = globalConfig.getInstallationId();
        try {
            if (installationId == null) {
                httpClient.nearPost(
                        Constants.API.INSTALLATIONS_PATH,
                        installationRequest.installationBody,
                        installationRequest.responseHandler);
            } else {
                String subPath = "/" + installationId;
                httpClient.nearPut(
                        Constants.API.INSTALLATIONS_PATH + subPath,
                        installationRequest.installationBody,
                        installationRequest.responseHandler);
            }
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            NearLog.d(TAG, "Data error in sending installation data");
        }
    }


    private class InstallationRequest {
        String installationBody;
        JsonHttpResponseHandler responseHandler;

        InstallationRequest(String installationBody, JsonHttpResponseHandler responseHandler) {
            this.installationBody = installationBody;
            this.responseHandler = responseHandler;
        }
    }
}
