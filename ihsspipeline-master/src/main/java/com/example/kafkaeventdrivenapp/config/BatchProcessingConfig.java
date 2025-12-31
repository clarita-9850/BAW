package com.example.kafkaeventdrivenapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchProcessingConfig {

    @Bean(name = "batchJobExecutor")
    public TaskExecutor batchJobExecutor(BatchSchedulerProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("batch-worker-");
        executor.setCorePoolSize(properties.getWorkerPoolSize());
        executor.setMaxPoolSize(properties.getWorkerPoolSize());
        executor.setQueueCapacity(properties.getMaxJobsPerPoll());
        executor.initialize();
        return executor;
    }
}

