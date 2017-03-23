package it.near.sdk.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 *
 * Monitor and determine app background/foreground state.
 *
 * @author claudiosuardi
 */
public class AppLifecycleMonitor implements Application.ActivityLifecycleCallbacks {

    private static String TAG = "AppLifecycleMonitor";
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    private OnLifecycleEventListener listener;

    public AppLifecycleMonitor(Application app, OnLifecycleEventListener listener){
        this.listener = listener;

        app.registerActivityLifecycleCallbacks(this);
    }

    // And these two public static functions
    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;


    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        Log.d(TAG, "application is in foreground: " + (resumed > paused));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
        triggerEvents();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        Log.d(TAG, "application is visible: " + (started > stopped));
        triggerEvents();
    }

    private void triggerEvents(){
        if (isApplicationVisible())
            listener.onForeground();
        else
            listener.onBackground();
    }

}