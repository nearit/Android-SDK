package it.near.sdk.Geopolis;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Geopolis.Beacons.BeaconNode;
import it.near.sdk.Geopolis.GeoFence.GeoFenceNode;
import it.near.sdk.MorpheusNear.Morpheus;
import it.near.sdk.Utils.NearUtils;

/**
 * Created by cattaneostefano on 10/10/2016.
 */

public class NodesManager {

    private static final String PREFS_SUFFIX = "NodesManager";
    private static final String NODES_CONFIG = "nodes_config";
    private List<Node> nodes;
    private Context mContext;
    private Morpheus morpheus;
    private SharedPreferences sp;

    public NodesManager(Context mContext) {
        this.mContext = mContext;
        this.nodes = new ArrayList<>();
        setUpMorpheusParser();

        String PACK_NAME = mContext.getApplicationContext().getPackageName();
        String PREFS_NAME = PACK_NAME + PREFS_SUFFIX;
        sp = mContext.getSharedPreferences(PREFS_NAME, 0);
    }

    /**
     * Set up Morpheus parser. Morpheus parses jsonApi encoded resources
     * https://github.com/xamoom/Morpheus
     * We didn't actually use this library due to its minSdkVersion. We instead imported its code and adapted it. And then fixed it.
     */
    private void setUpMorpheusParser() {
        morpheus = new Morpheus();
        // register your resources

        morpheus.getFactory().getDeserializer().registerResourceClass("nodes", Node.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("beacon_nodes", BeaconNode.class);
        morpheus.getFactory().getDeserializer().registerResourceClass("geofence_nodes", GeoFenceNode.class);
    }

    public List<Node> parseAndSetNodes(JSONObject jsonObject) {
        saveConfig(jsonObject.toString());
        this.nodes = NearUtils.parseList(morpheus, jsonObject, Node.class);
        return nodes;
    }

    public Node nodeFromId(String id) {
        if (nodes == null) {
            nodes = getNodes();
        }
        return findNode(nodes, id);
    }

    private Node findNode(List<Node> nodes, String id) {
        if (nodes == null){
            try {
                nodes = loadNodes();
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            if (nodes == null) return null;
        }
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().equals(id)) return node;
            Node foundNode = findNode(node.getChildren(), id);
            if (foundNode != null) return foundNode;
        }
        return null;
    }

    public List<Node> getNodes() {
        if (nodes == null) {
            try {
                nodes = loadNodes();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    private List<Node> loadNodes() throws JSONException {
        String config = getSavedConfig();
        if (config == null) return null;
        JSONObject configJson = new JSONObject(config);
        return NearUtils.parseList(morpheus, configJson, Node.class);
    }

    private void saveConfig(String json){
        sp.edit().putString(NODES_CONFIG, json).apply();
    }

    private String getSavedConfig(){
        return sp.getString(NODES_CONFIG, null);
    }
}
