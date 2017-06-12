package it.near.sdk.recipes.validation;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    @Before
    public void setUp() throws Exception {
        testRecipe = new Recipe();
        advScheduleValidator = new AdvScheduleValidator(mockCurrentTime);
    }

    @Test
    public void whenScheduleIsMissing_recipeIsAlwaysValid() {
        testRecipe.setScheduling(null);

        // right now
        mockCurrentCalendar(new DateTime());
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // yesterday at 12 O'clock
        mockCurrentCalendar(new DateTime().minusDays(1).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // tomorrow at 12 O'clock
        mockCurrentCalendar(new DateTime().plusDays(1).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // july 2nd 1989 at 16 O'clock
        mockCurrentCalendar(new DateTime(1989, 7, 2, 18, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // far in the future
        mockCurrentCalendar(new DateTime(2047, 10, 8, 22, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
    }

    @Test
    public void whenScheduleCoversEverytime_recipeIsAlwaysValid() throws Exception {
        List<Object> schedule = readJsonFile("complete_coverage_validity.json");
        testRecipe.setScheduling(schedule);

        // right now
        mockCurrentCalendar(new DateTime());
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // yesterday at 12 O'clock
        mockCurrentCalendar(new DateTime().minusDays(1).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // tomorrow at 12 O'clock
        mockCurrentCalendar(new DateTime().plusDays(1).withTime(12, 0, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // july 2nd 1989 at 16 O'clock
        mockCurrentCalendar(new DateTime(1989, 7, 2, 18, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        // far in the future
        mockCurrentCalendar(new DateTime(2047, 10, 8, 22, 0, 0));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));
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
    public void whenRecipeIsScheduledOnlyForSelectedDaysOfWeek_itsValidityIsChecked() throws Exception {
        List<Object> schedule = readJsonFile("only_mon_wed_thu_sat.json");
        testRecipe.setScheduling(schedule);

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.sunday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.monday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(true));

        mockCurrentCalendar(new DateTime().withDayOfWeek(DayOfWeek.tuesday()));
        assertThat(advScheduleValidator.validate(testRecipe), is(false));
    }

    private void mockCurrentCalendar(DateTime dateTime) {
        when(mockCurrentTime.currentCalendar())
                .thenReturn(buildCalendarFrom(dateTime));
    }

    private Calendar buildCalendarFrom(DateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.toDate());
        return calendar;
    }

    private List<Object> readJsonFile(String filename) throws Exception {
        JSONObject jsonObject = TestUtils.readJsonFile(getClass(), SCHEDULER_TEST_RES_FOLDER + "/" + filename);
        JSONArray jsonArray = jsonObject.getJSONArray("scheduling");
        List<Object> toReturn = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                toReturn.add(jsonArray.get(i));
            }
            return toReturn;
        } else {
            return null;
        }
    }


}