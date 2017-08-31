package it.near.sdk.morpheusnear;

import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class MorphUtilTest {

    @Test
    public void shouldBuildProperMorpheus() {
        Map<String, Class> map = Maps.newHashMap();
        map.put("string", String.class);
        map.put("long", Long.class);
        map.put("date", Date.class);
        map.put("list", List.class);

        Deserializer.setRegisteredClasses(Maps.<String, Class>newHashMap());
        Morpheus morpheus = MorphUtil.buildMorpheusFrom(map);

        assertThat(morpheus.getFactory().getDeserializer().getRegisteredClasses(), is(map));
    }

}