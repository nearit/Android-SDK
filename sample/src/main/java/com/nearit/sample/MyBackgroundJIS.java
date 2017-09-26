package com.nearit.sample;

import android.content.Intent;
import android.support.annotation.NonNull;

import it.near.sdk.logging.NearLog;
import it.near.sdk.recipes.background.BackgroundJobIntentService;


public class MyBackgroundJIS extends BackgroundJobIntentService {

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        NearLog.e("CUSTOM", "custom called");
        super.onHandleWork(intent);
    }
}
