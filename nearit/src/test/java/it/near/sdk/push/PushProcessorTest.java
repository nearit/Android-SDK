package it.near.sdk.push;

import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.zip.DataFormatException;

import it.near.sdk.reactions.simplenotificationplugin.SimpleNotificationReaction;
import it.near.sdk.recipes.RecipesManager;
import it.near.sdk.utils.FormatDecoder;

import static it.near.sdk.push.PushProcessor.NOTIFICATION;
import static it.near.sdk.push.PushProcessor.NOTIFICATION_BODY;
import static it.near.sdk.push.PushProcessor.REACTION_ACTION_ID;
import static it.near.sdk.push.PushProcessor.REACTION_BUNDLE;
import static it.near.sdk.push.PushProcessor.REACTION_BUNDLE_ID;
import static it.near.sdk.push.PushProcessor.REACTION_PLUGIN_ID;
import static it.near.sdk.push.PushProcessor.RECIPE_ID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PushProcessorTest {

    @Mock
    RecipesManager mockRecipeManager;
    @Mock
    FormatDecoder mockFormatDecoder;

    PushProcessor pushProcessor;

    Map pushMap;
    private String dummyRecipeId;
    private String dummyReactionName;
    private String dummyReactionAction;
    private String dummyReactionBundleId;
    private String dummyNotificationBody;
    private JSONObject dummyNotification;
    private String dummyCompressedData;
    private String dummyDecompressedData;

    @Before
    public void setUp() throws Exception {
        pushProcessor = new PushProcessor(mockRecipeManager, mockFormatDecoder);
        pushMap = new LinkedTreeMap<String, Object>();

        dummyRecipeId = "dummy_recipe_id";
        dummyReactionName = "dummy_reaction_name";
        dummyReactionAction = "dummy_reaction_action";
        dummyReactionBundleId = "dummy_reaction_bundle_id";
        dummyNotificationBody = "notification body";
        dummyNotification = new JSONObject();
        dummyNotification.put(NOTIFICATION_BODY, dummyNotificationBody);
        dummyCompressedData = "dummy_compressed_data";
        dummyDecompressedData = "dummy_decompressed_data";

        when(mockFormatDecoder.decodeBase64(dummyCompressedData)).thenReturn(new byte[6]);
        when(mockFormatDecoder.decompressZLIB(any(byte[].class))).thenReturn(dummyDecompressedData);
    }

    @Test
    public void oldFormatPush_shouldHaveOldFallbackBehaviour() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
    }

    @Test
    public void pushWithoutTheCompressedBundle_shouldOnlyAskForTheBundle() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
    }

    @Test
    public void pushWithSimpleNotification_shouldNotFallbackDueToMissingBundleId() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, SimpleNotificationReaction.PLUGIN_NAME);  // only for this plugin
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        // pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId); we don't use this
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, times(1)).processRecipe(eq(dummyRecipeId), eq(dummyNotificationBody), eq(SimpleNotificationReaction.PLUGIN_NAME), eq(dummyReactionAction), anyString());
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, SimpleNotificationReaction.PLUGIN_NAME, dummyReactionAction, dummyDecompressedData);
    }

    @Test
    public void pushWithEnabledCompressedBundle_shouldUseTheBundle() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);
        when(mockRecipeManager.processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData))
                .thenReturn(true);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
        verify(mockRecipeManager, times(1)).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
    }

    @Test
    public void pushWithDisabledCompressedBundle_shouldFallbackOnTriple() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);
        // the plugin can't accept the compressed bundle directly (e.g. coupons)
        when(mockRecipeManager.processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData))
                .thenReturn(false);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, times(1)).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
    }

    @Test
    public void whenNotificationIsMissing_shouldRevertToFallbackBehaviour() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        // pushMap.put(NOTIFICATION, dummyNotification.toString()); we don't do this
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
    }

    @Test
    public void whenNotificationIsMalformed_shouldRevertToFallbackBehaviour() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, "not a valid notification object");  // wrong format
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
    }

    @Test
    public void whenNotificationIsMissingTheBody_shouldRevertToFallbackBehaviour() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, "{\"error_cause\" : \"missing_body\"}");  // wrong format
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
    }

    @Test
    public void whenDecompressionThrowsIllegalArgument_shouldFallbackOnTriple() {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);
        when(mockFormatDecoder.decodeBase64(dummyCompressedData)).thenThrow(IllegalArgumentException.class);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
    }

    @Test
    public void whenDecompressionThrowsDataFormat_shouldFallbackOnTriple() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);
        when(mockFormatDecoder.decompressZLIB(any(byte[].class))).thenThrow(DataFormatException.class);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);
    }

    @Test
    public void whenDecompressionThrowsUnsupportedEncoding_shouldFallbackOnTriple() throws Exception {
        pushMap.put(RECIPE_ID, dummyRecipeId);
        pushMap.put(REACTION_PLUGIN_ID, dummyReactionName);
        pushMap.put(REACTION_ACTION_ID, dummyReactionAction);
        pushMap.put(REACTION_BUNDLE_ID, dummyReactionBundleId);
        pushMap.put(NOTIFICATION, dummyNotification.toString());
        pushMap.put(REACTION_BUNDLE, dummyCompressedData);
        when(mockFormatDecoder.decompressZLIB(any(byte[].class))).thenThrow(UnsupportedEncodingException.class);

        boolean result = pushProcessor.processPush(pushMap);
        assertThat(result, is(true));
        verify(mockRecipeManager, never()).processRecipe(dummyRecipeId);
        verify(mockRecipeManager, times(1)).processRecipe(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyReactionBundleId);
        verify(mockRecipeManager, never()).processReactionBundle(dummyRecipeId, dummyNotificationBody, dummyReactionName, dummyReactionAction, dummyDecompressedData);

    }
}
