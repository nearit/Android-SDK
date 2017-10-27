package it.near.sdk.communication;

import android.content.Context;
import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import it.near.sdk.GlobalConfig;
import it.near.sdk.R;
import it.near.sdk.utils.ApiKeyConfig;
import it.near.sdk.utils.NearUtils;

/**
 * @author cattaneostefano.
 */
public class NearAsyncHttpClient {

    private static final int OPTED_OUT_CODE = 451;

    private static AsyncHttpClient syncHttpClient = new SyncHttpClient();
    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private final Context context;
    private final GlobalConfig globalConfig;

    public NearAsyncHttpClient(Context context, GlobalConfig globalConfig) {
        this.context = context;
        this.globalConfig = globalConfig;
    }

    public RequestHandle nearGet(String url, NearJsonHttpResponseHandler responseHandlerInterface) throws AuthenticationException {
        return this.get(context, url, responseHandlerInterface);
    }

    public RequestHandle get(Context context, String url, NearJsonHttpResponseHandler responseHandler) throws AuthenticationException {
        return getClient().get(context,
                buildUrl(context, url),
                getHeaders(context, globalConfig.getProfileId()),
                null,
                new Middle(responseHandler));
    }

    private static AsyncHttpClient getClient() {
        if (Looper.myLooper() == null) {
            return syncHttpClient;
        }
        return asyncHttpClient;
    }

    public RequestHandle nearPost(String url, String requestBody, NearJsonHttpResponseHandler responseHandlerInterface) throws AuthenticationException, UnsupportedEncodingException {
        return this.post(context, url, requestBody, responseHandlerInterface);
    }

    public RequestHandle post(Context context, String url, String requestBody, NearJsonHttpResponseHandler responseHandler) throws AuthenticationException, UnsupportedEncodingException {
        HttpEntity body = new StringEntity(requestBody);
        return getClient().post(context,
                buildUrl(context, url),
                getHeaders(context, globalConfig.getProfileId()),
                body,
                Constants.Headers.jsonApiHeader,
                new Middle(responseHandler));
    }

    public RequestHandle nearPut(String url, String requestBody, NearJsonHttpResponseHandler responseHandler) throws AuthenticationException, UnsupportedEncodingException {
        return this.put(context, url, requestBody, responseHandler);
    }

    public RequestHandle put(Context context, String url, String requestBody, NearJsonHttpResponseHandler responseHandler) throws UnsupportedEncodingException, AuthenticationException {
        HttpEntity body = new StringEntity(requestBody);
        return getClient().put(context,
                buildUrl(context, url),
                getHeaders(context, globalConfig.getProfileId()),
                body,
                Constants.Headers.jsonApiHeader,
                new Middle(responseHandler));
    }

    public RequestHandle nearDelete(String url, NearJsonHttpResponseHandler responseHandlerInterface) throws AuthenticationException, UnsupportedEncodingException {
        return this.delete(context, url, responseHandlerInterface);
    }

    public RequestHandle delete(Context context, String url, NearJsonHttpResponseHandler responseHandlerInterface) throws AuthenticationException, UnsupportedEncodingException {
        return getClient().delete(context,
                buildUrl(context, url),
                getHeaders(context, globalConfig.getProfileId()),
                null,
                new Middle(responseHandlerInterface));
    }

    public RequestHandle nearMock(Context context, String url, NearJsonHttpResponseHandler responseHandlerInterface, int mockResId) {
        String mockedString = context.getApplicationContext().getResources().getString(mockResId);
        JSONObject mockedResponse = null;
        try {
            mockedResponse = new JSONObject(mockedString);
        } catch (JSONException ignored) {

        }
        responseHandlerInterface.onSuccess(200, null, mockedResponse);
        return null;
    }

    public RequestHandle nearMock(Context context, String url, String requestBody, NearJsonHttpResponseHandler responseHandlerInterface, int mockResId) {
        return nearMock(context, url, responseHandlerInterface, mockResId);
    }

    public class Middle extends NearJsonHttpResponseHandler {
        NearJsonHttpResponseHandler handler;

        public Middle(NearJsonHttpResponseHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            handler.onSuccess(statusCode, headers, response);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            handler.onSuccess(statusCode, headers, response);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            handler.onSuccess(statusCode, headers, responseString);
        }

        @Override
        public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
            if(statusCode == OPTED_OUT_CODE) {
                optOutCheck();
            }
            handler.onFailureUnique(statusCode, headers, throwable, responseString);
        }
    }

    private void optOutCheck() {
        globalConfig.setOptOut();
    }

    private static Header[] getHeaders(Context context, String profileId) throws AuthenticationException {
        String apiKey = ApiKeyConfig.readApiKey(context);
        return new Header[]{
                new BasicHeader(Constants.Headers.accessToken, "bearer " + apiKey),
                new BasicHeader(Constants.Headers.contentType, Constants.Headers.jsonApiHeader),
                new BasicHeader(Constants.Headers.accept, Constants.Headers.jsonApiHeader),
                new BasicHeader(Constants.Headers.version_header_key, String.valueOf(it.near.sdk.BuildConfig.API_VERSION)),
                new BasicHeader(Constants.Headers.near_version_header_key, String.valueOf(it.near.sdk.BuildConfig.NEAR_API_VERSION)),
                new BasicHeader(Constants.Headers.acceptLanguage, NearUtils.toBcp47Language(Locale.getDefault())),
                new BasicHeader(Constants.Headers.profile_id, profileId)
        };
    }

    private static String buildUrl(Context context, String relativeUrl) {
        String baseUrl = context.getResources().getString(R.string.API_BASE_URL) + relativeUrl;

        return baseUrl.replace("%5B", "[").replace("%5D", "]");
    }

}
