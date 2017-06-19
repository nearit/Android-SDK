package it.near.sdk.communication;

import android.os.Handler;
import android.os.Looper;

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
    private final GlobalConfig globalConfig;
    private boolean onGoingEdit = false;

    private static final int UNAUTHORIZED_ERROR_CODE = 403;
    private static final int NOT_FOUND_ERROR_CODE = 404;

    private ConcurrentLinkedQueue<InstallationRequest> installationRequests = new ConcurrentLinkedQueue<>();

    public NearInstallationRequestQueue(NearAsyncHttpClient httpClient, GlobalConfig globalConfig) {
        this.httpClient = checkNotNull(httpClient);
        this.globalConfig = checkNotNull(globalConfig);
    }

    public void registerInstallation(final String installationBody) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                InstallationRequest installationRequest = new InstallationRequest(installationBody, new NearJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        onGoingEdit = false;
                        NearLog.d(TAG, "Installation data sent");
                        // If the registration is correct, we save the installationId locally
                        try {
                            String installationId = response.getJSONObject("data").getString("id");
                            globalConfig.setInstallationId(installationId);
                        } catch (JSONException e) {
                            NearLog.d(TAG, "Data format error");
                        }
                        startConsumingQueue();
                    }

                    @Override
                    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                        super.onFailureUnique(statusCode, headers, throwable, responseString);
                        onGoingEdit = false;
                        if (statusCode == UNAUTHORIZED_ERROR_CODE ||
                                statusCode == NOT_FOUND_ERROR_CODE) {
                            globalConfig.setInstallationId(null);
                            registerInstallation(installationBody);
                        }
                        startConsumingQueue();
                    }
                });
                installationRequests.add(installationRequest);
                startConsumingQueue();

            }
        });
    }

    private void startConsumingQueue() {
        if (onGoingEdit) return;
        if (installationRequests.peek() != null) {
            onGoingEdit = true;
            InstallationRequest installationRequest = installationRequests.poll();
            try {
                registerOrEditInstallation(installationRequest);
            } catch (UnsupportedEncodingException | AuthenticationException e) {
                NearLog.d(TAG, "Data error in sending installation data");
                onGoingEdit = false;
            }
        }
    }

    private void registerOrEditInstallation(InstallationRequest installationRequest) throws UnsupportedEncodingException, AuthenticationException {
        String installationId = globalConfig.getInstallationId();
        if (installationId == null) {
            NearLog.e(TAG, "POST");
            httpClient.nearPost(
                    Constants.API.INSTALLATIONS_PATH,
                    installationRequest.installationBody,
                    installationRequest.responseHandler);
        } else {
            NearLog.e(TAG, "PUT " + installationId);
            String subPath = "/" + installationId;
            httpClient.nearPut(
                    Constants.API.INSTALLATIONS_PATH + subPath,
                    installationRequest.installationBody,
                    installationRequest.responseHandler);
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
