package it.near.sdk.Utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.Vector;


/**
 * Created by claudiosuardi on 03/02/15.
 *
 * Permette di monitorare il ciclo di vita di un'App, stabilendo se Ã¨ in background o meno.
 * Tramite l'observer Ã¨ possibile ricevere gli eventi relativi al cambio di stato.
 *
 */
public class AppLifecycleMonitor implements Application.ActivityLifecycleCallbacks {

    private static String TAG = "AppLifecycleMonitor";
    private static Application app;
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    private OnLifecycleEventListener listener;



    public AppLifecycleMonitor(Application app, OnLifecycleEventListener listener){
        this.app = app;
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

        triggerEvents();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        ULog.d(TAG, "application is in foreground: " + (resumed > paused));
        triggerEvents();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        ULog.d(TAG, "application is visible: " + (started > stopped));
    }


    private void triggerEvents(){
        if (isApplicationInForeground())
            listener.onForeground();
        else
            listener.onBackground();
    }


}