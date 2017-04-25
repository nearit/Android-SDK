package it.near.sdk.trackings;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;

public class TrackSender {

    private static final String TAG = "TrackSender";
    private static final int AUTH_EXC_STATUS_CODE = 403;
    private static final int UNSUPPORTED_ENCODING_STATUS_CODE = 422;

    private final NearAsyncHttpClient httpClient;

    public TrackSender(NearAsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void sendTrack(TrackRequest request, final RequestListener listener) {
        try {
            send(request, listener);
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Couldn't send tracking");
            listener.onFailure(AUTH_EXC_STATUS_CODE);
        } catch (UnsupportedEncodingException e) {
            NearLog.d(TAG, "Couldn't send tracking");
            listener.onFailure(UNSUPPORTED_ENCODING_STATUS_CODE);
        }
    }

    private void send(TrackRequest request, final RequestListener listener) throws AuthenticationException, UnsupportedEncodingException {
        httpClient.nearPost(request.url, request.body, new NearJsonHttpResponseHandler() {
            @Override
            public void setUsePoolThread(boolean pool) {
                super.setUsePoolThread(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                NearLog.d(TAG, "Tracking data sent.");
                listener.onSuccess();
            }

            @Override
            public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                NearLog.d(TAG, "Tracking data not sent. Error: " + statusCode);
                listener.onFailure(statusCode);
            }
        });
    }

    public interface RequestListener {
        void onSuccess();

        void onFailure(int statusCode);
    }
}
