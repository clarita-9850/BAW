# Nightly Batch Processing System Overview

## Timesheet Management System

---

## Table of Contents

### Part 1: Functional Overview
1. [Introduction](#introduction)
2. [What is Nightly Batch Processing?](#what-is-nightly-batch-processing)
3. [Design Goals](#design-goals)
4. [Key Capabilities](#key-capabilities)
5. [How Jobs Work](#how-jobs-work)
6. [Job Dependencies](#job-dependencies)
7. [Scheduling](#scheduling)
8. [Monitoring & Visibility](#monitoring--visibility)
9. [Security & Data Protection](#security--data-protection)

### Part 2: Technical Details
10. [Architecture](#architecture)
11. [Tech Stack](#tech-stack)
12. [End-to-End Flow](#end-to-end-flow)
13. [Core Components](#core-components)
14. [Configuration](#configuration)
15. [Adding New Jobs](#adding-new-jobs)

---

# Part 1: Functional Overview

---

## Introduction

The Nightly Batch Processing System handles automated report generation that runs during off-peak hours (overnight). Reports are generated while users are offline, and results are available for review the next business day.

### The Nightly Batch Concept

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      NIGHTLY BATCH TIMELINE                             │
└─────────────────────────────────────────────────────────────────────────┘

  BUSINESS DAY                    OVERNIGHT                    NEXT DAY
  ────────────                    ─────────                    ────────

  Users work      ───►    Batch jobs run    ───►    Reports ready
  Data collected          automatically             for review
                          (11 PM - 5 AM)

                     ┌────────────────────┐
                     │  BATCH WINDOW      │
                     │                    │
                     │  Job 1 ────────    │
                     │  Job 2 ──────────  │
                     │  Job 3 ────        │
                     │  Job 4 ──────────  │
                     │        ...         │
                     └────────────────────┘
                              │
                              ▼
                     ┌────────────────────┐
                     │  NEXT MORNING      │
                     │                    │
                     │  • Check job status│
                     │  • Review failures │
                     │  • Download reports│
                     │  • Fix & retry     │
                     └────────────────────┘
```

### Why Nightly Batch?

| Benefit | Description |
|---------|-------------|
| **Off-peak processing** | Heavy reports run when system is idle |
| **No user impact** | Processing doesn't slow down daytime operations |
| **Consistent schedule** | Reports ready at same time each day |
| **Complete data** | All daily transactions captured before processing |

---

## What is Nightly Batch Processing?

Nightly batch processing is a scheduled approach where jobs are queued and executed automatically during a defined overnight window. Unlike real-time processing, users don't wait for results—they review outcomes the next day.

### The Basic Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    NIGHTLY BATCH WORKFLOW                               │
└─────────────────────────────────────────────────────────────────────────┘

  EVENING (11:00 PM)                          MORNING (8:00 AM)
  ──────────────────                          ─────────────────

  ┌─────────────────┐                         ┌─────────────────┐
  │ Scheduler       │                         │ Operations      │
  │ triggers batch  │                         │ reviews status  │
  └────────┬────────┘                         └────────┬────────┘
           │                                           │
           ▼                                           ▼
  ┌─────────────────┐                         ┌─────────────────┐
  │ Jobs created    │                         │ Check dashboard │
  │ automatically   │                         │ - Completed: 12 │
  │ - Daily reports │                         │ - Failed: 2     │
  │ - Summaries     │                         │ - Duration logs │
  │ - Aggregations  │                         └────────┬────────┘
  └────────┬────────┘                                  │
           │                                           ▼
           ▼                                  ┌─────────────────┐
  ┌─────────────────┐                         │ If failures:    │
  │ Jobs process    │                         │ - Review errors │
  │ throughout      │                         │ - Fix issues    │
  │ the night       │                         │ - Retry jobs    │
  │                 │                         └────────┬────────┘
  │ Start/end times │                                  │
  │ logged for each │                                  ▼
  └─────────────────┘                         ┌─────────────────┐
                                              │ Reports ready   │
                                              │ for business    │
                                              │ users           │
                                              └─────────────────┘
```

### Key Concepts

| Term | Meaning |
|------|---------|
| **Batch Window** | The overnight period when jobs run (e.g., 11 PM - 5 AM) |
| **Job** | A single unit of work (e.g., one report generation) |
| **Job Run** | One execution of a job with logged start/end times |
| **Dependencies** | Jobs that must complete before others can start |
| **Post-Run Review** | Morning check of job statuses and results |

---

## Design Goals

The system is designed with four primary goals:

### 1. Resilience

The system handles failures gracefully and recovers automatically when possible.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         RESILIENCE FEATURES                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Automatic Retry                    Failure Isolation                   │
│  ────────────────                   ─────────────────                   │
│  Job fails → Wait → Retry           Job A fails →                       │
│  Up to 3 attempts                   Job B still runs                    │
│  Exponential backoff                Independent execution               │
│                                                                         │
│  State Persistence                  Graceful Degradation                │
│  ─────────────────                  ────────────────────                │
│  Jobs survive restarts              Partial results saved               │
│  Progress checkpointed              Can resume from failure             │
│  Nothing lost on crash                                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2. Flexibility

New jobs can be added easily without changing core system code.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         FLEXIBILITY FEATURES                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Configuration-Driven               Pluggable Jobs                      │
│  ────────────────────               ──────────────                      │
│  Add jobs via YAML config           Standard job interface              │
│  No code changes needed             New report types easy               │
│  Change schedules easily            Reusable components                 │
│                                                                         │
│  Dynamic Dependencies               Multiple Output Formats             │
│  ────────────────────               ───────────────────────             │
│  Define job chains in config        JSON, CSV, PDF, Excel               │
│  Single or multiple parents         Same job, different outputs         │
│  Conditional triggers                                                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3. Observability

Complete visibility into what ran, when, and how long it took.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       OBSERVABILITY FEATURES                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Every Job Run Logs:                                                    │
│  ───────────────────                                                    │
│  • Job ID and type                                                      │
│  • Start timestamp                                                      │
│  • End timestamp                                                        │
│  • Duration                                                             │
│  • Final status (COMPLETED/FAILED)                                      │
│  • Records processed                                                    │
│  • Error message (if failed)                                            │
│                                                                         │
│  Post-Run Dashboard Shows:                                              │
│  ─────────────────────────                                              │
│  • All jobs from last batch run                                         │
│  • Success/failure counts                                               │
│  • Jobs that exceeded expected duration                                 │
│  • Failed jobs with error details                                       │
│  • Dependency chain status                                              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4. Recoverability

Failed jobs can be investigated and retried without re-running the entire batch.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       RECOVERABILITY WORKFLOW                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Morning Review:                                                        │
│  ──────────────                                                         │
│                                                                         │
│  1. Check batch status dashboard                                        │
│     └── See: 2 jobs failed out of 14                                    │
│                                                                         │
│  2. Review failed jobs                                                  │
│     └── Job JOB_ABC123: "Connection timeout to database"                │
│     └── Job JOB_DEF456: "Invalid data in row 5023"                      │
│                                                                         │
│  3. Fix underlying issues                                               │
│     └── Database connectivity restored                                  │
│     └── Bad data record corrected                                       │
│                                                                         │
│  4. Retry failed jobs only                                              │
│     └── No need to re-run successful jobs                               │
│     └── Dependencies re-triggered automatically                         │
│                                                                         │
│  5. Verify completion                                                   │
│     └── All 14 jobs now COMPLETED                                       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Key Capabilities

### What the System Can Do

| Capability | Description | Benefit |
|------------|-------------|---------|
| **Scheduled Execution** | Jobs run automatically at configured times | No manual intervention needed |
| **Job Dependencies** | Jobs can trigger other jobs | Complex workflows automated |
| **Start/End Logging** | Every job logs timestamps | Performance tracking |
| **Duration Tracking** | Calculate how long each job takes | Identify slow jobs |
| **Automatic Retry** | Failed jobs retry with backoff | Handles transient failures |
| **Post-Run Status** | Dashboard shows all job outcomes | Morning review easy |
| **Selective Retry** | Retry only failed jobs | Efficient recovery |
| **Multiple Formats** | JSON, CSV, PDF, Excel output | Flexibility for consumers |
| **Role-Based Masking** | Sensitive data protected | Security compliance |
| **Priority Processing** | Critical jobs run first | Business priorities respected |

### Feature Implementation Status

| Feature | Status |
|---------|--------|
| Scheduled Execution | ✅ Implemented |
| Job Dependencies (Single) | ✅ Implemented |
| Job Dependencies (Multiple) | ✅ Implemented |
| Start/End Time Logging | ✅ Implemented |
| Duration Tracking | ✅ Implemented |
| Automatic Retry | ✅ Implemented |
| Post-Run Status Dashboard | ✅ Implemented |
| Multiple Output Formats | ✅ Implemented |
| Role-Based Data Masking | ✅ Implemented |
| Priority Processing | ✅ Implemented |
| Manual Job Retry (via API) | ❌ Not Implemented |
| Failure Email Notifications | ⚠️ Partial (logs only) |

---

## How Jobs Work

### Job Lifecycle

Every job goes through these stages during the nightly batch:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         JOB LIFECYCLE                                   │
└─────────────────────────────────────────────────────────────────────────┘

     ┌───────────────┐
     │  SCHEDULED    │  Cron triggers job creation at configured time
     │  (11:00 PM)   │
     └───────┬───────┘
             │
             ▼
     ┌───────────────┐
     │    QUEUED     │  Job waits in queue
     │               │  • Ordered by priority
     │  created_at   │  • Then by creation time
     │  logged       │
     └───────┬───────┘
             │
             │  Worker picks up job
             ▼
     ┌───────────────┐
     │  PROCESSING   │  Job executes
     │               │  • started_at logged
     │  started_at   │  • Progress tracked
     │  logged       │  • Chunked for large data
     └───────┬───────┘
             │
     ┌───────┴───────┬───────────────┐
     │               │               │
     ▼               ▼               ▼
┌─────────┐   ┌───────────┐   ┌───────────┐
│COMPLETED│   │  FAILED   │   │ CANCELLED │
│         │   │           │   │           │
│completed│   │ error_msg │   │           │
│_at      │   │ logged    │   │           │
│logged   │   │           │   │           │
└─────────┘   └───────────┘   └───────────┘
     │               │
     │               └──► Available for retry next morning
     │
     └──► May trigger dependent jobs
         Report available for download
```

### What Gets Logged

| Field | Description | Used For |
|-------|-------------|----------|
| `job_id` | Unique identifier | Tracking and debugging |
| `status` | QUEUED/PROCESSING/COMPLETED/FAILED | Status monitoring |
| `created_at` | When job was queued | Audit trail |
| `started_at` | When processing began | Duration calculation |
| `completed_at` | When processing ended | Duration calculation |
| `total_records` | Records to process | Progress tracking |
| `processed_records` | Records completed | Progress tracking |
| `error_message` | Failure reason | Debugging |
| `retry_count` | Number of attempts | Retry tracking |

### Duration Calculation

```
Duration = completed_at - started_at

Example Job Log:
─────────────────────────────────────────────────────────────
Job ID: JOB_ABC123
Type: COUNTY_DAILY
Status: COMPLETED
Created: 2025-12-08 23:00:00
Started: 2025-12-08 23:00:05
Completed: 2025-12-08 23:02:45
Duration: 2 minutes 40 seconds
Records: 5,000 processed
─────────────────────────────────────────────────────────────
```

---

## Job Dependencies

Jobs can be chained so completing one automatically triggers another.

### Single Dependency (One triggers One)

```
┌─────────────────┐         ┌─────────────────┐
│  County Report  │────────►│  Summary Report │
│   (Parent)      │ triggers│   (Child)       │
│                 │         │                 │
│ Runs at 11:00PM │         │ Runs after      │
│                 │         │ parent completes│
└─────────────────┘         └─────────────────┘
```

**Use Case:** Summary report aggregates data from county report—must wait for county data.

### Multiple Dependencies (Many trigger One)

```
┌─────────────────┐
│  Weekly Report  │────┐
│  (11:00 PM)     │    │
└─────────────────┘    │
                       │  BOTH must complete
┌─────────────────┐    │         │
│ Monthly Report  │────┘         ▼
│  (11:00 PM)     │    ┌─────────────────┐
└─────────────────┘    │ Consolidated    │
                       │ Report          │
                       │ (runs after     │
                       │ both complete)  │
                       └─────────────────┘
```

**Use Case:** Consolidated report needs data from both weekly and monthly—waits for both.

### Dependency Failure Handling

| Parent Status | Child Job |
|---------------|-----------|
| COMPLETED | Triggers normally |
| FAILED | Does NOT trigger (by default) |
| CANCELLED | Does NOT trigger |

This prevents cascading bad data—if a parent fails, dependent jobs wait for manual review.

---

## Scheduling

### Batch Schedule Configuration

Jobs are scheduled to run during the overnight batch window:

| Schedule | Time (IST) | Jobs Created |
|----------|------------|--------------|
| **Daily** | 11:00 PM | County reports, daily summaries |
| **Weekly** | Monday 11:00 PM | Weekly aggregations |
| **Monthly** | 1st of month 11:00 PM | Monthly reports |
| **Quarterly** | Jan/Apr/Jul/Oct 1st 11:00 PM | Quarterly reviews |
| **Yearly** | January 1st 11:00 PM | Annual reports |

### Nightly Batch Example

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    TYPICAL NIGHTLY BATCH RUN                            │
│                    (Every night at 11:00 PM)                            │
└─────────────────────────────────────────────────────────────────────────┘

11:00:00 PM - Scheduler triggers generateDailyReports()
              │
              ├── Creates 14 jobs across all profiles:
              │
              │   ADMIN Profile (2 jobs)
              │   ├── JOB_001: COUNTY_DAILY for ADMIN
              │   └── JOB_002: DAILY_SUMMARY for ADMIN
              │
              │   SUPERVISOR Profile (2 jobs)
              │   ├── JOB_003: COUNTY_DAILY for SUPERVISOR
              │   └── JOB_004: DAILY_SUMMARY for SUPERVISOR
              │
              │   CASE_WORKER Profiles (10 jobs - 5 counties × 2 types)
              │   ├── JOB_005: COUNTY_DAILY for County 1
              │   ├── JOB_006: DAILY_SUMMARY for County 1
              │   ├── JOB_007: COUNTY_DAILY for County 2
              │   └── ... etc
              │
11:00:05 PM - Workers start processing queued jobs
              │
11:00:05 PM - JOB_001 starts (ADMIN COUNTY_DAILY)
11:02:30 PM - JOB_001 completes → triggers JOB_002
11:02:35 PM - JOB_002 starts (ADMIN DAILY_SUMMARY)
              ... parallel processing continues ...
              │
11:45:00 PM - All 14 jobs completed
              │
              └── Results:
                  • 14 COMPLETED
                  • 0 FAILED
                  • Total duration: 45 minutes
                  • Reports ready for morning review
```

---

## Monitoring & Visibility

### Post-Run Review (Next Morning)

The morning after each batch run, operations staff review job outcomes:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    BATCH STATUS DASHBOARD                               │
│                    Run Date: 2025-12-08 (Last Night)                    │
└─────────────────────────────────────────────────────────────────────────┘

  SUMMARY
  ───────────────────────────────────────────────────────────────────────
  Total Jobs: 14          Completed: 12          Failed: 2
  Batch Start: 11:00 PM   Batch End: 11:52 PM    Duration: 52 minutes


  JOB DETAILS
  ───────────────────────────────────────────────────────────────────────
  Job ID      │ Type          │ Status    │ Start    │ End      │Duration
  ────────────┼───────────────┼───────────┼──────────┼──────────┼────────
  JOB_001     │ COUNTY_DAILY  │ COMPLETED │ 11:00:05 │ 11:02:30 │ 2m 25s
  JOB_002     │ DAILY_SUMMARY │ COMPLETED │ 11:02:35 │ 11:03:10 │ 35s
  JOB_003     │ COUNTY_DAILY  │ COMPLETED │ 11:00:05 │ 11:04:20 │ 4m 15s
  JOB_004     │ DAILY_SUMMARY │ COMPLETED │ 11:04:25 │ 11:05:00 │ 35s
  JOB_005     │ COUNTY_DAILY  │ ❌ FAILED │ 11:00:05 │ 11:01:30 │ 1m 25s
  JOB_006     │ DAILY_SUMMARY │ PENDING   │    -     │    -     │   -
  ...


  FAILED JOBS (Requires Attention)
  ───────────────────────────────────────────────────────────────────────
  JOB_005 - COUNTY_DAILY (County 1)
    Error: "Connection timeout after 3 retries"
    Retry Count: 3
    Last Attempt: 11:01:30 PM

  JOB_009 - COUNTY_DAILY (County 3)
    Error: "Invalid date format in record 2341"
    Retry Count: 1
    Last Attempt: 11:15:45 PM


  ACTIONS AVAILABLE
  ───────────────────────────────────────────────────────────────────────
  [ ] Retry Failed Jobs    [ ] Download Success Report    [ ] View Logs
```

### What to Check Each Morning

| Check | What to Look For | Action if Problem |
|-------|------------------|-------------------|
| **Job Count** | All expected jobs ran | Investigate missing jobs |
| **Failures** | Any FAILED status | Review error, fix, retry |
| **Duration** | Jobs within expected time | Investigate slow jobs |
| **Dependencies** | Chains completed | Check parent job status |
| **Output Files** | Reports generated | Verify file locations |

### Historical Tracking

Job history is preserved for trend analysis:

```
Job Performance Over Time:
─────────────────────────────────────────────────────────────────────────
Job: COUNTY_DAILY (ADMIN)

Date        │ Duration  │ Records  │ Status
────────────┼───────────┼──────────┼─────────
2025-12-08  │ 2m 25s    │ 5,000    │ COMPLETED
2025-12-07  │ 2m 18s    │ 4,850    │ COMPLETED
2025-12-06  │ 2m 30s    │ 5,100    │ COMPLETED
2025-12-05  │ 8m 45s    │ 5,000    │ COMPLETED  ← Anomaly detected
2025-12-04  │ 2m 22s    │ 4,900    │ COMPLETED

Average Duration: 2m 24s
Anomalies: 1 (Dec 5 - investigate)
```

---

## Security & Data Protection

### Role-Based Access

Different roles see different data levels in generated reports:

| Role | Data Access | Masking Applied |
|------|-------------|-----------------|
| **ADMIN** | All counties, all data | None |
| **SUPERVISOR** | Assigned counties | Partial (last 4 SSN) |
| **CASE_WORKER** | Single county only | Heavy (SSN hidden) |

### Field Masking Example

Same record in reports for different roles:

```
ADMIN Report:                SUPERVISOR Report:           CASE_WORKER Report:
─────────────                ──────────────────           ───────────────────
Name: John Doe               Name: John Doe               Name: J*** D**
SSN: 123-45-6789             SSN: ***-**-6789             SSN: ***-**-****
Address: 123 Main St         Address: *** Main St         Address: *****
```

### Security in Batch Context

| Measure | How It Works |
|---------|--------------|
| **JWT Capture** | User context stored with job at creation |
| **Role Applied at Runtime** | Masking applied when report generates |
| **County Filtering** | Jobs only access authorized county data |
| **Audit Trail** | All job runs logged with user context |

---

# Part 2: Technical Details

---

## Architecture

The system uses a **poll-based database queue** pattern optimized for nightly batch execution.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      NIGHTLY BATCH ARCHITECTURE                         │
└─────────────────────────────────────────────────────────────────────────┘

                         SCHEDULED TRIGGERS
                         (Cron-based, 11 PM)
  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
  │    Daily     │   │   Weekly     │   │   Monthly    │
  │  @Scheduled  │   │  @Scheduled  │   │  @Scheduled  │
  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
         │                  │                  │
         └──────────────────┼──────────────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │ScheduledReportSvc   │
                 │ - Creates jobs      │
                 │ - Sets priorities   │
                 │ - Logs created_at   │
                 └──────────┬──────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        PostgreSQL Database                              │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  report_jobs table (JOB QUEUE + HISTORY)                          │  │
│  │                                                                   │  │
│  │  job_id │ status     │ created_at │ started_at │ completed_at    │  │
│  │  ───────┼────────────┼────────────┼────────────┼──────────────── │  │
│  │  JOB_01 │ COMPLETED  │ 23:00:00   │ 23:00:05   │ 23:02:30        │  │
│  │  JOB_02 │ COMPLETED  │ 23:00:00   │ 23:02:35   │ 23:03:10        │  │
│  │  JOB_03 │ FAILED     │ 23:00:00   │ 23:00:05   │ 23:01:30        │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                            │
                            │ Poll every 5 seconds
                            ▼
                 ┌─────────────────────┐
                 │  BatchJobScheduler  │
                 │  - Find QUEUED jobs │
                 │  - Claim atomically │
                 │  - Log started_at   │
                 │  - Dispatch workers │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │   Worker Threads    │
                 │  Worker 1, Worker 2 │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │ BackgroundProcessor │
                 │  - Process chunks   │
                 │  - Apply masking    │
                 │  - Write output     │
                 │  - Log completed_at │
                 │  - Trigger deps     │
                 └─────────────────────┘
                            │
           ┌────────────────┼────────────────┐
           │                │                │
           ▼                ▼                ▼
    ┌────────────┐   ┌────────────┐   ┌────────────┐
    │ Output     │   │ Dependency │   │ Status     │
    │ Files      │   │ Trigger    │   │ Update     │
    │ ./reports/ │   │ Next jobs  │   │ COMPLETED  │
    └────────────┘   └────────────┘   └────────────┘
```

### Design Choices for Nightly Batch

| Choice | Rationale |
|--------|-----------|
| **Database as queue** | Jobs persist across restarts; full history retained |
| **Poll-based** | Simple; 5-second latency acceptable for batch |
| **Chunked processing** | Large nightly datasets processed efficiently |
| **Atomic claiming** | Multiple workers can run without conflicts |
| **Timestamp logging** | Full visibility into job timing |

---

## Tech Stack

### Core Technologies

| Component | Technology | Purpose |
|-----------|------------|---------|
| Runtime | Java 17 | Application runtime |
| Framework | Spring Boot 3.2 | Scheduling, dependency injection |
| Database | PostgreSQL 13 | Job queue, history, data storage |
| Security | Keycloak + OAuth2/JWT | Authentication and authorization |
| Connection Pool | HikariCP | Database connection management |

### Supporting Libraries

| Library | Purpose |
|---------|---------|
| Spring Data JPA | Database access |
| Spring Scheduler | Cron-based job triggering |
| Jackson | JSON processing |
| OpenPDF | PDF generation |
| Spring Mail | Email delivery |

---

## End-to-End Flow

### Nightly Batch Execution Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     NIGHTLY BATCH EXECUTION                             │
└─────────────────────────────────────────────────────────────────────────┘

STEP 1: CRON TRIGGERS (11:00 PM)
════════════════════════════════

  ┌─────────────────────────────────────────────────────────────────┐
  │  ScheduledReportService                                         │
  │                                                                 │
  │  @Scheduled(cron = "0 30 5 * * ?")  // 11:00 PM IST            │
  │  public void generateDailyReports() {                          │
  │      for (Profile profile : getAllProfiles()) {                │
  │          for (String reportType : getDailyReportTypes()) {     │
  │              jobQueueService.createJob(reportType, profile);   │
  │          }                                                      │
  │      }                                                          │
  │  }                                                              │
  └─────────────────────────────────────────────────────────────────┘


STEP 2: JOBS CREATED IN DATABASE
════════════════════════════════

  ┌─────────────────────────────────────────────────────────────────┐
  │  report_jobs table populated:                                   │
  │                                                                 │
  │  job_id   │ status │ report_type  │ created_at          │ ...  │
  │  ─────────┼────────┼──────────────┼─────────────────────┼───── │
  │  JOB_001  │ QUEUED │ COUNTY_DAILY │ 2025-12-08 23:00:00 │      │
  │  JOB_002  │ QUEUED │ DAILY_SUMM   │ 2025-12-08 23:00:00 │      │
  │  JOB_003  │ QUEUED │ COUNTY_DAILY │ 2025-12-08 23:00:00 │      │
  │  ... (14 jobs total)                                            │
  └─────────────────────────────────────────────────────────────────┘


STEP 3: WORKERS PROCESS JOBS
════════════════════════════

  ┌─────────────────────────────────────────────────────────────────┐
  │  BatchJobScheduler polls every 5 seconds:                       │
  │                                                                 │
  │  1. Find QUEUED jobs (ORDER BY priority DESC, created_at ASC)  │
  │  2. Claim job: UPDATE status='PROCESSING', started_at=NOW()    │
  │  3. Dispatch to worker thread                                   │
  └─────────────────────────────────────────────────────────────────┘
           │
           ▼
  ┌─────────────────────────────────────────────────────────────────┐
  │  BackgroundProcessingService (in worker thread):                │
  │                                                                 │
  │  1. Load job details                                            │
  │  2. Count total records                                         │
  │  3. Process in chunks of 1000:                                  │
  │     - Fetch chunk                                               │
  │     - Apply role-based masking                                  │
  │     - Write to output file                                      │
  │     - Update progress                                           │
  │  4. On completion:                                              │
  │     - Set status = COMPLETED                                    │
  │     - Set completed_at = NOW()                                  │
  │     - Trigger dependent jobs                                    │
  └─────────────────────────────────────────────────────────────────┘


STEP 4: DEPENDENCIES TRIGGERED
══════════════════════════════

  ┌─────────────────────────────────────────────────────────────────┐
  │  JobDependencyService:                                          │
  │                                                                 │
  │  Parent job COUNTY_DAILY completes                              │
  │       │                                                         │
  │       ▼                                                         │
  │  Check dependency config: COUNTY_DAILY → DAILY_SUMMARY          │
  │       │                                                         │
  │       ▼                                                         │
  │  Create new DAILY_SUMMARY job with status=QUEUED                │
  │       │                                                         │
  │       ▼                                                         │
  │  Worker picks up and processes DAILY_SUMMARY                    │
  └─────────────────────────────────────────────────────────────────┘


STEP 5: BATCH COMPLETES (~ 11:45 PM)
════════════════════════════════════

  ┌─────────────────────────────────────────────────────────────────┐
  │  Final state in report_jobs:                                    │
  │                                                                 │
  │  job_id  │ status    │ started_at │ completed_at │ duration    │
  │  ────────┼───────────┼────────────┼──────────────┼──────────── │
  │  JOB_001 │ COMPLETED │ 23:00:05   │ 23:02:30     │ 2m 25s      │
  │  JOB_002 │ COMPLETED │ 23:02:35   │ 23:03:10     │ 35s         │
  │  JOB_003 │ COMPLETED │ 23:00:05   │ 23:04:20     │ 4m 15s      │
  │  ... all jobs have timestamps logged                            │
  └─────────────────────────────────────────────────────────────────┘


STEP 6: MORNING REVIEW (8:00 AM Next Day)
═════════════════════════════════════════

  ┌─────────────────────────────────────────────────────────────────┐
  │  Operations staff:                                              │
  │                                                                 │
  │  1. Open batch status dashboard                                 │
  │  2. Review: 14 jobs, 12 completed, 2 failed                     │
  │  3. Check failed job error messages                             │
  │  4. Fix underlying issues                                       │
  │  5. Retry failed jobs                                           │
  │  6. Verify all reports available                                │
  └─────────────────────────────────────────────────────────────────┘
```

---

## Core Components

### Component Overview

| Component | File | Responsibility |
|-----------|------|----------------|
| **Scheduler** | ScheduledReportService.java | Cron-triggered job creation |
| **Queue Service** | JobQueueService.java | Create jobs, claim jobs, update status |
| **Batch Scheduler** | BatchJobScheduler.java | Poll for jobs, dispatch to workers |
| **Processor** | BackgroundProcessingService.java | Execute job logic, log timestamps |
| **Dependencies** | JobDependencyService.java | Trigger dependent jobs |
| **Masking** | FieldMaskingService.java | Role-based data masking |

### Key Code Patterns

**Job Creation with Timestamp:**
```java
public String createJob(String reportType, String profile) {
    ReportJobEntity job = new ReportJobEntity();
    job.setJobId(generateJobId());
    job.setStatus("QUEUED");
    job.setReportType(reportType);
    job.setCreatedAt(LocalDateTime.now());  // Log creation time
    return repository.save(job).getJobId();
}
```

**Processing with Start/End Logging:**
```java
public void processJob(String jobId) {
    ReportJobEntity job = repository.findById(jobId);
    job.setStartedAt(LocalDateTime.now());  // Log start time
    repository.save(job);

    try {
        // Process in chunks...
        processChunks(job);

        job.setStatus("COMPLETED");
        job.setCompletedAt(LocalDateTime.now());  // Log end time
    } catch (Exception e) {
        job.setStatus("FAILED");
        job.setErrorMessage(e.getMessage());
        job.setCompletedAt(LocalDateTime.now());  // Log failure time
    }
    repository.save(job);
}
```

---

## Configuration

### Schedule Configuration

```yaml
# Cron expressions (UTC - add 5:30 for IST)
# Format: second minute hour day-of-month month day-of-week

scheduled-reports:
  daily:   "0 30 17 * * ?"      # 11:00 PM IST daily
  weekly:  "0 30 17 * * MON"    # 11:00 PM IST Mondays
  monthly: "0 30 17 1 * ?"      # 11:00 PM IST 1st of month
```

### Processing Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `batch.scheduler.interval-ms` | 5000 | Poll interval (milliseconds) |
| `batch.executor.core-pool-size` | 2 | Number of worker threads |
| `batch.executor.queue-capacity` | 3 | Max jobs waiting for worker |
| `job-processing.default-chunk-size` | 1000 | Records per chunk |
| `job-processing.max-retries` | 3 | Retry attempts before failing |

### Dependency Configuration

```yaml
job-dependencies:
  dependencies:
    - parent-report-type: "COUNTY_DAILY"
      dependent-report-type: "DAILY_SUMMARY"
      condition: "ON_SUCCESS"
```

---

## Adding New Jobs

The system is designed to easily accommodate new job types.

### Steps to Add a New Job

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      ADDING A NEW JOB TYPE                              │
└─────────────────────────────────────────────────────────────────────────┘

1. DEFINE THE REPORT TYPE
   ───────────────────────
   Add to report type enum or configuration:

   report-types:
     - name: "NEW_WEEKLY_SUMMARY"
       description: "Weekly summary for compliance"


2. ADD TO SCHEDULE (if scheduled)
   ───────────────────────────────
   Update ScheduledReportService:

   @Scheduled(cron = "0 30 17 * * MON")  // Weekly
   public void generateWeeklyReports() {
       createJob("NEW_WEEKLY_SUMMARY", "ADMIN");
   }


3. CONFIGURE DEPENDENCIES (if needed)
   ───────────────────────────────────
   Add to application.yml:

   job-dependencies:
     dependencies:
       - parent-report-type: "WEEKLY_REPORT"
         dependent-report-type: "NEW_WEEKLY_SUMMARY"
         condition: "ON_SUCCESS"


4. IMPLEMENT PROCESSING LOGIC (if custom)
   ──────────────────────────────────────
   If standard processing works, no code changes needed.
   For custom logic, extend BackgroundProcessingService.


5. TEST
   ────
   - Trigger job manually
   - Verify timestamps logged
   - Check output file generated
   - Verify dependencies trigger
```

### No-Code Job Addition

For standard report jobs, new jobs can be added purely through configuration:

```yaml
# application.yml - No code changes needed

scheduled-jobs:
  jobs:
    - name: "COMPLIANCE_WEEKLY"
      schedule: "0 30 17 * * MON"
      profile: "ADMIN"
      format: "PDF"

    - name: "AUDIT_MONTHLY"
      schedule: "0 30 17 1 * ?"
      profile: "ADMIN"
      format: "CSV"
```

---

## Summary

### System at a Glance

| Aspect | Implementation |
|--------|----------------|
| **Pattern** | Nightly batch with database queue |
| **Schedule** | Cron-triggered (11 PM default) |
| **Processing** | Asynchronous, chunked |
| **Concurrency** | 2 workers |
| **Visibility** | Full timestamp logging |
| **Recovery** | Post-run review and retry |

### Design Goals Achieved

| Goal | How |
|------|-----|
| **Resilience** | Automatic retry, failure isolation, state persistence |
| **Flexibility** | Configuration-driven, pluggable jobs, easy additions |
| **Observability** | Start/end timestamps, duration tracking, status dashboard |
| **Recoverability** | Morning review, selective retry, error details logged |

### For More Details

- [Async Architecture Deep Dive](./BATCH_ARCHITECTURE_OVERVIEW.md)
- [Features and Scenarios](./BATCH_FEATURES_AND_SCENARIOS.md)
- [Manual Job Recovery (Planned)](./implement/MANUAL_JOB_RECOVERY.md)
