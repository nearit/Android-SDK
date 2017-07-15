package it.near.sdk.reactions.feedbackplugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import it.near.sdk.reactions.feedbackplugin.model.Feedback;

import static it.near.sdk.reactions.feedbackplugin.FeedbackEvent.COMMENT;
import static it.near.sdk.reactions.feedbackplugin.FeedbackEvent.PROFILE_ID;
import static it.near.sdk.reactions.feedbackplugin.FeedbackEvent.RATING;
import static it.near.sdk.reactions.feedbackplugin.FeedbackEvent.RECIPE_ID;
import static it.near.sdk.reactions.feedbackplugin.FeedbackEvent.RES_TYPE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class FeedbackEventTest {

    private FeedbackEvent feedbackEvent;

    @Before
    public void setUp() throws Exception {
        String recipeId = "recipeId";
        String feedbackId = "feedbackId";
        Feedback feedback = new Feedback();
        feedback.setId(feedbackId);
        feedback.setRecipeId(recipeId);
        feedbackEvent = new FeedbackEvent(feedback, 0, null);
    }

    @Test
    public void returnsPluginName() {
        assertThat(feedbackEvent.getPlugin(), is(FeedbackEvent.PLUGIN_NAME));
    }

    @Test(expected = JSONException.class)
    public void missingProfileIdShouldNotBuildBody() throws JSONException {
        feedbackEvent.toJsonAPI(null);
    }

    @Test(expected = JSONException.class)
    public void defaultRatingShouldNotBuildBody() throws JSONException {
        feedbackEvent.setRating(-1);
        feedbackEvent.toJsonAPI("profileId");
    }

    @Test(expected = JSONException.class)
    public void missingRecipeIdShouldNotBuildBody() throws JSONException {
        feedbackEvent.setRecipeId(null);
        feedbackEvent.toJsonAPI("profileId");
    }

    @Test(expected = JSONException.class)
    public void missingFeedbackIdShouldNotBuildBody() throws JSONException {
        feedbackEvent.setFeedbackId(null);
        feedbackEvent.toJsonAPI("profileId");
    }

    @Test
    public void shouldReturnJsonApiFormattedElement() throws JSONException {
        String profileId = "profileId";
        feedbackEvent.setComment("comment");
        String jsonApiString = feedbackEvent.toJsonAPI(profileId);
        JSONObject jsonObject = new JSONObject(jsonApiString);
        assertNotNull(jsonObject);
        assertNotNull(jsonObject = jsonObject.getJSONObject("data"));
        assertThat((String) jsonObject.get("type"), is(RES_TYPE));
        assertNotNull(jsonObject = jsonObject.getJSONObject("attributes"));
        assertThat((String) jsonObject.get(RECIPE_ID), is("recipeId"));
        assertThat((String) jsonObject.get(PROFILE_ID), is("profileId"));
        assertThat((Integer) jsonObject.get(RATING), is(0));
        assertThat((String) jsonObject.get(COMMENT), is("comment"));
    }
}
