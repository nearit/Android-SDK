package it.near.sdk.reactions.feedback;

import android.content.Context;

import org.json.JSONException;

import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.reactions.Event;
import it.near.sdk.utils.NearJsonAPIUtils;


/**
 * Created by cattaneostefano on 11/10/2016.
 */

public class FeedbackEvent extends Event {
    public static final String PLUGIN_NAME = "FeedbackEvent";
    private static final String RATING = "rating";
    private static final String RECIPE_ID = "recipe_id";
    private static final String COMMENT = "comment";
    private static final String PROFILE_ID = "profile_id";
    private static final String RES_TYPE = "answers";

    String feedbackId;
    int rating = -1;
    String comment;
    String recipeId;

    public FeedbackEvent(String feedbackId, int rating, String comment, String recipeId) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.comment = comment;
        this.recipeId = recipeId;
    }

    public FeedbackEvent(Feedback feedback, int rating, String comment){
        this.feedbackId = feedback.getId();
        this.rating = rating;
        this.comment = comment;
        this.recipeId = feedback.getRecipeId();
    }


    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    @Override
    public String getPlugin() {
        return PLUGIN_NAME;
    }

    public String toJsonAPI(GlobalConfig config) throws JSONException {
        String profileId = config.getProfileId();
        HashMap<String, Object> attributeMap = new HashMap<>();
        if (profileId == null ||
                rating == -1 ||
                recipeId == null ||
                feedbackId == null
                ) {
            throw new JSONException("missing data");
        }
        attributeMap.put(RATING, rating);
        attributeMap.put(COMMENT, comment);
        attributeMap.put(PROFILE_ID, profileId);
        attributeMap.put(RECIPE_ID, recipeId);
        return NearJsonAPIUtils.toJsonAPI(RES_TYPE, attributeMap);
    }
}
