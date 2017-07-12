package it.near.sdk.recipes.validation;

import com.google.gson.internal.LinkedTreeMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CurrentTime;

public class AdvScheduleValidator extends Validator {

    private static final String KEY_FROM = "from";
    private static final String KEY_TO = "to";
    private static final String KEY_DAYS = "days";

    private static final String HOUR_FORMAT_SEPARATOR = ":";
    private final CurrentTime currentTime;

    public AdvScheduleValidator(CurrentTime currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    boolean validate(Recipe recipe) {
        return recipe.scheduling == null ||
                checkScheduling(recipe.scheduling);
    }

    private boolean checkScheduling(List<Object> scheduling) {
        if (scheduling.isEmpty()) return true;
        for (Object toCast : scheduling) {
            try {
                Map<String, Object> dateFrame = (Map<String, Object>) toCast;
                if (dateFrameIsValid(dateFrame)) {
                    return true;
                }
            } catch (ClassCastException ignored) {

            }
        }
        return false;
    }

    private boolean dateFrameIsValid(Map<String, Object> dateFrame) {
        if (!dateFrame.containsKey(KEY_FROM) ||
                !dateFrame.containsKey(KEY_TO)) {
            return false;
        }

        if (dateFrameIsCurrent(dateFrame)) {
            return checkDailySchedule(dateFrame);
        } else {
            return false;
        }
    }

    private boolean dateFrameIsCurrent(Map<String, Object> dateFrame) {
        String fromDateString = (String) dateFrame.get(KEY_FROM);
        String toDateString = (String) dateFrame.get(KEY_TO);
        boolean validity = true;
        try {
            // do not move the dateformatter to be an instance variable, it messes the parsing
            SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date todaysDate = removeTime(currentTime.currentCalendar().getTime());
            if (fromDateString != null) {
                Date fromDate = DATE_FORMATTER.parse(fromDateString);
                validity &= (todaysDate.equals(fromDate) || todaysDate.after(fromDate));
            }

            if (toDateString != null) {
                Date toDate = DATE_FORMATTER.parse(toDateString);
                validity &= (todaysDate.equals(toDate) || todaysDate.before(toDate));
            }
        } catch (ParseException ignored) {
            return true;
        }
        return validity;
    }

    private boolean checkDailySchedule(Map<String, Object> dateFrame) {
        if (!dateFrame.containsKey(KEY_DAYS)) {
            return false;
        }
        Map<String, Object> daysHash = (LinkedTreeMap<String, Object>) dateFrame.get(KEY_DAYS);
        SimpleDateFormat dayOfWeekFormatter = new SimpleDateFormat("E", Locale.US);
        String currentDayOfWeek = dayOfWeekFormatter.format(currentTime.currentCalendar().getTime());
        if (!daysHash.containsKey(currentDayOfWeek)) {
            return false;
        }
        List<LinkedTreeMap<String, Object>> hourFrames = (List<LinkedTreeMap<String, Object>>) daysHash.get(currentDayOfWeek);
        return checkHourFrame(hourFrames);
    }

    private boolean checkHourFrame(List<LinkedTreeMap<String, Object>> hourFrames) {
        if (hourFrames == null || hourFrames.isEmpty()) return false;
        for (LinkedTreeMap<String, Object> hourFrame : hourFrames) {
            if (weAreInside(hourFrame)) {
                return true;
            }
        }
        return false;
    }

    private boolean weAreInside(LinkedTreeMap<String, Object> hourFrame) {
        if (!hourFrame.containsKey(KEY_FROM) || !hourFrame.containsKey(KEY_TO)) {
            return false;
        }
        try {
            String fromString = (String) hourFrame.get(KEY_FROM);
            String toString = (String) hourFrame.get(KEY_TO);

            Calendar fromCalendar = buildCalendar(fromString);
            Calendar toCalendar = buildCalendar(toString);
            Calendar now = fixedDateCurrentCalendar();

            return (fromCalendar.before(now) || fromCalendar.equals(now)) &&
                    (toCalendar.after(now) || toCalendar.equals(now));

        } catch (ClassCastException e) {
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Calendar fixedDateCurrentCalendar() {
        Calendar currentCalendar = currentTime.currentCalendar();
        Calendar fixedDate = new GregorianCalendar(TimeZone.getDefault());
        fixedDate.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
        fixedDate.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
        fixedDate.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));
        fixedDate.set(Calendar.MILLISECOND, 0);
        fixedDate.getTime();
        return fixedDate;
    }

    private Calendar buildCalendar(String formattedHours) throws NumberFormatException {
        String[] parts = formattedHours.split(HOUR_FORMAT_SEPARATOR);
        Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        cal.set(Calendar.SECOND, Integer.parseInt(parts[2]));
        cal.set(Calendar.MILLISECOND, 0);
        cal.getTime();
        return cal;
    }

    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
