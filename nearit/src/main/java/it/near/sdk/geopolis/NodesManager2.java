package it.near.sdk.geopolis;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.near.sdk.geopolis.beacons.BeaconNode;
import it.near.sdk.morpheusnear.Morpheus;
import it.near.sdk.utils.NearJsonAPIUtils;

import static it.near.sdk.utils.NearUtils.safe;

public class NodesManager2 {

    private List<Node> nodes;

    private Morpheus morpheus;
    private List<Node> emptyList = Collections.<Node>emptyList();

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
            return emptyList;
        }
    }

    public Node getNode(String nodeId) {
        return nodes != null ? findNodeIn(nodes, nodeId) : null;
    }

    private Node findNodeIn(List<Node> nodes, String id) {
        for (Node node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            } else if (node.children != null) {
                Node foundNode = findNodeIn(node.children, id);
                if (foundNode != null) return foundNode;
            }
        }
        return null;
    }

    public List<Node> getMonitoredNodesOnEnter(String nodeId) {

        if (nodes == null) return emptyList;
        Node node = findNodeIn(nodes, nodeId);
        if (node == null || BeaconNode.isBeacon(node)) return emptyList;

        return getMonitoredNodesOnEnter(node);
    }

    private List<Node> getMonitoredNodesOnEnter(Node node) {
        List<Node> regionsToMonitor = new ArrayList<>();
        regionsToMonitor.addAll(getSibilings(node));

        for (Node child : safe(node.children)) {
            if (!BeaconNode.isBeacon(child))
                regionsToMonitor.add(child);
        }
        return regionsToMonitor;
    }

    private List<Node> getSibilings(Node node) {
        if (node.parent != null)
            return node.parent.children;
        return nodes;
    }

    public List<Node> getMonitoredNodesOnExit(String nodeId) {
        if (nodes == null) return emptyList;
        Node node = findNodeIn(nodes, nodeId);
        if (node == null || BeaconNode.isBeacon(node)) return emptyList;
        if (node.parent == null) return nodes;
        return getMonitoredNodesOnEnter(node.parent);
    }


    public List<Node> getRangedNodesOnEnter(String nodeId) {
        if (nodes == null) return emptyList;
        Node node = findNodeIn(nodes, nodeId);
        if (node == null || BeaconNode.isBeacon(node) || node.children == null)
            return emptyList;

        for (Node child : node.children) {
            if (BeaconNode.isBeacon(child)){
                List<Node> regionsToRange = new ArrayList<>();
                regionsToRange.add(node);
                return regionsToRange;
            }
        }
        return emptyList;
    }
}
