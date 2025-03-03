package com.example.springbatch.component;

import com.example.springbatch.config.TaskExecutorConfig;
import com.example.springbatch.tasklet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@Slf4j
public class SpringBatchFlow {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final VirtualNetworkTasklet virtualNetworkTasklet;

    private final SubnetTasklet subnetTasklet;

    private final VirtualMachineTasklet virtualMachineTasklet;

    private final DiskTasklet diskTasklet;

    private final NetworkInterfaceTasklet networkInterfaceTasklet;

    private final RecoveryTasklet recoveryTasklet;

    private final JobRegistry jobRegistry;

    public SpringBatchFlow(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           VirtualNetworkTasklet virtualNetworkTasklet, SubnetTasklet subnetTasklet,
                           VirtualMachineTasklet virtualMachineTasklet, DiskTasklet diskTasklet, NetworkInterfaceTasklet networkInterfaceTasklet, RecoveryTasklet recoveryTasklet, JobRegistry jobRegistry) {

        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.virtualNetworkTasklet = virtualNetworkTasklet;
        this.subnetTasklet = subnetTasklet;
        this.virtualMachineTasklet = virtualMachineTasklet;
        this.diskTasklet = diskTasklet;
        this.networkInterfaceTasklet = networkInterfaceTasklet;
        this.recoveryTasklet = recoveryTasklet;
        this.jobRegistry = jobRegistry;
    }

    public Job discoveryJob(Long parentJobId) {

        Job job = new JobBuilder("discoveryJob"+parentJobId, jobRepository)
                .start(independentJobsFlow())
                .end()
                .build();
        registerJob(job);
        return job;
    }

    public Job recoveryJob() {

        Job job =  new JobBuilder("recoveryJob", jobRepository)
                .start(recoveryFlow())
                .end()
                .build();
        registerJob(job);
        return job;
    }

    public Flow recoveryFlow() {

        return new FlowBuilder<SimpleFlow>("recoveryFlow")
                .start(recoveryStep())
                .build();
    }

    private Flow independentJobsFlow() {

        return new FlowBuilder<SimpleFlow>("independentJobsFlow")
                .split(TaskExecutorConfig.getInstance())  // Enable parallel execution
                .add(dependentJobsFlow(), virtualMachineFlow(), diskFlow(), networkInterfaceFlow())
                .build();
    }

    public Flow dependentJobsFlow() {

        return new FlowBuilder<SimpleFlow>("dependentJobsFlow")
                .start(virtualNetworkStep())
                .next(jobExecutionDecider())
                .on("SUCCESS").to(subnetStep())
                .end();
    }

    public Flow virtualMachineFlow() {

        return new FlowBuilder<SimpleFlow>("virtualMachineFlow")
                .start(virtualMachineStep())
                .build();
    }

    public Flow networkInterfaceFlow() {

        return new FlowBuilder<SimpleFlow>("networkInterfaceFlow")
                .start(networkInterfaceStep())
                .build();
    }

    public Flow diskFlow() {

        return new FlowBuilder<SimpleFlow>("diskFlow")
                .start(diskStep())
                .build();
    }

    public Step virtualNetworkStep() {

        return new StepBuilder("virtualNetworkStep", jobRepository)
                .tasklet(virtualNetworkTasklet, transactionManager)
                .build();
    }

    public Step subnetStep() {

        return new StepBuilder("subnetStep", jobRepository)
                .tasklet(subnetTasklet, transactionManager)
                .build();
    }

    public Step virtualMachineStep() {

        return new StepBuilder("virtualMachineStep", jobRepository)
                .tasklet(virtualMachineTasklet, transactionManager)
                .build();
    }

    public Step networkInterfaceStep() {

        return new StepBuilder("networkInterface", jobRepository)
                .tasklet(networkInterfaceTasklet, transactionManager)
                .build();
    }

    public Step diskStep() {

        return new StepBuilder("diskStep", jobRepository)
                .tasklet(diskTasklet, transactionManager)
                .build();
    }

    public Step recoveryStep() {

        return new StepBuilder("recoveryStep", jobRepository)
                .tasklet(recoveryTasklet, transactionManager)
                .build();
    }

    public JobExecutionDecider jobExecutionDecider() {

        return (jobExecution, stepExecution) -> {

            boolean isDependentJobSuccess = (boolean) stepExecution.getJobExecution().getExecutionContext().get("isDependentJobSuccess");
            return isDependentJobSuccess ? new FlowExecutionStatus("SUCCESS") : new FlowExecutionStatus("FAILED");
        };
    }

    private void registerJob(Job job) {
        try {
            jobRegistry.register(new ReferenceJobFactory(job));
        } catch (Exception e) {
            throw new RuntimeException("Failed to register job: " + job.getName(), e);
        }
    }
}
