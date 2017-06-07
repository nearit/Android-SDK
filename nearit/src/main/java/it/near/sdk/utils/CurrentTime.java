package it.near.sdk.utils;

public class CurrentTime {

    public Long currentTimestamp() {
        return System.currentTimeMillis();
    }

    public Long currentTimeStampSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
