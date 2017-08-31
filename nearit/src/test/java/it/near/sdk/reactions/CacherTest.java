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

    List<ModelForCache> list;

    @Before
    public void setUp() throws Exception {
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        cacher = new Cacher<>(sharedPreferences);
        buildList();
    }

    private void buildList() {
        list = Lists.newArrayList(
                new ModelForCache("string1", 1, 1L, buildHash(1)),
                new ModelForCache("string2", 2, 2L, buildHash(2))
        );
    }

    private Map<String, Object> buildHash(int i) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("value", Double.valueOf(i));
        return map;
    }

    @Test
    public void simplePersistence() throws Exception {
        cacher.persistList(list);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(KEY_LIST), captor.capture());
        String stringifiedList = captor.getValue();
        when(sharedPreferences.getString(eq(KEY_LIST), anyString())).thenReturn(stringifiedList);
        Type type = new TypeToken<List<ModelForCache>>() {}.getType();
        List<ModelForCache> modelForCaches = cacher.loadList(type);
        assertThat(modelForCaches, contains(list.toArray()));
    }
}
