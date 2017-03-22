package it.near.sdk.geopolis;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.utils.NearJsonAPIUtils;

public class NodesManager2 {

    private List<Node> nodes;

    private Morpheus morpheus;

    public NodesManager2(Morpheus morpheus) {
        this.morpheus = morpheus;
    }

    public List<Node> setNodes(JSONObject jsonObject) {
        nodes = NearJsonAPIUtils.parseList(morpheus, jsonObject, Node.class);
        return nodes;
    }

    public List<Node> getRoots() {
        if (nodes != null) {
            return nodes;
        } else {
            return Collections.<Node>emptyList();
        }
    }

    public Node getNode(String nodeId) {
        // TODO real impl
        return null;
    }

    public List<Node> getMonitoredNodesOnEnter(String nodeId) {
        // TODO real impl
        return Collections.<Node>emptyList();
    }

    public List<Node> getMonitoredNodesOnExit(String nodeId) {
        // TODO real impl
        return Collections.<Node>emptyList();
    }


    public List<Node> getRangedNodesOnEnter(String nodeId) {
        // TODO real impl
        return Collections.<Node>emptyList();
    }
}
