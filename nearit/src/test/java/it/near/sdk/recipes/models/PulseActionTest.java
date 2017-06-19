package it.near.sdk.recipes.models;

import org.junit.Before;
import org.junit.Test;

import it.near.sdk.geopolis.trackings.Events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PulseActionTest {

    PulseAction pulseAction;

    @Before
    public void setUp() throws Exception {
        pulseAction = new PulseAction();
    }

    @Test
    public void rangeEventsAreConsidered_foreground() {
        pulseAction.setId(Events.RANGE_FAR);
        assertThat(pulseAction.isForeground(), is(true));
        pulseAction.setId(Events.RANGE_IMMEDIATE);
        assertThat(pulseAction.isForeground(), is(true));
        pulseAction.setId(Events.RANGE_NEAR);
        assertThat(pulseAction.isForeground(), is(true));
    }

    @Test
    public void nonRangeEventsAreConsidered_nonForeground() {
        pulseAction.setId(Events.ENTER_PLACE);
        assertThat(pulseAction.isForeground(), is(false));
        pulseAction.setId(Events.ENTER_REGION);
        assertThat(pulseAction.isForeground(), is(false));
        pulseAction.setId(Events.LEAVE_PLACE);
        assertThat(pulseAction.isForeground(), is(false));
        pulseAction.setId(Events.LEAVE_REGION);
        assertThat(pulseAction.isForeground(), is(false));
    }

}