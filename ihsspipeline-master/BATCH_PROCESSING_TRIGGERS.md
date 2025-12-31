# Batch Processing Triggers - Analysis

## Overview
The batch processing system has two main components:
1. **Job Creators** - Scheduled cron jobs that create and queue batch jobs
2. **Job Processor** - Polls the database and processes queued jobs

---

## 1. BatchJobScheduler (Job Processor)

**Location**: `src/main/java/com/example/kafkaeventdrivenapp/service/BatchJobScheduler.java`

**What it does:**
- Polls the database every **5 seconds** (configurable) for queued jobs
- Dispatches jobs to worker threads for processing
- Processes up to **3 jobs per poll** (configurable)

**Configuration** (`application.yml`):
```yaml
batch:
  scheduler:
    enabled: true                    # Enable/disable the scheduler
    interval-ms: 5000                # Poll every 5 seconds
    max-jobs-per-poll: 3            # Process max 3 jobs per poll
    worker-pool-size: 2             # Number of worker threads
```

**Trigger Method:**
```java
@Scheduled(fixedDelayString = "${batch.scheduler.interval-ms:5000}")
public void pollAndDispatchJobs()
```

**Flow:**
1. Checks if scheduler is enabled
2. Queries database for queued jobs (`findTopQueuedJobs`)
3. For each queued job:
   - Marks job as "PROCESSING"
   - Executes job in background thread pool
   - Calls `BackgroundProcessingService.processJob()`

---

## 2. ScheduledReportService (Job Creators)

**Location**: `src/main/java/com/example/kafkaeventdrivenapp/service/ScheduledReportService.java`

**Multiple cron jobs that create batch jobs:**

### A. Daily Reports
```java
@Scheduled(cron = "${report.scheduling.daily-report-cron:0 30 5 * * ?}", zone = "Asia/Kolkata")
public void generateDailyReports()
```
- **Schedule**: Every day at 5:30 AM IST (11:00 PM UTC previous day)
- **Config**: `report.scheduling.daily-report-cron: "0 30 5 * * ?"`
- **Action**: Creates batch jobs for all roles (ADMIN, SUPERVISOR, CASE_WORKER, etc.)

### B. Weekly Reports
```java
@Scheduled(cron = "${report.scheduling.weekly-report-cron:0 30 5 * * MON}", zone = "Asia/Kolkata")
public void generateWeeklyReports()
```
- **Schedule**: Every Monday at 5:30 AM IST
- **Config**: `report.scheduling.weekly-report-cron: "0 30 5 * * MON"`

### C. Monthly Reports
```java
@Scheduled(cron = "${report.scheduling.monthly-report-cron:0 30 5 1 * ?}", zone = "Asia/Kolkata")
public void generateMonthlyReports()
```
- **Schedule**: 1st of each month at 5:30 AM IST
- **Config**: `report.scheduling.monthly-report-cron: "0 30 5 1 * ?"`

### D. Quarterly Reports
```java
@Scheduled(cron = "${report.scheduling.quarterly-report-cron:0 30 5 1 1,4,7,10 ?}", zone = "Asia/Kolkata")
public void generateQuarterlyReports()
```
- **Schedule**: 1st of Jan, Apr, Jul, Oct at 5:30 AM IST
- **Config**: `report.scheduling.quarterly-report-cron: "0 30 5 1 1,4,7,10 ?"`

### E. Yearly Reports
```java
@Scheduled(cron = "${report.scheduling.yearly-report-cron:0 30 5 1 1 ?}", zone = "Asia/Kolkata")
public void generateYearlyReports()
```
- **Schedule**: January 1st at 5:30 AM IST
- **Config**: `report.scheduling.yearly-report-cron: "0 30 5 1 1 ?"`

### F. System Scheduler (County Reports)
```java
@Scheduled(cron = "${report.scheduler.system-scheduler.cron:0 */5 * * * ?}", zone = "Asia/Kolkata")
public void generateCountyReportsForScheduler()
```
- **Schedule**: Every **2 minutes** (for testing) - Configurable
- **Config**: `report.scheduler.system-scheduler.cron: "0 */2 * * * ?"`
- **Action**: Generates county-specific reports using SYSTEM_SCHEDULER user
- **Note**: Has a run limit of 10 times (for testing purposes)

---

## 3. Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│  ScheduledReportService (Cron Jobs)                         │
│  - Daily (5:30 AM daily)                                    │
│  - Weekly (5:30 AM Monday)                                   │
│  - Monthly (5:30 AM 1st of month)                           │
│  - Quarterly (5:30 AM quarter start)                        │
│  - Yearly (5:30 AM Jan 1)                                    │
│  - System Scheduler (Every 2 minutes)                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Creates jobs
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  Database (report_jobs table)                               │
│  Status: QUEUED                                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Polls every 5 seconds
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  BatchJobScheduler                                          │
│  @Scheduled(fixedDelay = 5000ms)                            │
│  - Finds top 3 queued jobs                                  │
│  - Marks as PROCESSING                                      │
│  - Dispatches to worker threads                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Executes in background
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  BackgroundProcessingService                                 │
│  - processJob(jobId)                                         │
│  - Generates report                                          │
│  - Updates job status (SUCCESS/FAILED)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Configuration Summary

### Enable/Disable Scheduling
```yaml
report:
  scheduling:
    enabled: true  # Master switch for all scheduled reports
  scheduler:
    system-scheduler:
      enabled: true  # Enable/disable system scheduler specifically
```

### Enable/Disable Batch Job Processor
```yaml
batch:
  scheduler:
    enabled: true  # Enable/disable the job poller
```

---

## 5. Key Points

1. **BatchJobScheduler** is the **active processor** - it runs continuously every 5 seconds
2. **ScheduledReportService** cron jobs are **job creators** - they create jobs and queue them
3. Jobs are stored in `report_jobs` table with status `QUEUED`
4. BatchJobScheduler picks up `QUEUED` jobs and processes them
5. System scheduler runs every 2 minutes (for testing) and has a 10-run limit
6. All other schedulers run at specific times (5:30 AM IST)

---

## 6. How to Disable Batch Processing

### Disable all scheduled reports:
```yaml
report:
  scheduling:
    enabled: false
```

### Disable batch job processor:
```yaml
batch:
  scheduler:
    enabled: false
```

### Disable only system scheduler:
```yaml
report:
  scheduler:
    system-scheduler:
      enabled: false
```

---

## 7. Monitoring

- Check job queue: `GET /api/batch-jobs`
- View batch jobs dashboard: `http://localhost:8080/batch-jobs-dashboard.html`
- Check application logs for:
  - `⏰ ScheduledReportService: Starting...`
  - `🚀 [JOB STARTED] Job ID: ...`
  - `✅ [JOB COMPLETED] Job ID: ...`
  - `❌ [JOB FAILED] Job ID: ...`

