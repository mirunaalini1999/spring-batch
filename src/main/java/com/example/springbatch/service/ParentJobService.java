package com.example.springbatch.service;

import com.example.springbatch.entity.ParentJob;
import com.example.springbatch.entity.SubJob;
import com.example.springbatch.mapper.ParentJobMapper;
import com.example.springbatch.mapper.SubJobMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParentJobService {

    private final ParentJobMapper parentJobMapper;

    private final SubJobMapper subJobMapper;

    public ParentJobService(ParentJobMapper parentJobMapper, SubJobMapper subJobMapper) {

        this.parentJobMapper = parentJobMapper;
        this.subJobMapper = subJobMapper;
    }
    
    public ParentJob create(ParentJob parentJob) {

        parentJobMapper.create(parentJob);
        
        List<SubJob> subJob = createSubJob(parentJob);


        return parentJob;
    }

    private List<SubJob> createSubJob(ParentJob parentJob) {

        Map<String, Object>context = parentJob.getContext();
        Map<String, String> resourceTypeDetails = (Map<String, String>) context.get("resourceTypeDetails");

        Map<String, Long> resourceTypeMap = resourceTypeDetails.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> Long.parseLong(e.getKey())));

        List<SubJob> subJobs = new ArrayList<>();

        for (String resourceType : resourceTypeMap.keySet()) {
            SubJob subJob = new SubJob();
            subJob.setId(resourceTypeMap.get(resourceType)); // Set ID from resourceTypeDetails
            subJob.setParentId(parentJob.getId());
            subJob.setPriority(parentJob.getPriority());
            subJob.setStatus("CREATED");

            Map<String, Object> subContext = new HashMap<>();
            subContext.put("resourceType", resourceType);

            if ("SUBNET".equals(resourceType)) {

                System.out.println("======>"+ resourceTypeMap.get("VIRTUAL_NETWORK"));
                subJob.setDependencyId(resourceTypeMap.get("VIRTUAL_NETWORK"));
                subJob.setIsDependent(true);
                subContext.put("DependsOn", "VIRTUAL_NETWORK");
            }
            else {
                subJob.setIsDependent(false);
            }
            subJob.setContext(subContext);
            subJobMapper.create(subJob);
            subJobs.add(subJob);
        }


        return subJobs;
    }
}
