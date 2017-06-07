package it.near.sdk.recipes.models;


import android.content.Intent;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import it.near.sdk.morpheusnear.Resource;
import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.utils.NearItIntentConstants;

/**
 * @author cattaneostefano
 */
public class Recipe extends Resource {

    @SerializedName("name")
    public String name;
    @SerializedName("notification")
    public HashMap<String, Object> notification;
    @SerializedName("labels")
    public HashMap<String, Object> labels;
    @SerializedName("scheduling")
    public HashMap<String, Object> scheduling;
    @SerializedName("cooldown")
    public HashMap<String, Object> cooldown;
    @SerializedName("pulse_plugin_id")
    public String pulse_plugin_id;
    @Relationship("pulse_bundle")
    public PulseBundle pulse_bundle;
    @Relationship("pulse_action")
    public PulseAction pulse_action;
    @SerializedName("reaction_plugin_id")
    public String reaction_plugin_id;
    @Relationship("reaction_bundle")
    public ReactionBundle reaction_bundle;
    @Relationship("reaction_action")
    public ReactionAction reaction_action;

    public static final String NOTIFIED_STATUS = "notified";
    public static final String ENGAGED_STATUS = "engaged";

    private static final String ONLINE = "online";
    public static final String DATE_SCHEDULING = "date";
    public static final String TIMETABLE_SCHEDULING = "timetable";
    public static final String DAYS_SCHEDULING = "days";

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


    public ReactionAction getReaction_action() {
        return reaction_action;
    }

    public void setReaction_action(ReactionAction reaction_action) {
        this.reaction_action = reaction_action;
    }

    public HashMap<String, Object> getCooldown() {
        return cooldown;
    }

    public void setScheduling(HashMap<String, Object> scheduling) {
        this.scheduling = scheduling;
    }

    public void setCooldown(HashMap<String, Object> cooldown) {
        this.cooldown = cooldown;
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

    public boolean isForegroundRecipe() {
        return getPulse_action().isForeground();
    }

    /**
     * Fill the intent with extras regarding the recipe and the parcelable content.
     * @param intent the intent for the background event.
     * @param recipe the recipe causing the intent.
     * @param parcelable the content to be delivered.
     */
    public static void fillIntentExtras(Intent intent, Recipe recipe, Parcelable parcelable) {

        intent.putExtra(NearItIntentConstants.RECIPE_ID, recipe.getId());
        // set notification text
        intent.putExtra(NearItIntentConstants.NOTIF_TITLE, recipe.getNotificationTitle());
        intent.putExtra(NearItIntentConstants.NOTIF_BODY, recipe.getNotificationBody());
        // set contet to show
        intent.putExtra(NearItIntentConstants.CONTENT, parcelable);
        // set the content type so the app can cast the parcelable to correct content
        intent.putExtra(NearItIntentConstants.REACTION_PLUGIN, recipe.getReaction_plugin_id());
        intent.putExtra(NearItIntentConstants.REACTION_ACTION, recipe.getReaction_action().getId());
        // set the pulse info
        intent.putExtra(NearItIntentConstants.PULSE_PLUGIN, recipe.getPulse_plugin_id());
        intent.putExtra(NearItIntentConstants.PULSE_ACTION, recipe.getPulse_action().getId());
        intent.putExtra(NearItIntentConstants.PULSE_BUNDLE, recipe.getPulse_bundle() != null ? recipe.getPulse_bundle().getId() : "");
    }
}
