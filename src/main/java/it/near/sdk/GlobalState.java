package it.near.sdk;

import android.content.Context;

import it.near.sdk.Beacons.Ranging.BeaconDynamicRadar;
import it.near.sdk.Beacons.Ranging.NearRangeNotifier;
import it.near.sdk.Push.PushManager;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.TraceNotifier;

/**
 * Class with global instances. Used internally.
 *
 * @author cattaneostefano
 */
public class GlobalState {
    private static final String TAG = "GlobalState";

    private static GlobalState mInstance = null;
    // private final RequestQueue requestQueue;

    private Context mContext;

    private TraceNotifier traceNotifier;
    private BeaconDynamicRadar beaconDynamicRadar;
    private NearNotifier nearNotifier;
    private RecipesManager recipesManager;
    private PushManager pushManager;


    public GlobalState(Context mContext) {
        this.mContext = mContext;
        /*requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.start();*/
    }

    public static GlobalState getInstance(Context context){
        if(mInstance == null)
        {
            mInstance = new GlobalState(context);
        }
        return mInstance;
    }

    public Context getmContext() {
        return mContext;
    }

/*
    public RequestQueue getRequestQueue(){ return requestQueue; }
*/


    public TraceNotifier getTraceNotifier() {
        return traceNotifier;
    }

    public void setTraceNotifier(TraceNotifier traceNotifier) {
        this.traceNotifier = traceNotifier;
    }

    public BeaconDynamicRadar getBeaconDynamicRadar() {
        return beaconDynamicRadar;
    }

    public void setBeaconDynamicRadar(BeaconDynamicRadar beaconDynamicRadar) {
        this.beaconDynamicRadar = beaconDynamicRadar;
    }

    public RecipesManager getRecipesManager() {
        return recipesManager;
    }

    public void setRecipesManager(RecipesManager recipesManager) {
        this.recipesManager = recipesManager;
    }

    public PushManager getPushManager() {
        return pushManager;
    }

    public void setPushManager(PushManager pushManager) {
        this.pushManager = pushManager;
    }

    public void setNearNotifier(NearNotifier nearNotifier) {
        this.nearNotifier = nearNotifier;
    }

    public NearNotifier getNearNotifier() {
        return nearNotifier;
    }
}
