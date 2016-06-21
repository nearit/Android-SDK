package it.near.sdk.Communication;

import android.content.Context;
import android.preference.PreferenceActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

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
public class NearAsyncHttpClient extends AsyncHttpClient {

    public RequestHandle nearPost(Context context, String url, String requestBody, ResponseHandlerInterface responseHandler) throws AuthenticationException, UnsupportedEncodingException {
        HttpEntity body = new StringEntity(requestBody);
        return super.post(context, buildUrl(context, url), getHeaders(context), body, Constants.Headers.jsonApiHeader, responseHandler);
    }

    public RequestHandle nearGet(Context context, String url, ResponseHandlerInterface responseHandler){
        return super.get(context, buildUrl(context, url), responseHandler);
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



    private Header[] getHeaders(Context context) throws AuthenticationException {
        return new Header[]{
                new BasicHeader(Constants.Headers.accessToken, "bearer " + GlobalConfig.getInstance(context).getApiKey()),
                new BasicHeader(Constants.Headers.contentType, Constants.Headers.jsonApiHeader),
                new BasicHeader(Constants.Headers.accept, Constants.Headers.jsonApiHeader)
        };
    }

    private String buildUrl(Context context, String relativeUrl){
        String baseUrl = context.getResources().getString(R.string.API_BASE_URL) + relativeUrl;

        return baseUrl.replace("%5B", "[").replace("%5D", "]");
    }

}
