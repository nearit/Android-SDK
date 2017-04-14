package com.nearit.sample;


import android.app.Application;

import it.near.sdk.NearItManager;

/**
 * Created by cattaneostefano on 02/12/2016.
 */

public class MyApplication extends Application {

    private static NearItManager nearItManager;

    @Override
    public void onCreate() {
        super.onCreate();
        nearItManager = new NearItManager(this, getString(R.string.near_api_key));
        nearItManager.initLifecycleMethods(this);
        nearItManager.setProximityNotificationIcon(R.drawable.common_full_open_on_phone);
        nearItManager.setPushNotificationIcon(R.drawable.googleg_disabled_color_18);
    }

    public static NearItManager getNearItManager() {
        return nearItManager;
    }
}
