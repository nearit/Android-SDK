package it.near.sdk.Push;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import it.near.sdk.Communication.Constants;
import it.near.sdk.Communication.NearNetworkUtil;
import it.near.sdk.GlobalConfig;
import it.near.sdk.GlobalState;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearUtils;

/**
 * Manager for push notifications.
 *
 * @author cattaneostefano
 */
public class PushManager {

    private static final String PUSH_PLUGIN_PATH = "push-machine";
    private static final String PUSH_PATH = "pushes";
    public static final String PUSH_RECEIVED_ACTION = "received";
    public static final String PUSH_OPENED_ACTION = "opened";
    String senderId;
    Context mContext;

    /**
     * Default constructor. Checks play services presence and register the device on GCM.
     *
     * @param mContext the app context.
     * @param senderId the senderId of the Android project.
     */
    public PushManager(Context mContext, String senderId) {
        this.senderId = senderId;
        this.mContext = mContext;
        GlobalConfig.getInstance(mContext).setSenderId(senderId);

        if (NearUtils.checkPlayServices(mContext)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(mContext, RegistrationIntentService.class);
            intent.putExtra(RegistrationIntentService.SENDER_ID, senderId);
            mContext.startService(intent);
        }
    }

    public void trackPush(String push_id, String action) {

        Uri url = Uri.parse(Constants.API.PLUGINS_ROOT).buildUpon()
                .appendPath(PUSH_PLUGIN_PATH)
                .appendPath(PUSH_PATH)
                .appendPath(push_id)
                .appendPath(action).build();

        HashMap<String, Object> map = buildPushTrackMap();

        try {
            String body = NearUtils.toJsonAPI("status", null, map);
            NearNetworkUtil.sendTrack(mContext, url.toString(), body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Object> buildPushTrackMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("installation_id", GlobalConfig.getInstance(mContext).getInstallationId());
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date now = new Date(System.currentTimeMillis());
        String formatted = sdf.format(now);
        map.put("timestamp", formatted);
        return map;
    }

    public void processPush(String push_id, String recipe_id) {
        trackPush(push_id, PushManager.PUSH_RECEIVED_ACTION);
        getRecipesManager().processRecipe(recipe_id);
    }

    private RecipesManager getRecipesManager(){return GlobalState.getInstance(mContext.getApplicationContext()).getRecipesManager();}

}
