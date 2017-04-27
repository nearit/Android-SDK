package it.near.sdk.utils;

public class ApplicationVisibility implements AppVisibilityDetector.AppVisibilityCallback {

    private AppVisibilityDetector.AppVisibilityCallback callback;

    public ApplicationVisibility() {
        AppVisibilityDetector.addVisibilityCallback(this);
    }

    public void setCallback(AppVisibilityDetector.AppVisibilityCallback callback) {
        this.callback = callback;
    }

    public void stop() {
        AppVisibilityDetector.removeVisibilityCallback(this);
    }

    @Override
    public void onAppGotoForeground() {
        callback.onAppGotoForeground();
    }

    @Override
    public void onAppGotoBackground() {
        callback.onAppGotoBackground();
    }
}
