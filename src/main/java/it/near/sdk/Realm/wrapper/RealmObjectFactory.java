package it.near.sdk.Realm.wrapper;

import it.near.sdk.MorpheusNear.Resource;
import io.realm.RealmObject;
import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;
import it.near.sdk.Realm.BeaconRealm;
import it.near.sdk.Realm.ContentRealm;
import it.near.sdk.Realm.MatchingRealm;

/**
 * Created by alessandrocolleoni on 23/03/16.
 * Factory used to get an instance of a {@link RealmObject} from a {@link Resource} or vice versa.
 */
public class RealmObjectFactory {


    public <T extends RealmObject> T getRealmObject(Resource object) {

        if (object instanceof NearBeacon) {
            return (T) new BeaconRealm((NearBeacon) object);
        } else if (object instanceof Matching) {
            return (T) new MatchingRealm((Matching) object);
        } else if (object instanceof Content) {
            return (T) new ContentRealm((Content) object);
        }

        return null;

    }

    public <T extends Resource> T getResource(RealmObject object) {

        if(object instanceof BeaconRealm) {
            return (T) ((BeaconRealm) object).convertToModel();
        } else if(object instanceof MatchingRealm) {
            return (T) ((MatchingRealm) object).convertToModel();
        } else if(object instanceof ContentRealm) {
            return (T) ((ContentRealm) object).convertToModel();
        }

        return null;

    }

}
