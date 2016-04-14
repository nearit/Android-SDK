package it.near.sdk.Reactions;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.MorpheusNear.Deserializer;
import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public abstract class Reaction {
    public List<String> supportedFlavors = null;
    protected RequestQueue requestQueue;
    protected Context mContext;
    protected Morpheus morpheus;

    public Reaction(Context mContext) {
        this.mContext = mContext;
        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.start();
    }

    public void setUpMorpheus(){
        HashMap<String, Class> classes = getModelHashMap();
        morpheus = new Morpheus();
        for (Map.Entry<String, Class> entry : classes.entrySet()){
            morpheus.getFactory().getDeserializer().registerResourceClass(entry.getKey(), entry.getValue());
        }
    }


    public List<String> getSupportedFlavors() {
        if (supportedFlavors == null){
            buildFlavors();
        }
        return supportedFlavors;
    }

    public void handleReaction(String reaction_ingredient, String reaction_flavor, String reaction_slice){
        if (!getIngredientName().equals(reaction_ingredient)){
            return;
        }
        handleReaction(reaction_flavor, reaction_slice);
    }

    protected <T> List<T> parseList(JSONObject json, Class<T> clazz) {
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

    public abstract void buildFlavors();
    public abstract void refreshConfig();
    public abstract String getIngredientName();
    protected abstract HashMap<String,Class> getModelHashMap();
    protected abstract void handleReaction(String reaction_flavor, String reaction_slice);

}
