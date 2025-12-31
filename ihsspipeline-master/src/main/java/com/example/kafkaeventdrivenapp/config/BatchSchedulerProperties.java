package com.example.kafkaeventdrivenapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.scheduler")
public class BatchSchedulerProperties {
    private boolean enabled = true;
    private long intervalMs = 5000;
    private int maxJobsPerPoll = 3;
    private int workerPoolSize = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public int getMaxJobsPerPoll() {
        return maxJobsPerPoll;
    }

    public void setMaxJobsPerPoll(int maxJobsPerPoll) {
        this.maxJobsPerPoll = maxJobsPerPoll;
    }

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }
}

