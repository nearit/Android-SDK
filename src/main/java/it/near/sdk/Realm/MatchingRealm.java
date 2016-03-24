package it.near.sdk.Realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import it.near.sdk.Models.Matching;
import it.near.sdk.Utils.ULog;

/**
 * Created by alessandrocolleoni on 23/03/16.
 *
 * Create a MatchingRealm object, used to persist on Realm (device persistence)
 *
 */
public class MatchingRealm extends RealmObject {

    @PrimaryKey
    String id;

    String content_id;

    String app_id;

    String beacon_id;

    Boolean active;

    public MatchingRealm() {}

    /**
     * Initialize a MatchingRealm with data's from a matching model
     * @param matching
     */
    public MatchingRealm(Matching matching) {

        id = matching.getId();
        content_id = matching.getContent_id();
        app_id = matching.getApp_id();
        beacon_id = matching.getBeacon_id();
        active = matching.getActive();
    }

    /**
     * Convert the MatchingRealm to a Matching model
     * @return converted matching
     */
    public Matching convertToModel() {

        Matching matching = new Matching();

        matching.setId(id);
        matching.setContent_id(content_id);
        matching.setApp_id(app_id);
        matching.setBeacon_id(beacon_id);
        matching.setActive(active);

        return matching;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent_id() {
        return content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getBeacon_id() {
        return beacon_id;
    }

    public void setBeacon_id(String beacon_id) {
        this.beacon_id = beacon_id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
