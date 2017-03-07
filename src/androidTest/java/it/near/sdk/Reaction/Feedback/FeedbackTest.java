package it.near.sdk.Reaction.Feedback;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.near.sdk.Reactions.Feedback.Feedback;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Created by cattaneostefano on 28/02/2017.
 */

@RunWith(AndroidJUnit4.class)
public class FeedbackTest {

    @Test
    public void feedbackIsParcelable() {
        Feedback feedback = new Feedback();
        feedback.setId("feedback_id");
        feedback.setRecipeId("recipe_id");
        feedback.setQuestion("what comes after five?");
        Parcel parcel = Parcel.obtain();
        feedback.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Feedback actual = Feedback.CREATOR.createFromParcel(parcel);
        assertEquals(feedback.getId(), actual.getId());
        assertEquals(feedback.getRecipeId(), actual.getRecipeId());
        assertEquals(feedback.getQuestion(), actual.getQuestion());
    }

}
