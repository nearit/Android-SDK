package it.near.sdk.morpheusnear;

import java.util.Map;

public class MorphUtil {

    public static Morpheus buildMorpheusFrom(Map<String, Class> classes) {
        Morpheus morpheus = new Morpheus();
        for (Map.Entry<String, Class> entry : classes.entrySet()) {
            morpheus.getFactory().getDeserializer().registerResourceClass(entry.getKey(), entry.getValue());
        }
        return morpheus;
    }
}
