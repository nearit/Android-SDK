package it.near.sdk.morpheusnear;

import org.hamcrest.core.IsInstanceOf;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import it.near.sdk.morpheusnear.models.TestChildModel;
import it.near.sdk.morpheusnear.models.TestModel;
import it.near.sdk.morpheusnear.models.TestWithChildModel;
import it.near.sdk.utils.NearJsonAPIUtils;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.allOf;
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
        morpheus.getFactory().getDeserializer().registerResourceClass("test_with_child", TestWithChildModel.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("test_child", TestChildModel.class);
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

    @Test
    public void parsingRelationshipSimple() throws JSONException {
        JSONObject jsonObject = new JSONObject("{\n" +
                "  \"data\" : {\n" +
                "    \"id\" : \"a7663e8c-1c7e-4c3f-95d5-df976f07f81a\",\n" +
                "    \"type\" : \"test_with_child\",\n" +
                "    \"attributes\" : {\n" +
                "      \"content\" : \"i've got a child\"\n" +
                "    },\n" +
                "    \"relationships\" : {\n" +
                "      \"child\" : {\n" +
                "        \"data\" : {\n" +
                "          \"id\" : \"e7cde6f7-c2fe-4e4d-9bdc-40dd9b4b4597\",\n" +
                "          \"type\" : \"test_child\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"included\" : [\n" +
                "    {\n" +
                "      \"id\" : \"e7cde6f7-c2fe-4e4d-9bdc-40dd9b4b4597\",\n" +
                "      \"type\" : \"test_child\",\n" +
                "      \"attributes\" : {\n" +
                "        \"favourite_child\" : true\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        TestWithChildModel objWithChild = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestWithChildModel.class);
        assertThat(objWithChild, is(notNullValue()));
        assertThat(objWithChild.getId(), is("a7663e8c-1c7e-4c3f-95d5-df976f07f81a"));
        assertThat(objWithChild.getContent(), is("i've got a child"));
        assertThat(objWithChild.getChild(), is(notNullValue()));
        TestChildModel child = objWithChild.getChild();
        assertThat(child.getIsFavoChild(), is(true));
    }

}
