package it.near.sdk.reaction.feedback;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.near.sdk.reactions.feedbackplugin.model.Feedback;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class FeedbackTest {

    @Test
    public void feedbackIsParcelable() {
        Feedback feedback = new Feedback();
        feedback.setId("feedback_id");
        feedback.setRecipeId("recipe_id");
        feedback.question = "what comes after five?";
        feedback.notificationMessage = "notif";
        Parcel parcel = Parcel.obtain();
        feedback.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        Feedback actual = Feedback.CREATOR.createFromParcel(parcel);
        assertThat(actual.getId(), is(feedback.getId()));
        assertThat(actual.getRecipeId(), is(feedback.getRecipeId()));
        assertThat(actual.question, is(feedback.question));
        assertThat(actual.notificationMessage, is(feedback.notificationMessage));
    }

}
