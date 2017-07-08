package it.near.sdk.reactions.feedbackplugin.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackTest {

    private Feedback feedback;

    @Before
    public void setUp() throws Exception {
        feedback = new Feedback();
    }

    @Test
    public void shouldNotHaveContentToInclude() {
        assertThat(feedback.hasContentToInclude(), is(false));
    }

}