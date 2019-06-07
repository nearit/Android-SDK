package com.nearit.sample_kotlin

import android.app.Application
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import it.near.sdk.NearItManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NearItManager.getInstance().setProximityNotificationIcon(R.drawable.common_full_open_on_phone)
        NearItManager.getInstance().setPushNotificationIcon(R.drawable.googleg_disabled_color_18)

        NearItManager.getInstance().addNotificationHistoryUpdateListener { items ->
            val count = items.count { it.isNew }
            Toast.makeText(this, count.toString(), LENGTH_SHORT).show()
        }

    }

}
