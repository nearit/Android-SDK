package it.near.sdk.recipes.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

import static it.near.sdk.recipes.models.Recipe.DATE_SCHEDULING;
import static it.near.sdk.recipes.models.Recipe.DAYS_SCHEDULING;
import static it.near.sdk.recipes.models.Recipe.TIMETABLE_SCHEDULING;

public class ScheduleValidator extends Validator {

    private final CurrentTime currentTime;

    public ScheduleValidator(CurrentTime currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    boolean validate(Recipe recipe) {
        Calendar now = currentTime.currentCalendar();
        HashMap<String, Object> scheduling = (HashMap<String, Object>) recipe.scheduling.get(0);
        return scheduling == null ||
                ( isDateValid(scheduling, now) &&
                        isTimetableValid(scheduling, now) &&
                        isDaysValid(scheduling, now));
    }

    /**
     * Check if the date range is valid.
     * @return if the date range is respected.
     */
    private boolean isDateValid(Map<String, Object> scheduling, Calendar now){
        Map<String, Object> date = (Map<String, Object>) scheduling.get(DATE_SCHEDULING);
        if (date == null) return true;
        String fromDateString = (String) date.get("from");
        String toDateString = (String) date.get("to");
        boolean valid = true;
        try {
            // do not move the dateformatter to be an instance variable, it messes the parsing
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            if (fromDateString != null) {
                Date fromDate = dateFormatter.parse(fromDateString);
                Calendar fromCalendarDate = Calendar.getInstance();
                fromCalendarDate.setTimeInMillis(fromDate.getTime());
                valid &= fromCalendarDate.before(now) || fromCalendarDate.equals(now);
            }
            if (toDateString != null) {
                Date toDate = dateFormatter.parse(toDateString);
                Calendar toCalendarDate = Calendar.getInstance();
                toCalendarDate.setTimeInMillis(toDate.getTime());
                valid &= toCalendarDate.after(now) || toCalendarDate.equals(now);
            }
        } catch (ParseException e) {
            return false;
        }
        return valid;
    }

    /**
     * Check if the time range is valid.
     * @return if the time range is respected.
     */
    private boolean isTimetableValid(Map<String, Object> scheduling, Calendar now) {
        Map<String, Object> timetable = (Map<String, Object>) scheduling.get(TIMETABLE_SCHEDULING);
        if (timetable == null) return true;
        String fromHour = (String) timetable.get("from");
        String toHour = (String) timetable.get("to");
        boolean valid = true;
        try {
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
            if (fromHour != null) {
                Date fromHourDate = timeFormatter.parse(fromHour);
                Calendar fromHourCalendar = Calendar.getInstance();
                fromHourCalendar.setTime(fromHourDate);
                valid &= fromHourCalendar.before(now) || fromHourCalendar.equals(now);
            }
            if (toHour != null){
                Date toHourDate = timeFormatter.parse(toHour);
                Calendar toHourCalendar = Calendar.getInstance();
                toHourCalendar.setTime(toHourDate);
                valid &= toHourCalendar.after(now) || toHourCalendar.equals(now);
            }
        } catch (ParseException e) {
            return false;
        }
        return valid;
    }

    /**
     * Check if the days selection is valid.
     * @return if the days selection is respected.
     */
    private boolean isDaysValid(Map<String, Object> scheduling, Calendar now) {
        List<String> days = (List<String>) scheduling.get(DAYS_SCHEDULING);
        if (days == null) return true;
        String todaysDate = getTodaysDate(now);

        return days.contains(todaysDate);
    }

    /**
     * Get today's day of week.
     * @return the day of week in "EE" format e.g. Sat.
     */
    private String getTodaysDate(Calendar now) {
        Date date = now.getTime();
        // 3 letter name form of the day
        return new SimpleDateFormat("EE", Locale.ENGLISH).format(date.getTime());

    }


}
