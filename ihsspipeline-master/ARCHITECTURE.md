# System Architecture

## Overview

The Timesheet Management System is a Spring Boot-based application designed for timesheet data management, reporting, and business intelligence. The system processes timesheet records with role-based access control, field-level data masking, and automated batch report generation.

## Technology Stack

- **Backend Framework**: Spring Boot 3.2.0 (Java 17)
- **Database**: PostgreSQL with JPA/Hibernate
- **Authentication/Authorization**: Keycloak (OAuth2/OIDC)
- **Event Logging**: In-process EventService (no external broker)
- **Report Generation**: PDF (OpenPDF), CSV, JSON, XML
- **Template Engine**: FreeMarker
- **Frontend**: Next.js 14 with React and TypeScript

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Web UI     │  │  Mobile App  │  │  BI Tools    │     │
│  │  (Next.js)   │  │  (Future)    │  │  (Tableau)   │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                    ┌────────▼────────┐
                    │   API Gateway   │
                    │  (Spring Boot)   │
                    │   Port 8080     │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│  Controllers   │  │  Services       │  │  Repositories  │
│  (REST API)    │  │  (Business      │  │  (Data Access) │
│                │  │   Logic)        │  │                │
└───────┬────────┘  └────────┬────────┘  └───────┬────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│   Keycloak     │  │ Event Service   │  │  PostgreSQL   │
│  (Auth/IDP)    │  │ (In-Process)    │  │   (Database)   │
│  Port 8080     │  │                 │  │   Port 5432   │
└────────────────┘  └─────────────────┘  └────────────────┘
```

## Core Components

### Controllers

- **AnalyticsController** - Real-time analytics and dashboard metrics
- **DataPipelineController** - Data extraction and processing pipeline
- **BusinessIntelligenceController** - BI report generation
- **CaseController** - Case management operations
- **PersonController** - Person search and management
- **FieldMaskingController** - Field-level data masking configuration
- **ReportDeliveryController** - Report generation and delivery
- **AuthController** - Authentication endpoints
- **CountyAccessController** - County-based access control
- **ProviderController** - Provider management
- **RecipientController** - Recipient management

### Services

- **ReportGenerationService** - Multi-format report generation (PDF, CSV, JSON, XML)
- **FieldMaskingService** - Dynamic field-level data masking
- **JobQueueService** - Asynchronous batch job processing
- **ScheduledReportService** - Cron-based scheduled report generation
- **EventService** - In-process event logging and audit trail
- **DataFetchingService** - Database query and data extraction
- **CountyBasedDataExtractionService** - County-filtered data extraction
- **EmailReportService** - Email delivery with attachments
- **SFTPDeliveryService** - Secure file transfer
- **NotificationService** - Notification management

### Security

- **JWT-based Authentication** - Keycloak OAuth2/OIDC integration
- **Role-Based Access Control (RBAC)** - 6 user roles:
  - CENTRAL_WORKER
  - DISTRICT_WORKER
  - COUNTY_WORKER
  - SUPERVISOR
  - CASE_WORKER
  - PROVIDER
  - RECIPIENT
- **County-based Data Filtering** - Geographic access control via JWT claims
- **Field-level Masking** - Dynamic field visibility based on user role

## Data Flow

### Request Processing Flow

1. **Authentication**: User authenticates with Keycloak, receives JWT token
2. **Authorization**: JWT token validated, user role and county extracted
3. **Data Filtering**: County-based filtering applied to all queries
4. **Field Masking**: Role-based field masking applied to response data
5. **Event Logging**: All operations logged via EventService
6. **Response**: Filtered and masked data returned to client

### Batch Job Processing

1. **Job Creation**: Report job created in database queue
2. **Job Scheduling**: Cron scheduler picks up scheduled jobs
3. **Job Execution**: Background worker processes job asynchronously
4. **Report Generation**: Multi-format report generated
5. **Delivery**: Report delivered via email or SFTP
6. **Notification**: User notified of completion

## Database Schema

### Core Tables

- **timesheets** - Timesheet records with location, department, status
- **report_jobs** - Batch job queue and status
- **cases** - Recipient case management
- **persons** - Person/recipient information
- **events** - Audit log and event history

### Key Fields

- **location** - County code (e.g., "Orange", "Los Angeles")
- **department** - Department name
- **status** - Timesheet status (APPROVED, PENDING, etc.)
- **employeeId** - Employee identifier
- **payPeriodStart/End** - Pay period dates

## County-Based Access Control

The system enforces strict county-based data filtering:

1. **JWT Token Claims**: County ID extracted from JWT token (`countyId` claim)
2. **No Fallbacks**: System fails explicitly if county not in token
3. **Database Filtering**: All queries filtered by user's county
4. **Role-Based Overrides**: Central workers can access all counties

## Event Logging

The system uses an in-process EventService (no external message broker):

- **Event Types**: Data extraction, report generation, field masking
- **Audit Trail**: All operations logged with user, timestamp, action
- **Notification Integration**: Events trigger notifications via NotificationService

## Deployment Architecture

### Local Development

- Docker Compose for all services
- PostgreSQL container
- Keycloak container (from sajeevs-codebase-main)
- Spring Boot application container

### Production

- AWS RDS for PostgreSQL
- Keycloak on EC2/ECS
- Spring Boot on EC2/ECS
- Frontend on EC2/ECS or S3+CloudFront

## Security Considerations

- **JWT Validation**: All requests validated against Keycloak
- **County Isolation**: Strict data filtering prevents cross-county access
- **Field Masking**: Sensitive fields masked based on role
- **Audit Logging**: All operations logged for compliance
- **Encryption**: Sensitive data encrypted at rest and in transit

