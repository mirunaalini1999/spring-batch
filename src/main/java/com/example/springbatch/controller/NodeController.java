package com.example.springbatch.controller;

import com.example.springbatch.entity.Node;
import com.example.springbatch.entity.ParentJob;
import com.example.springbatch.service.NodeService;
import com.example.springbatch.service.ParentJobService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final NodeService nodeService;

    public NodeController(NodeService nodeService) {

        this.nodeService = nodeService;
    }

    @PostMapping("/create")
    public List<Node> createJob(@RequestBody List<Node> nodes){

        return nodeService.createNode(nodes);
    }
}
