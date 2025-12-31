# AWS Batch Implementation Guide for Timesheet Reporting App

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Local Testing Setup](#local-testing-setup)
4. [Spring App Changes](#spring-app-changes)
5. [AWS Batch Configuration](#aws-batch-configuration)
6. [Database Changes](#database-changes)
7. [Deployment Guide](#deployment-guide)
8. [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)

---

## Overview

### Current Problem
- 2,500 batch jobs processing 350,000 records nightly
- Current processing time: ~8 hours (limited parallelism)
- Single Spring container with limited thread pool

### Solution: AWS Batch
- Parallel processing with 50-100+ workers
- Target processing time: 3-4 hours (50% reduction)
- Auto-scaling: 0 → 100 → 0 (pay only when running)

### Key Benefits
| Benefit | Description |
|---------|-------------|
| **Time Reduction** | 50%+ faster with parallel processing |
| **Cost Effective** | Fargate Spot pricing, scales to zero |
| **No Code Rewrite** | Uses existing Spring app Docker image |
| **Security Preserved** | Same Keycloak/JWT/field masking |
| **Managed Service** | AWS handles infrastructure |

---

## Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AWS BATCH ARCHITECTURE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  STEP 1: NIGHTLY TRIGGER                                                    │
│  ┌─────────────┐                                                            │
│  │ Spring App  │  @Scheduled(cron = "0 0 21 * * ?")                        │
│  │ Coordinator │  - Reads files from SFTP                                   │
│  └──────┬──────┘  - Submits 2,500 jobs to AWS Batch                        │
│         │                                                                    │
│         ▼                                                                    │
│  STEP 2: JOB QUEUE                                                          │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  AWS Batch Job Queue                                                 │    │
│  │  ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐     │    │
│  │  │Job 1│Job 2│Job 3│Job 4│Job 5│ ... │ ... │ ... │ ... │2500 │     │    │
│  │  └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘     │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│         │                                                                    │
│         ▼                                                                    │
│  STEP 3: PARALLEL PROCESSING                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Fargate Compute Environment (Auto-scales 0 → 100+)                 │    │
│  │                                                                      │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │    │
│  │  │Worker 1 │ │Worker 2 │ │Worker 3 │ │Worker 4 │ │Worker N │       │    │
│  │  │ Spring  │ │ Spring  │ │ Spring  │ │ Spring  │ │ Spring  │       │    │
│  │  │  App    │ │  App    │ │  App    │ │  App    │ │  App    │       │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘       │    │
│  │       │           │           │           │           │             │    │
│  │       └───────────┴───────────┴───────────┴───────────┘             │    │
│  │                               │                                      │    │
│  └───────────────────────────────┼──────────────────────────────────────┘    │
│                                  │                                           │
│                                  ▼                                           │
│  STEP 4: SHARED RESOURCES                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                       │
│  │   RDS Proxy  │  │   Keycloak   │  │   S3 Bucket  │                       │
│  │  (PostgreSQL)│  │   (Auth)     │  │  (Files)     │                       │
│  └──────────────┘  └──────────────┘  └──────────────┘                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility |
|-----------|---------------|
| **Spring Coordinator** | Triggers nightly batch, submits jobs to queue |
| **AWS Batch Job Queue** | Holds pending jobs, manages priorities |
| **Fargate Compute** | Runs worker containers, auto-scales |
| **Spring Worker** | Processes individual jobs (your existing logic) |
| **RDS Proxy** | Manages database connection pooling |
| **S3** | Stores inbound/outbound files |

---

## Local Testing Setup

### Overview
Before deploying to AWS, test the architecture locally using Docker Compose with:
- **Redis** - Simulates AWS Batch job queue
- **Multiple Spring Workers** - Simulates Fargate containers
- **LocalStack** - Simulates S3 (optional)

### Architecture for Local Testing

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         LOCAL TESTING ARCHITECTURE                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ PostgreSQL  │     │    Redis    │     │  Keycloak   │                   │
│  │  (exists)   │     │   (Queue)   │     │  (exists)   │                   │
│  └──────┬──────┘     └──────┬──────┘     └──────┬──────┘                   │
│         │                   │                   │                           │
│         └───────────────────┼───────────────────┘                           │
│                             │                                               │
│                             ▼                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    SPRING APP COORDINATOR                            │    │
│  │                    (Submits jobs to Redis)                           │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                             │                                               │
│         ┌───────────────────┼───────────────────┐                          │
│         ▼                   ▼                   ▼                          │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │  Worker 1   │     │  Worker 2   │     │  Worker 3   │                   │
│  │ Spring App  │     │ Spring App  │     │ Spring App  │                   │
│  │ (batch-worker)    │ (batch-worker)    │ (batch-worker)                  │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                              │
│  Scale with: docker-compose up --scale batch-worker=10                     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Docker Compose Configuration

Create file: `docker-compose.batch-test.yml`

```yaml
version: '3.8'

services:
  # ============================================
  # EXISTING SERVICES (already in your setup)
  # ============================================

  postgres:
    image: postgres:13
    container_name: trial-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: ihsscmips
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      POSTGRES_HOST_AUTH_METHOD: trust
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./postgres-init:/docker-entrypoint-initdb.d
    networks:
      - batch-test-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ============================================
  # NEW: REDIS (Job Queue - simulates AWS Batch)
  # ============================================

  redis:
    image: redis:7-alpine
    container_name: batch-redis
    ports:
      - "6379:6379"
    networks:
      - batch-test-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ============================================
  # COORDINATOR: Submits jobs to queue
  # ============================================

  batch-coordinator:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: batch-coordinator
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: coordinator
      # Database
      DB_HOST: trial-postgres
      DB_PORT: "5432"
      DB_NAME: ihsscmips
      SPRING_DATASOURCE_USERNAME: cmips_app
      SPRING_DATASOURCE_PASSWORD: cmips_app_password
      # Redis Queue
      REDIS_HOST: batch-redis
      REDIS_PORT: "6379"
      # Keycloak
      KEYCLOAK_AUTH_SERVER_URL: http://cmips-keycloak:8080
      KEYCLOAK_ISSUER_URI: http://cmips-keycloak:8080/realms/cmips
      # Batch mode
      BATCH_MODE: coordinator
      BATCH_WORKER_ENABLED: "false"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - batch-test-network
      - cmips-shared-network

  # ============================================
  # WORKERS: Process jobs from queue (SCALABLE)
  # ============================================

  batch-worker:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: worker
      # Database
      DB_HOST: trial-postgres
      DB_PORT: "5432"
      DB_NAME: ihsscmips
      SPRING_DATASOURCE_USERNAME: cmips_app
      SPRING_DATASOURCE_PASSWORD: cmips_app_password
      # Redis Queue
      REDIS_HOST: batch-redis
      REDIS_PORT: "6379"
      # Keycloak
      KEYCLOAK_AUTH_SERVER_URL: http://cmips-keycloak:8080
      KEYCLOAK_ISSUER_URI: http://cmips-keycloak:8080/realms/cmips
      # Batch mode
      BATCH_MODE: worker
      BATCH_WORKER_ENABLED: "true"
      # Worker polls queue for jobs
      BATCH_POLL_INTERVAL_MS: "1000"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      batch-coordinator:
        condition: service_started
    networks:
      - batch-test-network
      - cmips-shared-network
    deploy:
      replicas: 3  # Default 3 workers, can scale with --scale

  # ============================================
  # OPTIONAL: LocalStack for S3 simulation
  # ============================================

  localstack:
    image: localstack/localstack:latest
    container_name: localstack
    ports:
      - "4566:4566"
    environment:
      SERVICES: s3
      DEFAULT_REGION: us-east-1
      DATA_DIR: /tmp/localstack/data
    volumes:
      - localstack_data:/tmp/localstack
    networks:
      - batch-test-network

volumes:
  postgres_data:
  localstack_data:

networks:
  batch-test-network:
    driver: bridge
  cmips-shared-network:
    external: true
    name: cmips-shared-network
```

### Running Local Tests

```bash
# 1. Start the batch testing environment with 3 workers (default)
docker-compose -f docker-compose.batch-test.yml up -d

# 2. Scale to 10 workers to test parallelism
docker-compose -f docker-compose.batch-test.yml up -d --scale batch-worker=10

# 3. View logs from all workers
docker-compose -f docker-compose.batch-test.yml logs -f batch-worker

# 4. Submit test jobs via API
curl -X POST http://localhost:8080/api/batch/submit-test-jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{"jobCount": 100, "reportType": "COUNTY_DAILY"}'

# 5. Monitor job queue depth
docker exec batch-redis redis-cli LLEN batch:job:queue

# 6. Monitor worker processing
curl http://localhost:8080/api/batch/queue-status

# 7. Scale down when done
docker-compose -f docker-compose.batch-test.yml down
```

---

## Spring App Changes

### Overview of Changes Needed

| Change | File | Purpose |
|--------|------|---------|
| Add Redis dependency | `pom.xml` | Job queue communication |
| Add Spring profiles | `application.yml` | coordinator/worker modes |
| Create BatchQueueService | New service | Submit/poll jobs from Redis |
| Create BatchWorkerService | New service | Process jobs in worker mode |
| Create BatchCoordinatorService | New service | Submit jobs in coordinator mode |
| Add worker entry point | New class | Start worker on container boot |

### 1. Add Dependencies to pom.xml

```xml
<!-- Add to pom.xml dependencies section -->

<!-- Redis for job queue (local testing) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- AWS SDK for Batch (production) -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>batch</artifactId>
    <version>2.21.0</version>
</dependency>

<!-- AWS SDK for S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.21.0</version>
</dependency>
```

### 2. Application Configuration

Add to `application.yml`:

```yaml
# ============================================
# BATCH PROCESSING CONFIGURATION
# ============================================

batch:
  # Mode: 'coordinator' or 'worker'
  mode: ${BATCH_MODE:coordinator}

  # Worker configuration
  worker:
    enabled: ${BATCH_WORKER_ENABLED:false}
    poll-interval-ms: ${BATCH_POLL_INTERVAL_MS:1000}
    shutdown-timeout-seconds: 60

  # Queue configuration (Redis for local, SQS for AWS)
  queue:
    type: ${BATCH_QUEUE_TYPE:redis}  # redis or sqs
    name: ${BATCH_QUEUE_NAME:batch:job:queue}

  # AWS Batch configuration (production)
  aws:
    enabled: ${AWS_BATCH_ENABLED:false}
    region: ${AWS_REGION:us-east-1}
    job-queue: ${AWS_BATCH_JOB_QUEUE:batch-job-queue}
    job-definition: ${AWS_BATCH_JOB_DEFINITION:spring-batch-worker}

# Redis configuration (local testing)
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

---
# Coordinator profile
spring:
  config:
    activate:
      on-profile: coordinator

batch:
  mode: coordinator
  worker:
    enabled: false

---
# Worker profile
spring:
  config:
    activate:
      on-profile: worker

batch:
  mode: worker
  worker:
    enabled: true

# Disable scheduled tasks in worker mode
report:
  scheduling:
    enabled: false
```

### 3. Batch Job Message Model

Create: `src/main/java/com/example/kafkaeventdrivenapp/model/BatchJobMessage.java`

```java
package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobMessage {

    private String jobId;
    private String reportType;
    private String role;
    private String targetSystem;
    private String dataFormat;

    // File information
    private String inputFilePath;      // S3 path or local path
    private String outputFilePath;     // Where to write results

    // Processing parameters
    private Map<String, Object> parameters;

    // Metadata
    private Instant submittedAt;
    private String submittedBy;
    private Integer priority;          // Higher = more urgent
    private Integer retryCount;
    private Integer maxRetries;

    // For tracking
    private String correlationId;      // Link related jobs
}
```

### 4. Batch Queue Service (Redis Implementation)

Create: `src/main/java/com/example/kafkaeventdrivenapp/service/BatchQueueService.java`

```java
package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.BatchJobMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchQueueService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${batch.queue.name:batch:job:queue}")
    private String queueName;

    private static final String PROCESSING_QUEUE = "batch:job:processing";
    private static final String DEAD_LETTER_QUEUE = "batch:job:dead-letter";

    /**
     * Submit a job to the queue
     */
    public String submitJob(BatchJobMessage job) {
        if (job.getJobId() == null) {
            job.setJobId("JOB_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        job.setSubmittedAt(Instant.now());

        try {
            String jobJson = objectMapper.writeValueAsString(job);

            // Use LPUSH for FIFO (workers use BRPOPLPUSH)
            redisTemplate.opsForList().leftPush(queueName, jobJson);

            log.info("Submitted job {} to queue. Queue depth: {}",
                    job.getJobId(), getQueueDepth());

            return job.getJobId();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize job: {}", e.getMessage());
            throw new RuntimeException("Failed to submit job", e);
        }
    }

    /**
     * Poll for next job (blocking with timeout)
     * Uses BRPOPLPUSH for reliable processing
     */
    public Optional<BatchJobMessage> pollJob(Duration timeout) {
        try {
            // Atomically move job from main queue to processing queue
            String jobJson = redisTemplate.opsForList()
                    .rightPopAndLeftPush(queueName, PROCESSING_QUEUE, timeout);

            if (jobJson == null) {
                return Optional.empty();
            }

            BatchJobMessage job = objectMapper.readValue(jobJson, BatchJobMessage.class);
            log.debug("Polled job {} from queue", job.getJobId());

            return Optional.of(job);

        } catch (Exception e) {
            log.error("Failed to poll job: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Mark job as completed (remove from processing queue)
     */
    public void completeJob(BatchJobMessage job) {
        try {
            String jobJson = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().remove(PROCESSING_QUEUE, 1, jobJson);
            log.info("Completed job {}", job.getJobId());
        } catch (JsonProcessingException e) {
            log.error("Failed to complete job: {}", e.getMessage());
        }
    }

    /**
     * Move failed job to dead letter queue or retry
     */
    public void failJob(BatchJobMessage job, String errorMessage) {
        try {
            job.setRetryCount(job.getRetryCount() == null ? 1 : job.getRetryCount() + 1);
            String jobJson = objectMapper.writeValueAsString(job);

            // Remove from processing queue
            redisTemplate.opsForList().remove(PROCESSING_QUEUE, 1, jobJson);

            if (job.getRetryCount() < job.getMaxRetries()) {
                // Re-queue for retry
                log.warn("Retrying job {} (attempt {})", job.getJobId(), job.getRetryCount());
                redisTemplate.opsForList().leftPush(queueName, jobJson);
            } else {
                // Move to dead letter queue
                log.error("Job {} failed after {} attempts: {}",
                        job.getJobId(), job.getRetryCount(), errorMessage);
                redisTemplate.opsForList().leftPush(DEAD_LETTER_QUEUE, jobJson);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to handle job failure: {}", e.getMessage());
        }
    }

    /**
     * Get current queue depth
     */
    public Long getQueueDepth() {
        return redisTemplate.opsForList().size(queueName);
    }

    /**
     * Get processing queue depth
     */
    public Long getProcessingCount() {
        return redisTemplate.opsForList().size(PROCESSING_QUEUE);
    }

    /**
     * Get dead letter queue depth
     */
    public Long getDeadLetterCount() {
        return redisTemplate.opsForList().size(DEAD_LETTER_QUEUE);
    }
}
```

### 5. Batch Worker Service

Create: `src/main/java/com/example/kafkaeventdrivenapp/service/BatchWorkerService.java`

```java
package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.BatchJobMessage;
import com.example.kafkaeventdrivenapp.model.JobQueueEntry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.worker.enabled", havingValue = "true")
public class BatchWorkerService {

    private final BatchQueueService queueService;
    private final JobQueueService jobQueueService;  // Your existing service
    private final KeycloakTokenService keycloakTokenService;

    @Value("${batch.worker.poll-interval-ms:1000}")
    private long pollIntervalMs;

    @Value("${batch.worker.shutdown-timeout-seconds:60}")
    private int shutdownTimeoutSeconds;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread workerThread;

    @PostConstruct
    public void start() {
        running.set(true);
        workerThread = new Thread(this::processLoop, "batch-worker");
        workerThread.start();
        log.info("Batch worker started");
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping batch worker...");
        running.set(false);

        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(shutdownTimeoutSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Batch worker stopped");
    }

    private void processLoop() {
        log.info("Worker process loop started, polling every {}ms", pollIntervalMs);

        while (running.get()) {
            try {
                // Poll for job with timeout
                Optional<BatchJobMessage> jobOpt = queueService.pollJob(
                        Duration.ofMillis(pollIntervalMs));

                if (jobOpt.isPresent()) {
                    processJob(jobOpt.get());
                }

            } catch (Exception e) {
                if (running.get()) {
                    log.error("Error in worker loop: {}", e.getMessage(), e);
                    sleep(pollIntervalMs);  // Back off on error
                }
            }
        }
    }

    private void processJob(BatchJobMessage job) {
        log.info("Processing job: {} (type: {}, role: {})",
                job.getJobId(), job.getReportType(), job.getRole());

        Instant startTime = Instant.now();

        try {
            // 1. Get JWT token for the job's role
            String token = getTokenForRole(job.getRole());

            // 2. Create internal job entry (reuse existing logic)
            JobQueueEntry internalJob = createInternalJob(job, token);

            // 3. Process using existing batch logic
            jobQueueService.processJobInternal(internalJob);

            // 4. Mark job as completed
            queueService.completeJob(job);

            Duration duration = Duration.between(startTime, Instant.now());
            log.info("Job {} completed successfully in {}ms",
                    job.getJobId(), duration.toMillis());

        } catch (Exception e) {
            log.error("Job {} failed: {}", job.getJobId(), e.getMessage(), e);
            queueService.failJob(job, e.getMessage());
        }
    }

    private String getTokenForRole(String role) {
        // Use existing Keycloak service to get token for role's service account
        return keycloakTokenService.getServiceAccountToken(role);
    }

    private JobQueueEntry createInternalJob(BatchJobMessage message, String token) {
        JobQueueEntry entry = new JobQueueEntry();
        entry.setJobId(message.getJobId());
        entry.setReportType(message.getReportType());
        entry.setRole(message.getRole());
        entry.setTargetSystem(message.getTargetSystem());
        entry.setDataFormat(message.getDataFormat());
        entry.setParameters(message.getParameters());
        entry.setAuthToken(token);
        entry.setCreatedAt(Instant.now());
        return entry;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 6. Batch Coordinator Service

Create: `src/main/java/com/example/kafkaeventdrivenapp/service/BatchCoordinatorService.java`

```java
package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.BatchJobMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.mode", havingValue = "coordinator", matchIfMissing = true)
public class BatchCoordinatorService {

    private final BatchQueueService queueService;
    private final SftpService sftpService;  // Your existing SFTP service

    /**
     * Nightly batch trigger - submit all jobs to queue
     */
    @Scheduled(cron = "${report.scheduling.nightly-cron:0 0 21 * * ?}")
    public void triggerNightlyBatch() {
        log.info("Starting nightly batch job submission...");

        try {
            // 1. Get list of files from SFTP/inbound
            List<InboundFile> files = sftpService.listInboundFiles();

            log.info("Found {} inbound files to process", files.size());

            // 2. Submit each file as a job
            String correlationId = UUID.randomUUID().toString();
            int submitted = 0;

            for (InboundFile file : files) {
                BatchJobMessage job = BatchJobMessage.builder()
                        .reportType(file.getReportType())
                        .role(file.getTargetRole())
                        .targetSystem("SCHEDULED")
                        .dataFormat(file.getFormat())
                        .inputFilePath(file.getPath())
                        .outputFilePath(generateOutputPath(file))
                        .submittedBy("NIGHTLY_SCHEDULER")
                        .priority(file.getPriority())
                        .maxRetries(3)
                        .retryCount(0)
                        .correlationId(correlationId)
                        .parameters(Map.of(
                                "vendorId", file.getVendorId(),
                                "fileName", file.getFileName()
                        ))
                        .build();

                queueService.submitJob(job);
                submitted++;
            }

            log.info("Submitted {} jobs to queue with correlationId: {}",
                    submitted, correlationId);

        } catch (Exception e) {
            log.error("Failed to submit nightly batch: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual job submission endpoint
     */
    public List<String> submitJobs(List<BatchJobMessage> jobs) {
        List<String> jobIds = new ArrayList<>();
        String correlationId = UUID.randomUUID().toString();

        for (BatchJobMessage job : jobs) {
            job.setCorrelationId(correlationId);
            job.setSubmittedAt(Instant.now());
            if (job.getMaxRetries() == null) {
                job.setMaxRetries(3);
            }

            String jobId = queueService.submitJob(job);
            jobIds.add(jobId);
        }

        log.info("Manually submitted {} jobs with correlationId: {}",
                jobIds.size(), correlationId);

        return jobIds;
    }

    /**
     * Submit test jobs for load testing
     */
    public List<String> submitTestJobs(int count, String reportType, String role) {
        List<String> jobIds = new ArrayList<>();
        String correlationId = "TEST_" + UUID.randomUUID().toString().substring(0, 8);

        for (int i = 0; i < count; i++) {
            BatchJobMessage job = BatchJobMessage.builder()
                    .reportType(reportType)
                    .role(role)
                    .targetSystem("TEST")
                    .dataFormat("JSON")
                    .submittedBy("TEST_RUNNER")
                    .priority(5)
                    .maxRetries(1)
                    .retryCount(0)
                    .correlationId(correlationId)
                    .parameters(Map.of("testIndex", i))
                    .build();

            String jobId = queueService.submitJob(job);
            jobIds.add(jobId);
        }

        log.info("Submitted {} test jobs with correlationId: {}", count, correlationId);

        return jobIds;
    }

    private String generateOutputPath(InboundFile file) {
        return String.format("outbound/%s/%s_%s",
                file.getVendorId(),
                Instant.now().toEpochMilli(),
                file.getFileName());
    }
}
```

### 7. REST Controller for Batch Operations

Create: `src/main/java/com/example/kafkaeventdrivenapp/controller/BatchQueueController.java`

```java
package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.BatchJobMessage;
import com.example.kafkaeventdrivenapp.service.BatchCoordinatorService;
import com.example.kafkaeventdrivenapp.service.BatchQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.mode", havingValue = "coordinator", matchIfMissing = true)
public class BatchQueueController {

    private final BatchQueueService queueService;
    private final BatchCoordinatorService coordinatorService;

    /**
     * Get queue status
     */
    @GetMapping("/queue-status")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        return ResponseEntity.ok(Map.of(
                "pending", queueService.getQueueDepth(),
                "processing", queueService.getProcessingCount(),
                "deadLetter", queueService.getDeadLetterCount()
        ));
    }

    /**
     * Submit jobs manually
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitJobs(
            @RequestBody List<BatchJobMessage> jobs) {

        List<String> jobIds = coordinatorService.submitJobs(jobs);

        return ResponseEntity.ok(Map.of(
                "submitted", jobIds.size(),
                "jobIds", jobIds,
                "queueDepth", queueService.getQueueDepth()
        ));
    }

    /**
     * Submit test jobs for load testing
     */
    @PostMapping("/submit-test-jobs")
    public ResponseEntity<Map<String, Object>> submitTestJobs(
            @RequestBody TestJobRequest request) {

        List<String> jobIds = coordinatorService.submitTestJobs(
                request.getJobCount(),
                request.getReportType(),
                request.getRole()
        );

        return ResponseEntity.ok(Map.of(
                "submitted", jobIds.size(),
                "jobIds", jobIds.subList(0, Math.min(10, jobIds.size())),  // Return first 10
                "queueDepth", queueService.getQueueDepth()
        ));
    }

    /**
     * Trigger nightly batch manually
     */
    @PostMapping("/trigger-nightly")
    public ResponseEntity<Map<String, Object>> triggerNightly() {
        coordinatorService.triggerNightlyBatch();

        return ResponseEntity.ok(Map.of(
                "triggered", true,
                "queueDepth", queueService.getQueueDepth()
        ));
    }

    @lombok.Data
    public static class TestJobRequest {
        private int jobCount = 10;
        private String reportType = "COUNTY_DAILY";
        private String role = "CASE_WORKER";
    }
}
```

---

## AWS Batch Configuration

### Prerequisites
1. AWS Account with appropriate permissions
2. VPC with private subnets
3. ECR repository for Docker image
4. RDS PostgreSQL instance
5. RDS Proxy (recommended for connection pooling)

### AWS Resources to Create

#### 1. ECR Repository

```bash
# Create ECR repository
aws ecr create-repository \
    --repository-name timesheet-batch-worker \
    --region us-east-1

# Get login credentials
aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and push Docker image
docker build -t timesheet-batch-worker .
docker tag timesheet-batch-worker:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/timesheet-batch-worker:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/timesheet-batch-worker:latest
```

#### 2. IAM Roles

**Batch Service Role:**
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Service": "batch.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }
    ]
}
```

**Task Execution Role:**
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "secretsmanager:GetSecretValue",
                "s3:GetObject",
                "s3:PutObject"
            ],
            "Resource": "*"
        }
    ]
}
```

#### 3. Compute Environment (CloudFormation)

```yaml
# aws-batch-compute-environment.yml
AWSTemplateFormatVersion: '2010-09-09'
Description: AWS Batch Compute Environment for Timesheet Processing

Parameters:
  VpcId:
    Type: AWS::EC2::VPC::Id
    Description: VPC for Batch compute

  SubnetIds:
    Type: List<AWS::EC2::Subnet::Id>
    Description: Private subnets for Batch workers

  MaxvCpus:
    Type: Number
    Default: 256
    Description: Maximum vCPUs for compute environment

Resources:
  # Security Group for Batch Workers
  BatchSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: Security group for Batch workers
      VpcId: !Ref VpcId
      SecurityGroupEgress:
        - IpProtocol: -1
          CidrIp: 0.0.0.0/0

  # Compute Environment (Fargate Spot)
  BatchComputeEnvironment:
    Type: AWS::Batch::ComputeEnvironment
    Properties:
      ComputeEnvironmentName: timesheet-batch-fargate-spot
      Type: MANAGED
      State: ENABLED
      ComputeResources:
        Type: FARGATE_SPOT
        MaxvCpus: !Ref MaxvCpus
        Subnets: !Ref SubnetIds
        SecurityGroupIds:
          - !Ref BatchSecurityGroup

  # Job Queue
  BatchJobQueue:
    Type: AWS::Batch::JobQueue
    Properties:
      JobQueueName: timesheet-batch-queue
      State: ENABLED
      Priority: 1
      ComputeEnvironmentOrder:
        - Order: 1
          ComputeEnvironment: !Ref BatchComputeEnvironment

  # Job Definition
  BatchJobDefinition:
    Type: AWS::Batch::JobDefinition
    Properties:
      JobDefinitionName: timesheet-batch-worker
      Type: container
      PlatformCapabilities:
        - FARGATE
      ContainerProperties:
        Image: !Sub ${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/timesheet-batch-worker:latest
        ResourceRequirements:
          - Type: VCPU
            Value: "1"
          - Type: MEMORY
            Value: "2048"
        ExecutionRoleArn: !GetAtt BatchExecutionRole.Arn
        JobRoleArn: !GetAtt BatchJobRole.Arn
        NetworkConfiguration:
          AssignPublicIp: DISABLED
        LogConfiguration:
          LogDriver: awslogs
          Options:
            awslogs-group: /aws/batch/timesheet-worker
            awslogs-region: !Ref AWS::Region
            awslogs-stream-prefix: batch
        Environment:
          - Name: SPRING_PROFILES_ACTIVE
            Value: worker,aws
          - Name: BATCH_MODE
            Value: worker
          - Name: BATCH_WORKER_ENABLED
            Value: "true"
          - Name: AWS_BATCH_ENABLED
            Value: "true"
        Secrets:
          - Name: DB_PASSWORD
            ValueFrom: !Sub arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:timesheet/db-credentials
          - Name: KEYCLOAK_CLIENT_SECRET
            ValueFrom: !Sub arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:timesheet/keycloak-credentials
      RetryStrategy:
        Attempts: 3
      Timeout:
        AttemptDurationSeconds: 3600  # 1 hour max per job

  # Execution Role
  BatchExecutionRole:
    Type: AWS::IAM::Role
    Properties:
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Principal:
              Service: ecs-tasks.amazonaws.com
            Action: sts:AssumeRole
      ManagedPolicyArns:
        - arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
      Policies:
        - PolicyName: SecretsAccess
          PolicyDocument:
            Version: '2012-10-17'
            Statement:
              - Effect: Allow
                Action:
                  - secretsmanager:GetSecretValue
                Resource: !Sub arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:timesheet/*

  # Job Role (for S3 access, etc.)
  BatchJobRole:
    Type: AWS::IAM::Role
    Properties:
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Principal:
              Service: ecs-tasks.amazonaws.com
            Action: sts:AssumeRole
      Policies:
        - PolicyName: S3Access
          PolicyDocument:
            Version: '2012-10-17'
            Statement:
              - Effect: Allow
                Action:
                  - s3:GetObject
                  - s3:PutObject
                  - s3:ListBucket
                Resource:
                  - arn:aws:s3:::timesheet-batch-files
                  - arn:aws:s3:::timesheet-batch-files/*

Outputs:
  JobQueueArn:
    Value: !Ref BatchJobQueue
    Export:
      Name: TimesheetBatchJobQueue

  JobDefinitionArn:
    Value: !Ref BatchJobDefinition
    Export:
      Name: TimesheetBatchJobDefinition
```

#### 4. AWS Batch Service for Spring

Create: `src/main/java/com/example/kafkaeventdrivenapp/service/AwsBatchService.java`

```java
package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.BatchJobMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.aws.enabled", havingValue = "true")
public class AwsBatchService {

    private final BatchClient batchClient;
    private final ObjectMapper objectMapper;

    @Value("${batch.aws.job-queue}")
    private String jobQueue;

    @Value("${batch.aws.job-definition}")
    private String jobDefinition;

    /**
     * Submit a single job to AWS Batch
     */
    public String submitJob(BatchJobMessage job) {
        try {
            // Convert job to environment variables
            List<KeyValuePair> environment = new ArrayList<>();
            environment.add(KeyValuePair.builder()
                    .name("JOB_ID").value(job.getJobId()).build());
            environment.add(KeyValuePair.builder()
                    .name("REPORT_TYPE").value(job.getReportType()).build());
            environment.add(KeyValuePair.builder()
                    .name("ROLE").value(job.getRole()).build());
            environment.add(KeyValuePair.builder()
                    .name("INPUT_PATH").value(job.getInputFilePath()).build());
            environment.add(KeyValuePair.builder()
                    .name("OUTPUT_PATH").value(job.getOutputFilePath()).build());
            environment.add(KeyValuePair.builder()
                    .name("JOB_PARAMS").value(objectMapper.writeValueAsString(job.getParameters())).build());

            SubmitJobRequest request = SubmitJobRequest.builder()
                    .jobName(job.getJobId())
                    .jobQueue(jobQueue)
                    .jobDefinition(jobDefinition)
                    .containerOverrides(ContainerOverrides.builder()
                            .environment(environment)
                            .build())
                    .build();

            SubmitJobResponse response = batchClient.submitJob(request);

            log.info("Submitted job {} to AWS Batch, batch job ID: {}",
                    job.getJobId(), response.jobId());

            return response.jobId();

        } catch (Exception e) {
            log.error("Failed to submit job to AWS Batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to submit job to AWS Batch", e);
        }
    }

    /**
     * Submit multiple jobs in batch
     */
    public List<String> submitJobs(List<BatchJobMessage> jobs) {
        List<String> batchJobIds = new ArrayList<>();

        for (BatchJobMessage job : jobs) {
            String batchJobId = submitJob(job);
            batchJobIds.add(batchJobId);
        }

        return batchJobIds;
    }

    /**
     * Get job status
     */
    public Map<String, Object> getJobStatus(String batchJobId) {
        DescribeJobsRequest request = DescribeJobsRequest.builder()
                .jobs(batchJobId)
                .build();

        DescribeJobsResponse response = batchClient.describeJobs(request);

        if (response.jobs().isEmpty()) {
            return Map.of("status", "NOT_FOUND");
        }

        JobDetail job = response.jobs().get(0);

        return Map.of(
                "jobId", job.jobId(),
                "jobName", job.jobName(),
                "status", job.status().toString(),
                "statusReason", job.statusReason() != null ? job.statusReason() : "",
                "createdAt", job.createdAt(),
                "startedAt", job.startedAt() != null ? job.startedAt() : 0,
                "stoppedAt", job.stoppedAt() != null ? job.stoppedAt() : 0
        );
    }

    /**
     * Get queue status
     */
    public Map<String, Object> getQueueStatus() {
        // Count jobs by status
        int pending = countJobsByStatus(JobStatus.PENDING);
        int runnable = countJobsByStatus(JobStatus.RUNNABLE);
        int running = countJobsByStatus(JobStatus.RUNNING);
        int succeeded = countJobsByStatus(JobStatus.SUCCEEDED);
        int failed = countJobsByStatus(JobStatus.FAILED);

        return Map.of(
                "pending", pending,
                "runnable", runnable,
                "running", running,
                "succeeded", succeeded,
                "failed", failed
        );
    }

    private int countJobsByStatus(JobStatus status) {
        ListJobsRequest request = ListJobsRequest.builder()
                .jobQueue(jobQueue)
                .jobStatus(status)
                .build();

        return batchClient.listJobs(request).jobSummaryList().size();
    }
}
```

---

## Database Changes

### New Table: batch_job_log

```sql
-- Create batch job log table
CREATE TABLE IF NOT EXISTS batch_job_log (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL UNIQUE,
    correlation_id VARCHAR(50),
    report_type VARCHAR(50) NOT NULL,
    role VARCHAR(50),
    target_system VARCHAR(50),
    data_format VARCHAR(20),

    -- File information
    input_file_path VARCHAR(500),
    output_file_path VARCHAR(500),

    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,

    -- Timestamps
    submitted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    -- Metrics
    records_processed BIGINT,
    processing_time_ms BIGINT,

    -- Worker information
    worker_id VARCHAR(100),
    aws_batch_job_id VARCHAR(100),

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_batch_job_log_status ON batch_job_log(status);
CREATE INDEX idx_batch_job_log_correlation ON batch_job_log(correlation_id);
CREATE INDEX idx_batch_job_log_submitted ON batch_job_log(submitted_at);
CREATE INDEX idx_batch_job_log_report_type ON batch_job_log(report_type);

-- View for monitoring
CREATE OR REPLACE VIEW batch_job_summary AS
SELECT
    DATE(submitted_at) as batch_date,
    correlation_id,
    report_type,
    COUNT(*) as total_jobs,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
    COUNT(CASE WHEN status = 'PROCESSING' THEN 1 END) as processing,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
    AVG(processing_time_ms) as avg_processing_time_ms,
    SUM(records_processed) as total_records
FROM batch_job_log
GROUP BY DATE(submitted_at), correlation_id, report_type
ORDER BY batch_date DESC, correlation_id;
```

---

## Deployment Guide

### Phase 1: Local Testing

```bash
# Step 1: Ensure Keycloak and existing services are running
docker network create cmips-shared-network 2>/dev/null || true
cd /Users/mythreya/Desktop/sajeevs-codebase-main
docker-compose up -d

# Step 2: Build the updated Spring app
cd /Users/mythreya/Desktop/trial
mvn clean package -DskipTests

# Step 3: Start batch testing environment
docker-compose -f docker-compose.batch-test.yml up -d

# Step 4: Scale workers
docker-compose -f docker-compose.batch-test.yml up -d --scale batch-worker=5

# Step 5: Get a JWT token
TOKEN=$(curl -s -X POST "http://localhost:8085/realms/cmips/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=cmips-frontend" \
  -d "username=supervisor_ct1" \
  -d "password=password123" | jq -r '.access_token')

# Step 6: Submit test jobs
curl -X POST http://localhost:8080/api/batch/submit-test-jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"jobCount": 50, "reportType": "COUNTY_DAILY", "role": "CASE_WORKER"}'

# Step 7: Monitor queue status
watch -n 2 'curl -s http://localhost:8080/api/batch/queue-status | jq'

# Step 8: Check worker logs
docker-compose -f docker-compose.batch-test.yml logs -f batch-worker

# Step 9: Verify results in database
docker exec -it trial-postgres psql -U postgres -d ihsscmips -c \
  "SELECT status, COUNT(*) FROM batch_job_log GROUP BY status;"
```

### Phase 2: AWS Deployment

```bash
# Step 1: Create AWS resources
aws cloudformation deploy \
  --template-file aws-batch-compute-environment.yml \
  --stack-name timesheet-batch \
  --parameter-overrides \
    VpcId=vpc-xxxxx \
    SubnetIds=subnet-xxxxx,subnet-yyyyy \
    MaxvCpus=256 \
  --capabilities CAPABILITY_IAM

# Step 2: Store secrets in Secrets Manager
aws secretsmanager create-secret \
  --name timesheet/db-credentials \
  --secret-string '{"username":"cmips_app","password":"your-password"}'

aws secretsmanager create-secret \
  --name timesheet/keycloak-credentials \
  --secret-string '{"client_secret":"your-keycloak-secret"}'

# Step 3: Build and push Docker image
docker build -t timesheet-batch-worker .
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker tag timesheet-batch-worker:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/timesheet-batch-worker:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/timesheet-batch-worker:latest

# Step 4: Deploy coordinator (ECS or EC2)
# Update coordinator to use AWS Batch instead of Redis

# Step 5: Test AWS Batch job submission
aws batch submit-job \
  --job-name test-job-001 \
  --job-queue timesheet-batch-queue \
  --job-definition timesheet-batch-worker \
  --container-overrides '{
    "environment": [
      {"name": "JOB_ID", "value": "TEST_001"},
      {"name": "REPORT_TYPE", "value": "COUNTY_DAILY"},
      {"name": "ROLE", "value": "CASE_WORKER"}
    ]
  }'

# Step 6: Monitor job
aws batch describe-jobs --jobs <job-id>
```

---

## Monitoring and Troubleshooting

### Key Metrics to Monitor

| Metric | Source | Alert Threshold |
|--------|--------|-----------------|
| Queue Depth | Redis/AWS Batch | > 1000 jobs for > 30 min |
| Processing Time | Database | > 10 min avg per job |
| Failed Jobs | Database | > 5% failure rate |
| Worker Count | Docker/AWS Batch | < expected parallelism |
| DB Connections | RDS | > 80% max connections |

### Useful Commands

```bash
# Local Testing - Redis queue monitoring
docker exec batch-redis redis-cli LLEN batch:job:queue
docker exec batch-redis redis-cli LLEN batch:job:processing
docker exec batch-redis redis-cli LLEN batch:job:dead-letter

# Local Testing - Worker status
docker-compose -f docker-compose.batch-test.yml ps
docker-compose -f docker-compose.batch-test.yml logs --tail=50 batch-worker

# AWS Batch - Job monitoring
aws batch list-jobs --job-queue timesheet-batch-queue --job-status RUNNING
aws batch list-jobs --job-queue timesheet-batch-queue --job-status FAILED

# Database - Job status summary
psql -c "SELECT status, COUNT(*), AVG(processing_time_ms) FROM batch_job_log WHERE submitted_at > NOW() - INTERVAL '1 day' GROUP BY status;"
```

### Common Issues and Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| Jobs stuck in PENDING | Compute environment not scaling | Check max vCPUs, security groups |
| High job failure rate | DB connection exhaustion | Use RDS Proxy, increase pool size |
| Slow processing | Under-provisioned workers | Increase worker CPU/memory |
| Token expiration | Long-running jobs | Refresh token mid-job |
| Missing output files | S3 permissions | Check IAM role policies |

---

## Summary

### What Changes
1. Add Redis dependency and batch queue service
2. Add coordinator/worker Spring profiles
3. Create batch worker service that polls queue
4. Create batch coordinator service that submits jobs
5. Add new REST endpoints for batch operations
6. Create database table for job logging

### What Stays the Same
1. Your existing batch processing logic
2. Keycloak/JWT security
3. Field masking rules
4. Database schema (for business data)
5. Docker image (just add new components)

### Testing Approach
1. **Local first**: Docker Compose with Redis queue, scale workers
2. **Validate**: Submit 50-100 test jobs, verify parallel processing
3. **Load test**: Submit 500-1000 jobs, measure throughput
4. **AWS deployment**: Once local testing passes

### Expected Results
- **Local (5 workers)**: 5x faster than single container
- **Local (10 workers)**: 10x faster
- **AWS Batch (50-100 workers)**: 50-100x faster
- **Target**: 2,500 jobs in 3-4 hours ✅
