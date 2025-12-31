# Asynchronous Batch Processing Architecture

## Timesheet Management System

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Core Components](#core-components)
4. [Job Lifecycle](#job-lifecycle)
5. [Processing Model](#processing-model)
6. [Design Decisions](#design-decisions)
7. [Scalability](#scalability)

---

## Overview

The batch processing system uses a **poll-based database queue** architecture for asynchronous job execution. Jobs are stored in PostgreSQL and processed by background worker threads.

### Key Characteristics

| Attribute | Value |
|-----------|-------|
| Pattern | Poll-based Database Queue |
| Processing | Asynchronous, Chunked |
| Workers | 2 threads, 3 jobs/poll |
| Poll Interval | 5 seconds |
| Job Persistence | PostgreSQL `report_jobs` table |

---

## Architecture Pattern

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         ASYNC BATCH PROCESSING                          │
└─────────────────────────────────────────────────────────────────────────┘

                              JOB SUBMISSION
                              ──────────────
    ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
    │  REST API    │     │  Scheduled   │     │  Dependency  │
    │  Request     │     │  Cron Job    │     │  Trigger     │
    └──────┬───────┘     └──────┬───────┘     └──────┬───────┘
           │                    │                    │
           └────────────────────┼────────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │    JobQueueService    │
                    │   (Queue Job with     │
                    │    status=QUEUED)     │
                    └───────────┬───────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATABASE QUEUE                                  │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  PostgreSQL: report_jobs                                          │  │
│  │                                                                   │  │
│  │  job_id   │ status     │ priority │ created_at   │ ...           │  │
│  │  ─────────┼────────────┼──────────┼──────────────┼────────────── │  │
│  │  JOB_001  │ QUEUED     │ 7        │ 10:30:00     │               │  │
│  │  JOB_002  │ QUEUED     │ 5        │ 10:30:05     │               │  │
│  │  JOB_003  │ PROCESSING │ 8        │ 10:29:00     │               │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                │
                                │ Poll every 5 seconds
                                ▼
                    ┌───────────────────────┐
                    │   BatchJobScheduler   │
                    │  @Scheduled(5000ms)   │
                    │  - Poll QUEUED jobs   │
                    │  - Claim atomically   │
                    │  - Dispatch to pool   │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │  ThreadPoolExecutor   │
                    │  ┌─────────────────┐  │
                    │  │ batch-worker-1  │  │
                    │  │ batch-worker-2  │  │
                    │  └─────────────────┘  │
                    │    Queue capacity: 3  │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ BackgroundProcessing  │
                    │      Service          │
                    │  - Process chunks     │
                    │  - Update progress    │
                    │  - Handle retries     │
                    │  - Trigger deps       │
                    └───────────────────────┘
```

---

## Core Components

### 1. JobQueueService

Manages job queue operations.

```java
// Queue a new job
public String queueReportJob(BIReportRequest request, String jwtToken) {
    ReportJobEntity job = new ReportJobEntity();
    job.setJobId(generateJobId());
    job.setStatus("QUEUED");
    job.setJwtToken(jwtToken);     // Preserve user context
    job.setPriority(request.getPriority());
    job.setCreatedAt(LocalDateTime.now());
    return repository.save(job).getJobId();
}

// Atomically claim a job (prevents duplicate processing)
@Transactional
public Optional<ReportJobEntity> markJobAsProcessing(String jobId) {
    ReportJobEntity job = repository.findById(jobId).orElse(null);
    if (job != null && "QUEUED".equals(job.getStatus())) {
        job.setStatus("PROCESSING");
        job.setStartedAt(LocalDateTime.now());
        return Optional.of(repository.save(job));
    }
    return Optional.empty();  // Already claimed
}
```

### 2. BatchJobScheduler

Polls database and dispatches jobs to worker threads.

```java
@Scheduled(fixedDelayString = "${batch.scheduler.interval-ms:5000}")
public void pollAndDispatchJobs() {
    // 1. Query for QUEUED jobs (priority DESC, created_at ASC)
    List<ReportJobEntity> jobs = repository
        .findByStatusOrderByPriorityDescCreatedAtAsc("QUEUED", Limit.of(3));

    // 2. Claim and dispatch each job
    for (ReportJobEntity job : jobs) {
        jobQueueService.markJobAsProcessing(job.getJobId())
            .ifPresent(claimed ->
                executor.execute(() ->
                    backgroundService.processJob(claimed.getJobId())
                )
            );
    }
}
```

### 3. BackgroundProcessingService

Executes job logic with chunked processing.

```java
public void processJob(String jobId) {
    ReportJobEntity job = repository.findById(jobId);

    // Process in chunks for memory efficiency
    int chunkSize = 1000;
    long totalRecords = countRecords(job);
    long processed = 0;

    while (processed < totalRecords) {
        // Check for cancellation
        if (isJobCancelled(jobId)) {
            cleanup(job);
            return;
        }

        // Fetch and process chunk
        List<Data> chunk = fetchChunk(job, processed, chunkSize);
        writeToFile(chunk, outputFile);

        // Update progress
        processed += chunk.size();
        updateProgress(jobId, processed, totalRecords);
    }

    // Mark complete and trigger dependencies
    markCompleted(job);
    jobDependencyService.triggerDependentJobs(job);
}
```

### 4. JobDependencyService

Triggers dependent jobs when parent completes.

```java
public void triggerDependentJobs(ReportJobEntity parentJob) {
    // Find matching dependency rules
    List<DependencyRule> rules = findMatchingRules(parentJob);

    for (DependencyRule rule : rules) {
        if (rule.isMultipleDependency()) {
            // Check if ALL parents are complete
            if (checkMultipleDependencies(rule, parentJob)) {
                createDependentJob(rule, parentJob);
            }
        } else {
            // Single dependency - trigger immediately
            createDependentJob(rule, parentJob);
        }
    }
}
```

---

## Job Lifecycle

### Status State Machine

```
                         ┌─────────────┐
                         │   (start)   │
                         └──────┬──────┘
                                │ queueJob()
                                ▼
                         ┌─────────────┐
              ┌──────────│   QUEUED    │──────────┐
              │          └──────┬──────┘          │
              │                 │                 │
              │ cancel()        │ claim()         │ cancel()
              │                 ▼                 │
              │          ┌─────────────┐          │
              │          │ PROCESSING  │──────────┤
              │          └──────┬──────┘          │
              │                 │                 │
              │    ┌────────────┼────────────┐    │
              │    │            │            │    │
              │    ▼            ▼            ▼    │
              │ ┌──────┐  ┌──────────┐  ┌────────┐│
              └►│CANCEL│  │COMPLETED │  │ FAILED ││
                └──────┘  └──────────┘  └────────┘
```

### Status Transitions

| From | To | Trigger |
|------|----|---------|
| - | QUEUED | Job submitted via API, cron, or dependency |
| QUEUED | PROCESSING | Worker claims job atomically |
| QUEUED | CANCELLED | User cancels before processing starts |
| PROCESSING | COMPLETED | All chunks processed successfully |
| PROCESSING | FAILED | Max retries exceeded or fatal error |
| PROCESSING | CANCELLED | User cancels during processing |

---

## Processing Model

### Chunked Processing Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      CHUNKED PROCESSING                                 │
│                                                                         │
│  Total Records: 10,000                                                  │
│  Chunk Size: 1,000                                                      │
│                                                                         │
│  ┌─────────┐                                                            │
│  │ Chunk 1 │ → Fetch 1-1000 → Process → Write → Progress: 10%          │
│  └─────────┘                                                            │
│       │                                                                 │
│       │ Check cancelled? No → Continue                                  │
│       ▼                                                                 │
│  ┌─────────┐                                                            │
│  │ Chunk 2 │ → Fetch 1001-2000 → Process → Write → Progress: 20%       │
│  └─────────┘                                                            │
│       │                                                                 │
│       │ Check cancelled? No → Continue                                  │
│       ▼                                                                 │
│      ...                                                                │
│       │                                                                 │
│       ▼                                                                 │
│  ┌──────────┐                                                           │
│  │ Chunk 10 │ → Fetch 9001-10000 → Process → Write → Progress: 100%    │
│  └──────────┘                                                           │
│       │                                                                 │
│       ▼                                                                 │
│  ┌──────────────────────┐                                               │
│  │ COMPLETED            │                                               │
│  │ Trigger dependencies │                                               │
│  └──────────────────────┘                                               │
└─────────────────────────────────────────────────────────────────────────┘
```

### Retry Mechanism

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       RETRY BEHAVIOR                                    │
│                                                                         │
│  Chunk fails → Retry with exponential backoff                           │
│                                                                         │
│  Attempt 1: Fail → Wait 1s                                              │
│  Attempt 2: Fail → Wait 2s                                              │
│  Attempt 3: Fail → Mark job FAILED                                      │
│                                                                         │
│  Max retries: 3 (configurable)                                          │
│  Backoff: 1s, 2s, 3s (linear)                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Design Decisions

### 1. Database Queue vs Message Broker

**Chosen: Poll-based Database Queue**

```
┌─────────────────────────────────────────────────────────────────────────┐
│  DATABASE QUEUE (Chosen)              MESSAGE BROKER (Alternative)      │
│  ─────────────────────────            ─────────────────────────────     │
│                                                                         │
│  Scheduler ──(poll)──► PostgreSQL     Producer ──► Kafka ──► Consumer  │
│                                                                         │
│  ✓ No additional infrastructure       ✗ Requires Kafka/RabbitMQ         │
│  ✓ Transactional consistency          ✗ Eventual consistency            │
│  ✓ Easy debugging via SQL             ✗ Harder to inspect               │
│  ✓ Built-in persistence               ✓ High throughput                 │
│  ✗ Polling overhead                   ✓ Push-based delivery             │
│  ✗ 5s latency                         ✓ Sub-second latency              │
└─────────────────────────────────────────────────────────────────────────┘

Rationale: Simpler architecture suitable for expected load (~100 jobs/day)
```

### 2. Atomic Job Claiming

**Chosen: Status-check within transaction**

```java
@Transactional
public Optional<ReportJobEntity> markJobAsProcessing(String jobId) {
    ReportJobEntity job = repository.findById(jobId).orElse(null);
    if (job != null && "QUEUED".equals(job.getStatus())) {  // Check status
        job.setStatus("PROCESSING");                         // Update atomically
        return Optional.of(repository.save(job));
    }
    return Optional.empty();  // Another worker claimed it
}
```

**Why:** Prevents duplicate processing when multiple workers poll simultaneously.

### 3. Push-Based Dependency Triggering

**Chosen: Parent triggers child on completion**

```
┌─────────────────────────────────────────────────────────────────────────┐
│  PUSH MODEL (Chosen)                  PULL MODEL (Alternative)          │
│  ─────────────────────                ───────────────────────           │
│                                                                         │
│  Parent completes                     Child polls: "Are parents done?"  │
│       │                                    │                            │
│       ▼                                    │ (wasteful polling)         │
│  triggerDependentJobs()                    │                            │
│       │                                    │                            │
│       ▼                                    ▼                            │
│  Creates child job immediately        Eventually starts                 │
│                                                                         │
│  ✓ Lower latency                      ✗ Higher latency                  │
│  ✓ No wasted polling                  ✗ Polling overhead                │
│  ✓ Clean failure handling             ✗ Orphaned waiting jobs           │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4. JWT Token Capture

**Chosen: Store token with job at submission**

```
Job Submission                    Background Processing
───────────────                   ────────────────────
1. User submits with JWT    →     4. Load job from DB
2. Extract role/county      →     5. Extract role from stored JWT
3. Store JWT in job record  →     6. Apply role-based data filtering
```

**Why:** Preserves user authorization context for async processing.

---

## Scalability

### Current Configuration

| Parameter | Value | Throughput |
|-----------|-------|------------|
| Workers | 2 | 2 concurrent jobs |
| Jobs/poll | 3 | 3 jobs queued per cycle |
| Poll interval | 5s | ~36 jobs/minute max |
| Chunk size | 1000 | Memory-efficient |

### Horizontal Scaling

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       HORIZONTAL SCALING                                │
│                                                                         │
│  Single Instance                    Multiple Instances                  │
│  ───────────────                    ──────────────────                  │
│                                                                         │
│  ┌───────────┐                      ┌───────────┐                       │
│  │Instance 1 │◄──poll──►            │Instance 1 │◄──┐                   │
│  │ 2 workers │          PostgreSQL  │ 2 workers │   │                   │
│  └───────────┘                      └───────────┘   │                   │
│                                                     ├──► PostgreSQL     │
│                                     ┌───────────┐   │    (atomic        │
│                                     │Instance 2 │◄──┘     claiming)     │
│                                     │ 2 workers │                       │
│                                     └───────────┘                       │
│                                                                         │
│  Works out-of-box: Atomic claiming prevents duplicate processing        │
│  Double throughput: 4 concurrent jobs                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Performance Limits

| Scenario | Limit | Mitigation |
|----------|-------|------------|
| High volume | ~1000 jobs/day | Add instances or migrate to message broker |
| Large datasets | Memory constraints | Reduce chunk size |
| Latency sensitive | 5s max start delay | Reduce poll interval |

---

## Summary

The asynchronous batch processing architecture provides:

| Aspect | Approach |
|--------|----------|
| **Queue** | PostgreSQL table with poll-based consumption |
| **Concurrency** | Thread pool with atomic job claiming |
| **Processing** | Chunked for memory efficiency and progress tracking |
| **Dependencies** | Push-based triggering on parent completion |
| **Scaling** | Horizontal via multiple instances |

**Trade-offs:**
- Simplicity over throughput
- Consistency over latency
- Debuggability over performance
