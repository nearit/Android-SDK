package it.near.sdk.MorpheusNear;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.beans.HasProperty;
import org.hamcrest.core.IsInstanceOf;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.Recipes.Models.Recipe;
import it.near.sdk.Utils.NearJsonAPIUtils;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class MorpheusTest {

    Morpheus morpheus;

    @Before
    public void setUP(){
        morpheus = new Morpheus();
        morpheus.getFactory().getDeserializer().registerResourceClass("test", TestModel.class);
    }

    @Test
    public void parsingElement() throws JSONException {
        JSONObject jsonObject = new JSONObject("{\n" +
                "  \"data\": {\n" +
                "    \"type\": \"test\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"attributes\": {\n" +
                "      \"content\" : \"contenuto\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
        TestModel object = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestModel.class);
        assertNotNull(object);
        assertThat(object, instanceOf(TestModel.class));
        assertEquals("1", object.getId());
        assertEquals("contenuto", object.getContent());

    }

    @Test
    public void parsingList() throws JSONException {
        JSONObject jsonObject = new JSONObject("{\n" +
                "  \"data\" : [{\n" +
                "    \"type\" : \"test\",\n" +
                "    \"id\" : 1,\n" +
                "    \"attributes\" : {\n" +
                "      \"content\" : \"contenuto\"\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"type\" : \"test\",\n" +
                "    \"id\" : 2,\n" +
                "    \"attributes\" : {\n" +
                "      \"content\" : \"contenuto2\"\n" +
                "    }\n" +
                "  } ]\n" +
                "}");
        List<TestModel> objectList = NearJsonAPIUtils.parseList(morpheus, jsonObject, TestModel.class);
        assertNotNull(objectList);
        assertThat(objectList, not(empty()));
        assertThat(objectList, hasSize(2));
        assertThat(objectList, everyItem( IsInstanceOf.<TestModel>instanceOf(TestModel.class)));
    }



}
