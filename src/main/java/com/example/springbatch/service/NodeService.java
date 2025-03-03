package com.example.springbatch.service;

import com.example.springbatch.component.NodeRegistry;
import com.example.springbatch.config.TaskExecutorConfig;
import com.example.springbatch.entity.Node;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NodeService {

    public List<Node> createNode(List<Node> nodes) {

        List<Node> nodeList = new ArrayList<>();
        for (Node node : nodes) {
            Node nodeData = new Node(node.getId(), node.getCpuCores(), node.getRam(), node.getType(), node.getEndpoint());
            nodeList.add(nodeData);
            NodeRegistry.registerNode(nodeData);
            ThreadPoolTaskExecutor taskExecutor = TaskExecutorConfig.getInstance();
            //  Added a null check before updating the executor
            if (taskExecutor != null) {
                TaskExecutorConfig.initialize();
            }
        }
        return nodeList;
    }
}
