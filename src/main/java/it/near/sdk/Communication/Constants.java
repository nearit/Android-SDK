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

    public interface API{
        String matchings = "matchings";
        String beacons = "detectors/beacons";
        String contents = "contents";
        String image = "media/images";
        // todo add track path
        String track = "";
        String recipes = "recipes";
        String plugins = "plugins";

        interface PLUGINS {
            String beacon_forest =          plugins + "/beacon-forest";
            String simple_notification =    plugins + "/simple-notification";
            String content_notification =   plugins + "/content-notification";
            String poll_notification =      plugins + "/poll-notification";
        }


    }


}
