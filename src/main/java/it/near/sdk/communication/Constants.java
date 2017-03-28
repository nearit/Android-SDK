package it.near.sdk.communication;

/**
 * Contains constants for HTTP calls
 *
 * @author cattaneostefano
 */
public class Constants {

    /**
     * Headers constants
     */
    interface Headers {
        /**
         * Authorization field name
         */
        String accessToken = "Authorization";
        /**
         * Content-type field name
         */
        String contentType = "Content-Type";
        /**
         * Accept field name
         */
        String accept = "Accept";

        /**
         * Content-type value
         */
        String jsonApiHeader = "application/vnd.api+json";
        String version_header_key = "X-API-Version";
    }

    // -------------------- PATHS --------------------------

    /**
     * Url strings
     */
    public interface API {

        // ---------- Recipes paths ----------
        String RECIPES_PATH = "recipes";

        String PLUGINS_ROOT = "plugins";
        String INSTALLATIONS_PATH = "installations";
    }
}
