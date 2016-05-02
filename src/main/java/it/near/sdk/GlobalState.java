package it.near.sdk;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import it.near.sdk.Beacons.Ranging.BeaconDynamicRadar;
import it.near.sdk.Beacons.Monitoring.NearMonitorNotifier;
import it.near.sdk.Beacons.Ranging.NearRangeNotifier;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Utils.TraceNotifier;

/**
 * @author cattaneostefano
 */
public class GlobalState {
    private static final String TAG = "GlobalState";

    private static GlobalState mInstance = null;
    private final RequestQueue requestQueue;

    private Context mContext;

    private NearRangeNotifier nearRangeNotifier;
    private TraceNotifier traceNotifier;
    private BeaconDynamicRadar beaconDynamicRadar;
    private NearNotifier nearNotifier;
    private NearMonitorNotifier nearMonitorNotifier;


    public GlobalState(Context mContext) {
        this.mContext = mContext;
        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.start();
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

    public RequestQueue getRequestQueue(){ return requestQueue; }

    public NearRangeNotifier getNearRangeNotifier() {
        return nearRangeNotifier;
    }

    public void setNearRangeNotifier(NearRangeNotifier nearRangeNotifier) {
        this.nearRangeNotifier = nearRangeNotifier;
    }

    public NearMonitorNotifier getNearMonitorNotifier() {
        return nearMonitorNotifier;
    }

    public void setNearMonitorNotifier(NearMonitorNotifier nearMonitorNotifier) {
        this.nearMonitorNotifier = nearMonitorNotifier;
    }

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

    public void setNearNotifier(NearNotifier nearNotifier) {
        this.nearNotifier = nearNotifier;
    }

    public NearNotifier getNearNotifier() {
        return nearNotifier;
    }
}
