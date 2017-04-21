package it.near.sdk.trackings;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.near.sdk.utils.AppVisibilityDetector;
import it.near.sdk.utils.ApplicationVisibility;

public class TrackManager implements AppVisibilityDetector.AppVisibilityCallback {

    private final ConnectivityManager connectivityManager;
    private final TrackSender trackSender;
    private final TrackCache trackCache;
    private final ApplicationVisibility applicationVisibility;

    public TrackManager(ConnectivityManager connectivityManager,
                        TrackSender trackSender,
                        TrackCache trackCache,
                        ApplicationVisibility applicationVisibility) {
        this.connectivityManager = connectivityManager;
        this.trackSender = trackSender;
        this.trackCache = trackCache;
        this.applicationVisibility = applicationVisibility;
        applicationVisibility.setCallback(this);
    }

    public void sendTracking(TrackRequest trackRequest) {

    }

    @Override
    public void onAppGotoForeground() {

    }

    @Override
    public void onAppGotoBackground() {

    }

    private boolean isConnectionAvailable() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
