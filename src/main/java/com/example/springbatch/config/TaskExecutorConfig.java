package com.example.springbatch.config;

import com.example.springbatch.component.NodeRegistry;
import com.example.springbatch.entity.Node;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public final class TaskExecutorConfig {

    private static ThreadPoolTaskExecutor executor;

    public static ThreadPoolTaskExecutor getInstance() {
        if (executor == null) {
            initialize();
        }
        return executor;
    }

    public static void initialize() {

        executor = new ThreadPoolTaskExecutor();
        int availableThreads = Math.max(NodeRegistry.getAvailableThreads(Node.NodeType.CLOUD_WORKER), 1);
        executor.setCorePoolSize(availableThreads);
        executor.setMaxPoolSize(availableThreads);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Batch-Executor-");
        executor.initialize();
    }
}
