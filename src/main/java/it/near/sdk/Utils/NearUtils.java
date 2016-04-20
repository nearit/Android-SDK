package it.near.sdk.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

}
