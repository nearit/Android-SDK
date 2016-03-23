package it.near.sdk;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Created by alessandrocolleoni on 22/03/16.
 */
public class RealmWrapper {

    private static RealmWrapper mInstance = null;

    private static final String TAG = "RealmDB";

    private Context mContext = null;
    private RealmConfiguration realmConfiguration;
    private Realm realm;

    private RealmWrapper(Context mContext) {
        this.mContext = mContext;
        realmConfiguration = getRealmConfiguration();
        realm = getRealm();
    }

    public static RealmWrapper getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new RealmWrapper(mContext);
        }
        return mInstance;
    }

    public RealmConfiguration getRealmConfiguration() {
        return realmConfiguration != null ?
                realmConfiguration :
                new RealmConfiguration.Builder(mContext).build();
    }

    public Realm getRealm() {
        return realm != null ?
                realm :
                Realm.getInstance(getRealmConfiguration());
    }

}
