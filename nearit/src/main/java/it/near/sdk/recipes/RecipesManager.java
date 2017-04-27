package it.near.sdk.recipes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.GlobalConfig;
import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.reactions.Reaction;
import it.near.sdk.recipes.models.OperationAction;
import it.near.sdk.recipes.models.PulseAction;
import it.near.sdk.recipes.models.PulseBundle;
import it.near.sdk.recipes.models.ReactionAction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackManager;
import it.near.sdk.trackings.TrackRequest;
import it.near.sdk.utils.NearJsonAPIUtils;

/**
 * Menage recipes download, caching and direct calling.
 *
 * @author cattaneostefano
 */
public class RecipesManager {

    private static final String TAG = "RecipesManager";
    private static final String NEAR_RECIPES_PREFS_NAME = "NearRecipes";
    private static final String PROCESS_PATH = "process";
    private static final String EVALUATE = "evaluate";
    private static final String TRACKINGS_PATH = "trackings";

    private static RecipesManager instance;

    private Morpheus morpheus;
    private List<Recipe> recipes = new ArrayList<>();
    private HashMap<String, Reaction> reactions = new HashMap<>();
    private final NearAsyncHttpClient httpClient;
    private final SharedPreferences sp;
    private final RecipeCooler recipeCooler;
    private final GlobalConfig globalConfig;
    private final EvaluationBodyBuilder evaluationBodyBuilder;
    private final TrackManager trackManager;

    public RecipesManager(NearAsyncHttpClient httpClient,
                          GlobalConfig globalConfig,
                          RecipeCooler recipeCooler,
                          EvaluationBodyBuilder evaluationBodyBuilder,
                          SharedPreferences sp,
                          TrackManager trackManager) {
        this.httpClient = httpClient;
        this.globalConfig = globalConfig;
        this.recipeCooler = recipeCooler;
        this.evaluationBodyBuilder = evaluationBodyBuilder;
        this.sp = sp;
        this.trackManager = trackManager;

        try {
            recipes = loadChachedList();
        } catch (JSONException e) {
            NearLog.d(TAG, "Recipes format error");
        }
        setUpMorpheusParser();
        refreshConfig();
    }

    public static void setInstance(RecipesManager instance) {
        RecipesManager.instance = instance;
    }

    @Nullable
    public static RecipesManager getInstance() {
        return instance;
    }

    /**
     * Set up Morpheus parser. Morpheus parses jsonApi encoded resources
     * https://github.com/xamoom/Morpheus
     * We didn't actually use this library due to its minSdkVersion. We instead imported its code and adapted it.
     */
    private void setUpMorpheusParser() {
        morpheus = new Morpheus();
        // register your resources
        morpheus.getFactory().getDeserializer().registerResourceClass("recipes", Recipe.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_actions", PulseAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("operation_actions", OperationAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_actions", ReactionAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_bundles", PulseBundle.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_bundles", ReactionBundle.class);
    }

    public void addReaction(Reaction reaction) {
        reactions.put(reaction.getPluginName(), reaction);
    }

    /**
     * return the list of recipes
     *
     * @return the list of recipes
     */
    public List<Recipe> getRecipes() {
        return recipes != null ? recipes : new ArrayList<Recipe>();
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     */
    public void refreshConfig() {
        refreshConfig(new RecipeRefreshListener() {
            @Override
            public void onRecipesRefresh() {
            }

            @Override
            public void onRecipesRefreshFail() {
            }
        });
    }

    /**
     * Tries to refresh the recipes list. If some network problem occurs, a cached version will be used.
     * Plus a listener will be notified of the refresh process.
     */
    public void refreshConfig(final RecipeRefreshListener listener) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendPath(PROCESS_PATH).build();
        String requestBody = null;
        try {
            requestBody = evaluationBodyBuilder.buildEvaluateBody();
        } catch (JSONException e) {
            NearLog.d(TAG, "Can't build request body");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), requestBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, "Got recipes: " + response.toString());
                    recipes = NearJsonAPIUtils.parseList(morpheus, response, Recipe.class);
                    persistList(recipes);
                    listener.onRecipesRefresh();
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in downloading recipes: " + statusCode);
                    try {
                        recipes = loadChachedList();
                    } catch (JSONException e) {
                        NearLog.d(TAG, "Recipe format error");
                    }
                    listener.onRecipesRefreshFail();
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onRecipesRefreshFail();
        }
    }

    private void persistList(List<Recipe> recipes) {
        Gson gson = new Gson();
        String listStringified = gson.toJson(recipes);
        sp.edit()
            .putString(TAG, listStringified)
            .apply();
    }

    private List<Recipe> loadChachedList() throws JSONException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<Recipe>>() {
        }.getType();
        return gson.<ArrayList<Recipe>>fromJson(sp.getString(TAG, ""), collectionType);
    }

    /**
     * Tries to trigger a recipe, stating the plugin, action and bundle of the pulse.
     * If nothing matches, nothing happens.
     *
     * @param pulse_plugin the plugin of the pulse.
     * @param pulse_action the action of the pulse.
     * @param pulse_bundle the bundle of the pulse.
     */
    public void gotPulse(String pulse_plugin, String pulse_action, String pulse_bundle) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        if (recipes == null) return;
        // Find the recipes that matches the pulse
        for (Recipe recipe : recipes) {
            // TODO check for null pulse bundle
            if (recipe.getPulse_plugin_id().equals(pulse_plugin) &&
                    recipe.getPulse_action().getId().equals(pulse_action) &&
                    recipe.getPulse_bundle().getId().equals(pulse_bundle)) {
                matchingRecipes.add(recipe);
            }
        }

        // From all the recipes, filter the ones that are scheduled for now
        List<Recipe> validRecipes = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        for (Recipe matchingRecipe : matchingRecipes) {
            if (matchingRecipe.isScheduledNow(now)) {
                validRecipes.add(matchingRecipe);
            }
        }

        recipeCooler.filterRecipe(validRecipes);

        if (validRecipes.isEmpty()) {
            // if no recipe is found the the online fallback
            onlinePulseEvaluation(pulse_plugin, pulse_action, pulse_bundle);
        } else {
            // take the first recipe and run with it
            Recipe winnerRecipe = validRecipes.get(0);
            if (winnerRecipe.isEvaluatedOnline()) {
                evaluateRecipe(winnerRecipe.getId());
            } else {
                gotRecipe(winnerRecipe);
            }
        }
    }

    /**
     * Tries to trigger a recipe. If no reaction plugin can handle the recipe, nothing happens.
     *
     * @param recipe the recipe to trigger.
     */
    public void gotRecipe(Recipe recipe) {
        String stringRecipe = recipe.getName();
        Reaction reaction = reactions.get(recipe.getReaction_plugin_id());
        reaction.handleReaction(recipe);
    }

    /**
     * Process a recipe from its id. Typically called for processing a push recipe.
     *
     * @param id push id.
     */
    public void processRecipe(final String id) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(id)
                .appendQueryParameter("filter[core][profile_id]", globalConfig.getProfileId())
                .appendQueryParameter("include", "reaction_bundle")
                .build();
        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    String reactionPluginName = recipe.getReaction_plugin_id();
                    Reaction reaction = reactions.get(reactionPluginName);
                    reaction.handlePushReaction(recipe, id, recipe.getReaction_bundle());
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "single recipe failed");
                }
            });
        } catch (AuthenticationException e) {
        }
    }

    private void onlinePulseEvaluation(String pulse_plugin, String pulse_action, String pulse_bundle) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = evaluationBodyBuilder.buildEvaluateBody(pulse_plugin, pulse_action, pulse_bundle);
        } catch (JSONException e) {
            NearLog.d(TAG, "body build error");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), evaluateBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    if (recipe != null) {
                        gotRecipe(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in handling on failure: " + statusCode);
                }
            });
        } catch (AuthenticationException e) {
            NearLog.d(TAG, "Authentication error");
        } catch (UnsupportedEncodingException e) {
            NearLog.d(TAG, "Unsuported encoding");
        } catch (NullPointerException e) {
            NearLog.d(TAG, "Shouldn't be here");
        }
    }

    /**
     * Online evaluation of a recipe.
     *
     * @param recipeId recipe identifier.
     */
    public void evaluateRecipe(String recipeId) {
        NearLog.d(TAG, "Evaluating recipe: " + recipeId);
        if (recipeId == null) return;
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(recipeId)
                .appendPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = evaluationBodyBuilder.buildEvaluateBody();
        } catch (JSONException e) {
            NearLog.d(TAG, "body build error");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), evaluateBody, new NearJsonHttpResponseHandler() {
                @Override
                public void setUsePoolThread(boolean pool) {
                    super.setUsePoolThread(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    // TODO refactor plugin
                    if (recipe != null) {
                        gotRecipe(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in handling on failure: " + statusCode);
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            NearLog.d(TAG, "Error");
        }
    }

    /**
     * Sends tracking on a recipe. Lets choose the notified status.
     *
     * @param recipeId      the recipe identifier.
     * @param trackingEvent notified status to send. Can either be NO
     * @throws JSONException
     */
    public void sendTracking(String recipeId, String trackingEvent) throws JSONException {
        if (trackingEvent.equals(Recipe.NOTIFIED_STATUS)) {
            if (recipeCooler != null) {
                recipeCooler.markRecipeAsShown(recipeId);
            }
        }

        String trackingBody = buildTrackingBody(
                recipeId,
                trackingEvent
        );

        Uri url = Uri.parse(TRACKINGS_PATH).buildUpon().build();

        trackManager.sendTracking(new TrackRequest(url.toString(), trackingBody));
    }

    private String buildTrackingBody(String recipeId, String trackingEvent) throws JSONException {
        String profileId = globalConfig.getProfileId();
        String appId = globalConfig.getAppId();
        String installationId = globalConfig.getInstallationId();
        if (recipeId == null ||
                profileId == null ||
                installationId == null) {
            throw new JSONException("missing data");
        }
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date now = new Date(System.currentTimeMillis());
        String formattedDate = sdf.format(now);
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("profile_id", profileId);
        attributes.put("installation_id", installationId);
        attributes.put("app_id", appId);
        attributes.put("recipe_id", recipeId);
        attributes.put("event", trackingEvent);
        attributes.put("tracked_at", formattedDate);
        return NearJsonAPIUtils.toJsonAPI("trackings", attributes);
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NEAR_RECIPES_PREFS_NAME, Context.MODE_PRIVATE);
    }
}
