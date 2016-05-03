package it.near.sdk.Utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.MorpheusNear.JSONAPIObject;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Near utilities
 * @author cattaneostefano
 */
public class NearUtils {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Parse a list
     * @param morpheus the morpheus object. Its serializer must have been set to decode the Class of the objects of the list.
     * @param json json to parse
     * @param clazz class of the list object
     * @param <T> generic type
     * @return list of objects
     */
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

    /**
     * Turns an hashmap of values to a jsonapi resource string.
     * @param type the type of the jsonapi resource.
     * @param map values map.
     * @return codified string.
     * @throws JSONException
     */
    public static String toJsonAPI(String type, HashMap<String, Object> map) throws JSONException{
        return toJsonAPI(type, null, map);
    }

    /**
     * Turns an hasmap of values to a jsonapi resource string. ALso sets the id.
     * @param type the type of the jsonapi resource.
     * @param id id of the resource.
     * @param map values map.
     * @return codified string.
     * @throws JSONException
     */
    public static String toJsonAPI(String type, String id, HashMap<String, Object> map) throws JSONException {
        JSONObject attributesObj = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet() ){
            attributesObj.put(entry.getKey(), entry.getValue());
        }

        JSONObject dataObject = new JSONObject();
        if (id != null){
            dataObject.put("id", id);
        }
        dataObject.put("type", type);
        dataObject.put("attributes", attributesObj);

        JSONObject outerObj = new JSONObject();
        outerObj.put("data", dataObject);

        return outerObj.toString();
    }

    /**
     * Decode base 64 string
     * @param encoded encoded string
     * @return decoded string
     */
    public static String decodeString(String encoded) {
        byte[] dataDec = Base64.decode(encoded, Base64.DEFAULT);
        String decodedString = "";
        try {
            decodedString = new String(dataDec, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedString;
    }


    /**
     * Compute app Id from the Apptoken (apikey)
     * @param apiKey token
     * @return the App Id as defined in our servers
     */
    public static String fetchAppIdFrom(String apiKey) {
        String secondSegment = substringBetween(apiKey, ".",".");
        String decodedAK = decodeString(secondSegment);
        String appId = "";
        try {
            JSONObject jwt = new JSONObject(decodedAK);
            JSONObject account = jwt.getJSONObject("data").getJSONObject("account");
            appId = account.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return appId;
    }

    /**
     * String utility method
     * @param str initial string
     * @param open
     * @param close
     * @return
     */
    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *
     * @param context the app context
     * @return whether play services are available
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }
}