package it.near.sdk.recipes.background;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.util.List;

import static it.near.sdk.utils.NearItIntentConstants.ACTION;


public class NearBackgroundJobIntentService extends JobIntentService {

    private static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        String name = getResolverInfo(context, work.getStringExtra(ACTION));
        if (name == null) {
            enqueueWork(context, NearBackgroundJobIntentService.class, JOB_ID, work);
        } else {
            try {
                Class t = Class.forName(name);
                enqueueWork(context, t, JOB_ID, work);
            } catch (Throwable ignored) {
                enqueueWork(context, NearBackgroundJobIntentService.class, JOB_ID, work);
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

    @Nullable
    public static String getResolverInfo(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent().setAction(action).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA);
        if (resolveInfos.size() < 1) return null;
        return resolveInfos.get(0).serviceInfo.name;
    }
}
