package it.near.sdk.Recipes.Models;


import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.MorpheusNear.Annotations.Relationship;
import it.near.sdk.MorpheusNear.Resource;

/**
 * @author cattaneostefano
 */
public class Recipe extends Resource {

    @SerializedName("name")
    String name;
    @SerializedName("notification")
    HashMap<String, Object> notification;
    @SerializedName("labels")
    HashMap<String, Object> labels;
    @SerializedName("pulse_plugin_id")
    String pulse_plugin_id;
    @Relationship("pulse_bundle")
    PulseBundle pulse_bundle;
    @Relationship("pulse_action")
    PulseAction pulse_action;
    @SerializedName("reaction_plugin_id")
    String reaction_plugin_id;
    @Relationship("reaction_bundle")
    ReactionBundle reaction_bundle;
    @Relationship("reaction_action")
    ReactionAction reaction_action;
    /*@SerializedName("operation_plugin_id")
    String operation_plugin_id;
    @SerializedName("operation_bundle_id")
    String operation_bundle_id;*/
    /*@Relationship("operation_action")
    OperationAction operation_action;*/
    private static final String ONLINE = "online";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getNotification() {
        return notification;
    }

    public void setNotification(HashMap<String, Object> notification) {
        this.notification = notification;
    }

    public HashMap<String, Object> getLabels() {
        return labels;
    }

    public boolean isEvaluatedOnline(){
        if (!labels.containsKey(ONLINE)){
            return false;
        } else {
            return labels.get(ONLINE).equals(true);
        }
    }

    public void setLabels(HashMap<String, Object> labels) {
        this.labels = labels;
    }

    public String getPulse_plugin_id() {
        return pulse_plugin_id;
    }

    public void setPulse_plugin_id(String pulse_plugin_id) {
        this.pulse_plugin_id = pulse_plugin_id;
    }

    public PulseBundle getPulse_bundle() {
        return pulse_bundle;
    }

    public void setPulse_bundle(PulseBundle pulse_bundle) {
        this.pulse_bundle = pulse_bundle;
    }

    /*public String getOperation_plugin_id() {
        return operation_plugin_id;
    }

    public void setOperation_plugin_id(String operation_plugin_id) {
        this.operation_plugin_id = operation_plugin_id;
    }

    public String getOperation_bundle_id() {
        return operation_bundle_id;
    }

    public void setOperation_bundle_id(String operation_bundle_id) {
        this.operation_bundle_id = operation_bundle_id;
    }*/

    public String getReaction_plugin_id() {
        return reaction_plugin_id;
    }

    public void setReaction_plugin_id(String reaction_plugin_id) {
        this.reaction_plugin_id = reaction_plugin_id;
    }

    public ReactionBundle getReaction_bundle() {
        return reaction_bundle;
    }

    public void setReaction_bundle(ReactionBundle reaction_bundle) {
        this.reaction_bundle = reaction_bundle;
    }

    public PulseAction getPulse_action() {
        return pulse_action;
    }

    public void setPulse_action(PulseAction pulse_action) {
        this.pulse_action = pulse_action;
    }

    /*public OperationAction getOperation_action() {
        return operation_action;
    }

    public void setOperation_action(OperationAction operation_action) {
        this.operation_action = operation_action;
    }*/

    public ReactionAction getReaction_action() {
        return reaction_action;
    }

    public void setReaction_action(ReactionAction reaction_action) {
        this.reaction_action = reaction_action;
    }

    public String getNotificationTitle() {
        if (getNotification().containsKey("title")){
            return getNotification().get("title").toString();
        }
        return null;
    }

    public String getNotificationBody() {
        if (getNotification().containsKey("body")){
            return getNotification().get("body").toString();
        }
        return null;
    }
}
