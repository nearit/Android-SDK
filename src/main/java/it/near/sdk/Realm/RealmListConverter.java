package it.near.sdk.Realm;

import java.util.ArrayList;
import java.util.List;

import at.rags.morpheus.Resource;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by alessandrocolleoni on 23/03/16.
 *
 * This class will convert a RealmList to a List
 * Methods for each type will be created.
 *
 */
public class RealmListConverter {

    /**
     *
     * This method takes a RealmList made by RealmString. This will be converted to a List of Strings
     *
     * @param list to convert
     * @return converted list
     */
    public static List<String> convertToStringList(RealmList<RealmString> list) {

        List<String> convertedList = new ArrayList<String>();

        for (RealmString string : list) {
            convertedList.add(string.getVal());
        }

        return convertedList;
    }

}
