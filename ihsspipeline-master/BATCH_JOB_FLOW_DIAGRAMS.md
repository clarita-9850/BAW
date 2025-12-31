# Batch Job Processing - Complete Flow Diagrams

## Overview
This document provides comprehensive flow diagrams for the batch job processing system, showing how jobs are created, processed, and how dependencies are tracked.

---

## 1. Job Creation Flow (Entry Points)

### 1.1 Scheduled Job Creation Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SCHEDULED JOB CREATION FLOW                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  Spring @EnableScheduling (Application Startup)                        │
│  - Enables scheduled task execution                                     │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  ScheduledReportService (Cron Jobs)                                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ @Scheduled(cron = "0 30 5 * * ?") - Daily Reports                │  │
│  │ @Scheduled(cron = "0 30 5 * * MON") - Weekly Reports            │  │
│  │ @Scheduled(cron = "0 30 5 1 * ?") - Monthly Reports             │  │
│  │ @Scheduled(cron = "0 30 5 1 1,4,7,10 ?") - Quarterly Reports   │  │
│  │ @Scheduled(cron = "0 30 5 1 1 ?") - Yearly Reports              │  │
│  │ @Scheduled(cron = "0 */2 * * * ?") - System Scheduler           │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ 1. Cron triggers at scheduled time
                        │ 2. Gets SYSTEM_SCHEDULER token from Keycloak
                        │ 3. Creates BIReportRequest for each role/report type
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobQueueService.queueReportJob()                                       │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Generates unique Job ID (JOB_XXXXXXXX)                       │  │
│  │ • Creates ReportJobEntity                                        │  │
│  │ • Sets status = "QUEUED"                                         │  │
│  │ • Stores JWT token with job                                      │  │
│  │ • Serializes request data                                        │  │
│  │ • Estimates completion time                                       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Saves to database
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Database (report_jobs table)                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ job_id: JOB_XXXXXXXX                                              │  │
│  │ status: QUEUED                                                    │  │
│  │ report_type: DAILY_REPORT                                         │  │
│  │ user_role: CASE_WORKER                                            │  │
│  │ jwt_token: <token>                                                │  │
│  │ parent_job_id: NULL (no parent)                                  │  │
│  │ created_at: <timestamp>                                          │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Manual Job Creation Flow (API Request)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    MANUAL JOB CREATION FLOW                             │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  Frontend / API Client                                                  │
│  POST /api/reports/generate                                             │
│  Headers: Authorization: Bearer <JWT_TOKEN>                             │
│  Body: { reportType, targetSystem, dataFormat, ... }                   │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BusinessIntelligenceController.generateBIReport()                       │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ 1. Validates Authorization header                                │  │
│  │ 2. Extracts JWT token                                            │  │
│  │ 3. Parses JWT to get user role & countyId                         │  │
│  │ 4. Creates BIReportRequest from request body                     │  │
│  │ 5. Sets role from JWT (security - ignores request body)          │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Calls queueReportJob()
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobQueueService.queueReportJob()                                       │
│  (Same as scheduled flow)                                               │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Saves to database
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Database (report_jobs table)                                          │
│  Status: QUEUED                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Job Processing Flow (Complete Pipeline)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    JOB PROCESSING FLOW                                  │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  BatchJobScheduler                                                       │
│  @Scheduled(fixedDelay = 5000ms) - Runs every 5 seconds                 │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ pollAndDispatchJobs()                                             │  │
│  │ 1. Checks if scheduler enabled                                   │  │
│  │ 2. Queries: findTopQueuedJobs(maxJobsPerPoll=3)                  │  │
│  │ 3. Gets jobs with status = "QUEUED"                               │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ For each QUEUED job found
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobQueueService.markJobAsProcessing()                                   │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Uses optimistic locking (version field)                       │  │
│  │ • Updates status: QUEUED → PROCESSING                            │  │
│  │ • Sets started_at timestamp                                      │  │
│  │ • Returns job if successfully claimed                            │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ If successfully claimed
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BatchProcessingConfig.batchJobExecutor                                 │
│  ThreadPoolTaskExecutor (worker-pool-size: 2)                           │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Executes job in background thread                               │  │
│  │ • Calls: backgroundProcessingService.processJob(jobId)            │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Executes in worker thread
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService.processJob()                               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ 1. Loads job from database by jobId                              │  │
│  │ 2. Deserializes BIReportRequest from job.requestData             │  │
│  │ 3. Extracts JWT token from job.jwtToken                          │  │
│  │ 4. Calls processReportInBackground()                             │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService.processReportInBackground()                │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ 1. Checks if job was cancelled                                    │  │
│  │ 2. Parses JWT token to extract user role & countyId               │  │
│  │ 3. Creates PipelineExtractionRequest                             │  │
│  │ 4. Calls processDataInChunksStreaming()                          │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService.processDataInChunksStreaming()              │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ 1. Calculates total records to process                            │  │
│  │ 2. Processes data in chunks (chunkSize from request)             │  │
│  │ 3. For each chunk:                                               │  │
│  │    a. Fetches data (DataFetchingService)                         │  │
│  │    b. Applies field masking (ReportGenerationService)           │  │
│  │    c. Writes to file stream                                      │  │
│  │    d. Updates job progress                                      │  │
│  │ 4. Returns result file path                                       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Returns resultPath
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobQueueService.setJobResult()                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Updates status: PROCESSING → COMPLETED                          │  │
│  │ • Sets result_path to file location                              │  │
│  │ • Sets completed_at timestamp                                    │  │
│  │ • Updates progress to 100%                                       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Job marked as COMPLETED
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService.triggerDependentJobs()                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Checks if JobDependencyService available                        │  │
│  │ • Reloads job from DB to get latest status                       │  │
│  │ • Only triggers if status = "COMPLETED"                          │  │
│  │ • Calls JobDependencyService.triggerDependentJobs()               │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ If dependencies found
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobDependencyService.triggerDependentJobs()                            │
│  (See Dependency Flow below)                                            │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Dependency Tracking Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DEPENDENCY TRACKING FLOW                              │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService.triggerDependentJobs()                     │
│  (Called after job completes successfully)                             │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Passes: completedJob, jwtToken
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobDependencyService.triggerDependentJobs()                             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ STEP 1: Check if dependency system enabled                      │  │
│  │ STEP 2: Extract parent job details                             │  │
│  │   - parentReportType: DAILY_REPORT                              │  │
│  │   - parentRole: CASE_WORKER                                    │  │
│  │   - parentStatus: COMPLETED                                     │  │
│  │ STEP 3: Find matching dependencies                              │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Calls findMatchingDependencies()
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobDependencyService.findMatchingDependencies()                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Reads from JobDependencyConfig (from application.yml)         │  │
│  │ • Filters dependencies by:                                       │  │
│  │   - parentReportType matches                                     │  │
│  │   - parentRole matches (if specified in config)                  │  │
│  │   - condition met (ON_SUCCESS or ON_COMPLETION)                │  │
│  │ • Returns list of matching JobDependency objects                │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Returns matching dependencies
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobDependencyService.triggerDependentJobs() - Processing               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ For each matching dependency:                                    │  │
│  │                                                                   │  │
│  │ IF Single Dependency:                                            │  │
│  │   └─> Create dependent job immediately                           │  │
│  │                                                                   │  │
│  │ IF Multiple Dependency:                                          │  │
│  │   └─> Call checkMultipleDependencies()                          │  │
│  │       • Queries DB for all required parent report types         │  │
│  │       • Checks if ALL parents have status = COMPLETED           │  │
│  │       • If all found → Create dependent job                     │  │
│  │       • If not all found → Skip (wait for other parents)         │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ For each dependency to trigger
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobDependencyService.createDependentJobRequest()                       │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Creates BIReportRequest for dependent job                      │  │
│  │ • Sets dependentReportType from config                            │  │
│  │ • Sets role (from config or inherits from parent)                │  │
│  │ • Sets targetSystem, dataFormat, priority                        │  │
│  │ • Sets chunkSize (uses dependent-chunk-size from config)         │  │
│  │ • Adds parentJobId to metadata                                   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Calls queueReportJob() with parentJobId
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobQueueService.queueReportJob(request, jwtToken, parentJobId)         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Generates dependent job ID                                    │  │
│  │ • Creates ReportJobEntity                                        │  │
│  │ • Sets parentJobId = <parent job ID>                             │  │
│  │ • Sets status = "QUEUED"                                         │  │
│  │ • Saves to database                                              │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Dependent job queued
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Database (report_jobs table)                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ job_id: JOB_DEPENDENT_XX                                         │  │
│  │ status: QUEUED                                                    │  │
│  │ report_type: DAILY_SUMMARY                                        │  │
│  │ parent_job_id: JOB_PARENT_XX  ← Links to parent                  │  │
│  │ user_role: CASE_WORKER                                           │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ BatchJobScheduler picks up dependent job
                        │ (Same processing flow as parent job)
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Dependent Job Processing (Same as parent job flow)                     │
│  • Processed by BatchJobScheduler                                       │
│  • Executed by BackgroundProcessingService                              │
│  • Can have its own dependent jobs (chain dependencies)                │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Complete End-to-End Flow (All Services)

```
┌─────────────────────────────────────────────────────────────────────────┐
│              COMPLETE BATCH JOB PROCESSING FLOW                          │
│                    (All Services & Interactions)                        │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  ENTRY POINTS                                                             │
│  ┌──────────────────────┐  ┌──────────────────────┐                     │
│  │ 1. Scheduled Cron    │  │ 2. Manual API Call   │                     │
│  │    (ScheduledReport  │  │    (BusinessIntel    │                     │
│  │     Service)         │  │     Controller)      │                     │
│  └──────────┬───────────┘  └──────────┬───────────┘                     │
│             │                         │                                  │
│             └──────────┬──────────────┘                                 │
│                        │                                                 │
│                        ▼                                                 │
│            ┌───────────────────────────┐                                │
│            │  JobQueueService          │                                │
│            │  queueReportJob()         │                                │
│            │  • Generate Job ID        │                                │
│            │  • Create Entity          │                                │
│            │  • Set status = QUEUED    │                                │
│            └───────────┬───────────────┘                                │
│                        │                                                 │
│                        │ Save to DB                                      │
│                        ▼                                                 │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  DATABASE (report_jobs table)                                            │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Status: QUEUED                                                    │  │
│  │ Waiting for scheduler to pick up                                 │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │
                           │ Polled every 5 seconds
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BatchJobScheduler                                                       │
│  @Scheduled(fixedDelay = 5000ms)                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ pollAndDispatchJobs()                                             │  │
│  │ • Query: findTopQueuedJobs(3)                                     │  │
│  │ • For each job: markJobAsProcessing()                            │  │
│  │ • Execute in thread pool                                          │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │
                           │ Status: QUEUED → PROCESSING
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  ThreadPoolTaskExecutor (batchJobExecutor)                               │
│  Worker Thread Pool (size: 2)                                            │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Executes: backgroundProcessingService.processJob(jobId)           │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService                                              │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ processJob(jobId)                                                 │  │
│  │  1. Load job from DB                                             │  │
│  │  2. Deserialize request                                          │  │
│  │  3. Extract JWT token                                            │  │
│  │  4. processReportInBackground()                                 │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
│                           │                                               │
│                           ▼                                               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ processReportInBackground()                                       │  │
│  │  1. Parse JWT for user role & countyId                           │  │
│  │  2. Create PipelineExtractionRequest                            │  │
│  │  3. processDataInChunksStreaming()                               │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
│                           │                                               │
│                           ▼                                               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ processDataInChunksStreaming()                                   │  │
│  │  For each chunk:                                                 │  │
│  │    └─> DataFetchingService.fetchData()                          │  │
│  │    └─> ReportGenerationService.applyFieldMasking()             │  │
│  │    └─> Write to file stream                                     │  │
│  │    └─> Update job progress                                      │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │
                           │ Returns resultPath
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobQueueService.setJobResult()                                          │
│  • Status: PROCESSING → COMPLETED                                       │
│  • Set result_path                                                       │
│  • Set completed_at                                                      │
└───────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Job completed successfully
                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService.triggerDependentJobs()                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ • Check if JobDependencyService available                        │  │
│  │ • Reload job to verify status = COMPLETED                        │  │
│  │ • Call JobDependencyService.triggerDependentJobs()              │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  JobDependencyService                                                    │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ triggerDependentJobs()                                          │  │
│  │  1. Find matching dependencies (JobDependencyConfig)            │  │
│  │  2. For each dependency:                                          │  │
│  │     IF Single: Create dependent job                               │  │
│  │     IF Multiple: Check all parents completed → Create job        │  │
│  │  3. createDependentJobRequest()                                   │  │
│  │  4. JobQueueService.queueReportJob(..., parentJobId)             │  │
│  └───────────────────────┬──────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │
                           │ Dependent jobs queued
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Database (report_jobs table)                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Dependent jobs with parentJobId set                               │  │
│  │ Status: QUEUED                                                     │  │
│  │ (Cycle repeats - BatchJobScheduler picks them up)                 │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Service Responsibilities Summary

### **JobQueueService**
- **Responsibility**: Job queue management
- **Key Methods**:
  - `queueReportJob()` - Creates and queues new jobs
  - `markJobAsProcessing()` - Claims job for processing (optimistic locking)
  - `setJobResult()` - Marks job as completed
  - `updateJobStatus()` - Updates job status
  - `getJobStatus()` - Retrieves job status

### **BatchJobScheduler**
- **Responsibility**: Polls database and dispatches jobs
- **Key Methods**:
  - `pollAndDispatchJobs()` - Runs every 5 seconds, finds QUEUED jobs, dispatches to workers
- **Configuration**: `batch.scheduler.*` in application.yml

### **BackgroundProcessingService**
- **Responsibility**: Executes job processing logic
- **Key Methods**:
  - `processJob()` - Entry point for job execution
  - `processReportInBackground()` - Main processing logic
  - `processDataInChunksStreaming()` - Chunked data processing
  - `triggerDependentJobs()` - Initiates dependency checking

### **JobDependencyService**
- **Responsibility**: Tracks and triggers dependent jobs
- **Key Methods**:
  - `triggerDependentJobs()` - Main dependency handler
  - `findMatchingDependencies()` - Finds matching dependency rules
  - `checkMultipleDependencies()` - Validates multiple parent jobs
  - `createDependentJobRequest()` - Creates dependent job request

### **ScheduledReportService**
- **Responsibility**: Creates scheduled jobs via cron
- **Key Methods**:
  - `generateDailyReports()` - Daily cron job
  - `generateWeeklyReports()` - Weekly cron job
  - `generateMonthlyReports()` - Monthly cron job
  - `generateQuarterlyReports()` - Quarterly cron job
  - `generateYearlyReports()` - Yearly cron job
  - `generateCountyReportsForScheduler()` - System scheduler cron job

### **BusinessIntelligenceController**
- **Responsibility**: API endpoint for manual job creation
- **Key Methods**:
  - `generateBIReport()` - REST endpoint for creating jobs

### **DataFetchingService**
- **Responsibility**: Fetches data from database
- **Used by**: BackgroundProcessingService during chunk processing

### **ReportGenerationService**
- **Responsibility**: Applies field masking and generates reports
- **Used by**: BackgroundProcessingService during chunk processing

---

## 6. Database Schema (Key Fields)

```sql
report_jobs table:
├── job_id (PK)              -- Unique job identifier
├── status                    -- QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED
├── report_type              -- DAILY_REPORT, WEEKLY_REPORT, etc.
├── user_role                -- CASE_WORKER, SUPERVISOR, ADMIN, etc.
├── parent_job_id            -- NULL for parent jobs, job_id for dependent jobs
├── jwt_token                -- Stored JWT token for authentication
├── request_data             -- Serialized BIReportRequest (JSON)
├── result_path              -- File path to generated report
├── progress                 -- 0-100 percentage
├── created_at               -- Job creation timestamp
├── started_at               -- Processing start timestamp
├── completed_at             -- Completion timestamp
├── estimated_completion_time -- Estimated completion time
└── version                  -- Optimistic locking version field
```

---

## 7. Configuration Points

### **Batch Scheduler**
```yaml
batch:
  scheduler:
    enabled: true              # Enable/disable scheduler
    interval-ms: 5000         # Poll interval (5 seconds)
    max-jobs-per-poll: 3       # Max jobs per poll
    worker-pool-size: 2        # Thread pool size
```

### **Job Dependencies**
```yaml
job-dependencies:
  enabled: true                # Enable/disable dependency system
  dependencies:
    - parent-report-type: "DAILY_REPORT"
      dependent-report-type: "DAILY_SUMMARY"
      condition: "ON_SUCCESS"   # or "ON_COMPLETION"
```

### **Scheduled Reports**
```yaml
report:
  scheduling:
    enabled: true
    daily-report-cron: "0 30 5 * * ?"
    weekly-report-cron: "0 30 5 * * MON"
    # ... other cron expressions
```

---

## 8. Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ERROR HANDLING FLOW                                  │
└─────────────────────────────────────────────────────────────────────────┘

Job Processing Error:
  BackgroundProcessingService.processJob()
    └─> Catch Exception
        └─> JobQueueService.updateJobStatus(jobId, "FAILED", errorMessage)
            └─> Status: PROCESSING → FAILED
                └─> Dependent jobs NOT triggered (only on SUCCESS)

Dependency Error:
  JobDependencyService.triggerDependentJobs()
    └─> Catch Exception
        └─> Log error (does NOT affect parent job status)
            └─> Parent job remains COMPLETED

Job Claiming Error:
  JobQueueService.markJobAsProcessing()
    └─> Optimistic locking failure
        └─> Job already claimed by another thread
            └─> Returns empty Optional
                └─> BatchJobScheduler skips this job
```

---

## 9. Key Design Patterns

1. **Optimistic Locking**: Prevents race conditions when claiming jobs
2. **Thread Pool Pattern**: Parallel job processing with configurable pool size
3. **Dependency Chain Pattern**: Jobs can have dependent jobs, creating chains
4. **Chunked Processing**: Large datasets processed in chunks for memory efficiency
5. **Event-Driven**: Cron jobs trigger job creation, scheduler triggers processing
6. **Separation of Concerns**: Each service has a single responsibility

---

## 10. Monitoring Points

- **Job Queue Size**: Number of QUEUED jobs in database
- **Processing Jobs**: Number of PROCESSING jobs
- **Completed Jobs**: Success rate and completion times
- **Failed Jobs**: Error messages and failure reasons
- **Dependent Jobs**: Number of dependent jobs created
- **Worker Thread Utilization**: Active vs idle threads

