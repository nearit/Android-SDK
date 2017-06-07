package it.near.sdk.recipes.validation;

import android.support.annotation.Nullable;

import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleValidatorTest {

    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String KEY_FROM = "from";
    private static final String KEY_TO = "to";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIMETABLE = "timetable";
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT, Locale.US);

    @Mock
    private CurrentTime mockCurrentTime;

    private ScheduleValidator scheduleValidator;
    private Recipe testRecipe;

    @Before
    public void setUp() throws Exception {
        testRecipe = new Recipe();
        when(mockCurrentTime.currentCalendar()).thenReturn(Calendar.getInstance());
        scheduleValidator = new ScheduleValidator(mockCurrentTime);
    }

    @Test
    public void whenSchedulingIsMissing_theRecipeIsValid() {
        assertThat(scheduleValidator.validate(testRecipe), is(true));
    }

    @Test
    public void whenSchedulingIsForThisMonth_theRecipeIsValid() {
        // when a recipe is scheduled for the month of january 2017
        DateTime startPeriod = new DateTime(2017, 1, 1, 0, 0, 0).withTimeAtStartOfDay();
        DateTime endPeriod = new DateTime(2017, 1, 31, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildScheduling(startPeriod, endPeriod, null, null);
        testRecipe.setScheduling(scheduling);
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startPeriod));
        // then it is valid on the start date
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endPeriod));
        // then also on the end date
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(new DateTime(2017, 1, 15, 0, 0, 0)));
        // then is valid in the middle of the period
        assertThat(scheduleValidator.validate(testRecipe), is(true));
    }

    @Test
    public void whenNotScheduledForToday_theRecipeIsNotValid() {
        // when a recipe is scheduled for the month of january 2017
        DateTime startPeriod = new DateTime(2017, 1, 1, 0, 0, 0).withTimeAtStartOfDay();
        DateTime endPeriod = new DateTime(2017, 1, 31, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildScheduling(startPeriod, endPeriod, null, null);
        testRecipe.setScheduling(scheduling);
        // then it is not valid the day before the start
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startPeriod.minusDays(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        // then is not valid the day after the end
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endPeriod.plusDays(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        // then is not valid a month after the end
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endPeriod.plusMonths(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        // then is not valid a year before
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startPeriod.minusYears(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
    }

    @Test
    public void whenRecipeOnlyHasLowerBound_thenValidityIsChecked() {
        // when a recipe is scheduled from february 1st 2017
        DateTime startDate = new DateTime(2017, 2, 1, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildScheduling(startDate, null, null, null);
        testRecipe.setScheduling(scheduling);

        // then is valid from that day on
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startDate));
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startDate.plusMonths(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startDate.plusYears(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(true));

        // then is not valid before that day
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startDate.minusDays(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startDate.minusMonths(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startDate.minusYears(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
    }

    @Test
    public void whenRecipeOnlyHasUpperBound_thenValidityIsChecked() {
        // when a recipe is scheduled until january 31st 2017
        DateTime endDate = new DateTime(2017, 1, 31, 0, 0, 0).withTimeAtStartOfDay();
        HashMap<String, Object> scheduling = buildScheduling(null, endDate, null, null);
        testRecipe.setScheduling(scheduling);

        // then is valid up until that day
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endDate));
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endDate.minusMonths(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endDate.minusYears(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(true));

        // then is not valid afterwards
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endDate.plusDays(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endDate.plusMonths(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endDate.plusYears(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
    }

    @Test
    public void whenRecipeIsScheduledATimeOfDay_isValidityChecked() throws ParseException {
        // when a recipe is scheduled for this time of day
        LocalTime startTime = new LocalTime(8, 0, 0);
        LocalTime endTime = new LocalTime(20, 0, 0);
        HashMap<String, Object> scheduling = buildScheduling(null, null, startTime, endTime);
        testRecipe.setScheduling(scheduling);

        // then is valid during the period
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startTime));
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endTime));
        assertThat(scheduleValidator.validate(testRecipe), is(true));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startTime.plusHours(3)));
        assertThat(scheduleValidator.validate(testRecipe), is(true));

        // then is not valid outside of the period
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startTime.minusMillis(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endTime.plusMinutes(1)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(startTime.minusHours(2)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(endTime.plusHours(2)));
        assertThat(scheduleValidator.validate(testRecipe), is(false));
    }

    private HashMap<String, Object> buildScheduling(DateTime startDate, DateTime endDate,
                                                    LocalTime startTime, LocalTime endTime) {
        HashMap<String, Object> scheduling = Maps.newHashMap();
        if (startDate != null || endDate != null)
            scheduling.put(KEY_DATE, buildSchedulingBlockForDate(startDate, endDate));
        if (startTime != null || endTime != null)
            scheduling.put(KEY_TIMETABLE, buildSchedulingBlockForTimeOfDay(startTime, endTime));
        return scheduling;
    }

    private Map<String, Object> buildSchedulingBlockForDate(@Nullable DateTime startDate, @Nullable DateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
        Map<String, Object> date = Maps.newLinkedHashMap();
        if (startDate != null) date.put(KEY_FROM, startDate.toString(formatter));
        if (endDate != null) date.put(KEY_TO, endDate.toString(formatter));
        return date;
    }

    private Map<String, Object> buildSchedulingBlockForTimeOfDay(@Nullable LocalTime startTime, @Nullable LocalTime endTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(TIME_FORMAT);
        Map<String, Object> timetable = Maps.newLinkedHashMap();
        if (startTime != null) timetable.put(KEY_FROM, startTime.toString(fmt));
        if (endTime != null) timetable.put(KEY_TO, endTime.toString(fmt));
        return timetable;
    }

    private Calendar buildCalendarFrom(DateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.toDate());
        return calendar;
    }

    private Calendar buildCalendarFrom(LocalTime localTime) throws ParseException {
        Date fromHourDate = timeFormatter.parse(localTime.toString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromHourDate);
        return calendar;
    }
}