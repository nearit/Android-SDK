package it.near.sdk.Trackings;

/**
 * Created by cattaneostefano on 05/10/2016.
 */

public interface Events {
    public static final String ENTER_PLACE = "enter_place";
    public static final String LEAVE_PLACE = "leave_place";
    public static final String ENTER_REGION = "enter_area";
    public static final String LEAVE_REGION = "leave_area";
    public static final String ENTER_BEACON = "enter_beacon";
    public static final String LEAVE_BEACON = "leave_beacon";
    public static final String RANGE_IMMEDIATE = "ranging.immediate";
    public static final String RANGE_NEAR = "ranging.near";
    public static final String RANGE_FAR = "ranging.far";
}
