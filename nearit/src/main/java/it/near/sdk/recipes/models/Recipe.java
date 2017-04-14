package it.near.sdk.recipes.models;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;


import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.near.sdk.communication.NearNetworkUtil;
import it.near.sdk.GlobalConfig;
import it.near.sdk.morpheusnear.annotations.Relationship;
import it.near.sdk.morpheusnear.Resource;
import it.near.sdk.utils.NearItIntentConstants;
import it.near.sdk.utils.NearJsonAPIUtils;

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



    /**
     * Builds the tracking send request body.
     * @param globalConfig the app global config.
     * @param recipeId the recipe identifier.
     * @param trackingEvent the tracking event string.
     * @return the http body string.
     * @throws JSONException
     */
    public static String buildTrackingBody(GlobalConfig globalConfig, String recipeId, String trackingEvent) throws JSONException {
        String profileId = globalConfig.getProfileId();
        String appId = globalConfig.getAppId();
        String installationId = globalConfig.getInstallationId();
        if (recipeId == null ||
                profileId == null ||
                installationId == null ){
            throw new JSONException("missing data");
        }
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date now = new Date(System.currentTimeMillis());
        String formattedDate = sdf.format(now);
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("profile_id", profileId);
        attributes.put("installation_id", installationId);
        attributes.put("app_id", appId);
        attributes.put("recipe_id", recipeId);
        attributes.put("event", trackingEvent);
        attributes.put("tracked_at", formattedDate);
        return NearJsonAPIUtils.toJsonAPI("trackings", attributes);
    }

    public boolean isForegroundRecipe() {
        return getPulse_action().isForeground();
    }

    /**
     * Check if the recipe is valid according to the scheduling information.
     * @return the validity of the recipe.
     */
    public boolean isScheduledNow(Calendar now){
        return scheduling == null ||
                ( isDateValid(now) &&
                isTimetableValid(now) &&
                isDaysValid(now) );
    }

    /**
     * Check if the date range is valid.
     * @return if the date range is respected.
     */
    private boolean isDateValid(Calendar now){
        Map<String, Object> date = (Map<String, Object>) scheduling.get(DATE_SCHEDULING);
        if (date == null) return true;
        String fromDateString = (String) date.get("from");
        String toDateString = (String) date.get("to");
        boolean valid = true;
        try {
            // do not move the dateformatter to be an instance variable, it messes the parsing
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            if (fromDateString != null) {
                Date fromDate = dateFormatter.parse(fromDateString);
                Calendar fromCalendarDate = Calendar.getInstance();
                fromCalendarDate.setTimeInMillis(fromDate.getTime());
                valid &= fromCalendarDate.before(now) || fromCalendarDate.equals(now);
            }
            if (toDateString != null) {
                Date toDate = dateFormatter.parse(toDateString);
                Calendar toCalendarDate = Calendar.getInstance();
                toCalendarDate.setTimeInMillis(toDate.getTime());
                valid &= toCalendarDate.after(now) || toCalendarDate.equals(now);
            }
        } catch (ParseException e) {
            return false;
        }
        return valid;
    }

    /**
     * Check if the time range is valid.
     * @return if the time range is respected.
     */
    private boolean isTimetableValid(Calendar now) {
        Map<String, Object> timetable = (Map<String, Object>) scheduling.get(TIMETABLE_SCHEDULING);
        if (timetable == null) return true;
        String fromHour = (String) timetable.get("from");
        String toHour = (String) timetable.get("to");
        boolean valid = true;
        try {
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
            if (fromHour != null) {
                Date fromHourDate = timeFormatter.parse(fromHour);
                Calendar fromHourCalendar = Calendar.getInstance();
                fromHourCalendar.setTime(fromHourDate);
                valid &= fromHourCalendar.before(now) || fromHourCalendar.equals(now);
            }
            if (toHour != null){
                Date toHourDate = timeFormatter.parse(toHour);
                Calendar toHourCalendar = Calendar.getInstance();
                toHourCalendar.setTime(toHourDate);
                valid &= toHourCalendar.after(now) || toHourCalendar.equals(now);
            }
        } catch (ParseException e) {
            return false;
        }
        return valid;
    }

    /**
     * Check if the days selection is valid.
     * @return if the days selection is respected.
     */
    private boolean isDaysValid(Calendar now) {
        List<String> days = (List<String>) scheduling.get(DAYS_SCHEDULING);
        if (days == null) return true;
        String todaysDate = getTodaysDate(now);

        return days.contains(todaysDate);
    }

    /**
     * Get today's day of week.
     * @return the day of week in "EE" format e.g. Sat.
     */
    private String getTodaysDate(Calendar now) {
        Date date = now.getTime();
        // 3 letter name form of the day
        return new SimpleDateFormat("EE", Locale.ENGLISH).format(date.getTime());

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
