package com.nearit.sample;

import android.app.Application;

import it.near.sdk.NearItManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // NearItManager.init(this, getString(R.string.near_api_key));

        NearItManager.getInstance().setProximityNotificationIcon(R.drawable.common_full_open_on_phone);
        NearItManager.getInstance().setPushNotificationIcon(R.drawable.googleg_disabled_color_18);
    }

}
