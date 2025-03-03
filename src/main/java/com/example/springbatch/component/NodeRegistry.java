package com.example.springbatch.component;

import com.example.springbatch.entity.Node;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class NodeRegistry {

    private static Map<String, Node> nodes = new ConcurrentHashMap<>();

    private NodeRegistry() {

    }

    // Register a new node
    public static void registerNode(Node node) {
        nodes.put(node.getId(), node);
    }

    // Get all registered nodes
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    // Get available threads across all nodes
    public static int getAvailableThreads(Node.NodeType nodeType) {
        return nodes.values().stream()
                .filter(node -> node.getType() == nodeType)
                .mapToInt(Node::getCpuCores) // Sum up available CPU cores
                .sum();
    }

    // Get available threads in a specific node
    public int getAvailableThreadsInNode(String nodeId) {
        Node node = nodes.get(nodeId);
        return (node != null) ? node.getCpuCores() : 0;
    }

    // Update node CPU cores (e.g., when a job is allocated or completed)
    public void updateNode(String nodeId, int usedCores) {
        Node node = nodes.get(nodeId);
        if (node != null) {
            node.setCpuCores(Math.max(node.getCpuCores() - usedCores, 0)); // Prevent negative values
        }
    }
}
