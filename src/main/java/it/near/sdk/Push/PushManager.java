package it.near.sdk.Push;

import android.content.Context;
import android.content.Intent;

import it.near.sdk.GlobalConfig;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearUtils;

/**
 * Manager for push notifications.
 *
 * @author cattaneostefano
 */
public class PushManager {

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
}
