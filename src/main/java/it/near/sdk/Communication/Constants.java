package it.near.sdk.Communication;

/**
 * Created by cattaneostefano on 16/03/16.
 */
public class Constants {

    public interface Headers {
        String accessToken = "Authorization";
        String contentType = "Content-Type";
        String accept = "Accept";

        String jsonApiHeader = "application/vnd.api+json";
    }

    // -------------------- PATHS --------------------------
    // might put them in resources
    public interface API{
        String beacons = "detectors/beacons";
        String image = "media/images";
        // todo add track path
        String recipes = "recipes";
        String recipes_include_flavors = recipes + "?include=pulse_flavor,operation_flavor,reaction_flavor";
        String plugins = "plugins";
        String installations = "installations";

        interface PLUGINS {
            String beacon_forest =          plugins + "/beacon-forest";
            String simple_notification =    plugins + "/simple-notification";
            String content_notification =   plugins + "/content-notification";
            String poll_notification =      plugins + "/poll-notification";
        }


    }


}
