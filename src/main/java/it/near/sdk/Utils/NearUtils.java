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
 * @author cattaneostefano
 */
public class NearUtils {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

    public static String toJsonAPI(String type, HashMap<String, Object> map) throws JSONException{
        return toJsonAPI(type, null, map);
    }

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
