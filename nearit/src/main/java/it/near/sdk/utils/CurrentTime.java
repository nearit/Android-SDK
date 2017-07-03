package it.near.sdk.utils;

import java.util.Calendar;

public class CurrentTime {

    public Long currentTimeStampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public Long currentTimestamp() {return System.currentTimeMillis();}

    public Calendar currentCalendar() { return Calendar.getInstance();}
}
