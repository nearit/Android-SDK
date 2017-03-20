package it.near.sdk.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.morpheusnear.JsonApiObject;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.morpheusnear.Resource;

/**
 * Serializing utils mothods for building json api coded strings.
 * Created by cattaneostefano on 18/10/2016.
 */

public class NearJsonAPIUtils {
    private static final String TAG = "NearJsonAPIUtils";

    /**
     * Turns an hashmap of values to a jsonapi resource string.
     * @param type the type of the jsonapi resource.
     * @param map the attribute map.
     * @return codified string.
     * @throws JSONException
     */
    public static String toJsonAPI(String type, HashMap<String, Object> map) throws JSONException{
        return toJsonAPI(type, null, map);
    }

    /**
     * Turns a list of hashmaps into a json api array of resources of the same type.
     * @param type the type of the jsonapi resource.
     * @param maps the maps of attributes.
     * @return codified string.
     * @throws JSONException
     */
    public static String toJsonAPI(String type, List<HashMap<String, Object>> maps) throws JSONException{
        JSONArray array = new JSONArray();
        for (HashMap<String, Object> map : maps) {
            array.put(getResObj(type, null, map));
        }
        JSONObject outerObj = new JSONObject();
        outerObj.put("data", array);
        return outerObj.toString();
    }

    /**
     * Turns an hasmap of values to a jsonapi resource string. Also sets the id.
     * @param type the type of the jsonapi resource.
     * @param id id of the resource.
     * @param map values map.
     * @return codified string.
     * @throws JSONException
     */
    public static String toJsonAPI(String type, String id, HashMap<String, Object> map) throws JSONException {
        JSONObject dataObject = getResObj(type, id, map);
        JSONObject outerObj = new JSONObject();
        outerObj.put("data", dataObject);
        return outerObj.toString();
    }

    /**
     * Build the data object of the jsonapi resource.
     * @param type
     * @param id
     * @param map
     * @return
     * @throws JSONException
     */
    private static JSONObject getResObj(String type, String id, HashMap<String, Object> map) throws JSONException {
        JSONObject attributesObj = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet() ){
            if (entry.getValue() instanceof HashMap){
                attributesObj.put(entry.getKey(), new JSONObject((Map) entry.getValue()));
            } else {
                attributesObj.put(entry.getKey(), entry.getValue()!=null ? entry.getValue() : JSONObject.NULL);
            }
        }

        JSONObject dataObject = new JSONObject();
        if (id != null){
            dataObject.put("id", id);
        }
        dataObject.put("type", type);
        dataObject.put("attributes", attributesObj);
        return dataObject;
    }

    /**
     * Parse a list.
     * @param morpheus the morpheus object. Its serializer must have been set to decode the Class of the objects of the list.
     * @param json json to parse.
     * @param clazz class of the list object.
     * @param <T> generic type.
     * @return list of objects.
     */
    public static <T> List<T> parseList(Morpheus morpheus, JSONObject json, Class<T> clazz) {
        JsonApiObject jsonApiObject = null;
        try {
            jsonApiObject = morpheus.parse(json);
        } catch (Exception e) {
            Log.d(TAG, "Parsing error");
        }

        List<T> returnList = new ArrayList<T>();
        if (jsonApiObject.getResources() == null) return returnList;

        for (Resource r : jsonApiObject.getResources()){
            returnList.add((T) r);
        }
        return returnList;
    }

    /**
     * Parse an object.
     * @param morpheus the morpheus object. Its serializer must have been set to decode the Class of the objects of the list.
     * @param json json to parse.
     * @param clazz class of the object.
     * @param <T> generic type.
     * @return casted object.
     */
    public static <T> T parseElement(Morpheus morpheus, JSONObject json, Class<T> clazz){
        JsonApiObject jsonApiObject = null;
        try {
            jsonApiObject = morpheus.parse(json);
        } catch (Exception e) {
            Log.d(TAG, "Parsing error");
        }
        return (T) jsonApiObject.getResource();
    }
}
