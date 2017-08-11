package it.near.sdk.recipes.background;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import it.near.sdk.NearItManager;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearItIntentConstants;

public class AutoTrackingReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TrackingInfo trackingInfo = intent.getParcelableExtra(NearItIntentConstants.TRACKING_INFO);
        NearItManager.getInstance(context).sendTracking(trackingInfo, Recipe.ENGAGED_STATUS);
        Intent launcherIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtras(intent.getExtras());
        context.startActivity(launcherIntent);
    }
}
