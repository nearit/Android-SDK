package it.near.sdk.geopolis.trackings;

public interface Events {

    String ENTER_PLACE_ACTION = "enter_place";
    String LEAVE_PLACE_ACTION = "leave_place";
    String ENTER_AREA_ACTION = "enter_area";
    String LEAVE_AREA_ACTION = "leave_area";
    String RANGING_IMMEDIATE_ACTION = "ranging.immediate";
    String RANGING_NEAR_ACTION = "ranging.near";

    String ENTER_TAGS_ACTION = "enter_tags";
    String LEAVE_TAGS_ACTION = "leave_tags";
    String RANGING_TAGS_IMMEDIATE = "ranging_tags.immediate";
    String RANGING_TAGS_NEAR = "ranging_tags.near";

    GeoEvent ENTER_PLACE = new GeoEvent(ENTER_PLACE_ACTION, ENTER_TAGS_ACTION);
    GeoEvent LEAVE_PLACE = new GeoEvent(LEAVE_PLACE_ACTION, LEAVE_TAGS_ACTION);
    GeoEvent ENTER_REGION = new GeoEvent(ENTER_AREA_ACTION, ENTER_TAGS_ACTION);
    GeoEvent LEAVE_REGION = new GeoEvent(LEAVE_AREA_ACTION, LEAVE_TAGS_ACTION);
    GeoEvent RANGE_IMMEDIATE = new GeoEvent(RANGING_IMMEDIATE_ACTION, RANGING_TAGS_IMMEDIATE);
    GeoEvent RANGE_NEAR = new GeoEvent(RANGING_NEAR_ACTION, RANGING_TAGS_NEAR);

    class GeoEvent {
        public final String event;
        public final String fallback;

        GeoEvent(String event, String fallback) {
            this.event = event;
            this.fallback = fallback;
        }
    }
}
