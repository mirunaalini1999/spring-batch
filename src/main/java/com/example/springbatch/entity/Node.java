package com.example.springbatch.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Node {

    private String id;
    private int cpuCores;
    private int ram;
    private NodeType type;
    private String endpoint;
    private boolean healthy; // Node health status

    // Constructor
    public Node(String id, int cpuCores, int ram, NodeType type, String endpoint) {
        this.id = id;
        this.cpuCores = cpuCores;
        this.ram = ram;
        this.type = type;
        this.endpoint = endpoint;
        this.healthy = true; // Default to healthy
    }


    public enum NodeType {

        CLOUD_WORKER, MANAGER;
    }
}
