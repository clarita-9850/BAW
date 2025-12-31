package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.config.BatchSchedulerProperties;
import com.example.kafkaeventdrivenapp.entity.ReportJobEntity;
import com.example.kafkaeventdrivenapp.repository.ReportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchJobScheduler {

    private final ReportJobRepository jobRepository;
    private final JobQueueService jobQueueService;
    private final BackgroundProcessingService backgroundProcessingService;
    private final TaskExecutor batchJobExecutor;
    private final BatchSchedulerProperties properties;

    @Autowired
    public BatchJobScheduler(ReportJobRepository jobRepository,
                             JobQueueService jobQueueService,
                             BackgroundProcessingService backgroundProcessingService,
                             @org.springframework.beans.factory.annotation.Qualifier("batchJobExecutor") TaskExecutor batchJobExecutor,
                             BatchSchedulerProperties properties) {
        this.jobRepository = jobRepository;
        this.jobQueueService = jobQueueService;
        this.backgroundProcessingService = backgroundProcessingService;
        this.batchJobExecutor = batchJobExecutor;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${batch.scheduler.interval-ms:5000}")
    public void pollAndDispatchJobs() {
        if (!properties.isEnabled()) {
            return;
        }

        List<ReportJobEntity> queuedJobs = jobRepository.findTopQueuedJobs(properties.getMaxJobsPerPoll());
        if (queuedJobs.isEmpty()) {
            return;
        }

        for (ReportJobEntity job : queuedJobs) {
            jobQueueService.markJobAsProcessing(job.getJobId())
                .ifPresent(claimedJob -> batchJobExecutor.execute(() -> backgroundProcessingService.processJob(claimedJob.getJobId())));
        }
    }
}

