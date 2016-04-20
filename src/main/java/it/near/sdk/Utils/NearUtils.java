package it.near.sdk.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 20/04/16.
 */
public class NearUtils {

    public static <T> List<T> parseList(Morpheus morpheus, JSONObject json, Class<T> clazz) {
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

    public static String toJsonAPI(String type, HashMap<String, Object> map) throws JSONException {
        JSONObject attributesObj = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet() ){
            attributesObj.put(entry.getKey(), entry.getValue());
        }

        JSONObject dataObject = new JSONObject();
        dataObject.put("type", type);
        dataObject.put("attributes", attributesObj);

        JSONObject outerObj = new JSONObject();
        outerObj.put("data", dataObject);

        return outerObj.toString();
    }

}
