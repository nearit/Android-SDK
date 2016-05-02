package it.near.sdk.Communication;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.R;

/**
 * Create a JsonObjectRequest (from the volley library) subclass with the proper Near headers.
 * Also includes appending of the relative path to the host.
 * HTTP methods are Volley constants (e.g. <code>Method.GET</code>)
 * @author cattaneostefano
 */
public class CustomJsonRequest extends JsonObjectRequest {
    private Context mContext;

    /**
     * Constructor for GET request, with no body.
     *
     * @param context the app context
     * @param url the url to query
     * @param listener success listener
     * @param errorListener error listener
     */
    public CustomJsonRequest(Context context, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(composeUrl(context, url), listener, errorListener);
        this.mContext = context;
    }

    /**
     * Constructor for http requests with no body.
     *
     * @param context the app context
     * @param method the HTTP method, as defined by the Volley library
     * @param url the url to query
     * @param listener success listener
     * @param errorListener error listener
     */
    public CustomJsonRequest(Context context, int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, composeUrl(context, url), listener, errorListener);
        this.mContext = context;
    }

    /**
     * Constructor for http requests with a <code>String</code> body.
     *
     * @param context the app context
     * @param method the HTTP method, as defined by the Volley library
     * @param url the url to query
     * @param requestBody body string
     * @param listener success listener
     * @param errorListener error listener
     */
    public CustomJsonRequest(Context context, int method, String url, String requestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, composeUrl(context, url), requestBody, listener, errorListener);
        this.mContext = context;
    }

    /**
     * Constructor for http requests with a <code>JSONObject</code> body.
     *
     * @param context the app context
     * @param method the HTTP method, as defined by the Volley library
     * @param url the url to query
     * @param jsonRequest body JSON
     * @param listener success listener
     * @param errorListener error listener
     */
    public CustomJsonRequest(Context context, int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, composeUrl(context, url), jsonRequest, listener, errorListener);
        this.mContext = context;
    }

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
     * <code>null</code>, <code>POST</code> otherwise.
     *
     * @param context the app context
     * @param url the url to query
     * @param jsonRequest body JSON
     * @param listener success listener
     * @param errorListener error listener
     */
    public CustomJsonRequest(Context context, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(composeUrl(context, url), jsonRequest, listener, errorListener);
        this.mContext = context;
    }

    /**
     * Return headers for HTTP calls
     * @return
     * @throws AuthFailureError
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map headers = new HashMap();
        headers.put(Constants.Headers.accessToken, "bearer " + GlobalConfig.getInstance(mContext).getApiKey());
        headers.put(Constants.Headers.contentType, Constants.Headers.jsonApiHeader);
        headers.put(Constants.Headers.accept, Constants.Headers.jsonApiHeader);
        return headers;
    }

    /**
     * Appends a path to the host
     * @param context
     * @param url
     * @return
     */
    private static String composeUrl(Context context, String url) {
        String baseUrl = context.getResources().getString(R.string.API_BASE_URL);
        return baseUrl + url;
    }

}
