package it.near.sdk.Reactions.Feedback;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import it.near.sdk.GlobalConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackEventTest {

    @Mock
    GlobalConfig mockGlobalConfig;

    @Test
    public void testToJsonApi() throws JSONException {
        FeedbackEvent feedbackEvent = new FeedbackEvent("feedback_id", 3, "cool", "recipe_id");
        when(mockGlobalConfig.getProfileId()).thenReturn("profile_id");
        String actual = feedbackEvent.toJsonAPI(mockGlobalConfig);
        assertThat(actual, is(notNullValue()));
    }

}
