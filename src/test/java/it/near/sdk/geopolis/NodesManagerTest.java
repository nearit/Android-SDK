package it.near.sdk.geopolis;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import it.near.sdk.TestUtils;
import it.near.sdk.geopolis.beacons.BeaconNode;
import it.near.sdk.geopolis.geofences.GeoFenceNode;
import it.near.sdk.morpheusnear.Morpheus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class NodesManagerTest {

    private static final String TEST_RES_FOLDER = "nodes_manager";

    private NodesManager2 nodesManager;

    @Before
    public void setUp() {
        Morpheus morpheus = new Morpheus();
        morpheus.getFactory().getDeserializer().registerResourceClass("nodes", Node.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("beacon_nodes", BeaconNode.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("geofence_nodes", GeoFenceNode.class);

        nodesManager = new NodesManager2(morpheus);
    }

    @Test
    public void shouldHandleEmptyConfig() throws Exception {
        JSONObject input = readJsonFile("empty_config.json");
        List<Node> initalNodes = nodesManager.setNodes(input);
        assertThat(initalNodes, hasSize(0));
        assertThat(nodesManager.getRoots(), hasSize(0));
        assertThat(nodesManager.getNode("dummy_id"), is(nullValue()));
    }

    @Test
    public void shouldHandleSingleGFConfig() throws Exception {
        JSONObject input = readJsonFile("single_gf.json");
        List<Node> initalNodes = nodesManager.setNodes(input);
        assertThat(initalNodes, hasSize(1));
        assertThat(nodesManager.getRoots(), hasSize(1));
        assertThat(nodesManager.getNode("d7881a25-fc82-49ec-836d-d47276e38a55"), is(notNullValue()));
        assertThat(nodesManager.getNode("dummy_id"), is(nullValue()));
        assertThat(nodesManager.getMonitoredNodesOnEnter("d7881a25-fc82-49ec-836d-d47276e38a55"), hasSize(1));
        assertThat(nodesManager.getMonitoredNodesOnExit("d7881a25-fc82-49ec-836d-d47276e38a55"), hasSize(1));
        assertThat(nodesManager.getRangedNodesOnEnter("d7881a25-fc82-49ec-836d-d47276e38a55"), hasSize(0));
    }

    @Test
    public void shouldHandleMultiGFConfig() throws Exception {
        JSONObject input = readJsonFile("gf_array.json");
        List<Node> initalNodes = nodesManager.setNodes(input);
        assertThat(initalNodes, hasSize(4));
        assertThat(nodesManager.getRoots(), hasSize(4));
        assertThat(nodesManager.getNode("f4a62f53-5130-479d-ba6b-151255307dab"), is(notNullValue()));
        assertThat(nodesManager.getNode("770fc5ef-fcb3-44e1-945d-a5c9ce16f1e3"), is(notNullValue()));
        assertThat(nodesManager.getNode("dummy_id"), is(nullValue()));
        assertThat(nodesManager.getMonitoredNodesOnEnter("f4a62f53-5130-479d-ba6b-151255307dab"), hasSize(4));
        assertThat(nodesManager.getMonitoredNodesOnExit("770fc5ef-fcb3-44e1-945d-a5c9ce16f1e3"), hasSize(4));
        assertThat(nodesManager.getRangedNodesOnEnter("770fc5ef-fcb3-44e1-945d-a5c9ce16f1e3"), hasSize(0));
    }

    @Test
    public void shoudlHandleMultiLevelGFConfig() throws Exception {
        JSONObject input = readJsonFile("multi_level_gf.json");
        List<Node> initalNodes = nodesManager.setNodes(input);
        assertThat(initalNodes, hasSize(10));
        assertThat(nodesManager.getRoots(), hasSize(10));
        assertThat(nodesManager.getNode("48d37439-8181-4f4c-8028-584ff6ca79a9"), is(notNullValue()));
        assertThat(nodesManager.getNode("214cf1d1-19bb-46fa-aa46-1c8e115db6c1"), is(notNullValue()));
        assertThat(nodesManager.getNode("dummy_id"), is(nullValue()));
        assertThat(nodesManager.getMonitoredNodesOnEnter("48d37439-8181-4f4c-8028-584ff6ca79a9"), hasSize(10));
        assertThat(nodesManager.getMonitoredNodesOnExit("48d37439-8181-4f4c-8028-584ff6ca79a9"), hasSize(10));
        assertThat(nodesManager.getMonitoredNodesOnEnter("e5d67e06-57e9-4c97-bf5d-2f7c3c4510f4"), hasSize(15));
        assertThat(nodesManager.getMonitoredNodesOnExit("e5d67e06-57e9-4c97-bf5d-2f7c3c4510f4"), hasSize(10));
        assertThat(nodesManager.getRangedNodesOnEnter("e5d67e06-57e9-4c97-bf5d-2f7c3c4510f4"), hasSize(0));
    }

    @Test
    public void shouldHandleGFAndBeaconConfig() throws Exception {
        JSONObject input = readJsonFile("beacon_areas_in_bg.json");
        List<Node> initalNodes = nodesManager.setNodes(input);
        assertThat(initalNodes, hasSize(10));
        assertThat(nodesManager.getRoots(), hasSize(10));
        assertThat(nodesManager.getNode("d142ce27-f22a-4462-b23e-715331d01e1b"), is(notNullValue()));
        assertThat(nodesManager.getNode("4435d9fb-c0fe-48a7-811b-87769e38b84d"), is(notNullValue()));
        assertThat(nodesManager.getNode("6e076bcb-f583-4643-a192-122f98138530"), is(notNullValue()));
        assertThat(nodesManager.getNode("e2c3174c-bfb9-4a16-aa28-b05fe310e8ad"), is(notNullValue()));
        assertThat(nodesManager.getNode("28160b69-52a8-4f96-8fe2-aaa36c9bd794"), is(notNullValue()));
        assertThat(nodesManager.getNode("ca7bb03e-beef-4554-bd9e-035f06374d4b"), is(notNullValue()));
        assertThat(nodesManager.getNode("1a8613a4-134b-4504-b0c8-62d47422afdf"), is(notNullValue()));
        // entering a root node with no children
        assertThat(nodesManager.getMonitoredNodesOnEnter("528ac400-6272-4992-afba-672c037a12a0"), hasSize(10));
        // entering a root node with 5 children
        assertThat(nodesManager.getMonitoredNodesOnEnter("4435d9fb-c0fe-48a7-811b-87769e38b84d"), hasSize(15));
        // exiting a root node that had children
        assertThat(nodesManager.getMonitoredNodesOnExit("4435d9fb-c0fe-48a7-811b-87769e38b84d"), hasSize(10));
        // entering a node with 4 sibiligs and 1 child
        assertThat(nodesManager.getMonitoredNodesOnEnter("6e076bcb-f583-4643-a192-122f98138530"), hasSize(6));
        // exiting from that node
        assertThat(nodesManager.getMonitoredNodesOnExit("6e076bcb-f583-4643-a192-122f98138530"), hasSize(15));
        // entering a node with no sibilings and 7 children
        assertThat(nodesManager.getMonitoredNodesOnEnter("e2c3174c-bfb9-4a16-aa28-b05fe310e8ad"), hasSize(8));
        // exiting that node
        assertThat(nodesManager.getMonitoredNodesOnExit("e2c3174c-bfb9-4a16-aa28-b05fe310e8ad"), hasSize(6));
        // entering a node with 6 sibilings and beacon children
        assertThat(nodesManager.getMonitoredNodesOnEnter("28160b69-52a8-4f96-8fe2-aaa36c9bd794"), hasSize(7));
        // exiting that node
        assertThat(nodesManager.getMonitoredNodesOnExit("28160b69-52a8-4f96-8fe2-aaa36c9bd794"), hasSize(8));
        // entering a beacon node, special case
        assertThat(nodesManager.getMonitoredNodesOnEnter("ca7bb03e-beef-4554-bd9e-035f06374d4b"), hasSize(0));
        // exiting a beacon node, special case
        assertThat(nodesManager.getMonitoredNodesOnExit("ca7bb03e-beef-4554-bd9e-035f06374d4b"), hasSize(0));
        // ranging nodes of nodes with no beacon chidren
        assertThat(nodesManager.getRangedNodesOnEnter("4435d9fb-c0fe-48a7-811b-87769e38b84d"), hasSize(0));
        assertThat(nodesManager.getRangedNodesOnEnter("6e076bcb-f583-4643-a192-122f98138530"), hasSize(0));
        assertThat(nodesManager.getRangedNodesOnEnter("e2c3174c-bfb9-4a16-aa28-b05fe310e8ad"), hasSize(0));
        // ranging nodes of a node with 2 beacon children
        assertThat(nodesManager.getRangedNodesOnEnter("28160b69-52a8-4f96-8fe2-aaa36c9bd794"), hasSize(2));
        // ranging nodes of a beacon, special case
        assertThat(nodesManager.getRangedNodesOnEnter("ca7bb03e-beef-4554-bd9e-035f06374d4b"), hasSize(0));

    }

    private JSONObject readJsonFile(String filename) throws Exception {
        return TestUtils.readJsonFile(getClass(), TEST_RES_FOLDER + "/" + filename);
    }

}
