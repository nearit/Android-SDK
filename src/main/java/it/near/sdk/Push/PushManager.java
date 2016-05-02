package it.near.sdk.Push;

import android.content.Context;
import android.content.Intent;

import it.near.sdk.GlobalConfig;
import it.near.sdk.Recipes.RecipesManager;
import it.near.sdk.Utils.NearUtils;

/**
 * @author cattaneostefano
 */
public class PushManager {

    String senderId;
    Context mContext;
    RecipesManager recipesManager;

    public PushManager(Context mContext, String senderId, RecipesManager recipesManager) {
        this.senderId = senderId;
        this.mContext = mContext;
        this.recipesManager = recipesManager;
        GlobalConfig.getInstance(mContext).setSenderId(senderId);

        if (NearUtils.checkPlayServices(mContext)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(mContext, RegistrationIntentService.class);
            intent.putExtra(RegistrationIntentService.SENDER_ID, senderId);
            mContext.startService(intent);
        }
    }
}
