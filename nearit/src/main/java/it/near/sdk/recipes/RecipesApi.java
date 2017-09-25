package it.near.sdk.recipes;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import it.near.sdk.GlobalConfig;
import it.near.sdk.communication.Constants;
import it.near.sdk.communication.NearAsyncHttpClient;
import it.near.sdk.communication.NearJsonHttpResponseHandler;
import it.near.sdk.logging.NearLog;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.recipes.models.OperationAction;
import it.near.sdk.recipes.models.PulseAction;
import it.near.sdk.recipes.models.PulseBundle;
import it.near.sdk.recipes.models.ReactionAction;
import it.near.sdk.recipes.models.ReactionBundle;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.ListMetaBundle;
import it.near.sdk.utils.NearJsonAPIUtils;

public class RecipesApi {

    private static final String PROCESS_PATH = "process";
    private static final String EVALUATE = "evaluate";

    private static final String TAG = "RecipesApi";
    private static final String ONLINE_EVALUATION = "online_evaluation";
    private final NearAsyncHttpClient httpClient;
    private final Morpheus morpheus;
    private final EvaluationBodyBuilder evaluationBodyBuilder;
    private final GlobalConfig globalConfig;

    private RecipesApi(NearAsyncHttpClient httpClient, Morpheus morpheus, EvaluationBodyBuilder evaluationBodyBuilder, GlobalConfig globalConfig) {
        this.httpClient = httpClient;
        this.morpheus = morpheus;
        this.evaluationBodyBuilder = evaluationBodyBuilder;
        this.globalConfig = globalConfig;
    }

    void processRecipes(final RecipesListener recipeFetcher) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendPath(PROCESS_PATH).build();
        String requestBody = null;
        try {
            requestBody = evaluationBodyBuilder.buildEvaluateBody();
        } catch (JSONException e) {
            NearLog.d(TAG, "Can't build request body");
            recipeFetcher.onRecipeProcessError();
            return;
        }

        try {
            httpClient.nearPost(url.toString(), requestBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.v(TAG, "Got recipes: " + response.toString());
                    ListMetaBundle<Recipe> recipeListMetaBundle =
                            NearJsonAPIUtils.parseListAndMeta(morpheus, response, Recipe.class);

                    boolean online_ev = false;
                    if (recipeListMetaBundle.meta.containsKey(ONLINE_EVALUATION)) {
                        online_ev = (boolean) recipeListMetaBundle.meta.get(ONLINE_EVALUATION);
                    }
                    recipeFetcher.onRecipeProcessSuccess(recipeListMetaBundle.list, online_ev);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "Error in downloading recipes: " + statusCode);
                    recipeFetcher.onRecipeProcessError();
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            recipeFetcher.onRecipeProcessError();
        }
    }

    void fetchRecipe(String recipeId, final SingleRecipeListener recipeFethcer) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(recipeId)
                .appendQueryParameter("filter[core][profile_id]", globalConfig.getProfileId())
                .appendQueryParameter("include", "reaction_bundle")
                .build();
        try {
            httpClient.nearGet(url.toString(), new NearJsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    NearLog.d(TAG, response.toString());
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    recipeFethcer.onRecipeFetchSuccess(recipe);
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    NearLog.d(TAG, "single recipe failed");
                    recipeFethcer.onRecipeFetchError("Server error");
                }
            });
        } catch (AuthenticationException e) {
            recipeFethcer.onRecipeFetchError("Authentication error");
        }
    }

    void evaluateRecipe(String recipeId, final SingleRecipeListener listener) {
        NearLog.d(TAG, "Evaluating recipe: " + recipeId);
        if (recipeId == null) {
            listener.onRecipeFetchError("no recipe id set");
            return;
        }
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(recipeId)
                .appendPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = evaluationBodyBuilder.buildEvaluateBody();
        } catch (JSONException e) {
            listener.onRecipeFetchError("body build error");
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
                    if (recipe != null) {
                        listener.onRecipeFetchSuccess(recipe);
                    }
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    listener.onRecipeFetchError("Server error");
                }
            });
        } catch (AuthenticationException | UnsupportedEncodingException e) {
            listener.onRecipeFetchError("Error");
        }

    }

    void onlinePulseEvaluation(String pulse_plugin, String pulse_action, String pulse_bundle, final SingleRecipeListener listener) {
        Uri url = Uri.parse(Constants.API.RECIPES_PATH).buildUpon()
                .appendEncodedPath(EVALUATE).build();
        String evaluateBody = null;
        try {
            evaluateBody = evaluationBodyBuilder.buildEvaluateBody(pulse_plugin, pulse_action, pulse_bundle);
        } catch (JSONException e) {
            listener.onRecipeFetchError("can't build evaluation body");
            return;
        }

        try {
            httpClient.nearPost(url.toString(), evaluateBody, new NearJsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Recipe recipe = NearJsonAPIUtils.parseElement(morpheus, response, Recipe.class);
                    if (recipe != null) {
                        listener.onRecipeFetchSuccess(recipe);

                    }
                    listener.onRecipeFetchError("no recipe for pulse");
                }

                @Override
                public void onFailureUnique(int statusCode, Header[] headers, Throwable throwable, String responseString) {
                    listener.onRecipeFetchError("Server error");
                }
            });
        } catch (AuthenticationException e) {
            listener.onRecipeFetchError("Authentication error");
        } catch (UnsupportedEncodingException e) {
            NearLog.d(TAG, "Unsupported encoding");
        } catch (NullPointerException e) {
            listener.onRecipeFetchError("Shouldn't be here");
        }
    }

    private static Morpheus buildMorpheus() {
        Morpheus morpheus = new Morpheus();
        morpheus.getFactory().getDeserializer().registerResourceClass("recipes", Recipe.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_actions", PulseAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("operation_actions", OperationAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_actions", ReactionAction.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("pulse_bundles", PulseBundle.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("reaction_bundles", ReactionBundle.class);
        return morpheus;
    }

    public static RecipesApi obtain(Context context, RecipesHistory recipesHistory, GlobalConfig globalConfig) {
        EvaluationBodyBuilder evaluationBodyBuilder = EvaluationBodyBuilder.obtain(globalConfig, recipesHistory);
        return new RecipesApi(
                new NearAsyncHttpClient(context),
                RecipesApi.buildMorpheus(),
                evaluationBodyBuilder,
                globalConfig
        );
    }

    interface RecipesListener {
        void onRecipeProcessSuccess(List<Recipe> recipes, boolean online_evaluation_fallback);

        void onRecipeProcessError();
    }

    public interface SingleRecipeListener {
        void onRecipeFetchSuccess(Recipe recipe);

        void onRecipeFetchError(String error);
    }
}
