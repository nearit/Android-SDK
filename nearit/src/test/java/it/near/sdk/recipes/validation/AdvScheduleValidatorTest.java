package it.near.sdk.recipes.validation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import it.near.sdk.DayOfWeek;
import it.near.sdk.TestUtils;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdvScheduleValidatorTest {

    private static final String SCHEDULER_TEST_RES_FOLDER = "scheduler";

    @Mock
    CurrentTime mockCurrentTime;

    private AdvScheduleValidator advScheduleValidator;

    private Recipe testRecipe;
    private Recipe testRecipe2;
    private Recipe testRecipe3;

    @Before
    public void setUp() throws Exception {
        testRecipe = new Recipe();
        testRecipe2 = new Recipe();
        testRecipe3 = new Recipe();
        advScheduleValidator = new AdvScheduleValidator(mockCurrentTime);
    }

    @Test
    public void whenScheduleIsMissing_recipeIsAlwaysValid() throws Exception {
        testRecipe.setScheduling(null);

        List<Object> completeCoverageSchedule = readJsonFile("complete_coverage_validity.json");
        testRecipe2.setScheduling(completeCoverageSchedule);

        List<Object> edgeCaseSchedule = readJsonFile("always_valid_edge_case.json");
        testRecipe3.setScheduling(edgeCaseSchedule);

        // right now
        mockCurrentCalendar(new DateTime());
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));
        assertThat(advScheduleValidator.validate(testRecipe3), is(true));

        // yesterday at 12 O'clock
        mockCurrentCalendar(new DateTime().minusDays(1).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));
        assertThat(advScheduleValidator.validate(testRecipe3), is(true));

        // tomorrow at 12 O'clock
        mockCurrentCalendar(new DateTime().plusDays(1).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));
        assertThat(advScheduleValidator.validate(testRecipe3), is(true));

        // july 2nd 1989 at 16 O'clock
        mockCurrentCalendar(new DateTime(1989, 7, 2, 18, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));
        assertThat(advScheduleValidator.validate(testRecipe3), is(true));

        // far in the future
        mockCurrentCalendar(new DateTime(2047, 10, 8, 22, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));
        assertThat(advScheduleValidator.validate(testRecipe3), is(true));
    }

    @Test
    public void whenRecipeScheduleForDatePeriod_itsValidityIsChecked() throws Exception {
        List<Object> schedule = readJsonFile("only_during_jun_2017.json");
        testRecipe.setScheduling(schedule);

        // middle of june
        mockCurrentCalendar(new DateTime(2017, 6, 15, 12, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // start of june
        mockCurrentCalendar(new DateTime(2017, 6, 1, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // end of june
        mockCurrentCalendar(new DateTime(2017, 6, 30, 23, 59, 59));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // way before
        mockCurrentCalendar(new DateTime(2017, 1, 1, 12, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        //just before
        mockCurrentCalendar(new DateTime(2017, 5, 31, 23, 59, 59));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // just after
        mockCurrentCalendar(new DateTime(2017, 7, 1, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // way after
        mockCurrentCalendar(new DateTime(2017, 8, 20, 18, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
    }

    @Test
    public void whenValidityPeriodsHaveSingleLimits_TheirValidityIsChecked() throws Exception {
        List<Object> schedule = readJsonFile("single_limit_periods.json");
        testRecipe.setScheduling(schedule);

        mockCurrentCalendar(new DateTime(2017, 6, 1, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime(2047, 8, 20, 12, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime(2017, 3, 5, 23, 59, 59));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime(1989, 7, 2, 16, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        mockCurrentCalendar(new DateTime(2017, 5, 31, 23, 59, 59));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime(2017, 4, 15, 12, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime(2017, 3, 6, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
    }

    @Test
    public void whenRecipeIsScheduledOnlyForSelectedDaysOfWeek_itsValidityIsChecked() throws Exception {
        List<Object> noDaysSchedule = readJsonFile("only_mon_wed_thu_sat.json");
        testRecipe.setScheduling(noDaysSchedule);

        List<Object> emptyDaysSchedule = readJsonFile("empty_days_schedule.json");
        testRecipe2.setScheduling(emptyDaysSchedule);

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.sunday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        assertThat(advScheduleValidator.validate(testRecipe2), is(false));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.tuesday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        assertThat(advScheduleValidator.validate(testRecipe2), is(false));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.wednesday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.thursday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.friday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        assertThat(advScheduleValidator.validate(testRecipe2), is(false));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.saturday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        assertThat(advScheduleValidator.validate(testRecipe2), is(true));
    }

    @Test
    public void whenRecipeIsScheduledForMultipleTimeframes_itsValidityIsChecked() throws Exception {
        List<Object> schedule = readJsonFile("various_daily_timeframes.json");
        testRecipe.setScheduling(schedule);

        // true values
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(9, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(9, 30, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(10, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(12, 30, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(13, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(14, 45, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(17, 15, 27, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(18, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(18, 50, 3, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // false values
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.tuesday()).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(8, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(8, 59, 59, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(10, 0, 1, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(11, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(12, 29, 59, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(14, 45, 1, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(16, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(17, 15, 26, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()).withTime(18, 50, 4, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
    }

    @Test
    public void whenRecipeHasOverLappingConditions_theyAddUp() throws Exception {
        List<Object> schedule = readJsonFile("multiple_validity_periods.json");
        testRecipe.setScheduling(schedule);

        // on a day of june
        DateTime juneDate = new DateTime(2017, 6, 15, 0, 0);
        // june 2017 exclusive hours
        mockCurrentCalendar(juneDate.withTime(10, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // 2017 exclusive hours
        mockCurrentCalendar(juneDate.withTime(15, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // always on hours
        mockCurrentCalendar(juneDate.withTime(17, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // 20th august 2017 exclusive hours
        mockCurrentCalendar(juneDate.withTime(20, 30, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // on a day of 2017
        DateTime dayOf2017 = new DateTime(2017, 10, 3, 0, 0);
        // june 2017 exclusive hours
        mockCurrentCalendar(dayOf2017.withTime(10, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // 2017 exclusive hours
        mockCurrentCalendar(dayOf2017.withTime(15, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // always on hours
        mockCurrentCalendar(dayOf2017.withTime(17, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // 20th august 2017 exclusive hours
        mockCurrentCalendar(dayOf2017.withTime(20, 30, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));


        // on another year
        DateTime otherYearDate = new DateTime(2014, 3, 4, 0, 0);
        // june 2017 exclusive hours
        mockCurrentCalendar(otherYearDate.withTime(10, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // 2017 exclusive hours
        mockCurrentCalendar(otherYearDate.withTime(15, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // always on hours
        mockCurrentCalendar(otherYearDate.withTime(17, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // 20th august 2017 exclusive hours
        mockCurrentCalendar(otherYearDate.withTime(20, 30, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));


        // on august 20th 2017
        DateTime august20th2017 = new DateTime(2017, 8, 20, 0, 0);
        // june 2017 exclusive hours
        mockCurrentCalendar(august20th2017.withTime(10, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        // 2017 exclusive hours
        mockCurrentCalendar(august20th2017.withTime(15, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // always on hours
        mockCurrentCalendar(august20th2017.withTime(17, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // 20th august 2017 exclusive hours
        mockCurrentCalendar(august20th2017.withTime(20, 30, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

    }

    private void mockCurrentCalendar(DateTime dateTime) {
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(dateTime));
    }

    private Calendar buildCalendarFrom(DateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        // set the timezone from java standard library Timezone because Jodatime cannot be initialized in a unit test
        calendar.setTime(dateTime.withZoneRetainFields(
                DateTimeZone.forTimeZone(TimeZone.getDefault())).toDate()
        );
        calendar.getTime();
        return calendar;
    }

    private List<Object> readJsonFile(String filename) throws Exception {
        JSONObject jsonObject = TestUtils.readJsonFile(getClass(), SCHEDULER_TEST_RES_FOLDER + "/" + filename);
        JSONArray jsonArray = jsonObject.getJSONArray("scheduling");
        List<Object> toReturn = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String stringified = jsonArray.get(i).toString();
                toReturn.add(new Gson().fromJson(
                        stringified, new TypeToken<HashMap<String, Object>>() {
                        }.getType()
                ));
            }
            return toReturn;
        } else {
            return null;
        }
    }


}