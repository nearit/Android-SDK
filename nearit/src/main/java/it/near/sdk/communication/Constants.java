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

        String near_version_header_key = "X-Near-Version";

        String acceptLanguage = "Accept-Language";
    }

    // -------------------- PATHS --------------------------

    /**
     * Url strings
     */
    public interface API {

        String INCLUDE_PARAMETER = "include";

        // ---------- Recipes paths ----------
        String RECIPES_PATH = "recipes";

        String PLUGINS_ROOT = "plugins";
        String INSTALLATIONS_PATH = "installations";
    }
}
