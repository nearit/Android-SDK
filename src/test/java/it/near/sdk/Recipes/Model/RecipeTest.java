package it.near.sdk.Recipes.Model;

import android.content.Context;
import android.content.res.Resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.near.sdk.Recipes.Models.Recipe;

/**
 * Created by cattaneostefano on 17/02/2017.
 */

public class RecipeTest {

    Recipe mRecipe;

    @Before
    public void prepareBeforeTests() {
       /* Context context = mock(Context.class);
        Context appContext = mock(Context.class);
        Resources resources = mock(Resources.class);
        when(resources.openRawResource(anyInt())).thenReturn(mock(InputStream.class));
        when(appContext.getResources()).thenReturn(resources);
        when(context.getApplicationContext()).thenReturn(appContext);
        JodaTimeAndroid.init(context);*/
    }

    @Before
    public void setUp(){
        mRecipe = new Recipe();
    }

    @Test
    public void whenSchedulingIsMissing_theRecipeIsValid(){
        assertTrue(mRecipe.isScheduledNow(Calendar.getInstance()));
    }

    @Test
    public void whenSchedulingIsForThisMonth_theRecipeIsValid() {
        // when a recipe is scheduled for the month of january 2017
        DateTime startPeriod = new DateTime(2017, 1, 1, 0, 0, 0).withTimeAtStartOfDay();
        DateTime endPeriod = new DateTime(2017, 1, 31, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildSchedulingBlockForDate(startPeriod, endPeriod);
        mRecipe.setScheduling(scheduling);
        Calendar lowerBound = buildCalendarFrom(startPeriod);
        // then it is valid on the start date
        assertTrue(mRecipe.isScheduledNow(lowerBound));
        Calendar upperBound = buildCalendarFrom(endPeriod);
        // then also on the end date
        assertTrue(mRecipe.isScheduledNow(upperBound));
        Calendar middleOfPeriod = buildCalendarFrom(new DateTime(2017, 1, 15, 0, 0, 0));
        // then is valid in the middle of the period
        assertTrue(mRecipe.isScheduledNow(middleOfPeriod));
    }

    @Test
    public void whenNotScheduledForToday_theRecipeIsNotValid() {
        // when a recipe is scheduled for the month of january 2017
        DateTime startPeriod = new DateTime(2017, 1, 1, 0, 0, 0).withTimeAtStartOfDay();
        DateTime endPeriod = new DateTime(2017, 1, 31, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildSchedulingBlockForDate(startPeriod, endPeriod);
        mRecipe.setScheduling(scheduling);
        Calendar dayBefore = buildCalendarFrom(startPeriod.minusDays(1));
        // then it is not valid the day before the start
        assertFalse(mRecipe.isScheduledNow(dayBefore));
        Calendar dayAfter = buildCalendarFrom(endPeriod.plusDays(1));
        // then is not valid the day after the end
        assertFalse(mRecipe.isScheduledNow(dayAfter));
        Calendar monthAfter = buildCalendarFrom(endPeriod.plusMonths(1));
        // then is not valid a month after the end
        assertFalse(mRecipe.isScheduledNow(monthAfter));
        Calendar yearBefore = buildCalendarFrom(startPeriod.minusYears(1));
        // then is not valid a year before
        assertFalse(mRecipe.isScheduledNow(yearBefore));
    }

    private HashMap<String, Object> buildSchedulingBlockForDate(DateTime start, DateTime end){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        Map<String, Object> date = Maps.newLinkedHashMap(
                ImmutableMap.<String, Object>builder().
                        put("from", start.toString(formatter)).
                        put("to", end.toString(formatter)).
                        build());
        HashMap<String, Object> scheduling = Maps.newHashMap();
        scheduling.put("date", date);
        return scheduling;
    }

    private Calendar buildCalendarFrom(DateTime dateTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.toDate());
        return calendar;
    }

}
