package com.example.springbatch.component;

import com.example.springbatch.entity.ParentJob;
import com.example.springbatch.entity.SubJob;
import com.example.springbatch.mapper.ParentJobMapper;
import com.example.springbatch.mapper.SubJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final ParentJobMapper parentJobMapper;
    private final SubJobMapper subJobMapper;
    private final SpringBatchFlow springBatchFlow;
    private final JobRegistry jobRegistry;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    @Autowired
    public BatchJobScheduler(JobLauncher jobLauncher, ParentJobMapper parentJobMapper, SubJobMapper subJobMapper, SpringBatchFlow springBatchFlow, JobRegistry jobRegistry, JobOperator jobOperator, JobExplorer jobExplorer) {

        this.jobLauncher = jobLauncher;
        this.parentJobMapper = parentJobMapper;
        this.subJobMapper = subJobMapper;
        this.springBatchFlow = springBatchFlow;
        this.jobRegistry = jobRegistry;
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
    }


    @Async  // Runs this method in a separate thread
    @Scheduled(fixedDelay = 3600000) // Run every 1 hour = 3600000
    public void runBatchJob() {

        try {
            log.info("+++++++++++++++ SCHEDULER +++++++++++++++");

            List<ParentJob> highestPriorityJob = parentJobMapper.findJobBasedOnPriority(1L);

            if (CollectionUtils.isEmpty(highestPriorityJob)) {

                // First time discovery triggered
                List<ParentJob> parentJobs = parentJobMapper.findJobsToTrigger(2L);
                if (CollectionUtils.isEmpty(parentJobs)) {
                    return;
                }
                Long parentJobId = parentJobs.getFirst().getId();
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .addLong("parentJobId", parentJobId)
                        .toJobParameters();
                parentJobMapper.updateStatus(parentJobId, "IN_PROGRESS");
                jobLauncher.run(springBatchFlow.discoveryJob(parentJobId), jobParameters);
                updateStatus(parentJobId);
            } else {

                if (jobRegistry.getJobNames().contains("discoveryJob1")) {
                    List<Long> executionIds = new ArrayList<>(jobOperator.getRunningExecutions("discoveryJob1"));
                    Long pausedId = null;

                    List<ParentJob> parentJobs = parentJobMapper.findJobsToTrigger(1L);
                    if (CollectionUtils.isEmpty(parentJobs)) {
                        return;
                    }
                    Long parentJobId = parentJobs.getFirst().getId();
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .addLong("parentJobId", parentJobId)
                            .toJobParameters();

                    if (!CollectionUtils.isEmpty(executionIds)) {
                        pausedId = executionIds.getFirst();
                        jobOperator.stop(pausedId);

                        parentJobMapper.updateStatus(parentJobId, "IN_PROGRESS");
                        jobLauncher.run(springBatchFlow.recoveryJob(), jobParameters);
                        updateStatus(parentJobId);
                        restartJob(pausedId);

                    } else {
                        parentJobMapper.updateStatus(parentJobId, "IN_PROGRESS");
                        jobLauncher.run(springBatchFlow.recoveryJob(), jobParameters);
                        updateStatus(parentJobId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void restartJob(Long pausedId) throws InterruptedException {

        if (pausedId != null) {
            JobExecution pausedExecution = jobExplorer.getJobExecution(pausedId);

            if (pausedExecution != null) {
                BatchStatus status = pausedExecution.getStatus();
                log.info("Paused Job ID: {}, Status: {}", pausedId, status);

                // ✅ Only restart if job was STOPPED or FAILED
                while (status == BatchStatus.STOPPING) {
                    log.info("Job is still STOPPING. Waiting for it to fully stop...");
                    Thread.sleep(2000); // Wait 1 second before checking again
                    pausedExecution = jobExplorer.getJobExecution(pausedId);
                    status = pausedExecution.getStatus();
                }

                // ✅ Only restart if job is fully STOPPED
                if (status == BatchStatus.STOPPED) {
                    try {
                        log.info("Restarting stopped discoveryJob with ID: {}", pausedId);
                        jobOperator.restart(pausedId);
                        log.info("Successfully restarted discoveryJob with ID: {}", pausedId);
                    } catch (Exception e) {
                        log.error("Failed to restart discoveryJob (ID: {}), Exception: {}", pausedId, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void updateStatus(Long parentJobId) {

        List<SubJob> subJobs = subJobMapper.getSubJobs(parentJobId);

        boolean allSuccess = true;
        for (SubJob subJob : subJobs) {
            if ("FAILED".equals(subJob.getStatus())) {
                parentJobMapper.updateStatus(parentJobId, "FAILED");
                return;
            } else if ("IN_PROGRESS".equals(subJob.getStatus())) {
                log.warn("Parent job {} has sub-jobs still in progress", parentJobId);
                return;
            } else if (!"COMPLETED".equals(subJob.getStatus())) {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            parentJobMapper.updateStatus(parentJobId, "COMPLETED");
        }
    }
}