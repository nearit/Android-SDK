package it.near.sdk.Realm.wrapper;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.MorpheusNear.Resource;
import io.realm.RealmList;
import io.realm.RealmObject;
import it.near.sdk.Realm.RealmString;

/**
 * Created by alessandrocolleoni on 23/03/16.
 *
 * This class will be used as a converter between Resources or other objects to realm objects
 * and vice versa.
 *
 */
public class RealmListConverter {

    /**
     *
     * This method takes a {@link RealmList} made by {@link RealmString}.
     * This will be converted to a List of Strings
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

    /**
     *
     * This method takes a List of resources and will be converted to a List of {@link RealmObject}
     *
     * @param list to convert
     * @return converted list
     */
    public static <T extends RealmObject> List<T> convertToRealmList(List<?> list) {

        RealmList<T> convertedList = new RealmList<T>();
        RealmObjectFactory realmObjectFactory = new RealmObjectFactory();

        for (Resource resource : (List<Resource>) list) {
            convertedList.add((T) realmObjectFactory.getRealmObject(resource));
        }

        return convertedList;
    }

}
