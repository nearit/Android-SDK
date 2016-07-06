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

        String PLUGINS_ROOT = "plugins";
        String INSTALLATIONS_PATH = "installations";

        /** Plugin specific url strings */
        interface PLUGINS {
            // ---------- simple notification plugin ----------
            String SIMPLE_NOTIFICATION_PATH = PLUGINS_ROOT + "/simple-notification";
            String SIMPLE_NOTIFICATION_LIST = SIMPLE_NOTIFICATION_PATH + "/notifications";

        }


    }


}
