package it.near.sdk.Communication;

import android.content.Context;
import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BuildConfig;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import it.near.sdk.GlobalConfig;
import it.near.sdk.R;

/**
 * @author cattaneostefano.
 */
public class NearAsyncHttpClient {

    public static AsyncHttpClient syncHttpClient = new SyncHttpClient();
    public static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();


    public static RequestHandle nearGet(Context context, String url, ResponseHandlerInterface responseHandler) throws AuthenticationException {
        return getClient().get(context,
                buildUrl(context, url),
                getHeaders(context),
                null,
                responseHandler);
    }

    private static AsyncHttpClient getClient() {
        if (Looper.myLooper() == null){
            return syncHttpClient;
        }
        return asyncHttpClient;
    }

    public static RequestHandle nearPost(Context context, String url, String requestBody, ResponseHandlerInterface responseHandler) throws AuthenticationException, UnsupportedEncodingException {
        HttpEntity body = new StringEntity(requestBody);
        return getClient().post(context,
                buildUrl(context, url),
                getHeaders(context),
                body,
                Constants.Headers.jsonApiHeader,
                responseHandler);
    }

    public static RequestHandle nearPut(Context context, String url, String requestBody, ResponseHandlerInterface responseHandler) throws UnsupportedEncodingException, AuthenticationException {
        HttpEntity body = new StringEntity(requestBody);
        return getClient().put(context,
                buildUrl(context, url),
                getHeaders(context),
                body,
                Constants.Headers.jsonApiHeader,
                responseHandler);
    }

    public RequestHandle nearMock(Context context, String url, NearJsonHttpResponseHandler responseHandlerInterface, int mockResId){
        String mockedString = context.getApplicationContext().getResources().getString(mockResId);
        JSONObject mockedResponse = null;
        try {
            mockedResponse = new JSONObject(mockedString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        responseHandlerInterface.onSuccess(200, null, mockedResponse);
        return null;
    }

    public RequestHandle nearMock(Context context, String url, String requestBody, NearJsonHttpResponseHandler responseHandlerInterface, int mockResId){
        return nearMock(context, url, responseHandlerInterface, mockResId);
    }

    /**
 * Return headers for HTTP calls
 * @return a map of headers
 */

   /* @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map headers = new HashMap();
        headers.put(Constants.Headers.accessToken, "bearer " + GlobalConfig.getInstance(mContext).getApiKey());
        headers.put(Constants.Headers.contentType, Constants.Headers.jsonApiHeader);
        headers.put(Constants.Headers.accept, Constants.Headers.jsonApiHeader);
        return headers;
    }*/



    private static Header[] getHeaders(Context context) throws AuthenticationException {
        return new Header[]{
                new BasicHeader(Constants.Headers.accessToken, "bearer " + GlobalConfig.getInstance(context).getApiKey()),
                new BasicHeader(Constants.Headers.contentType, Constants.Headers.jsonApiHeader),
                new BasicHeader(Constants.Headers.accept, Constants.Headers.jsonApiHeader),
                new BasicHeader(Constants.Headers.version_header_key, String.valueOf(it.near.sdk.BuildConfig.API_VERSION))
        };
    }

    private static String buildUrl(Context context, String relativeUrl){
        String baseUrl = it.near.sdk.BuildConfig.BASE_URL + relativeUrl;

        return baseUrl.replace("%5B", "[").replace("%5D", "]");
    }

}
