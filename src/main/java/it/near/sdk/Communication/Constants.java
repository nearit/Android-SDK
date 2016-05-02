package it.near.sdk.Communication;

/**
 * Contains constants for HTTP calls
 * @author cattaneostefano
 */
public class Constants {

    /**
     * Headers constants
     */
    public interface Headers {
        /** Authorization field name */
        String accessToken = "Authorization";
        /** Content-type field name */
        String contentType = "Content-Type";
        /** Accept field name */
        String accept = "Accept";

        /** Content-type value */
        String jsonApiHeader = "application/vnd.api+json";
    }

    // -------------------- PATHS --------------------------

    /** Url strings */
    public interface API{

        // ---------- Recipes paths ----------
        String RECIPES_PATH = "recipes";
        String RECIPES_PATH_WITH_FLAVORS = RECIPES_PATH + "?include=pulse_flavor,operation_flavor,reaction_flavor";

        String PLUGINGS_ROOT = "plugins";
        String INSTALLATIONS_PATH = "installations";

        /** Plugin specific url strings */
        interface PLUGINS {
            // ---------- beacon forest ----------
            String BEACON_FOREST_PATH =         PLUGINGS_ROOT + "/beacon-forest";
            String BEACON_FOREST_BEACONS =      BEACON_FOREST_PATH + "/beacons";
            String BEACON_FOREST_TRACKINGS =    BEACON_FOREST_PATH + "/trackings";

            // ---------- simple notification plugin ----------
            String SIMPLE_NOTIFICATION_PATH = PLUGINGS_ROOT + "/simple-notification";
            String SIMPLE_NOTIFICATION_LIST = SIMPLE_NOTIFICATION_PATH + "/notifications";

            // ---------- content notification plugin ----------
            String CONTENT_NOTIFICATION =   PLUGINGS_ROOT + "/content-notification";
            String CONTENT_NOTIFICATION_LIST = CONTENT_NOTIFICATION + "/notifications";
            String CONTENT_NOTIFICATION_LIST_WITH_IMAGES = CONTENT_NOTIFICATION_LIST + "?include=images";

            // ---------- poll notification plugin ----------
            String POLL_NOTIFICATION =      PLUGINGS_ROOT + "/poll-notification";
            String POLL_NOTIFICATION_LIST = POLL_NOTIFICATION + "/notifications";
        }


    }


}
