package it.near.sdk.Utils;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import it.near.sdk.Reactions.Content.Content;
import it.near.sdk.Reactions.Content.ContentReaction;
import it.near.sdk.Reactions.Coupon.Coupon;
import it.near.sdk.Reactions.Coupon.CouponReaction;
import it.near.sdk.Reactions.CustomJSON.CustomJSON;
import it.near.sdk.Reactions.CustomJSON.CustomJSONReaction;
import it.near.sdk.Reactions.Feedback.Feedback;
import it.near.sdk.Reactions.Feedback.FeedbackReaction;
import it.near.sdk.Reactions.Poll.Poll;
import it.near.sdk.Reactions.Poll.PollReaction;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotification;
import it.near.sdk.Reactions.SimpleNotification.SimpleNotificationReaction;
import it.near.sdk.Recipes.Models.Recipe;

/**
 * Near utilities
 * @author cattaneostefano
 */
public class NearUtils {

    /**
     * Compute app Id from the Apptoken (apikey)
     * @param apiKey token
     * @return the App Id as defined in our servers
     */
    public static String fetchAppIdFrom(String apiKey) {
        String secondSegment = substringBetween(apiKey, ".",".");
        String appId = "";
        try {
            String decodedAK = decodeString(secondSegment);
            JSONObject jwt = new JSONObject(decodedAK);
            JSONObject account = jwt.getJSONObject("data").getJSONObject("account");
            appId = account.getString("id");
        } catch (Exception e) {
            Log.e("NearITErrors", "Error while processing NearIT API token. Please check if you are using the correct key.");
        }
        return appId;
    }

    /**
     * Decode base 64 string
     * @param encoded encoded string
     * @return decoded string
     */
    private static String decodeString(String encoded) throws NullPointerException{
        byte[] dataDec = Base64.decode(encoded, Base64.DEFAULT);
        String decodedString = "";
        try {
            decodedString = new String(dataDec, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return decodedString;
    }

    /**
     * String utility method
     * @param str initial string
     * @param open
     * @param close
     * @return
     */
    private static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    /**
     * Utility method for automatically casting content. It notifies the listener if the intent contains a recognized core content.
     * @param intent The intent to analyze.
     * @param listener Contains a callback method for each content type.
     * @return true if the content was recognized as core and passed to a callback method, false if it wasn't.
     */
    public static boolean parseCoreContents(Intent intent, CoreContentsListener listener) {
        String reaction_plugin = intent.getStringExtra(NearItIntentConstants.REACTION_PLUGIN);
        String recipeId = intent.getStringExtra(NearItIntentConstants.RECIPE_ID);

        if (!intent.hasExtra(NearItIntentConstants.CONTENT)) return false;

        return parseContent(intent, intent.getParcelableExtra(NearItIntentConstants.CONTENT), recipeId, reaction_plugin, listener);
    }

    /**
     * Parse a parcelable content received from a proximity receiver. Since there's no intent, the intent field of the listener callback methods is always null.
     * @param content the @{@link Parcelable} content to parse. This contains the object set in the what component of the recipe.
     * @param recipe the recipe object.
     * @param listener the listener for the casted content types.
     * @return true if the content was recognized as core and passed to a callback method, false if it wasn't.
     */
    public static boolean parseCoreContents(Parcelable content, Recipe recipe, CoreContentsListener listener) {
        String reaction_plugin = recipe.getReaction_plugin_id();
        String recipeId = recipe.getId();

        return parseContent(null, content, recipeId, reaction_plugin, listener);
    }

    private static boolean parseContent(Intent intent, Parcelable content, String recipeId, String reaction_plugin, CoreContentsListener listener) {
        boolean coreContent = false;
        if (reaction_plugin == null) return false;
        switch (reaction_plugin) {
            case ContentReaction.PLUGIN_NAME :
                Content c_notif = (Content) content;
                listener.gotContentNotification(intent, c_notif, recipeId);
                coreContent = true;
                break;
            case SimpleNotificationReaction.PLUGIN_NAME :
                SimpleNotification s_notif = (SimpleNotification) content;
                listener.gotSimpleNotification(intent, s_notif, recipeId);
                coreContent = true;
                break;
            case PollReaction.PLUGIN_NAME :
                Poll p_notif = (Poll) content;
                listener.gotPollNotification(intent, p_notif, recipeId);
                coreContent = true;
                break;
            case CouponReaction.PLUGIN_NAME :
                Coupon coup_notif = (Coupon) content;
                listener.gotCouponNotification(intent, coup_notif, recipeId);
                coreContent = true;
                break;
            case CustomJSONReaction.PLUGIN_NAME :
                CustomJSON custom_notif = (CustomJSON) content;
                listener.gotCustomJSONNotification(intent, custom_notif, recipeId);
                coreContent = true;
                break;
            case FeedbackReaction.PLUGIN_NAME :
                Feedback f_notif = (Feedback) content;
                listener.gotFeedbackNotification(intent, f_notif, recipeId);
                coreContent = true;
                break;
        }
        return coreContent;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param errorMessage the exception message to use if the check fails
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }
}
