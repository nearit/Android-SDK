package it.near.sdk.geopolis.trackings;

public interface Events {
    String ENTER_PLACE = "enter_place";
    String LEAVE_PLACE = "leave_place";
    String ENTER_REGION = "enter_area";
    String LEAVE_REGION = "leave_area";
    String RANGE_IMMEDIATE = "ranging.immediate";
    String RANGE_NEAR = "ranging.near";
    String RANGE_FAR = "ranging.far";
}
