package it.near.sdk.recipes.background;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import static it.near.sdk.utils.NearItIntentConstants.ACTION;


public class BackgroundJobIntentService extends JobIntentService {

    private static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        String name = NearItIntentService.getResolverInfo(context, work.getStringExtra(ACTION));
        if (name == null) {
            enqueueWork(context, BackgroundJobIntentService.class, JOB_ID, work);
        } else {
            try {
                Class t = Class.forName(name);
                enqueueWork(context, t, JOB_ID, work);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.e("JIS", "jis running");
        // Create the notification about the content inside the intent
        if (intent != null) {
            NearItIntentService.sendSimpleNotification(this, intent);
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            NearItBroadcastReceiver.completeWakefulIntent(intent);
        }
    }
}
