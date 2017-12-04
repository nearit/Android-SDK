package com.nearit.sample_kotlin

import android.app.Application
import it.near.sdk.NearItManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NearItManager.getInstance().disableDefaultRangingNotifications()

        NearItManager.getInstance().setProximityNotificationIcon(R.drawable.common_full_open_on_phone)
        NearItManager.getInstance().setPushNotificationIcon(R.drawable.googleg_disabled_color_18)

    }

}