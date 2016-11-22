package it.near.sdk.Geopolis.Background;

import android.content.Intent;

import org.json.JSONException;

import it.near.sdk.GlobalConfig;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.BaseIntentService;
import it.near.sdk.Utils.IntentConstants;
import it.near.sdk.Utils.NearNotification;

/**
 * IntentService that gets notified when we receive a region event. It is explicitly called by {@link RegionBroadcastReceiver}.
 * It has to be manually added to the manifest like in the sample below inside the <code>application</code> tag.
 *
 * <pre>
 *     {@code
 *          <!-- region messages -->
 *          <service android:name="it.near.sdk.Beacons.Monitoring.RegionIntentService" />
 *     }
 * </pre>
 * @author cattaneostefano
 */
public class RegionIntentService extends BaseIntentService {

    private static String TAG = "RegionIntentService";
    /**
     * Constant for the notification
     */

    /**
     * Default constructor.
     */
    public RegionIntentService() {
        super("RegionIntentService");
    }

    /**
     * Handle the intent that started the service. The default implementation sends a system notification with the component that launched
     * the recipe and the type of content to be delivered to the app. Since those messages are intented for debug use, it is strongly raccomended
     * to extends this class and override this method, without calling super() and releasing the wake lock the same way it is done here.
     *
     * @param intent the intent to handle
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        sendSimpleNotification(intent);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        RegionBroadcastReceiver.completeWakefulIntent(intent);
    }



}
