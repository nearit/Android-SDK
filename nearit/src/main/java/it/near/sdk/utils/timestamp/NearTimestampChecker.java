package it.near.sdk.utils.timestamp;

import java.util.List;

public class NearTimestampChecker {

    static final String RECIPE = "recipes";
    static final String GEOPOLIS = "geopolis";
    private final NearItTimeStampApi api;

    public NearTimestampChecker(NearItTimeStampApi api) {
        this.api = api;
    }

    public void checkRecipeTimeStamp(long localTimestamp, SyncCheckListener syncCheckListener) {
        checkTimeStampFor(RECIPE, localTimestamp, syncCheckListener);
    }

    public void checkGeopolisTimeStamp(long localTimestamp, SyncCheckListener syncCheckListener) {
        checkTimeStampFor(GEOPOLIS, localTimestamp, syncCheckListener);
    }

    private void checkTimeStampFor(final String what, final long localTimestamp, final SyncCheckListener syncCheckListener) {
        api.fetchTimeStamps(new NearItTimeStampApi.TimeStampListener() {
            @Override
            public void onSuccess(List<CacheTimestamp> timestamps) {
                if (isMyCacheHotFor(what, localTimestamp, timestamps)) {
                    syncCheckListener.syncNotNeeded();
                } else {
                    syncCheckListener.syncNeeded();
                }
            }

            @Override
            public void onError(String message) {
                syncCheckListener.syncNeeded();
            }
        });
    }

    private boolean isMyCacheHotFor(String what, long localTimestamp, List<CacheTimestamp> timestamps) {
        if (timestamps == null || timestamps.isEmpty())
            return false;

        for (CacheTimestamp timestamp : timestamps) {
            if (timestamp.what.equals(what)) {
                return timestamp.time.longValue() < localTimestamp;
            }
        }

        return false;
    }

    // public static NearTimestampChecker create();

    public interface SyncCheckListener {
        void syncNeeded();
        void syncNotNeeded();
    }
}
