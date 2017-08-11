package it.near.sdk.recipes.pulse;

import java.util.List;

import it.near.sdk.trackings.TrackingInfo;

public class TriggerRequest {
    public String plugin_name;
    public String plugin_action;
    public String bundle_id;
    public String plugin_tag_action;
    public List<String> tags;

    public TrackingInfo trackingInfo;
}
