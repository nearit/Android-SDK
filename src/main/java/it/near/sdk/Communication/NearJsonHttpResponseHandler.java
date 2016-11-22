package it.near.sdk.Communication;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

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
        this.onFailureUnique(statusCode, headers, throwable, errorResponse != null ? errorResponse.toString() : null);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        this.onFailureUnique(statusCode, headers, throwable, errorResponse != null ? errorResponse.toString() : null);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        this.onFailureUnique(statusCode, headers, throwable, responseString);
    }

    public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
        AsyncHttpClient.log.w(LOG_TAG, "onFailure(int, Header[], Throwable, JSONObject) was not overriden, but callback was received", throwable);
    }
}
