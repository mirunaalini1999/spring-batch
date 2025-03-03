package com.example.springbatch.controller;

import com.example.springbatch.service.ParentJobService;
import com.example.springbatch.entity.ParentJob;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final ParentJobService parentJobService;

    public JobController(ParentJobService parentJobService) {

        this.parentJobService = parentJobService;
    }

    @PostMapping("/create")

    public ParentJob createJob(@RequestBody ParentJob parentJob){

        return parentJobService.create(parentJob);
    }
}
