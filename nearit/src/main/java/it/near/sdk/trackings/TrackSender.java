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
    private boolean optedOut;

    public TrackSender(NearAsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    void sendTrack(TrackRequest request, final RequestListener listener) {
        if (!optedOut) {
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
    }

    private void send(TrackRequest request, final RequestListener listener) throws AuthenticationException, UnsupportedEncodingException {
        if (!optedOut) {
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
        } else {
            //  user opted out, still notify success but do not send tracking
            NearLog.d(TAG, "User opted out");
            listener.onSuccess();
        }
    }

    public interface RequestListener {
        void onSuccess();

        void onFailure(int statusCode);
    }

    public void onOptOut() {
        optedOut = true;
    }
}
