package com.example.springbatch.tasklet;

import com.example.springbatch.component.ApiRetryer;
import com.example.springbatch.component.BucketFileReader;
import com.example.springbatch.entity.SubJob;
import com.example.springbatch.mapper.SubJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubnetTasklet implements Tasklet {

    private final SubJobMapper subJobMapper;

    private final ApiRetryer apiRetryer;

    public SubnetTasklet(SubJobMapper subJobMapper, ApiRetryer apiRetryer) {
        this.subJobMapper = subJobMapper;
        this.apiRetryer = apiRetryer;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Long subJobId = null;
        String status = null;
        try {
            var jobParameters = chunkContext.getStepContext().getJobParameters();
            var jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();

            Long parentJobId = null;
            String subnetPath = null;
            if (jobParameters.containsKey("parentJobId")) {
                parentJobId = (Long) jobParameters.get("parentJobId");
            }
            log.info("******* SUBNET TASKLET STARTED WITH PARENT JOB ID: {} *******", parentJobId);
            if (jobExecutionContext.containsKey("subnetPath")) {
                subnetPath = (String) jobExecutionContext.get("subnetPath");
            }
            SubJob subJob = subJobMapper.findByResourceType(parentJobId, "SUBNET");

            if (subJob == null) {
                log.info("NO SUBNET JOB FOUND FOR PARENT JOB ID {}", parentJobId);
                return RepeatStatus.FINISHED;
            }
            subJobId = subJob.getId();
            subJobMapper.updateStatus(subJobId, "IN_PROGRESS");

//            String fileName = BucketFileReader.getBucketFile("sitappranix", subnetPath);
//            log.info("Subnet file name: {}", fileName);

            String httpResponse = apiRetryer.triggerApiWithRetry(subnetPath);
            log.info("THE HTTP RESPONSE IS "+ httpResponse);

            status = "SUCCESS";

            log.info("******* SUBNET TASKLET ENDED WITH PARENT JOB ID: {} *******", parentJobId);

        } catch (Exception e) {
            status = "FAILED";
            e.printStackTrace();
        }
        finally {
            if (subJobId != null && status != null) {

                log.info("Update status: {} for sub job id: {}", status, subJobId);
                subJobMapper.updateStatus(subJobId, status);
            }
            return RepeatStatus.FINISHED;
        }
    }
}
