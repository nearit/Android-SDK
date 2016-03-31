package it.near.sdk.Realm.wrapper;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.MorpheusNear.Resource;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import it.near.sdk.Realm.MatchingRealm;


/**
 * Created by alessandrocolleoni on 22/03/16.
 */
public class RealmWrapper {

    private static RealmWrapper mInstance = null;

    private static final String TAG = "RealmDB";

    private Context mContext = null;
    private RealmConfiguration realmConfiguration;
    private Realm realm;
    private RealmObjectFactory realmObjectFactory;

    private RealmWrapper(Context mContext) {
        this.mContext = mContext;
        realmConfiguration = getRealmConfiguration();
        realm = getRealm();
        realmObjectFactory = new RealmObjectFactory();
    }

    public static RealmWrapper getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new RealmWrapper(mContext);
        }
        return mInstance;
    }

    /**
     * get a RealmConfiguration
     *
     * @return
     */
    public RealmConfiguration getRealmConfiguration() {
        return realmConfiguration != null ?
                realmConfiguration :
                new RealmConfiguration.Builder(mContext).build();
    }

    /**
     * get the Realm
     *
     * @return
     */
    public Realm getRealm() {
        return realm != null ?
                realm :
                Realm.getInstance(getRealmConfiguration());
    }

    /**
     * Save a Resource into the Realm
     * Takes a {@link Resource} as argument, retrieve its ResourceObject model
     * (done by the {@link RealmObjectFactory}) and save it into the Realm
     *
     * @param resource
     */
    public <T extends Resource> void save(T resource) {

        RealmObject realmObject = realmObjectFactory.getRealmObject(resource);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();

    }

    /**
     *
     * Save a list of Resrouce into the Realm.
     * Takes a list of {@link Resource} as argument and save it to the Realm, after the conversion
     * to a {@link io.realm.RealmList}
     *
     * @param resourceList
     * @param <T>
     */
    public <T extends Resource> void saveList(List<T> resourceList) {

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(RealmListConverter.convertToRealmList(resourceList));
        realm.commitTransaction();

        /*for (T resource : resourceList) {
            save(resource);
        }*/

    }

    /**
     * Delete a resource
     * Takes the {@link Resource} that has to been deleted from the device.
     * This resource will be used to retrieve the {@link RealmObject} used by Realm
     *
     * @param resource
     * @param <T>
     */
    public <T extends Resource> void delete(T resource) {

        RealmObject toDelete = getRealmObject(resource, resource.getId());

        realm.beginTransaction();
        toDelete.removeFromRealm();
        realm.commitTransaction();
    }

    /**
     * Delete a list of resources.
     * Cycle through the list and call the {@link #delete(Resource)} for every resource
     * @param resourceList
     * @param <T>
     */
    public <T extends Resource> void deleteList(List<T> resourceList) {

        for (T resource : resourceList) {
            delete(resource);
        }

    }

    /**
     * Get a single record from Realm and convert it to a Model.
     *
     * @param resource
     * @param id
     * @param <T>
     * @return
     */
    public <T extends Resource> T getOneById(T resource, String id) {
        RealmObject result = getRealmObject(resource, id);
        return realmObjectFactory.getResource(result);
    }

    /**
     * method used to retrieve the realm object
     *
     * @param resource
     * @param id
     * @param <T>
     * @return
     */
    private <T extends RealmObject> T getRealmObject(Resource resource, String id) {
        return (T) realm.where(realmObjectFactory.getRealmObject(resource).getClass()).equalTo("id", id).findFirst();
    }

    /**
     * Retrieve all the {@link RealmObject} of a specific resource class.
     * All those objects will be converted n a list of Models.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends Resource> List<T> getAll(Class clazz) {

        T resource = null;

        try {
            // instantiate an empty resource, used to retrieve the correct RealmObject
            resource = (T) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        List<T> convertedList = new ArrayList<T>();
        RealmResults<MatchingRealm> realmResults = realm.where(MatchingRealm.class).findAll();

        for (RealmObject ro : realmResults) {
            convertedList.add((T) realmObjectFactory.getResource(ro));
        }

        return convertedList;

    }

}
