package it.near.sdk.operation;

import android.content.SharedPreferences;

import com.google.common.collect.Maps;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static it.near.sdk.operation.UserDataCacheManager.SP_MAP_KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDataCacheManagerTest {

    @Mock
    private SharedPreferences mockSharedPreferences;
    @Mock
    private SharedPreferences.Editor mockEditor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private UserDataCacheManager cacheManager;

    @Before
    public void setUp() {
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), ArgumentMatchers.anyString())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);
        cacheManager = new UserDataCacheManager(mockSharedPreferences);
    }

    @Test
    public void emptyCache_shouldBeEmpty() {
        when(mockSharedPreferences.getString(SP_MAP_KEY, null)).thenReturn(null);
        assertTrue(cacheManager.getUserData().isEmpty());
        assertFalse(cacheManager.hasData());
        cacheManager.removeAllData();
        assertTrue(cacheManager.getUserData().isEmpty());
        assertFalse(cacheManager.hasData());
        HashMap<String, String> dummySentData = Maps.newHashMap();
        dummySentData.put("not", "cached");
        assertFalse(cacheManager.removeSentData(dummySentData));
        assertTrue(cacheManager.getUserData().isEmpty());
        assertFalse(cacheManager.hasData());
        cacheManager.removeAllData();
        assertTrue(cacheManager.getUserData().isEmpty());
        assertFalse(cacheManager.hasData());
    }

    @Test
    public void ifNotCached_shouldNotBeRemoved() {
        cacheManager.setUserData("dummy", "dummy");
        verify(mockEditor, times(1)).putString(anyString(), stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), "{\"dummy\":\"dummy\"}");
        assertThat(cacheManager.getUserData().size(), is(1));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy", "dummy"));
        when(mockSharedPreferences.getString(SP_MAP_KEY, null)).thenReturn("{\"dummy\":\"dummy\"}");
        //  should not remove the following data
        HashMap<String, String> notCachedData = Maps.newHashMap();
        notCachedData.put("not", "cached");
        cacheManager.removeSentData(notCachedData);
        assertThat(cacheManager.getUserData().size(), is(1));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy", "dummy"));
    }

    @Test
    public void previouslyCached_shouldBeReturned() {
        cacheManager.setUserData("dummy", "dummy");
        verify(mockEditor, times(1)).putString(anyString(), stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), "{\"dummy\":\"dummy\"}");
        assertThat(cacheManager.getUserData().size(), is(1));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy", "dummy"));
        when(mockSharedPreferences.getString(SP_MAP_KEY, null)).thenReturn("{\"dummy\":\"dummy\"}");
        cacheManager.setUserData("dummy2", "dummy2");
        verify(mockEditor, times(2)).putString(anyString(), stringCaptor.capture());
        assertEquals(stringCaptor.getValue(),
                "{" +
                        "\"dummy\":\"dummy\"," +
                        "\"dummy2\":\"dummy2\"" +
                        "}");
        assertThat(cacheManager.getUserData().size(), is(2));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy", "dummy"));
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy2", "dummy2"));
    }

    @Test
    public void addMultipleUserData() {
        cacheManager.setUserData("dummy", "dummy");
        cacheManager.setUserData("dummy2", "dummy2");
        cacheManager.setUserData("dummy3", "dummy3");
        assertThat(cacheManager.getUserData().size(), is(3));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy", "dummy"));
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy2", "dummy2"));
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy3", "dummy3"));
        cacheManager.setUserData("dummy3", "dummy4");
        assertThat(cacheManager.getUserData().size(), is(3));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy3", "dummy4"));
    }

    @Test
    public void updateDataPoint() {
        cacheManager.setUserData("dummy", "dummy");
        verify(mockEditor, times(1)).putString(anyString(), stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), "{\"dummy\":\"dummy\"}");
        assertThat(cacheManager.getUserData().size(), is(1));
        assertTrue(cacheManager.hasData());
        cacheManager.setUserData("dummy", "dummy2");
        assertTrue(cacheManager.getUserData().containsKey("dummy"));
        verify(mockEditor, times(2)).putString(anyString(), stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), "{\"dummy\":\"dummy2\"}");
        assertThat(cacheManager.getUserData().size(), is(1));
        assertTrue(cacheManager.hasData());
        assertThat(cacheManager.getUserData(), IsMapContaining.hasEntry("dummy", "dummy2"));
    }

    @Test
    public void removeSentData() {
        HashMap<String, String> toBeRemoved = Maps.newHashMap();
        cacheManager.setUserData("dummy", "dummy");
        cacheManager.setUserData("dummy2", "dummy2");
        //  2 add
        verify(mockEditor, times(2)).putString(anyString(), anyString());
        toBeRemoved.put("dummy", "dummy");
        cacheManager.removeSentData(toBeRemoved);
        assertThat(cacheManager.getUserData().size(), is(1));
        //  + 1 remove
        verify(mockEditor, times(3)).putString(anyString(), anyString());
        cacheManager.setUserData("dummy", "dummy");
        cacheManager.setUserData("dummy", "dummy2");
        //  + 1 add + 1 update
        verify(mockEditor, times(5)).putString(anyString(), anyString());
        toBeRemoved = Maps.newHashMap();
        toBeRemoved.put("dummy", "dummy");
        cacheManager.removeSentData(toBeRemoved);
        assertThat(cacheManager.getUserData().size(), is(2));
        //  no more putString
        verify(mockEditor, times(5)).putString(anyString(), anyString());
    }

    @Test
    public void removeAllData() {
        cacheManager.setUserData("dummy", "dummy");
        cacheManager.setUserData("dummy2", "dummy2");
        cacheManager.setUserData("dummy3", "dummy3");
        cacheManager.removeAllData();
        verify(mockEditor).clear();
        assertTrue(cacheManager.getUserData().isEmpty());
    }

    @Test
    public void deserializingMalformedString_shouldReturnEmptyMap() {
        when(mockSharedPreferences.getString(SP_MAP_KEY, null)).thenReturn("Malformed");
        assertTrue(cacheManager.getUserData().isEmpty());
    }

}