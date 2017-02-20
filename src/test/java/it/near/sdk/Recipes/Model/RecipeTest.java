package it.near.sdk.Recipes.Model;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

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
        HashMap<String, Object> scheduling = buildScheduling(startPeriod, endPeriod, null, null);
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
        HashMap<String, Object> scheduling = buildScheduling(startPeriod, endPeriod, null, null);
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

    @Test
    public void whenRecipeOnlyHasLowerBound_thenValidyIsChecked() {
        // when a recipe is scheduled from february 1st 2017
        DateTime startDate = new DateTime(2017, 2, 1, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildScheduling(startDate, null, null, null);
        mRecipe.setScheduling(scheduling);

        Calendar thatDay = buildCalendarFrom(startDate);
        Calendar monthLater = buildCalendarFrom(startDate.plusMonths(1));
        Calendar yearLater = buildCalendarFrom(startDate.plusYears(1));
        // then is valid from that day on
        assertTrue(mRecipe.isScheduledNow(thatDay));
        assertTrue(mRecipe.isScheduledNow(monthLater));
        assertTrue(mRecipe.isScheduledNow(yearLater));
        Calendar dayBefore = buildCalendarFrom(startDate.minusDays(1));
        Calendar monthBefore = buildCalendarFrom(startDate.minusMonths(1));
        Calendar yearBefore = buildCalendarFrom(startDate.minusYears(1));
        // then is not valid before that day
        assertFalse(mRecipe.isScheduledNow(dayBefore));
        assertFalse(mRecipe.isScheduledNow(monthBefore));
        assertFalse(mRecipe.isScheduledNow(yearBefore));
    }

    @Test
    public void whenRecipeOnlyHasUpperBound_thenValidityIsChecked() {
        // when a recipe is scheduled until january 31st 2017
        DateTime endDate = new DateTime(2017, 1, 31, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildScheduling(null, endDate, null, null);
        mRecipe.setScheduling(scheduling);

        Calendar thatDay = buildCalendarFrom(endDate);
        Calendar monthBefore = buildCalendarFrom(endDate.minusMonths(1));
        Calendar yearBefore = buildCalendarFrom(endDate.minusYears(1));
        // then is valid up until that day
        assertTrue(mRecipe.isScheduledNow(thatDay));
        assertTrue(mRecipe.isScheduledNow(monthBefore));
        assertTrue(mRecipe.isScheduledNow(yearBefore));
        Calendar nextDay = buildCalendarFrom(endDate.plusDays(1));
        Calendar nextMonth = buildCalendarFrom(endDate.plusMonths(1));
        Calendar nextYear = buildCalendarFrom(endDate.plusYears(1));
        assertFalse(mRecipe.isScheduledNow(nextDay));
        assertFalse(mRecipe.isScheduledNow(nextMonth));
        assertFalse(mRecipe.isScheduledNow(nextYear));
    }

    @Test
    public void whenRecipeIsScheduledATimeOfDay_isValidityChecked() throws ParseException {
        // when a recipe is scheduled for this time of day
        LocalTime startTime = new LocalTime(8, 0, 0);
        LocalTime endTime = new LocalTime(20, 0, 0);
        HashMap<String, Object> scheduling = buildScheduling(null, null, startTime, endTime);
        mRecipe.setScheduling(scheduling);

        Calendar atTheStart = buildCalendarFrom(startTime);
        Calendar atTheEnd = buildCalendarFrom(endTime);
        Calendar inTheMiddle =buildCalendarFrom(startTime.plusHours(3));
        // then is valid during the period
        assertTrue(mRecipe.isScheduledNow(atTheStart));
        assertTrue(mRecipe.isScheduledNow(atTheEnd));
        assertTrue(mRecipe.isScheduledNow(inTheMiddle));

        Calendar justBefore = buildCalendarFrom(startTime.minusMillis(1));
        Calendar justAfter = buildCalendarFrom(endTime.plusMinutes(1));
        Calendar before = buildCalendarFrom(startTime.minusHours(2));
        Calendar after = buildCalendarFrom(endTime.plusHours(2));
        // then is not valid outside of the period
        assertFalse(mRecipe.isScheduledNow(justBefore));
        assertFalse(mRecipe.isScheduledNow(justAfter));
        assertFalse(mRecipe.isScheduledNow(before));
        assertFalse(mRecipe.isScheduledNow(after));
    }

    @Test
    public void whenRecipeIsNotScheduledForThisTimeOfDay_isNotValid() throws ParseException {
        // when a recipe is not scheduled for this time of day
        LocalTime startTime = new LocalTime(15, 0, 0);
        LocalTime endTime = new LocalTime(18, 0, 0);
        HashMap<String, Object> scheduling = buildScheduling(null, null, startTime, endTime);
        mRecipe.setScheduling(scheduling);


    }

    private HashMap<String, Object> buildScheduling(DateTime startDate, DateTime endDate,
                                                         LocalTime startTime, LocalTime endTime){
        HashMap<String, Object> scheduling = Maps.newHashMap();
        if (startDate != null || endDate != null)
            scheduling.put("date", buildSchedulingBlockForDate(startDate, endDate));
        if (startTime != null || endTime != null)
            scheduling.put("timetable", buildSchedulingBlockForTimeOfDay(startTime, endTime));
        return scheduling;
    }

    private Map<String, Object> buildSchedulingBlockForDate(@Nullable DateTime startDate, @Nullable DateTime endDate){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        Map<String, Object> date = Maps.newLinkedHashMap();
        if (startDate != null) date.put("from", startDate.toString(formatter));
        if (endDate != null) date.put("to", endDate.toString(formatter));
        return date;
    }

    private Map<String, Object> buildSchedulingBlockForTimeOfDay(@Nullable LocalTime startTime, @Nullable LocalTime endTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
        Map<String, Object> timetable = Maps.newLinkedHashMap();
        if (startTime != null) timetable.put("from", startTime.toString(fmt));
        if (endTime != null) timetable.put("to", endTime.toString(fmt));
        return timetable;
    }


    private Calendar buildCalendarFrom(DateTime dateTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.toDate());
        return calendar;
    }

    private Calendar buildCalendarFrom(LocalTime localTime) throws ParseException {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        Date fromHourDate = timeFormatter.parse(localTime.toString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromHourDate);
        return calendar;
    }

}
