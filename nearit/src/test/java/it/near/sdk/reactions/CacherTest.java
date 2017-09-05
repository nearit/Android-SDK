package it.near.sdk.reactions;

import android.content.SharedPreferences;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import it.near.sdk.reactions.testmodels.ModelForCache;

import static it.near.sdk.reactions.Cacher.KEY_LIST;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CacherTest {

    private Cacher<ModelForCache> cacher;

    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor editor;

    private List<ModelForCache> list;

    @Before
    public void setUp() throws Exception {
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        cacher = new Cacher<>(sharedPreferences);
    }

    @Test
    public void simplePersistence() throws Exception {
        list = Lists.newArrayList(
                new ModelForCache("string1", 1, 1L, buildMap(1)),
                new ModelForCache("string2", 2, 2L, buildMap(2))
        );
        cacher.persistList(list);
        // capture argument
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(KEY_LIST), captor.capture());
        String stringifiedList = captor.getValue();
        when(sharedPreferences.getString(eq(KEY_LIST), anyString())).thenReturn(stringifiedList);
        Type type = new TypeToken<List<ModelForCache>>() {
        }.getType();
        List<ModelForCache> modelForCaches = cacher.loadList(type);
        assertThat(modelForCaches, contains(list.toArray()));
    }

    @Test
    public void nullValuesPersistance() throws Exception {
        list = Lists.newArrayList(
                new ModelForCache(null, 0, 0L, null)
        );
        cacher.persistList(list);
        // capture argument
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(KEY_LIST), captor.capture());
        String stringifiedList = captor.getValue();
        when(sharedPreferences.getString(eq(KEY_LIST), anyString())).thenReturn(stringifiedList);
        Type type = new TypeToken<List<ModelForCache>>() {
        }.getType();
        List<ModelForCache> modelForCaches = cacher.loadList(type);
        assertThat(modelForCaches, contains(list.toArray()));
    }

    @Test
    public void hashMapWithNullValues() throws Exception {
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("string", "my_string");
        map.put("int", ((Integer) 3).doubleValue());
        map.put("long", (Double) 9D);
        map.put("list", Lists.newArrayList("one", "two"));
        map.put("null_object", null);
        list = Lists.newArrayList(
                new ModelForCache(null, 0, 0L, map)
        );
        cacher.persistList(list);
        // capture argument
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(KEY_LIST), captor.capture());
        String stringifiedList = captor.getValue();
        when(sharedPreferences.getString(eq(KEY_LIST), anyString())).thenReturn(stringifiedList);
        Type type = new TypeToken<List<ModelForCache>>() {
        }.getType();
        List<ModelForCache> modelForCaches = cacher.loadList(type);
        assertThat(modelForCaches, contains(list.toArray()));
    }

    private void buildList() {
        list = Lists.newArrayList(
                new ModelForCache("string1", 1, 1L, buildMap(1)),
                new ModelForCache("string2", 2, 2L, buildMap(2))
        );
    }

    private Map<String, Object> buildMap(int i) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("value", (double) i);
        return map;
    }
}
