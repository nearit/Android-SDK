package it.near.sdk.morpheusnear;

import org.hamcrest.core.IsInstanceOf;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import it.near.sdk.morpheusnear.models.TestChildModel;
import it.near.sdk.morpheusnear.models.TestModel;
import it.near.sdk.morpheusnear.models.TestWithChildModel;
import it.near.sdk.morpheusnear.models.TestWithChildrenModel;
import it.near.sdk.TestUtils;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.NearJsonAPIUtils;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created by cattaneostefano on 27/02/2017.
 */

public class MorpheusTest {

    private static final String MORPHEUS_TEST_RES_FOLDER = "morpheus";
    private Morpheus morpheus;

    @Before
    public void setUP() {
        morpheus = new Morpheus();
        morpheus.getFactory().getDeserializer().registerResourceClass("test", TestModel.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("test_child", TestChildModel.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("test_with_child", TestWithChildModel.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("test_with_children", TestWithChildrenModel.class);
    }

    @Test
    public void parseElement() throws Exception {
        JSONObject jsonObject = readJsonFile("single_resource.json");
        TestModel object = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestModel.class);
        assertNotNull(object);
        assertThat(object, instanceOf(TestModel.class));
        assertThat(object.getId(), is("1"));
        assertThat(object.content, is("contenuto"));
        assertThat(object.double_value.doubleValue(), is(45.09843));
        assertThat(object.int_value.intValue(), is(5000));
    }

    @Test
    public void parseElementWithExtraAttribute() throws Exception {
        JSONObject jsonObject = readJsonFile("extra_attribute_resource.json");
        TestModel object = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestModel.class);
        assertNotNull(object);
        assertThat(object, instanceOf(TestModel.class));
        assertThat(object.getId(), is("1"));
        assertThat(object.content, is("contenuto"));
    }

    @Test
    public void parseElementWithMissingAttribute() throws Exception {
        JSONObject jsonObject = readJsonFile("missing_attributes_resource.json");
        TestModel object = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestModel.class);
        assertNotNull(object);
        assertThat(object, instanceOf(TestModel.class));
        assertThat(object.getId(), is("1"));
        assertThat(object.content, is(nullValue()));
    }

    @Test
    public void parsingList() throws Exception {
        JSONObject jsonObject = readJsonFile("resource_array.json");
        List<TestModel> objectList = NearJsonAPIUtils.parseList(morpheus, jsonObject, TestModel.class);
        assertNotNull(objectList);
        assertThat(objectList, not(empty()));
        assertThat(objectList, hasSize(2));
        assertThat(objectList, everyItem(IsInstanceOf.<TestModel>instanceOf(TestModel.class)));
    }

    @Test
    public void parsingRelationshipSimple() throws Exception {
        JSONObject jsonObject = readJsonFile("simple_relationship_resource.json");
        TestWithChildModel objWithChild = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestWithChildModel.class);
        assertThat(objWithChild, is(notNullValue()));
        assertThat(objWithChild.getId(), is("a7663e8c-1c7e-4c3f-95d5-df976f07f81a"));
        assertThat(objWithChild.content, is("i've got a child"));
        assertThat(objWithChild.child, is(notNullValue()));
        TestChildModel child = objWithChild.child;
        assertThat(child.getIsFavoChild(), is(true));
    }

    @Test
    public void parsingMultipleRelationship() throws Exception {
        JSONObject jsonObject = readJsonFile("multi_relationship_resource.json");
        TestWithChildrenModel objWithChildren = NearJsonAPIUtils.parseElement(morpheus, jsonObject, TestWithChildrenModel.class);
        assertThat(objWithChildren, is(notNullValue()));
        assertThat(objWithChildren.getId(), is("a7663e8c-1c7e-4c3f-95d5-df976f07f81a"));
        assertThat(objWithChildren.content, is("i've got children"));
        List<TestChildModel> children = objWithChildren.children;
        assertThat(children, hasSize(2));
        assertThat(children.get(0).getId(), is("e7cde6f7-c2fe-4e4d-9bdc-40dd9b4b4597"));
        assertThat(children.get(0).getIsFavoChild(), is(false));
        assertThat(children.get(1).getId(), is("d232d2c1-1c47-4888-bb38-5c7e0893dea5"));
        assertThat(children.get(1).getIsFavoChild(), is(false));
    }

    // TODO inheritance in models
    // TODO transitive relationships
    // TODO circular relationships

    private JSONObject readJsonFile(String filename) throws Exception {
        return TestUtils.readJsonFile(getClass(), MORPHEUS_TEST_RES_FOLDER + "/" + filename);
    }

}
