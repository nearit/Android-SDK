package it.near.sdk.Communication;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import cz.msebera.android.httpclient.Header;

/**
 * Near response handler to merge all failure methods into one.
 *
 * Created by cattaneostefano on 17/10/2016.
 */

public class NearJsonHttpResponseHandler extends JsonHttpResponseHandler {

    private static final String LOG_TAG = "NearResponseHandler";

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        this.onFailureUnique(statusCode, headers, throwable, errorResponse != null ? errorResponse.toString() : "");
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        this.onFailureUnique(statusCode, headers, throwable, errorResponse != null ? errorResponse.toString() : "");
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        this.onFailureUnique(statusCode, headers, throwable, responseString);
    }

    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
        AsyncHttpClient.log.w(LOG_TAG, "onFailure(int, Header[], Throwable, JSONObject) was not overriden, but callback was received", throwable);
    }

    protected Object parseResponse(byte[] responseBody) throws JSONException {
        if (null == responseBody)
            return null;
        Object result = null;
        //trim the string to prevent start with blank, and test if the string is valid JSON, because the parser don't do this :(. If JSON is not valid this will return null
        String jsonString = getResponseString(responseBody, getCharset());
        if (jsonString != null) {
            jsonString = jsonString.trim();
            result = new JSONObject(jsonString);
        }
        if (result == null) {
            result = jsonString;
        }
        return result;
    }
}
