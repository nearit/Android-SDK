package com.nearit.sample;

import android.content.Intent;

import androidx.annotation.NonNull;

import it.near.sdk.recipes.background.NearBackgroundJobIntentService;

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

public class MyBackgroundJIS extends NearBackgroundJobIntentService {

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // in this sample I don't customize anything, I even call super
        super.onHandleWork(intent);
    }
}
