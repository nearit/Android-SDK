package it.near.sdk.trackings;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static it.near.sdk.trackings.TrackCache.KEY_DISK_CACHE;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TrackCacheTest {

    private static final boolean DEFAULT_SENDING_STATUS = false;
    @Mock
    SharedPreferences mockSharedPreferences;
    @Mock
    SharedPreferences.Editor mockEditor;
    @Captor
    private ArgumentCaptor<Set<String>> stringSetCaptor;

    private TrackCache trackCache;

    @Before
    public void setUp() {
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putStringSet(anyString(), ArgumentMatchers.<String>anySet())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);
        trackCache = new TrackCache(mockSharedPreferences);
    }

    @Test
    public void emptyCache_shouldBeEmpty() {
        // empty cache
        when(mockSharedPreferences.getStringSet(KEY_DISK_CACHE, null)).thenReturn(null);
        // should be empty empty
        assertThat(trackCache.getRequests(), hasSize(0));
        trackCache.removeAll();
        // should still be empty after removing all trackings
        assertThat(trackCache.getRequests(), hasSize(0));
        TrackRequest dummy = new TrackRequest("", "");
        // should not remove non cached trackings
        assertThat(trackCache.removeFromCache(dummy), is(false));
        assertThat(trackCache.getRequests(), hasSize(0));
        trackCache.removeAll();
        assertThat(trackCache.getRequests(), hasSize(0));
    }

    @Test
    public void trackRequest_shouldCached() {
        when(mockSharedPreferences.getStringSet(KEY_DISK_CACHE, null)).thenReturn(null);
        assertThat(trackCache.getRequests(), hasSize(0));
        TrackRequest dummy = new TrackRequest("dummy", "dummy");
        // cache with one tracking
        trackCache.addToCache(dummy);
        // should write item on disk
        verify(mockEditor, times(1)).putStringSet(anyString(), stringSetCaptor.capture());
        assertThat(stringSetCaptor.getValue(), hasItem(dummy.getJsonObject().toString()));
        // should return tracking
        assertThat(trackCache.getRequests(), hasSize(1));
        assertThat(trackCache.getRequests(), hasItem(dummy));
        // after removing non cached tracking
        trackCache.removeFromCache(new TrackRequest("not", "cached"));
        // should return tracking
        assertThat(trackCache.getRequests(), hasSize(1));
        assertThat(trackCache.getRequests(), hasItem(dummy));
        // after adding another tracking
        TrackRequest dummy2 = new TrackRequest("dummy2", "dummy2");
        trackCache.addToCache(dummy2);
        verify(mockEditor, times(2)).putStringSet(anyString(), stringSetCaptor.capture());
        // writes both request to cache
        assertThat(stringSetCaptor.getValue(), hasItems(
                dummy.getJsonObject().toString(),
                dummy2.getJsonObject().toString()));
        // reloading the cache
        assertThat(trackCache.getRequests(), hasSize(2));
        assertThat(trackCache.getRequests(), hasItems(dummy, dummy2));
    }

    @Test
    public void cachedTracking_shouldBeRemoved() {
        when(mockSharedPreferences.getStringSet(KEY_DISK_CACHE, null)).thenReturn(null);
        assertThat(trackCache.getRequests(), hasSize(0));
        TrackRequest dummy = new TrackRequest("dummy", "dummy");
        TrackRequest dummy2 = new TrackRequest("dummy2", "dummy2");
        // cache with 2 trackings
        trackCache.addToCache(dummy);
        trackCache.addToCache(dummy2);
        // removing tracking should be successful
        assertThat(trackCache.removeFromCache(dummy), is(true));
        // and tracking should be removed
        assertThat(trackCache.getRequests(), not(hasItem(dummy)));
        assertThat(trackCache.getRequests(), hasItem(dummy2));
        // removing already removed tracking should not be successful
        assertThat(trackCache.removeFromCache(dummy), is(false));
        // removing the last tracking should be successful
        assertThat(trackCache.removeFromCache(dummy2), is(true));
        // and the cache should be empty
        assertThat(trackCache.getRequests(), not(anyOf(hasItem(dummy), hasItem(dummy2))));
        assertThat(trackCache.getRequests(), hasSize(0));
    }

    @Test
    public void previouslyCachedTrackings_shouldBeReturned() {
        TrackRequest dummy = new TrackRequest("dummy", "dummy");
        TrackRequest dummy2 = new TrackRequest("dummy2", "dummy2");
        Set<String> setToReturn = new HashSet<>();
        setToReturn.add(dummy.getJsonObject().toString());
        setToReturn.add(dummy2.getJsonObject().toString());
        when(mockSharedPreferences.getStringSet(KEY_DISK_CACHE, null)).thenReturn(setToReturn);
        assertThat(trackCache.getRequests(), hasSize(2));
        assertThat(trackCache.getRequests(), hasItems(dummy, dummy2));
    }

    @Test
    public void sendingStatus_shouldNotBePersisted() {
        TrackRequest dummyRequest = new TrackRequest("dummy", "dummy");
        trackCache.addToCache(dummyRequest);
        assertThat(trackCache.getRequests().get(0).sending, is(DEFAULT_SENDING_STATUS));
        dummyRequest.sending = true;
        assertThat(trackCache.getRequests().get(0).sending, is(true));
        Set<String> setToReturn = new HashSet<>();
        setToReturn.add("{\"url\" : \"dummy\", \"body\" : \"dummy\"}"); /* no sending status is returned */
        when(mockSharedPreferences.getStringSet(KEY_DISK_CACHE, null)).thenReturn(setToReturn);
        trackCache = new TrackCache(mockSharedPreferences);
        assertThat(trackCache.getRequests().get(0), is(dummyRequest));
        assertThat(trackCache.getRequests().get(0).sending, is(DEFAULT_SENDING_STATUS));
    }

    @Test
    public void ifOptedOut_shouldClearCacheAndSP() {
        TrackRequest dummy = new TrackRequest("dummy", "dummy");
        trackCache.addToCache(dummy);
        assertThat(trackCache.getRequests(), (hasItem(dummy)));

        trackCache.onOptOut();
        verify(mockEditor, atLeastOnce()).clear();
        assertThat(trackCache.getRequests(), not(hasItem(dummy)));
    }
}
