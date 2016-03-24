package it.near.sdk.Realm.wrapper;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import at.rags.morpheus.Resource;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import it.near.sdk.Models.Matching;
import it.near.sdk.Realm.MatchingRealm;
import it.near.sdk.Utils.ULog;


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
     * Save an object into the Realm
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

    public <T extends Resource> void saveList(List<T> resourceList) {

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(RealmListConverter.convertToRealmList(resourceList));
        realm.commitTransaction();

        /*for (T resource : resourceList) {
            save(resource);
        }*/

    }

    public <T extends Resource> void deleteList(List<T> resourceList) {

        for (T resource : resourceList) {
            delete(resource);
        }

    }

    /**
     * Delete a Realm object
     * Takes the {@link Resource} that has to been deleted locally. This will be used to retrieve
     * the {@link RealmObject} used by Realm
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

        RealmObject realmObject = realmObjectFactory.getRealmObject(resource);
        List<T> convertedList = new ArrayList<T>();
        ULog.d("TEST", realmObject.getClass().toString());

        RealmResults<MatchingRealm> r = realm.where(MatchingRealm.class).findAll();

        for (RealmObject ro : r) {
            convertedList.add((T) realmObjectFactory.getResource(ro));
        }

        return convertedList;

    }

}
