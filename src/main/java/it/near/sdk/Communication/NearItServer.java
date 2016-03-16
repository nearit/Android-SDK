package it.near.sdk.Communication;

import android.content.Context;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import at.rags.morpheus.Deserializer;
import at.rags.morpheus.JSONAPIObject;
import at.rags.morpheus.Morpheus;
import it.near.sdk.Models.Beacon;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItServer {

    private static NearItServer mInstance = null;
    private static final String TAG = "NearItServer";
    private final Morpheus morpheus;
    private Context mContext;

    private RequestQueue requestQueue;

    private final String inputString = "{\n" +
            "  \"data\": {\n" +
            "    \"id\": \"076bbace-b028-4214-9e51-92bf5d51c735\",\n" +
            "    \"type\": \"beacons\",\n" +
            "    \"attributes\": {\n" +
            "      \"name\": \"becco tonante\",\n" +
            "      \"major\": 444,\n" +
            "      \"proximity_uuid\": \"fb83be70-7d75-4bcb-8362-3ed8f8e99572\",\n" +
            "      \"range\": 0,\n" +
            "      \"minor\": 45,\n" +
            "      \"color\": \"white\",\n" +
            "      \"app_id\": \"fb83be70-7d75-4bcb-8362-3ed8f8e99572\",\n" +
            "      \"creator_id\": \"3391d4b6-4e54-4217-97d1-729e78f8c134\",\n" +
            "      \"trashed\": false,\n" +
            "      \"map_placed\": false,\n" +
            "      \"map_placement_x\": null,\n" +
            "      \"map_placement_y\": null\n" +
            "    },\n" +
            "    \"relationships\": {\n" +
            "      \"matching\": {\n" +
            "        \"data\": null\n" +
            "      }\n" +
            "    },\n" +
            "    \"meta\": {\n" +
            "      \"matched\": false\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private final String inputStringContent = "{\n" +
            "  \"data\": {\n" +
            "    \"id\": \"33GW3ZrpDRz4Eb\",\n" +
            "    \"type\": \"contents\",\n" +
            "    \"attributes\": {\n" +
            "      \"title\": \"2xxasd\",\n" +
            "      \"short_description\": \"2\",\n" +
            "      \"long_description\": \"<p>2</p>\",\n" +
            "      \"creator_id\": \"33f12c79-7176-4764-beca-53437a06b90b\",\n" +
            "      \"created_at\": \"2016-02-24T15:21:12.089+00:00\",\n" +
            "      \"updated_at\": \"2016-03-02T14:39:13.085+00:00\",\n" +
            "      \"photo_ids\": [\n" +
            "        \"6e7978c0-6fde-4ac9-a2f0-0976f1315dc9\"\n" +
            "      ],\n" +
            "      \"trashed\": false,\n" +
            "      \"app_id\": \"5210eed1-e471-4c37-a4dd-832513864645\"\n" +
            "    },\n" +
            "    \"relationships\": {\n" +
            "      \"matchings\": {\n" +
            "        \"data\": []\n" +
            "      }\n" +
            "    },\n" +
            "    \"meta\": {\n" +
            "      \"matching_count\": 0\n" +
            "    }\n" +
            "  }\n" +
            "}";


    private NearItServer(Context context) {
        this.mContext = context;
        morpheus = new Morpheus();
        //register your resources
        Deserializer.registerResourceClass("beacons", Beacon.class);
        Deserializer.registerResourceClass("contents", Content.class);
        Deserializer.registerResourceClass("matchings", Matching.class);

        requestQueue = Volley.newRequestQueue(context);
        requestQueue.start();
    }

    public static NearItServer getInstance(Context context){
        if(mInstance == null)
        {
            mInstance = new NearItServer(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }


    public void downloadNearConfiguration(){

        requestQueue.add(new CustomJsonRequest(mContext, Constants.API.matchings, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ULog.d(TAG, response.toString());
                setMatchings(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ULog.d(TAG, "error " + error.toString() );
            }
        }));

    }


    public void setMatchings(JSONObject matchings) {
        JSONAPIObject jsonapiObject = null;
        try {
            jsonapiObject = morpheus.parse(matchings.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Matching matching = (Matching)jsonapiObject.getResources().get(0);
        ULog.d(TAG, "Matching Id: " + matching.getId());
    }
}
