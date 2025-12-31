# Enterprise Batch Processing Architecture
## High-Volume File Processing System for 2500+ Daily Jobs

**Document Version:** 1.0
**Date:** December 2024
**Author:** Architecture Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Requirements Overview](#2-requirements-overview)
3. [Current System Analysis](#3-current-system-analysis)
4. [High-Level Architecture](#4-high-level-architecture)
5. [Compute Resource Calculation](#5-compute-resource-calculation)
6. [AWS Architecture (Recommended)](#6-aws-architecture-recommended)
7. [Azure Architecture (Alternative)](#7-azure-architecture-alternative)
8. [Enhanced Application Architecture](#8-enhanced-application-architecture)
9. [Code Implementation Guide](#9-code-implementation-guide)
10. [Auto-Scaling Configuration](#10-auto-scaling-configuration)
11. [Capacity Planning](#11-capacity-planning)
12. [Implementation Roadmap](#12-implementation-roadmap)
13. [Spring Batch Comparison](#13-spring-batch-comparison)
14. [Best Practices & Recommendations](#14-best-practices--recommendations)
15. [Cost Analysis](#15-cost-analysis)
16. [References](#16-references)

---

## 1. Executive Summary

This document outlines the architecture for a high-volume batch processing system capable of handling:

- **2,500+ daily batch jobs**
- **Millions of records** across all jobs
- **Inbound processing** (morning) - Files from external vendors via SFTP
- **Outbound processing** (nightly) - Modified data files sent back to vendors
- **Parallel execution** of independent jobs
- **Cloud deployment** on AWS or Azure

The proposed architecture uses a queue-based, auto-scaling approach that can process all jobs within a 4-hour window while optimizing costs through spot/preemptible instances.

---

## 2. Requirements Overview

### 2.1 Functional Requirements

| Requirement | Description |
|-------------|-------------|
| **File Ingestion** | Accept files from 100+ vendors via SFTP |
| **File Formats** | CSV, JSON, XML, Fixed-width |
| **Processing** | Validate, transform, apply business rules, field masking |
| **Output Generation** | Generate modified files in vendor-specific formats |
| **Scheduling** | Morning batch (6 AM - 10 AM), Nightly batch (10 PM - 2 AM) |
| **SLA** | Complete all jobs within 4-hour window |

### 2.2 Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| **Throughput** | 2,500 jobs / 4 hours |
| **Records** | 25+ million records daily |
| **Availability** | 99.9% uptime |
| **Scalability** | Handle 2x load spikes |
| **Cost Efficiency** | Optimize using spot instances |
| **Monitoring** | Real-time job status visibility |

### 2.3 Job Characteristics

| Metric | Value | Notes |
|--------|-------|-------|
| Total Daily Jobs | 2,500 | Independent jobs |
| Average Records/Job | 10,000 | Range: 1K - 100K |
| Total Daily Records | 25 million | 2,500 × 10,000 |
| Avg Processing Time/Record | 10ms | Read + Transform + Write |
| Avg Processing Time/Job | ~100 seconds | 10,000 × 10ms |
| Time Window | 4 hours | Per batch window |

---

## 3. Current System Analysis

### 3.1 Existing Architecture

The current custom batch processing system consists of:

| Component | File | Purpose |
|-----------|------|---------|
| Job Scheduler | `BatchJobScheduler.java` | Polls for jobs every 5 seconds |
| Job Queue | `JobQueueService.java` | Database-backed job queue |
| Processing | `BackgroundProcessingService.java` | Streaming chunk processor |
| Dependencies | `JobDependencyService.java` | Parent-child job chains |
| Storage | PostgreSQL + `ReportJobEntity` | Job metadata persistence |
| Thread Pool | Configurable | Default: 2 workers |

### 3.2 Current Capabilities

| Feature | Status | Notes |
|---------|--------|-------|
| Chunked Processing | ✅ | 1000 records default |
| Progress Tracking | ✅ | Real-time updates |
| Job Status Management | ✅ | QUEUED → PROCESSING → COMPLETED/FAILED |
| Job Dependencies | ✅ | Single and multiple parent jobs |
| Retry Mechanism | ✅ | 3 retries per chunk |
| JWT Integration | ✅ | Security/field masking |
| Output Formats | ✅ | JSON, CSV, XML, PDF |
| Job Cancellation | ✅ | Supported |
| Priority Ordering | ✅ | Priority-based queue |

### 3.3 Current Limitations

| Limitation | Impact |
|------------|--------|
| Single JVM | Cannot scale beyond single machine |
| Fixed Workers | 2 workers, not auto-scaling |
| Database Polling | Inefficient for high volume |
| No Partitioning | Cannot split large jobs |
| Manual Restart | No checkpoint-based recovery |

---

## 4. High-Level Architecture

### 4.1 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           VENDOR FILE EXCHANGE LAYER                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐                   │
│   │  Vendor A    │     │  Vendor B    │     │  Vendor N    │                   │
│   │  SFTP Client │     │  SFTP Client │     │  SFTP Client │                   │
│   └──────┬───────┘     └──────┬───────┘     └──────┬───────┘                   │
│          │                    │                    │                            │
│          └────────────────────┼────────────────────┘                            │
│                               ▼                                                 │
│   ┌─────────────────────────────────────────────────────────────┐              │
│   │     AWS Transfer Family / Azure Blob SFTP Endpoint          │              │
│   │     (Managed SFTP → Cloud Storage)                          │              │
│   └─────────────────────────────┬───────────────────────────────┘              │
│                                 ▼                                               │
│   ┌─────────────────────────────────────────────────────────────┐              │
│   │     S3 Bucket / Azure Blob Storage                          │              │
│   │     /inbound/{vendor_id}/{date}/                            │              │
│   │     /outbound/{vendor_id}/{date}/                           │              │
│   └─────────────────────────────┬───────────────────────────────┘              │
│                                 │                                               │
└─────────────────────────────────┼───────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         JOB ORCHESTRATION LAYER                                 │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   ┌─────────────────────────────────────────────────────────────┐              │
│   │     Event Trigger (S3 Event / EventBridge / Scheduler)      │              │
│   └─────────────────────────────┬───────────────────────────────┘              │
│                                 ▼                                               │
│   ┌─────────────────────────────────────────────────────────────┐              │
│   │     Job Orchestrator                                        │              │
│   │     (AWS Step Functions / Azure Durable Functions)          │              │
│   │                                                             │              │
│   │     • Discovers new files                                   │              │
│   │     • Creates job manifests                                 │              │
│   │     • Manages job dependencies                              │              │
│   │     • Tracks completion status                              │              │
│   │     • Handles retries and failures                          │              │
│   └─────────────────────────────┬───────────────────────────────┘              │
│                                 │                                               │
│                    ┌────────────┼────────────┐                                  │
│                    ▼            ▼            ▼                                  │
│               ┌─────────┐ ┌─────────┐ ┌─────────┐                              │
│               │ Job     │ │ Job     │ │ Job     │                              │
│               │ Queue 1 │ │ Queue 2 │ │ Queue N │  (Priority Queues)           │
│               │ (High)  │ │ (Medium)│ │ (Low)   │                              │
│               └────┬────┘ └────┬────┘ └────┬────┘                              │
│                    │           │           │                                    │
└────────────────────┼───────────┼───────────┼────────────────────────────────────┘
                     │           │           │
                     ▼           ▼           ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         COMPUTE LAYER (Auto-Scaling)                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   ┌─────────────────────────────────────────────────────────────────────────┐  │
│   │                    Container Orchestration Platform                      │  │
│   │            (AWS ECS/Fargate or Azure AKS/Container Apps)                │  │
│   ├─────────────────────────────────────────────────────────────────────────┤  │
│   │                                                                         │  │
│   │   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       ┌─────────┐   │  │
│   │   │ Worker  │ │ Worker  │ │ Worker  │ │ Worker  │  ...  │ Worker  │   │  │
│   │   │ Pod 1   │ │ Pod 2   │ │ Pod 3   │ │ Pod 4   │       │ Pod N   │   │  │
│   │   │ 4 vCPU  │ │ 4 vCPU  │ │ 4 vCPU  │ │ 4 vCPU  │       │ 4 vCPU  │   │  │
│   │   │ 8GB RAM │ │ 8GB RAM │ │ 8GB RAM │ │ 8GB RAM │       │ 8GB RAM │   │  │
│   │   └─────────┘ └─────────┘ └─────────┘ └─────────┘       └─────────┘   │  │
│   │                                                                         │  │
│   │   Auto-scales: 10 (min) ←──────────────────────────→ 200 (max) workers │  │
│   │                                                                         │  │
│   └─────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATA LAYER                                         │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐          │
│   │  PostgreSQL RDS   │  │  Redis/ElastiCache│  │  S3/Blob Storage  │          │
│   │  (Job Metadata)   │  │  (Job Status Cache)│  │  (Report Files)   │          │
│   │  • Job status     │  │  • Real-time status│  │  • Input files    │          │
│   │  • Audit logs     │  │  • Progress updates│  │  • Output files   │          │
│   │  • Dependencies   │  │  • Distributed lock│  │  • Audit logs     │          │
│   └───────────────────┘  └───────────────────┘  └───────────────────┘          │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Component Overview

| Layer | Component | Purpose |
|-------|-----------|---------|
| **File Exchange** | SFTP Endpoint | Receive/send files from/to vendors |
| **File Exchange** | Cloud Storage | Store inbound/outbound files |
| **Orchestration** | Event Trigger | Initiate batch processing |
| **Orchestration** | Job Orchestrator | Manage workflow and dependencies |
| **Orchestration** | Message Queues | Distribute jobs to workers |
| **Compute** | Worker Pool | Process jobs in parallel |
| **Compute** | Auto-Scaler | Scale workers based on load |
| **Data** | PostgreSQL | Job metadata and audit logs |
| **Data** | Redis | Real-time status cache |
| **Data** | Cloud Storage | Input/output files |

---

## 5. Compute Resource Calculation

### 5.1 Worker Calculation Formula

```
Required Workers = (Total Jobs × Avg Job Duration) / (Available Time × Utilization Factor)

Where:
- Total Jobs = 2,500
- Avg Job Duration = 100 seconds (1.67 minutes)
- Available Time = 4 hours = 240 minutes
- Utilization Factor = 0.8 (80% efficiency accounting for overhead)

Required Workers = (2,500 × 1.67) / (240 × 0.8)
                 = 4,175 / 192
                 = ~22 workers (theoretical minimum)
```

### 5.2 Recommended Worker Configuration

| Scenario | Workers | vCPU/Worker | Memory/Worker | Total vCPU | Total Memory |
|----------|---------|-------------|---------------|------------|--------------|
| **Conservative** | 50 | 4 vCPU | 8 GB | 200 vCPU | 400 GB |
| **Standard** | 100 | 4 vCPU | 8 GB | 400 vCPU | 800 GB |
| **Peak Load** | 200 | 4 vCPU | 8 GB | 800 vCPU | 1.6 TB |

### 5.3 Processing Time Estimates

| Workers | Jobs/Worker | Time to Complete 2500 Jobs |
|---------|-------------|---------------------------|
| 25 | 100 | ~2.8 hours |
| 50 | 50 | ~1.4 hours |
| 100 | 25 | ~42 minutes |
| 200 | 12-13 | ~22 minutes |

**Recommendation**: Start with 50-100 workers with auto-scaling to 200 for peak periods.

### 5.4 Worker Sizing Guidelines

| Worker Size | vCPU | Memory | Best For |
|-------------|------|--------|----------|
| **Small** | 2 | 4 GB | Simple file transformations |
| **Medium** | 4 | 8 GB | Standard processing (recommended) |
| **Large** | 8 | 16 GB | Complex transformations, large files |
| **XLarge** | 16 | 32 GB | Memory-intensive PDF generation |

---

## 6. AWS Architecture (Recommended)

### 6.1 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AWS ARCHITECTURE                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    FILE INGESTION                                │   │
│  │                                                                  │   │
│  │   AWS Transfer Family ──▶ S3 Bucket ──▶ S3 Event Notification   │   │
│  │   (SFTP Endpoint)        /inbound/       triggers Lambda         │   │
│  └────────────────────────────────────────────────────┬────────────┘   │
│                                                       │                 │
│                                                       ▼                 │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    JOB ORCHESTRATION                             │   │
│  │                                                                  │   │
│  │   EventBridge Scheduler ──▶ Step Functions ──▶ SQS Queues       │   │
│  │   (6 AM & 10 PM triggers)  (Distributed Map)   (Job Messages)   │   │
│  └────────────────────────────────────────────────────┬────────────┘   │
│                                                       │                 │
│                                                       ▼                 │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COMPUTE (AWS Batch + Fargate)                │   │
│  │                                                                  │   │
│  │   ┌──────────────────────────────────────────────────────────┐  │   │
│  │   │  Compute Environment (Fargate)                            │  │   │
│  │   │  • Min vCPUs: 0 (scale to zero when idle)                │  │   │
│  │   │  • Max vCPUs: 800 (200 × 4 vCPU tasks)                   │  │   │
│  │   │  • Fargate Spot: 70% cost savings                        │  │   │
│  │   └──────────────────────────────────────────────────────────┘  │   │
│  │                                                                  │   │
│  │   ┌──────────────────────────────────────────────────────────┐  │   │
│  │   │  Job Queues (Priority-based)                             │  │   │
│  │   │  • HIGH: Critical vendor files (SLA < 1 hour)            │  │   │
│  │   │  • MEDIUM: Standard processing (SLA < 4 hours)           │  │   │
│  │   │  • LOW: Non-urgent files (SLA < 8 hours)                 │  │   │
│  │   └──────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    DATA & MONITORING                             │   │
│  │                                                                  │   │
│  │   RDS PostgreSQL    ElastiCache Redis    CloudWatch + X-Ray     │   │
│  │   (Job Metadata)    (Status Cache)       (Monitoring)           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 AWS Services

| Service | Purpose | Configuration |
|---------|---------|---------------|
| **AWS Transfer Family** | Managed SFTP endpoint | SFTP → S3 integration |
| **Amazon S3** | File storage | Inbound/outbound buckets |
| **Amazon EventBridge** | Scheduled triggers | Cron for 6 AM / 10 PM |
| **AWS Step Functions** | Workflow orchestration | Distributed Map pattern |
| **Amazon SQS** | Job queues | Priority-based queues |
| **AWS Batch** | Job scheduling | Manages Fargate tasks |
| **AWS Fargate** | Serverless compute | Auto-scaling workers |
| **Amazon RDS** | PostgreSQL database | Job metadata |
| **Amazon ElastiCache** | Redis cache | Status caching |
| **Amazon CloudWatch** | Monitoring | Logs, metrics, alarms |
| **AWS X-Ray** | Distributed tracing | Request tracing |

### 6.3 AWS Step Functions Workflow

```json
{
  "Comment": "Process 2500 vendor files in parallel",
  "StartAt": "DiscoverFiles",
  "States": {
    "DiscoverFiles": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:discoverFilesFunction",
      "Next": "ProcessFilesInParallel"
    },
    "ProcessFilesInParallel": {
      "Type": "Map",
      "ItemsPath": "$.files",
      "MaxConcurrency": 200,
      "Iterator": {
        "StartAt": "SubmitBatchJob",
        "States": {
          "SubmitBatchJob": {
            "Type": "Task",
            "Resource": "arn:aws:states:::batch:submitJob.sync",
            "Parameters": {
              "JobName.$": "$.fileName",
              "JobQueue": "batch-job-queue",
              "JobDefinition": "file-processor-job",
              "ContainerOverrides": {
                "Environment": [
                  {"Name": "INPUT_FILE", "Value.$": "$.s3Path"},
                  {"Name": "VENDOR_ID", "Value.$": "$.vendorId"}
                ]
              }
            },
            "End": true
          }
        }
      },
      "Next": "NotifyCompletion"
    },
    "NotifyCompletion": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:notifyFunction",
      "End": true
    }
  }
}
```

### 6.4 AWS Cost Estimate

| Service | Configuration | Monthly Cost (Est.) |
|---------|---------------|---------------------|
| AWS Transfer Family | SFTP endpoint, 1TB transfer | ~$220 |
| S3 | 500 GB storage, 10M requests | ~$50 |
| AWS Batch + Fargate | 200 tasks × 4hr/day × 30 days | ~$1,000 |
| Fargate Spot | 70% discount | ~$300 |
| Step Functions | 75K state transitions | ~$20 |
| SQS | 5M messages/month | ~$2 |
| RDS PostgreSQL | db.r6g.large | ~$200 |
| ElastiCache Redis | cache.r6g.large | ~$150 |
| CloudWatch | Logs, metrics, alarms | ~$50 |
| **Total** | | **~$1,500-2,200/month** |

---

## 7. Azure Architecture (Alternative)

### 7.1 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AZURE ARCHITECTURE                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    FILE INGESTION                                │   │
│  │                                                                  │   │
│  │   Azure Blob SFTP ──▶ Blob Storage ──▶ Event Grid              │   │
│  │   Endpoint            /inbound/        triggers Function        │   │
│  └────────────────────────────────────────────────────────────────┬┘   │
│                                                                    │    │
│                                                                    ▼    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    JOB ORCHESTRATION                             │   │
│  │                                                                  │   │
│  │   Azure Durable Functions (Fan-out/Fan-in Pattern)              │   │
│  │   ├── Orchestrator Function (manages workflow)                  │   │
│  │   ├── Activity Functions (process individual files)             │   │
│  │   └── Service Bus Queues (job distribution)                     │   │
│  └────────────────────────────────────────────────────────────────┬┘   │
│                                                                    │    │
│                                                                    ▼    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COMPUTE (AKS with KEDA)                      │   │
│  │                                                                  │   │
│  │   Azure Kubernetes Service                                       │   │
│  │   ├── Worker Deployment (Spring Boot app)                       │   │
│  │   ├── KEDA Autoscaler (scale on queue depth)                    │   │
│  │   ├── Min Replicas: 5                                           │   │
│  │   ├── Max Replicas: 200                                         │   │
│  │   └── Spot Node Pools (70% cost savings)                        │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    DATA & MONITORING                             │   │
│  │                                                                  │   │
│  │   Azure PostgreSQL   Azure Redis Cache   Application Insights   │   │
│  │   (Job Metadata)     (Status Cache)      (Monitoring)           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.2 Azure Services

| Service | Purpose | Configuration |
|---------|---------|---------------|
| **Azure Blob SFTP** | Managed SFTP endpoint | SFTP → Blob integration |
| **Azure Blob Storage** | File storage | Inbound/outbound containers |
| **Azure Event Grid** | Event triggers | Blob events |
| **Azure Durable Functions** | Workflow orchestration | Fan-out/fan-in pattern |
| **Azure Service Bus** | Job queues | Priority queues |
| **Azure Kubernetes Service** | Container orchestration | Worker pods |
| **KEDA** | Auto-scaling | Queue-based scaling |
| **Azure PostgreSQL** | Database | Job metadata |
| **Azure Cache for Redis** | Caching | Status cache |
| **Application Insights** | Monitoring | Logs, metrics, tracing |

### 7.3 Azure Cost Estimate

| Service | Configuration | Monthly Cost (Est.) |
|---------|---------------|---------------------|
| Azure Blob SFTP | Enabled, 1TB transfer | ~$220 |
| Blob Storage | 500 GB, Hot tier | ~$50 |
| AKS | D4s_v3 nodes × 10-50 | ~$1,000 |
| Spot VMs | 70% discount | ~$350 |
| Durable Functions | 75K executions | ~$20 |
| Service Bus | Standard tier, 5M messages | ~$10 |
| Azure PostgreSQL | 2 vCore, 8GB | ~$150 |
| Azure Cache Redis | C2 Standard | ~$120 |
| Application Insights | 5GB logs/month | ~$15 |
| **Total** | | **~$1,400-2,400/month** |

---

## 8. Enhanced Application Architecture

### 8.1 Application Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    YOUR APPLICATION ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    JOB COORDINATOR SERVICE                       │   │
│  │                    (New Component)                               │   │
│  │                                                                  │   │
│  │   Responsibilities:                                              │   │
│  │   ├── File Discovery: Scan S3/Blob for new vendor files         │   │
│  │   ├── Job Creation: Create job records for each file            │   │
│  │   ├── Partitioning: Distribute jobs across workers              │   │
│  │   ├── Dependency Management: Track parent-child relationships   │   │
│  │   └── Completion Tracking: Aggregate status from all workers    │   │
│  │                                                                  │   │
│  │   APIs:                                                          │   │
│  │   ├── POST /api/batch/discover - Trigger file discovery         │   │
│  │   ├── POST /api/batch/execute  - Start batch execution          │   │
│  │   └── GET  /api/batch/status   - Get batch completion status    │   │
│  └──────────────────────────────────────────┬──────────────────────┘   │
│                                             │                           │
│                          ┌──────────────────┼──────────────────┐        │
│                          ▼                  ▼                  ▼        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    MESSAGE QUEUES (SQS/Service Bus)             │   │
│  │                                                                  │   │
│  │   ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │   │
│  │   │ HIGH_PRIORITY│ │ MED_PRIORITY │ │ LOW_PRIORITY │            │   │
│  │   │ Queue        │ │ Queue        │ │ Queue        │            │   │
│  │   │ (VIP Vendors)│ │ (Standard)   │ │ (Bulk)       │            │   │
│  │   └──────┬───────┘ └──────┬───────┘ └──────┬───────┘            │   │
│  └──────────┼────────────────┼────────────────┼────────────────────┘   │
│             │                │                │                         │
│             └────────────────┼────────────────┘                         │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    WORKER POOL (Auto-Scaling)                   │   │
│  │                                                                  │   │
│  │   Deployed as: ECS Fargate Tasks / AKS Pods                     │   │
│  │   Scaling: Based on queue depth (KEDA / Target Tracking)        │   │
│  │   Instances: 10 (min) ←──────────────────────→ 200 (max)        │   │
│  │                                                                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.2 New Components to Develop

| Component | Purpose | Priority |
|-----------|---------|----------|
| `BatchCoordinatorService` | Orchestrate batch execution | High |
| `QueueBasedBatchWorker` | Process jobs from queue | High |
| `VendorConfigService` | Manage vendor-specific settings | Medium |
| `FileDiscoveryService` | Scan cloud storage for files | High |
| `BatchMonitoringService` | Track batch completion | Medium |

---

## 9. Code Implementation Guide

### 9.1 Batch Coordinator Service

```java
package com.example.kafkaeventdrivenapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BatchCoordinatorService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private JobQueueService jobQueueService;

    @Autowired
    private VendorConfigService vendorConfigService;

    private static final String INBOUND_BUCKET = "vendor-files";
    private static final String INBOUND_PREFIX = "inbound/";

    /**
     * Discover all new files from vendors and create jobs
     * Runs at 6 AM daily for morning batch
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public BatchExecutionSummary discoverAndQueueMorningJobs() {
        log.info("========================================");
        log.info("Starting MORNING batch discovery...");
        log.info("========================================");

        return discoverAndQueueJobs("MORNING");
    }

    /**
     * Queue outbound jobs for nightly processing
     * Runs at 10 PM daily for nightly batch
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public BatchExecutionSummary discoverAndQueueNightlyJobs() {
        log.info("========================================");
        log.info("Starting NIGHTLY batch discovery...");
        log.info("========================================");

        return discoverAndQueueJobs("NIGHTLY");
    }

    /**
     * Core discovery and queuing logic
     */
    public BatchExecutionSummary discoverAndQueueJobs(String batchType) {
        String datePrefix = LocalDate.now().toString();
        String searchPrefix = INBOUND_PREFIX + datePrefix + "/";

        // 1. List all files in inbound folder for today
        List<S3Object> files = s3Client.listObjectsV2(builder -> builder
                .bucket(INBOUND_BUCKET)
                .prefix(searchPrefix))
            .contents();

        log.info("Discovered {} files for {} batch", files.size(), batchType);

        // 2. Create job for each file
        List<String> jobIds = new ArrayList<>();
        int highPriority = 0, mediumPriority = 0, lowPriority = 0;

        for (S3Object file : files) {
            VendorFileInfo info = parseVendorInfo(file.key());
            VendorConfig config = vendorConfigService.getConfig(info.getVendorId());

            JobMessage job = JobMessage.builder()
                .jobId(generateJobId())
                .vendorId(info.getVendorId())
                .fileName(info.getFileName())
                .inputPath("s3://" + INBOUND_BUCKET + "/" + file.key())
                .outputPath("s3://" + INBOUND_BUCKET + "/outbound/" +
                           info.getVendorId() + "/" + datePrefix + "/")
                .priority(config.getPriority())
                .batchType(batchType)
                .fileSize(file.size())
                .build();

            // 3. Send to appropriate queue based on priority
            String queueUrl = getQueueForPriority(config.getPriority());
            sqsTemplate.send(queueUrl, job);

            // 4. Track in database
            jobQueueService.createJob(job);
            jobIds.add(job.getJobId());

            // Count by priority
            switch (config.getPriority()) {
                case HIGH -> highPriority++;
                case MEDIUM -> mediumPriority++;
                case LOW -> lowPriority++;
            }

            log.debug("Queued job {} for vendor {} (priority: {})",
                     job.getJobId(), info.getVendorId(), config.getPriority());
        }

        log.info("========================================");
        log.info("Batch discovery complete:");
        log.info("  Total jobs queued: {}", jobIds.size());
        log.info("  High priority: {}", highPriority);
        log.info("  Medium priority: {}", mediumPriority);
        log.info("  Low priority: {}", lowPriority);
        log.info("========================================");

        return BatchExecutionSummary.builder()
            .batchType(batchType)
            .totalJobs(jobIds.size())
            .jobIds(jobIds)
            .highPriorityCount(highPriority)
            .mediumPriorityCount(mediumPriority)
            .lowPriorityCount(lowPriority)
            .build();
    }

    private String generateJobId() {
        return "JOB_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private VendorFileInfo parseVendorInfo(String key) {
        // Parse: inbound/2024-12-11/VENDOR001/data_file.csv
        String[] parts = key.split("/");
        return VendorFileInfo.builder()
            .vendorId(parts[2])
            .fileName(parts[3])
            .build();
    }

    private String getQueueForPriority(Priority priority) {
        return switch (priority) {
            case HIGH -> "https://sqs.../batch-jobs-high";
            case MEDIUM -> "https://sqs.../batch-jobs-medium";
            case LOW -> "https://sqs.../batch-jobs-low";
        };
    }
}
```

### 9.2 Queue-Based Batch Worker

```java
package com.example.kafkaeventdrivenapp.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;

@Service
@Slf4j
public class QueueBasedBatchWorker {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private BackgroundProcessingService processingService;

    @Autowired
    private JobQueueService jobQueueService;

    @Autowired
    private VendorConfigService vendorConfigService;

    /**
     * Process jobs from HIGH priority queue
     */
    @SqsListener(value = "${batch.queue.high}",
                 deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processHighPriorityJob(JobMessage message) {
        processJob(message, "HIGH");
    }

    /**
     * Process jobs from MEDIUM priority queue
     */
    @SqsListener(value = "${batch.queue.medium}",
                 deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMediumPriorityJob(JobMessage message) {
        processJob(message, "MEDIUM");
    }

    /**
     * Process jobs from LOW priority queue
     */
    @SqsListener(value = "${batch.queue.low}",
                 deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processLowPriorityJob(JobMessage message) {
        processJob(message, "LOW");
    }

    /**
     * Core job processing logic
     */
    private void processJob(JobMessage message, String priority) {
        String jobId = message.getJobId();
        String workerId = getWorkerId();

        log.info("═══════════════════════════════════════════════════════════");
        log.info("Worker {} picked up {} priority job: {}", workerId, priority, jobId);
        log.info("  Vendor: {}", message.getVendorId());
        log.info("  Input: {}", message.getInputPath());
        log.info("═══════════════════════════════════════════════════════════");

        long startTime = System.currentTimeMillis();

        try {
            // 1. Mark job as processing
            jobQueueService.markJobAsProcessing(jobId);

            // 2. Download input file from S3
            log.info("[{}] Downloading input file...", jobId);
            Path inputFile = downloadFromS3(message.getInputPath());

            // 3. Get vendor configuration
            VendorConfig config = vendorConfigService.getConfig(message.getVendorId());

            // 4. Process file using existing processing service
            log.info("[{}] Processing file ({} bytes)...", jobId, message.getFileSize());
            ProcessingResult result = processingService.processVendorFile(
                jobId,
                inputFile,
                config
            );

            // 5. Upload result to S3
            log.info("[{}] Uploading result...", jobId);
            String outputPath = uploadToS3(message.getOutputPath(), result.getOutputFile());

            // 6. Mark job as completed
            jobQueueService.setJobResult(jobId, outputPath);

            long duration = System.currentTimeMillis() - startTime;
            log.info("═══════════════════════════════════════════════════════════");
            log.info("Job {} COMPLETED successfully", jobId);
            log.info("  Records processed: {}", result.getRecordCount());
            log.info("  Duration: {} ms", duration);
            log.info("  Output: {}", outputPath);
            log.info("═══════════════════════════════════════════════════════════");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("═══════════════════════════════════════════════════════════");
            log.error("Job {} FAILED after {} ms", jobId, duration);
            log.error("  Error: {}", e.getMessage());
            log.error("═══════════════════════════════════════════════════════════");

            jobQueueService.markJobAsFailed(jobId, e.getMessage());
            throw new RuntimeException("Job processing failed: " + e.getMessage(), e);
        }
    }

    private String getWorkerId() {
        // In ECS/Kubernetes, this returns the container/pod ID
        String hostname = System.getenv("HOSTNAME");
        return hostname != null ? hostname : "local-worker";
    }

    private Path downloadFromS3(String s3Path) {
        // Parse s3://bucket/key format
        String[] parts = s3Path.replace("s3://", "").split("/", 2);
        String bucket = parts[0];
        String key = parts[1];

        Path tempFile = Path.of("/tmp", key.substring(key.lastIndexOf('/') + 1));
        s3Client.getObject(
            builder -> builder.bucket(bucket).key(key),
            tempFile
        );
        return tempFile;
    }

    private String uploadToS3(String outputPath, Path localFile) {
        String[] parts = outputPath.replace("s3://", "").split("/", 2);
        String bucket = parts[0];
        String keyPrefix = parts[1];
        String key = keyPrefix + localFile.getFileName();

        s3Client.putObject(
            builder -> builder.bucket(bucket).key(key),
            localFile
        );
        return "s3://" + bucket + "/" + key;
    }
}
```

### 9.3 Supporting Classes

```java
// JobMessage.java
@Data
@Builder
public class JobMessage {
    private String jobId;
    private String vendorId;
    private String fileName;
    private String inputPath;
    private String outputPath;
    private Priority priority;
    private String batchType;
    private Long fileSize;
}

// BatchExecutionSummary.java
@Data
@Builder
public class BatchExecutionSummary {
    private String batchType;
    private int totalJobs;
    private List<String> jobIds;
    private int highPriorityCount;
    private int mediumPriorityCount;
    private int lowPriorityCount;
}

// VendorConfig.java
@Data
@Builder
public class VendorConfig {
    private String vendorId;
    private String vendorName;
    private Priority priority;
    private String inputFormat;
    private String outputFormat;
    private Map<String, String> fieldMappings;
    private List<String> validationRules;
}

// Priority.java
public enum Priority {
    HIGH, MEDIUM, LOW
}
```

---

## 10. Auto-Scaling Configuration

### 10.1 AWS ECS Auto-Scaling (CloudFormation)

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'Batch Worker Auto-Scaling Configuration'

Resources:
  # ECS Cluster
  BatchCluster:
    Type: AWS::ECS::Cluster
    Properties:
      ClusterName: batch-processing-cluster
      CapacityProviders:
        - FARGATE
        - FARGATE_SPOT
      DefaultCapacityProviderStrategy:
        - CapacityProvider: FARGATE_SPOT
          Weight: 4
        - CapacityProvider: FARGATE
          Weight: 1

  # Task Definition
  BatchWorkerTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: batch-worker
      Cpu: '4096'      # 4 vCPU
      Memory: '8192'   # 8 GB
      NetworkMode: awsvpc
      RequiresCompatibilities:
        - FARGATE
      ExecutionRoleArn: !GetAtt ECSTaskExecutionRole.Arn
      TaskRoleArn: !GetAtt ECSTaskRole.Arn
      ContainerDefinitions:
        - Name: batch-worker
          Image: !Sub '${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/batch-worker:latest'
          Essential: true
          Environment:
            - Name: SPRING_PROFILES_ACTIVE
              Value: production
            - Name: BATCH_QUEUE_HIGH
              Value: !Ref HighPriorityQueue
            - Name: BATCH_QUEUE_MEDIUM
              Value: !Ref MediumPriorityQueue
            - Name: BATCH_QUEUE_LOW
              Value: !Ref LowPriorityQueue
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: /ecs/batch-worker
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: ecs

  # ECS Service
  BatchWorkerService:
    Type: AWS::ECS::Service
    Properties:
      Cluster: !Ref BatchCluster
      ServiceName: batch-worker-service
      TaskDefinition: !Ref BatchWorkerTaskDefinition
      DesiredCount: 10
      LaunchType: FARGATE
      NetworkConfiguration:
        AwsvpcConfiguration:
          Subnets:
            - !Ref PrivateSubnet1
            - !Ref PrivateSubnet2
          SecurityGroups:
            - !Ref BatchWorkerSecurityGroup

  # Auto-Scaling Target
  BatchWorkerScalingTarget:
    Type: AWS::ApplicationAutoScaling::ScalableTarget
    Properties:
      ServiceNamespace: ecs
      ResourceId: !Sub 'service/${BatchCluster}/${BatchWorkerService.Name}'
      ScalableDimension: ecs:service:DesiredCount
      MinCapacity: 10
      MaxCapacity: 200
      RoleARN: !GetAtt AutoScalingRole.Arn

  # Scale Out Policy - Based on Queue Depth
  ScaleOutPolicy:
    Type: AWS::ApplicationAutoScaling::ScalingPolicy
    Properties:
      PolicyName: batch-worker-scale-out
      PolicyType: StepScaling
      ScalingTargetId: !Ref BatchWorkerScalingTarget
      StepScalingPolicyConfiguration:
        AdjustmentType: ChangeInCapacity
        Cooldown: 60
        MetricAggregationType: Average
        StepAdjustments:
          - MetricIntervalLowerBound: 0
            MetricIntervalUpperBound: 100
            ScalingAdjustment: 10
          - MetricIntervalLowerBound: 100
            MetricIntervalUpperBound: 500
            ScalingAdjustment: 25
          - MetricIntervalLowerBound: 500
            ScalingAdjustment: 50

  # Scale In Policy
  ScaleInPolicy:
    Type: AWS::ApplicationAutoScaling::ScalingPolicy
    Properties:
      PolicyName: batch-worker-scale-in
      PolicyType: StepScaling
      ScalingTargetId: !Ref BatchWorkerScalingTarget
      StepScalingPolicyConfiguration:
        AdjustmentType: ChangeInCapacity
        Cooldown: 300
        MetricAggregationType: Average
        StepAdjustments:
          - MetricIntervalUpperBound: 0
            ScalingAdjustment: -10

  # CloudWatch Alarm for Scale Out
  QueueDepthAlarmHigh:
    Type: AWS::CloudWatch::Alarm
    Properties:
      AlarmName: batch-queue-depth-high
      MetricName: ApproximateNumberOfMessagesVisible
      Namespace: AWS/SQS
      Statistic: Sum
      Period: 60
      EvaluationPeriods: 1
      Threshold: 100
      ComparisonOperator: GreaterThanThreshold
      Dimensions:
        - Name: QueueName
          Value: !GetAtt HighPriorityQueue.QueueName
      AlarmActions:
        - !Ref ScaleOutPolicy

  # SQS Queues
  HighPriorityQueue:
    Type: AWS::SQS::Queue
    Properties:
      QueueName: batch-jobs-high
      VisibilityTimeout: 300
      MessageRetentionPeriod: 86400
      RedrivePolicy:
        deadLetterTargetArn: !GetAtt DeadLetterQueue.Arn
        maxReceiveCount: 3

  MediumPriorityQueue:
    Type: AWS::SQS::Queue
    Properties:
      QueueName: batch-jobs-medium
      VisibilityTimeout: 300
      MessageRetentionPeriod: 86400

  LowPriorityQueue:
    Type: AWS::SQS::Queue
    Properties:
      QueueName: batch-jobs-low
      VisibilityTimeout: 600
      MessageRetentionPeriod: 172800

  DeadLetterQueue:
    Type: AWS::SQS::Queue
    Properties:
      QueueName: batch-jobs-dlq
      MessageRetentionPeriod: 1209600  # 14 days
```

### 10.2 Azure AKS with KEDA

```yaml
# Kubernetes Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: batch-worker
  namespace: batch-processing
spec:
  replicas: 10
  selector:
    matchLabels:
      app: batch-worker
  template:
    metadata:
      labels:
        app: batch-worker
    spec:
      nodeSelector:
        kubernetes.azure.com/scalesetpriority: spot
      tolerations:
        - key: kubernetes.azure.com/scalesetpriority
          operator: Equal
          value: spot
          effect: NoSchedule
      containers:
        - name: batch-worker
          image: myregistry.azurecr.io/batch-worker:latest
          resources:
            requests:
              cpu: "4"
              memory: "8Gi"
            limits:
              cpu: "4"
              memory: "8Gi"
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: production
            - name: SERVICE_BUS_CONNECTION
              valueFrom:
                secretKeyRef:
                  name: batch-secrets
                  key: service-bus-connection
---
# KEDA ScaledObject
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: batch-worker-scaler
  namespace: batch-processing
spec:
  scaleTargetRef:
    name: batch-worker
  pollingInterval: 15
  cooldownPeriod: 60
  minReplicaCount: 10
  maxReplicaCount: 200
  triggers:
    # Scale based on HIGH priority queue
    - type: azure-servicebus
      metadata:
        queueName: batch-jobs-high
        messageCount: "5"
        connectionFromEnv: SERVICE_BUS_CONNECTION
    # Scale based on MEDIUM priority queue
    - type: azure-servicebus
      metadata:
        queueName: batch-jobs-medium
        messageCount: "10"
        connectionFromEnv: SERVICE_BUS_CONNECTION
    # Scale based on LOW priority queue
    - type: azure-servicebus
      metadata:
        queueName: batch-jobs-low
        messageCount: "20"
        connectionFromEnv: SERVICE_BUS_CONNECTION
---
# Horizontal Pod Autoscaler (fallback)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: batch-worker-hpa
  namespace: batch-processing
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: batch-worker
  minReplicas: 10
  maxReplicas: 200
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

---

## 11. Capacity Planning

### 11.1 Resource Sizing Table

| Load Level | Jobs | Workers | vCPU Total | Memory Total | Est. Time | Monthly Cost |
|------------|------|---------|------------|--------------|-----------|--------------|
| **Light** | 500 | 25 | 100 | 200 GB | ~35 min | ~$500 |
| **Normal** | 2,500 | 100 | 400 | 800 GB | ~45 min | ~$1,500 |
| **Peak** | 5,000 | 200 | 800 | 1.6 TB | ~45 min | ~$3,000 |
| **Burst** | 10,000 | 400 | 1,600 | 3.2 TB | ~45 min | ~$6,000 |

### 11.2 Scaling Thresholds

| Metric | Scale Out | Scale In |
|--------|-----------|----------|
| Queue Depth | > 10 msgs/worker | < 2 msgs/worker |
| CPU Utilization | > 70% | < 30% |
| Memory Utilization | > 80% | < 40% |
| Processing Latency | > 5 min/job | N/A |

### 11.3 Database Sizing

| Component | Normal Load | Peak Load |
|-----------|-------------|-----------|
| PostgreSQL | db.r6g.large (2 vCPU, 16GB) | db.r6g.xlarge (4 vCPU, 32GB) |
| Redis | cache.r6g.large (2 vCPU, 13GB) | cache.r6g.xlarge (4 vCPU, 26GB) |
| Storage IOPS | 3,000 | 10,000 |
| Connections | 100 | 400 |

---

## 12. Implementation Roadmap

### Phase 1: Foundation (2-3 weeks)

| Task | Duration | Dependencies |
|------|----------|--------------|
| Set up AWS/Azure infrastructure | 3 days | None |
| Configure VPC, subnets, security groups | 2 days | Infrastructure |
| Set up S3/Blob storage buckets | 1 day | Infrastructure |
| Configure AWS Transfer Family / Azure Blob SFTP | 2 days | Storage |
| Create Docker image for worker | 2 days | None |
| Set up ECR/ACR container registry | 1 day | Docker image |
| Deploy to ECS Fargate / AKS | 3 days | All above |

### Phase 2: Queue-Based Processing (2-3 weeks)

| Task | Duration | Dependencies |
|------|----------|--------------|
| Implement `BatchCoordinatorService` | 3 days | Phase 1 |
| Implement `QueueBasedBatchWorker` | 3 days | Phase 1 |
| Set up SQS/Service Bus queues | 2 days | Infrastructure |
| Configure Dead Letter Queues | 1 day | Queues |
| Implement priority queue routing | 2 days | Queues |
| Configure auto-scaling policies | 2 days | Workers |
| Integration testing | 3 days | All above |

### Phase 3: Monitoring & Optimization (1-2 weeks)

| Task | Duration | Dependencies |
|------|----------|--------------|
| Set up CloudWatch/App Insights dashboards | 2 days | Phase 2 |
| Configure alerts for failures | 1 day | Dashboards |
| Implement distributed tracing | 2 days | Workers |
| Create monitoring runbooks | 2 days | Alerts |
| Performance tuning | 3 days | Tracing |

### Phase 4: Production Hardening (1-2 weeks)

| Task | Duration | Dependencies |
|------|----------|--------------|
| Implement retry policies | 2 days | Phase 3 |
| Set up vendor-specific SLAs | 2 days | Monitoring |
| Create failure runbooks | 2 days | Alerts |
| Load testing (2x expected load) | 3 days | All above |
| Security review | 2 days | All above |
| Documentation | 2 days | All above |

---

## 13. Spring Batch Comparison

### 13.1 Feature Comparison

| Feature | Current System | Spring Batch | Recommended |
|---------|----------------|--------------|-------------|
| **Job Queuing** | Custom DB queue | JobRepository | Keep Current |
| **Chunk Processing** | ✅ Custom | ✅ Built-in | Either |
| **Progress Tracking** | ✅ Custom | ✅ Built-in | Either |
| **Retry Logic** | ✅ 3 retries | ✅ Declarative | Spring Batch |
| **Skip Logic** | ❌ | ✅ Built-in | Spring Batch |
| **Restartability** | ❌ Manual | ✅ Automatic | Spring Batch |
| **Partitioning** | ❌ | ✅ Built-in | Spring Batch |
| **Remote Processing** | ❌ | ✅ Built-in | Spring Batch |
| **JWT Integration** | ✅ Deep | ❌ Custom needed | Current |
| **Field Masking** | ✅ Built-in | ❌ Custom needed | Current |
| **Learning Curve** | ✅ Known | ❌ New concepts | Current |

### 13.2 Recommendation

**For your current scale (2,500 jobs, millions of records):**

- **Keep the custom system** for existing functionality
- **Add queue-based distribution** for horizontal scaling
- **Consider Spring Batch** if you need:
  - Automatic checkpoint-based restartability
  - Built-in partitioning for very large files
  - Remote chunking across machines

### 13.3 Hybrid Approach

You can integrate Spring Batch selectively:

```java
// Use Spring Batch for specific complex jobs
@Bean
public Job complexVendorJob(JobRepository jobRepository) {
    return new JobBuilder("complexVendorJob", jobRepository)
        .start(partitionStep())
        .build();
}

@Bean
public Step partitionStep() {
    return new StepBuilder("partitionStep", jobRepository)
        .partitioner("workerStep", new VendorFilePartitioner())
        .step(workerStep())
        .gridSize(10)
        .taskExecutor(taskExecutor())
        .build();
}
```

---

## 14. Best Practices & Recommendations

### 14.1 Do's ✅

| Practice | Reason |
|----------|--------|
| Use managed services | Less operational overhead |
| Queue-based architecture | Decouples submission from processing |
| Auto-scaling on queue depth | Responsive to actual load |
| Spot/Preemptible instances | 70% cost savings |
| Partition by vendor | Isolate failures, track SLAs |
| Idempotent processing | Safe retries |
| Dead Letter Queues | Capture failed messages |
| Distributed tracing | Debug production issues |

### 14.2 Don'ts ❌

| Anti-Pattern | Better Approach |
|--------------|-----------------|
| Process files synchronously | Use async queue-based |
| Fixed worker count | Auto-scale based on load |
| Store files locally | Use cloud storage |
| Skip monitoring | Implement comprehensive observability |
| Ignore failures | Implement DLQ and alerting |
| Single point of failure | Deploy across multiple AZs |

### 14.3 Security Considerations

| Area | Recommendation |
|------|----------------|
| **SFTP Access** | Use SSH keys, not passwords |
| **Data Encryption** | Enable S3/Blob encryption at rest |
| **Transit Encryption** | Use TLS for all connections |
| **IAM/RBAC** | Least privilege access |
| **Secrets Management** | Use AWS Secrets Manager / Azure Key Vault |
| **Network Security** | VPC with private subnets |
| **Audit Logging** | Enable CloudTrail / Activity Log |

---

## 15. Cost Analysis

### 15.1 AWS Monthly Cost Breakdown

| Service | Configuration | Cost |
|---------|---------------|------|
| AWS Transfer Family | SFTP endpoint | $220 |
| S3 Storage | 500 GB | $12 |
| S3 Requests | 10M requests | $40 |
| Fargate (On-Demand) | 20% of compute | $200 |
| Fargate Spot | 80% of compute | $240 |
| SQS | 5M messages | $2 |
| RDS PostgreSQL | db.r6g.large | $200 |
| ElastiCache Redis | cache.r6g.large | $150 |
| CloudWatch | Logs + Metrics | $50 |
| Data Transfer | 1 TB outbound | $90 |
| **Total** | | **~$1,200-1,500** |

### 15.2 Azure Monthly Cost Breakdown

| Service | Configuration | Cost |
|---------|---------------|------|
| Blob SFTP | Enabled | $220 |
| Blob Storage | 500 GB Hot | $12 |
| Blob Operations | 10M operations | $50 |
| AKS (Regular) | 20% of nodes | $200 |
| AKS (Spot) | 80% of nodes | $280 |
| Service Bus | Standard, 5M msgs | $10 |
| PostgreSQL | 2 vCore | $150 |
| Redis Cache | C2 Standard | $120 |
| Application Insights | 5 GB | $15 |
| Data Transfer | 1 TB | $85 |
| **Total** | | **~$1,100-1,400** |

### 15.3 Cost Optimization Tips

1. **Use Spot Instances**: 70% savings on compute
2. **Auto-scale to Zero**: Scale down during off-hours
3. **Reserved Capacity**: Commit to 1-year for databases
4. **Storage Lifecycle**: Move old files to cold storage
5. **Right-size Workers**: Monitor and adjust vCPU/memory

---

## 16. References

### AWS Documentation
- [AWS Batch Documentation](https://aws.amazon.com/batch/)
- [AWS Transfer Family](https://aws.amazon.com/aws-transfer-family/)
- [AWS Step Functions](https://aws.amazon.com/step-functions/)
- [Amazon ECS Fargate](https://aws.amazon.com/fargate/)

### Azure Documentation
- [Azure Batch](https://azure.microsoft.com/services/batch/)
- [Azure Blob SFTP](https://learn.microsoft.com/azure/storage/blobs/secure-file-transfer-protocol-support)
- [Azure Kubernetes Service](https://azure.microsoft.com/services/kubernetes-service/)
- [KEDA](https://keda.sh/)

### Spring Batch
- [Spring Batch Reference](https://docs.spring.io/spring-batch/reference/)
- [Scaling and Parallel Processing](https://docs.spring.io/spring-batch/reference/scalability.html)

### Architecture Patterns
- [AWS High Volume Batch Processing](https://aws.amazon.com/blogs/compute/orchestrating-high-performance-computing-with-aws-step-functions-and-aws-batch/)
- [Azure High Volume Transaction Processing](https://learn.microsoft.com/azure/architecture/example-scenario/mainframe/process-batch-transactions)

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | December 2024 | Architecture Team | Initial document |

---

**End of Document**
