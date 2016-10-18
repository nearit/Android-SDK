package it.near.sdk;

import android.content.Context;

import it.near.sdk.Geopolis.Beacons.Ranging.BeaconDynamicRadar;
import it.near.sdk.Push.PushManager;
import it.near.sdk.Recipes.NearNotifier;
import it.near.sdk.Recipes.RecipesManager;

/**
 * Class with global instances. Used internally.
 *
 * @author cattaneostefano
 */
public class GlobalState {
    private static final String TAG = "GlobalState";

    private static GlobalState mInstance = null;

    private Context mContext;

    private RecipesManager recipesManager;
    private PushManager pushManager;


    public GlobalState(Context mContext) {
        this.mContext = mContext;
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
}
