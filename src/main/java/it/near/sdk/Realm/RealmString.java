package it.near.sdk.Realm;

import io.realm.RealmObject;

/**
 * Created by alessandrocolleoni on 23/03/16.
 *
 * Wrapper for String class. Needed by RealmList
 *
 */
public class RealmString extends RealmObject{

    private String val;

    public RealmString() {}

    public RealmString(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
