package com.nearit.sample_kotlin

import android.content.Intent
import it.near.sdk.recipes.background.NearBackgroundJobIntentService

/**
 * This is the manifest element for the IntentService
 * <service android:name=".MyBackgroundJIS"
 * android:exported="false"
 * android:permission="android.permission.BIND_JOB_SERVICE">
 *  <intent-filter>
 *      <action android:name="it.near.sdk.permission.GEO_MESSAGE" />
 *      <category android:name="android.intent.category.DEFAULT" />
 *  </intent-filter>
 *  <intent-filter>
 *      <action android:name="it.near.sdk.permission.PUSH_MESSAGE" />
 *      <category android:name="android.intent.category.DEFAULT" />
 *  </intent-filter>
 * </service>
 */

class MyBackgroundJIS : NearBackgroundJobIntentService() {

    override fun onHandleWork(intent: Intent) {
        // in this sample I don't customize anything, I even call super
        super.onHandleWork(intent)
    }

}