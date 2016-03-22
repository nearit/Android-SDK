package it.near.sdk.Communication;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.rags.morpheus.Deserializer;
import at.rags.morpheus.JSONAPIObject;
import at.rags.morpheus.Morpheus;
import at.rags.morpheus.Resource;
import it.near.sdk.GlobalState;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Realm.wrapper.RealmWrapper;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItServer {

    private static NearItServer mInstance = null;
    private static final String TAG = "NearItServer";

    private final Configuration configuration;
    private Morpheus morpheus;
    private Context mContext;
    private RequestQueue requestQueue;
    RealmWrapper realmWrapper;

    private NearItServer(Context context) {
        this.mContext = context;
        realmWrapper = RealmWrapper.getInstance(mContext);
        setUpParser();
        configuration = new Configuration();
        GlobalState.getInstance(context).setConfiguration(configuration);
        requestQueue = Volley.newRequestQueue(context);
        requestQueue.start();
    }

    private void setUpParser() {
        morpheus = new Morpheus();
        //register your resources
        Deserializer.registerResourceClass("beacons", NearBeacon.class);
        Deserializer.registerResourceClass("contents", Content.class);
        Deserializer.registerResourceClass("matchings", Matching.class);
    }

    public static NearItServer getInstance(Context context){
        if(mInstance == null) {
            mInstance = new NearItServer(context);

        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void downloadNearConfiguration(){
        String filter = Filter.build().addFilter("active", "true").print();
        requestQueue.add(new CustomJsonRequest(mContext, Constants.API.matchings + filter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ULog.d(TAG, "Matchings downloaded: " + response.toString());
                List<Matching> matchings = parseList(response, Matching.class);

                configuration.setMatchingList(matchings);
                realmWrapper.saveList(matchings);

                downloadBeacons(matchings);
                downloadContents(matchings);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ULog.d(TAG, "error " + error.toString() );
            }
        }));

    }

    private void downloadBeacons(List<Matching> matchings) {
        for (Matching matching : matchings){
            requestQueue.add(new CustomJsonRequest(mContext, Constants.API.beacons + "/" + matching.getBeacon_id(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ULog.d(TAG, "Beacon downloaded: " + response.toString());
                    NearBeacon beacon = parse(response, NearBeacon.class);

                    configuration.addBeacon(beacon);
                    realmWrapper.save(beacon);
                    GlobalState.getInstance(mContext).getAltBeaconWrapper().configureScanner(configuration);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "error " + error.toString() );
                }
            }));
        }
        ULog.d(TAG, "");


    }

    private void downloadContents(List<Matching> matchings) {

        for (Matching matching : matchings) {
            requestQueue.add(new CustomJsonRequest(mContext, Constants.API.contents + "/" + matching.getContent_id(), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    ULog.d(TAG, "Content downloaded" + response.toString());
                    Content content = parse(response, Content.class);
                    configuration.addContent(content);
                    realmWrapper.save(content);
                    GlobalState.getInstance(mContext).setConfiguration(configuration);

                }
            },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "error " + error.toString() );
                }
            }));
        }

    }

    private <T> List<T> parseList(JSONObject json, Class<T> clazz) {
        JSONAPIObject jsonapiObject = null;
        try {
            jsonapiObject = morpheus.parse(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<T> returnList = new ArrayList<T>();

        for (Resource r : jsonapiObject.getResources()){
            returnList.add((T) r);
        }

        return returnList;
    }

    private <T> T parse(JSONObject json, Class<T> clazz){
        JSONAPIObject jsonapiObject = null;
        try {
            jsonapiObject = morpheus.parse(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T)jsonapiObject.getResource();
    }

}
