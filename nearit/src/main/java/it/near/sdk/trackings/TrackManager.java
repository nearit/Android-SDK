package it.near.sdk.trackings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.utils.AppVisibilityDetector;
import it.near.sdk.utils.ApplicationVisibility;

public class TrackManager implements AppVisibilityDetector.AppVisibilityCallback {

    private final ConnectivityManager connectivityManager;
    private final TrackSender trackSender;
    private final TrackCache trackCache;
    private final ApplicationVisibility applicationVisibility;
    private boolean optedOut;

    TrackManager(ConnectivityManager connectivityManager,
                 TrackSender trackSender,
                 TrackCache trackCache,
                 ApplicationVisibility applicationVisibility) {
        this.connectivityManager = connectivityManager;
        this.trackSender = trackSender;
        this.trackCache = trackCache;
        this.applicationVisibility = applicationVisibility;
        this.applicationVisibility.setCallback(this);
    }

    public void sendTracking(final TrackRequest trackRequest) {
        if(!optedOut) {
            trackCache.addToCache(trackRequest);
            launchCachedRequests();
        }
    }

    private void launchCachedRequests() {
        if (isConnectionAvailable()) {
            for (final TrackRequest trackRequest : trackCache.getRequests()) {
                if (!trackRequest.sending) {
                    trackRequest.sending = true;
                    sendCachedRequest(trackRequest);
                }
            }
        }
    }

    private void sendCachedRequest(final TrackRequest trackRequest) {
        if(!optedOut) {
            trackSender.sendTrack(trackRequest, new TrackSender.RequestListener() {
                @Override
                public void onSuccess() {
                    trackRequest.sending = false;
                    trackCache.removeFromCache(trackRequest);
                }

                @Override
                public void onFailure(int statusCode) {
                    trackRequest.sending = false;
                }
            });
        }
    }

    @Override
    public void onAppGotoForeground() {
        if (!optedOut) {
            launchCachedRequests();
        }
    }

    @Override
    public void onAppGotoBackground() {

    }

    private boolean isConnectionAvailable() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static TrackManager obtain(Context context, GlobalConfig globalConfig) {
        return new TrackManager(
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE),
                new TrackSender(new NearAsyncHttpClient(context, globalConfig)),
                new TrackCache(TrackCache.getSharedPreferences(context)),
                new ApplicationVisibility());
    }

    public void onOptOut() {
        optedOut = true;
        trackCache.onOptOut();
        trackSender.onOptOut();
    }
}
