package it.near.sdk.Communication;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Models.ImageSet;
import it.near.sdk.MorpheusNear.Deserializer;
import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.BuildConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.Models.Configuration;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Models.NearBeacon;
import it.near.sdk.Realm.wrapper.RealmWrapper;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItServer {

    private static NearItServer mInstance = null;
    private static final String TAG = "NearItServer";
    private String APP_PACKAGE_NAME;
    private final Configuration configuration;
    private Morpheus morpheus;
    private Context mContext;
    private RequestQueue requestQueue;
    RealmWrapper realmWrapper;

    private NearItServer(Context context) {
        this.mContext = context;
        APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;

        setUpMorpheusParser();
        realmWrapper = RealmWrapper.getInstance(mContext);
        configuration = new Configuration();
        GlobalState.getInstance(context).setConfiguration(configuration);

        // Set-up a request queue for making http requests
        // with Google own volley library
        // https://developer.android.com/training/volley/index.html
        requestQueue = Volley.newRequestQueue(context);
        requestQueue.start();
    }

    public static NearItServer getInstance(Context context){
        if(mInstance == null) {
            mInstance = new NearItServer(context);

        }
        return mInstance;
    }

    /**
     * Set up Morpheus parser. Morpheus parses jsonApi encoded resources
     * https://github.com/xamoom/Morpheus
     * We didn't actually use this library due to its minSdkVersion. We instead imported its code and adapted it.
     */
    private void setUpMorpheusParser() {
        morpheus = new Morpheus();
        //register your resources
        Deserializer.registerResourceClass("beacons", NearBeacon.class);
        Deserializer.registerResourceClass("contents", Content.class);
        Deserializer.registerResourceClass("matchings", Matching.class);
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
        configuration.setBeaconList(new ArrayList<NearBeacon>());
        for (Matching matching : matchings){
            requestQueue.add(new CustomJsonRequest(mContext, Constants.API.beacons + "/" + matching.getBeacon_id(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ULog.d(TAG, "Beacon downloaded: " + response.toString());
                    NearBeacon beacon = parse(response, NearBeacon.class);

                    configuration.addBeacon(beacon);
                    realmWrapper.save(beacon);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "error " + error.toString() );
                }
            }));
        }

    }


    private void downloadContents(List<Matching> matchings) {
        configuration.setContentList(new ArrayList<Content>());
        for (Matching matching : matchings) {
            requestQueue.add(new CustomJsonRequest(mContext, Constants.API.contents + "/" + matching.getContent_id(), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    ULog.d(TAG, "Content downloaded" + response.toString());
                    Content content = parse(response, Content.class);
                    configuration.addContent(content);
                    realmWrapper.save(content);
                    resolveContentLinksAndSave(content);
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

    private void resolveContentLinksAndSave(final Content content) {
        content.setImageSets(new ArrayList<ImageSet>());
        for (final String mediaId : content.getPhotoIds()){
            requestQueue.add(new CustomJsonRequest(mContext, Constants.API.image + "/" + mediaId, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ULog.d(TAG, "Image link downloaded " + response.toString());
                    ImageSet set = new ImageSet();
                    try {
                        JSONObject data = response.getJSONObject("data");
                        JSONObject attributes = data.getJSONObject("attributes");
                        JSONObject image = attributes.getJSONObject("image");
                        String fullLink = image.getString("url");
                        set.setFullSize(fullLink);
                        JSONObject max_1920 = image.getJSONObject("max_1920_jpg");
                        String bigLink = max_1920.getString("url");
                        set.setBigSize(bigLink);
                        JSONObject square_300 = image.getJSONObject("square_300");
                        String smallLink = square_300.getString("url");
                        set.setSmallSize(smallLink);
                        content.getImageSets().add(set);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ULog.d(TAG, "error " + error.toString() );
                }
            }));
        }
    }



    public void sendTrack(String track){
        // todo do something with the body
        String body = track;
        requestQueue.add(new CustomJsonRequest(mContext, Request.Method.POST, Constants.API.track, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }));
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
