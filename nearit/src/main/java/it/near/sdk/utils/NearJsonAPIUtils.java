package it.near.sdk.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.JsonApiObject;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.morpheusnear.Resource;

import static it.near.sdk.utils.NearUtils.safe;

/**
 * Serializing utils methods for building json api coded strings.
 */

public class NearJsonAPIUtils {
    private static final String TAG = "NearJsonAPIUtils";
    private static final String KEY_DATA_ELEMENT = "data";
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ATTRIBUTES = "attributes";

    /**
     * Turns an hashmap of values to a jsonapi resource string.
     *
     * @param type the type of the jsonapi resource.
     * @param map  the attribute map.
     * @return codified string.
     * @throws JSONException if map can be transformed into JSONObject
     */
    public static String toJsonAPI(String type, HashMap<String, Object> map) throws JSONException {
        return toJsonAPI(type, null, map);
    }

    /**
     * Turns a list of hashmaps into a json api array of resources of the same type.
     *
     * @param type the type of the jsonapi resource.
     * @param maps the maps of attributes.
     * @return codified string.
     * @throws JSONException if map can be transformed into JSONObject
     */
    public static String toJsonAPI(String type, List<HashMap<String, Object>> maps) throws JSONException {
        JSONArray resources = new JSONArray();
        for (HashMap<String, Object> map : safe(maps)) {
            resources.put(buildResourceObject(type, null, map));
        }
        JSONObject jsonApiObject = new JSONObject();
        jsonApiObject.put(KEY_DATA_ELEMENT, resources);
        return jsonApiObject.toString();
    }

    /**
     * Turns an hashmap of values to a jsonapi resource string. Also sets the id.
     *
     * @param type jsonapi resource type.
     * @param id   id of the resource.
     * @param map  values map.
     * @return codified string.
     * @throws JSONException if map can be transformed into JSONObject
     */
    public static String toJsonAPI(String type, String id, HashMap<String, Object> map) throws JSONException {
        JSONObject resource = buildResourceObject(type, id, map);
        JSONObject jsonApiObject = new JSONObject();
        jsonApiObject.put(KEY_DATA_ELEMENT, resource);
        return jsonApiObject.toString();
    }

    /**
     * Build the data object of the jsonapi resource.
     *
     * @param type jsonapi resource type.
     * @param id   id of the resource.
     * @param map  values map
     * @return JSONObject representation of map object.
     * @throws JSONException if map can be transformed into JSONObject
     */
    private static JSONObject buildResourceObject(String type, String id, HashMap<String, Object> map) throws JSONException {
        if (map == null)
            throw new JSONException("Attribute map can't be null");

        JSONObject attributes = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof HashMap) {
                attributes.put(entry.getKey(), new JSONObject((Map) entry.getValue()));
            } else {
                attributes.put(entry.getKey(), entry.getValue() != null ? entry.getValue() : JSONObject.NULL);
            }
        }

        JSONObject resource = new JSONObject();
        if (id != null) {
            resource.put(KEY_ID, id);
        }
        resource.put(KEY_TYPE, type);
        resource.put(KEY_ATTRIBUTES, attributes);
        return resource;
    }

    /**
     * Parse a list.
     *
     * @param morpheus the morpheus object. Its serializer must have been set to decode the Class of the objects of the list.
     * @param json     json to parse.
     * @param clazz    class of the list object.
     * @param <T>      generic type.
     * @return list of objects.
     */
    public static <T> List<T> parseList(Morpheus morpheus, JSONObject json, Class<T> clazz) {
        JsonApiObject jsonApiObject = null;
        try {
            jsonApiObject = morpheus.parse(json);
        } catch (Exception e) {
            NearLog.d(TAG, "Parsing error");
        }

        List<T> returnList = new ArrayList<T>();
        if (jsonApiObject == null ||
                jsonApiObject.getResources() == null) return returnList;

        for (Resource r : jsonApiObject.getResources()) {
            returnList.add((T) r);
        }
        return returnList;
    }

    /**
     * Parse an object.
     *
     * @param morpheus the morpheus object. Its serializer must have been set to decode the Class of the objects of the list.
     * @param json     json to parse.
     * @param clazz    class of the object.
     * @param <T>      generic type.
     * @return casted object.
     */
    public static <T> T parseElement(Morpheus morpheus, JSONObject json, Class<T> clazz) {
        JsonApiObject jsonApiObject = null;
        try {
            jsonApiObject = morpheus.parse(json);
        } catch (Exception e) {
            NearLog.d(TAG, "Parsing error");
        }
        return (T) jsonApiObject.getResource();
    }
}
