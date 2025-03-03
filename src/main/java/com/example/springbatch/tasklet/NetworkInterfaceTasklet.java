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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NetworkInterfaceTasklet implements Tasklet {

    private final SubJobMapper subJobMapper;

    private final ApiRetryer apiRetryer;

    public NetworkInterfaceTasklet(SubJobMapper subJobMapper, ApiRetryer apiRetryer) {
        this.subJobMapper = subJobMapper;
        this.apiRetryer = apiRetryer;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        Long subJobId = null;
        String status = null;
        try {
            var jobParameters = chunkContext.getStepContext().getJobParameters();

            Long parentJobId = null;
            if (jobParameters.containsKey("parentJobId")) {
                parentJobId = (Long) jobParameters.get("parentJobId");
            }
            log.info("******* NETWORK INTERFACE TASKLET STARTED WITH PARENT JOB ID: {} *******", parentJobId);
            SubJob subJob = subJobMapper.findByResourceType(parentJobId, "NETWORK_INTERFACE");

            if (subJob == null) {
                log.info("NO NETWORK INTERFACE JOB FOUND FOR PARENT JOB ID {}", parentJobId);
                return RepeatStatus.FINISHED;
            }
            subJobId = subJob.getId();
            subJobMapper.updateStatus(subJobId, "IN_PROGRESS");

//            String fileName = BucketFileReader.getBucketFile("sitappranix", "t67108526/20087324/1063/1729142940000/eastus/network_interface.txt");
//            log.info("Network interface file name: {}", fileName);

            String httpResponse = apiRetryer.triggerApiWithRetry("http://localhost:1020/resource/network_interface");
            log.info("THE HTTP RESPONSE IS "+ httpResponse);

            status = "SUCCESS";

            log.info("******* NETWORK INTERFACE TASKLET ENDED WITH PARENT JOB ID: {} *******", parentJobId);

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
