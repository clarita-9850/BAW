# CMIPS Application - Project Documentation

## Table of Contents
1. [Executive Summary](#1-executive-summary)
2. [System Architecture](#2-system-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Backend Application](#4-backend-application)
5. [Frontend Application](#5-frontend-application)
6. [API Gateway](#6-api-gateway)
7. [Authentication & Authorization](#7-authentication--authorization)
8. [Database Schema](#8-database-schema)
9. [API Reference](#9-api-reference)
10. [Event-Driven Architecture](#10-event-driven-architecture)
11. [Deployment](#11-deployment)
12. [Security Features](#12-security-features)

---

## 1. Executive Summary

**CMIPS (Case Management and Payroll Information Platform)** is an enterprise-grade, fully configurable case management system designed for government agencies. The platform enables management of cases, timesheets, Electronic Visit Verification (EVV), work queues, and comprehensive reporting.

### Key Capabilities

| Feature | Description |
|---------|-------------|
| **Role-Based Access Control** | Unlimited custom roles with fine-grained permissions |
| **Location-Based Access** | County/district-based data isolation via Keycloak groups |
| **Field-Level Data Masking** | 6 masking types configurable per role/report |
| **Method-Level Authorization** | API endpoint protection via Keycloak policies |
| **Multi-Format Reporting** | PDF, CSV, JSON, XML report generation |
| **Batch Job Processing** | Scheduled and on-demand report automation |
| **Electronic Visit Verification** | GPS-based check-in/check-out tracking |
| **Real-Time Notifications** | Task and event notifications with polling |
| **Multi-Language Support** | English, Spanish, Chinese, Armenian |
| **Dynamic Theming** | County-specific color themes |

### Configuration Philosophy
- **Zero Hardcoding**: All business rules, roles, and permissions are configurable
- **Dynamic Configuration**: Changes take effect immediately without code deployment
- **Centralized Management**: All configurations stored in Keycloak

---

## 2. System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER LAYER                                      │
│    Admin │ Supervisor │ Case Worker │ Provider │ Recipient                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Next.js)                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  Dashboards │  │  Auth       │  │  API Client │  │  Theme      │         │
│  │  (per role) │  │  Context    │  │  (Axios)    │  │  Context    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│                              Port: 3000                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY (Spring Cloud Gateway)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  Route      │  │  JWT        │  │  Auth       │  │  CORS       │         │
│  │  Config     │  │  Validation │  │  Proxy      │  │  Handler    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│                              Port: 8090                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌───────────────────────┐ ┌─────────────────┐ ┌─────────────────────────────┐
│   KEYCLOAK IAM        │ │  SPRING BOOT    │ │       KAFKA                 │
│  ┌─────────────────┐  │ │  BACKEND        │ │  ┌─────────────────────┐    │
│  │ Realm: cmips    │  │ │                 │ │  │ Topics:             │    │
│  │ OAuth2/OIDC     │  │ │  ┌───────────┐  │ │  │ - timesheet-events  │    │
│  │ JWT Tokens      │  │ │  │Controllers│  │ │  │ - task-events       │    │
│  │ 58 County Groups│  │ │  │ (14)      │  │ │  │ - case-events       │    │
│  └─────────────────┘  │ │  └───────────┘  │ │  │ - notification-events│   │
│       Port: 8080      │ │  ┌───────────┐  │ │  └─────────────────────┘    │
└───────────────────────┘ │  │ Services  │  │ │       Port: 9092           │
                          │  │ (29)      │  │ └─────────────────────────────┘
                          │  └───────────┘  │
                          │  ┌───────────┐  │
                          │  │Repositories│ │
                          │  │ (8)       │  │
                          │  └───────────┘  │
                          │   Port: 8081    │
                          └────────┬────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────┐
                    │      POSTGRESQL             │
                    │  ┌─────────────────────┐    │
                    │  │ Schema: application │    │
                    │  │ Schema: keycloak    │    │
                    │  └─────────────────────┘    │
                    │        Port: 5432           │
                    └─────────────────────────────┘
```

### Service Communication Flow

```
Frontend (3000)
    │
    ├── POST /api/auth/login ─────────────────────┐
    │                                              │
    ▼                                              ▼
API Gateway (8090) ─────────────────────────► Keycloak (8080)
    │                                              │
    │ JWT Validation                               │ JWT Token
    │                                              │
    ▼                                              │
Backend (8081) ◄───────────────────────────────────┘
    │
    ├── JPA ──────► PostgreSQL (5432)
    │
    └── Events ───► Kafka (9092)
```

---

## 3. Technology Stack

### Backend
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.2.0 |
| Language | Java | 17 |
| Database | PostgreSQL | 15 |
| ORM | Spring Data JPA | - |
| Security | Spring Security OAuth2 | - |
| Messaging | Apache Kafka | 7.5.0 |
| IAM | Keycloak | 23.0.0 |
| PDF Generation | OpenPDF | - |
| Template Engine | FreeMarker | - |
| Build Tool | Maven | - |

### Frontend
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Next.js | 16.0.3 |
| Language | TypeScript | 5 |
| UI Library | React | 19.2.0 |
| Styling | Tailwind CSS | 4 |
| State Management | React Query | 5.0.0 |
| HTTP Client | Axios | 1.13.2 |
| i18n | i18next | 23.11.0 |
| Charts | Recharts | 3.4.1 |

### API Gateway
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Cloud Gateway | 2023.0.0 |
| Reactive Support | Spring WebFlux | - |

### Infrastructure
| Component | Technology | Version |
|-----------|------------|---------|
| Containerization | Docker | - |
| Orchestration | Docker Compose | 3.8 |
| Message Broker | Apache Kafka | 7.5.0 |
| Coordination | Zookeeper | 7.5.0 |

---

## 4. Backend Application

### Directory Structure

```
backend/src/main/java/com/cmips/
├── annotation/           # Custom annotations (@RequirePermission)
├── aspect/               # AOP aspects (AuthorizationAspect)
├── config/               # Configuration classes
│   ├── SecurityConfig.java
│   ├── KafkaConfig.java
│   └── WebConfig.java
├── controller/           # REST API controllers (14)
├── dto/                  # Data Transfer Objects
├── entity/               # JPA Entity classes (8)
├── event/                # Kafka event models
├── model/                # Business models
├── repository/           # Spring Data JPA repositories (8)
├── service/              # Business logic services (29)
├── util/                 # Utility classes
└── CmipsMvpApplication.java
```

### Controllers (14)

| Controller | Base Path | Description |
|------------|-----------|-------------|
| AuthController | `/api/auth` | Login, token refresh |
| TimesheetController | `/api/timesheets` | Timesheet CRUD, workflow |
| EVVController | `/evv` | Check-in/out, location tracking |
| TaskController | `/api/tasks` | Task management |
| WorkQueueController | `/api/work-queues` | Work queue operations |
| NotificationController | `/api/notifications` | User notifications |
| CaseController | `/api/cases` | Case management |
| BusinessIntelligenceController | `/api/bi` | Report job management |
| AnalyticsController | `/api/analytics` | Real-time metrics |
| PersonController | `/api/person` | Person CRUD |
| ProviderRecipientController | `/api/provider-recipient` | Relationships |
| KeycloakAdminController | `/api/admin/keycloak` | User/role management |
| FieldMaskingController | `/api/field-masking` | Data masking config |
| DataPipelineController | `/api/pipeline` | Data extraction |

### Services (29)

| Category | Services |
|----------|----------|
| **Core Business** | TimesheetService, EVVService, TaskService, PersonService, CaseService |
| **Authorization** | KeycloakAuthorizationService, KeycloakPolicyEvaluationService, FieldLevelAuthorizationService |
| **Admin** | KeycloakAdminService, KeycloakGroupService |
| **Reporting** | ReportGenerationService, CSVReportGeneratorService, PDFReportGeneratorService |
| **Delivery** | EmailReportService, SFTPDeliveryService |
| **Data Processing** | FieldMaskingService, QueryBuilderService, DataFetchingService |
| **Job Management** | JobQueueService, ScheduledReportService |
| **Utilities** | EncryptionService, CountyCodeMappingService, EventService |

### Repositories (8)

| Repository | Entity | Key Methods |
|------------|--------|-------------|
| TimesheetRepository | Timesheet | 30+ query methods, aggregation |
| TaskRepository | Task | findByAssignedTo, status queries |
| EVVRepository | EVVRecord | Provider/recipient queries |
| NotificationRepository | Notification | User notification queries |
| PersonRepository | Person | Search, CRUD |
| ProviderRecipientRepository | ProviderRecipient | Relationship queries |
| ReportJobRepository | ReportJob | Job tracking |
| WorkQueueSubscriptionRepository | WorkQueueSubscription | Subscription management |

---

## 5. Frontend Application

### Directory Structure

```
frontend-nextjs/
├── app/                          # Next.js App Router
│   ├── layout.tsx                # Root layout with providers
│   ├── page.tsx                  # Home page
│   ├── login/                    # Login page
│   ├── admin/
│   │   ├── keycloak/             # Keycloak admin panel
│   │   └── field-masking/        # Field masking config
│   ├── caseworker/
│   │   ├── dashboard/            # Case worker dashboard
│   │   └── cases/                # Case management
│   ├── provider/
│   │   ├── dashboard/            # Provider dashboard
│   │   ├── evv-checkin/          # EVV check-in
│   │   ├── timesheets/           # Timesheet management
│   │   └── profile/              # Provider profile
│   ├── supervisor/
│   │   └── dashboard/            # Supervisor dashboard
│   ├── recipient/
│   │   └── dashboard/            # Recipient dashboard
│   ├── analytics/                # Analytics dashboard
│   ├── batch-jobs/               # Batch job management
│   ├── timesheets/               # General timesheet view
│   └── api/                      # API routes (server-side)
├── components/
│   ├── structure/                # Header, Footer, Navigation
│   └── patterns/                 # Reusable UI patterns
├── contexts/
│   ├── AuthContext.tsx           # Authentication state
│   └── CountyThemeContext.tsx    # Dynamic theming
├── lib/
│   ├── api.ts                    # Axios client configuration
│   ├── services/                 # API service layer
│   └── i18n/                     # Internationalization
└── public/
    └── cagov/                    # CA.gov template assets
```

### Routes by Role

| Role | Routes |
|------|--------|
| **Admin** | `/admin/keycloak`, `/admin/field-masking`, `/decode-jwt` |
| **Supervisor** | `/supervisor/dashboard`, `/analytics`, `/batch-jobs` |
| **Case Worker** | `/caseworker/dashboard`, `/caseworker/cases`, `/my-workspace`, `/inbox` |
| **Provider** | `/provider/dashboard`, `/provider/evv-checkin`, `/provider/timesheets`, `/provider/profile` |
| **Recipient** | `/recipient/dashboard` |
| **Common** | `/login`, `/timesheets`, `/persons`, `/accessibility` |

### State Management

```
┌─────────────────────────────────────────────────────┐
│                  Provider Chain                      │
├─────────────────────────────────────────────────────┤
│  QueryClientProvider (React Query)                   │
│    └── I18nProvider (Internationalization)          │
│          └── AuthProvider (Authentication)          │
│                └── CountyThemeProvider (Theming)    │
│                      └── App Content                │
└─────────────────────────────────────────────────────┘
```

### Service Layer

| Service | Functionality |
|---------|---------------|
| **CaseService** | CRUD, activation, closure, SSN verification |
| **PersonService** | Search, create, update persons |
| **TaskService** | Task CRUD, status management |
| **JobService** | Batch job creation, status polling, downloads |
| **AnalyticsService** | Summary, demographics, trends, ad-hoc queries |
| **ReportService** | Report generation, data extraction |
| **FieldMaskingService** | Masking rules configuration |

### Internationalization

| Language | Code | Status |
|----------|------|--------|
| English | en | Default |
| Spanish | es | Supported |
| Chinese | zh | Supported |
| Armenian | hy | Supported |

### County Theming

| County Code | Name | Primary Color |
|-------------|------|---------------|
| CTA | Sacramento | Dark Blue (#153554) |
| CTB | Orange County | Orange (#A15801) |
| CTC | Shasta | Green (#336c39) |
| Default | Oceanside | Blue (Admin) |

---

## 6. API Gateway

### Route Configuration

| Route Pattern | Target | Description |
|---------------|--------|-------------|
| `/api/auth/**` | Keycloak | Authentication (login, refresh) |
| `/api/cmips/**` | Backend | Rewrites to `/api/{segment}` |
| `/api/analytics/**` | Backend | Analytics endpoints |
| `/api/bi/**` | Backend | Business Intelligence |
| `/api/pipeline/**` | Backend | Data pipeline |
| `/api/reports/**` | Backend | Report generation |
| `/api/field-masking/**` | Backend | Field masking config |
| `/api/**` | Backend | Catch-all for other APIs |

### Custom Filters

| Filter | Order | Purpose |
|--------|-------|---------|
| **AuthProxyFilter** | - | Transforms login JSON to form-urlencoded for Keycloak |
| **JwtValidationFilter** | -100 | Extracts user info from JWT, adds X-User-* headers |
| **KeycloakHttpsHeaderFilter** | 10000 | Sets X-Forwarded-Proto: https for Keycloak |

### Headers Added to Backend Requests

| Header | Source | Description |
|--------|--------|-------------|
| `X-User-Id` | JWT subject | User's unique ID |
| `X-User-Name` | JWT preferred_username | Username |
| `X-User-Email` | JWT email | User's email |
| `X-User-Roles` | JWT realm_access.roles | Comma-separated roles |
| `Authorization` | Original request | Bearer token forwarded |

### CORS Configuration

- **Allowed Origins**: `localhost:3000`, `localhost:3001`, `127.0.0.1:3000`, `127.0.0.1:3001`
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS, PATCH
- **Allow Credentials**: true
- **Max Age**: 3600 seconds

---

## 7. Authentication & Authorization

### Authentication Flow

```
┌──────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
│ Frontend │      │ Gateway  │      │ Keycloak │      │ Backend  │
└────┬─────┘      └────┬─────┘      └────┬─────┘      └────┬─────┘
     │                 │                 │                 │
     │ POST /api/auth/login              │                 │
     │ {username, password}              │                 │
     │────────────────►│                 │                 │
     │                 │                 │                 │
     │                 │ Transform to    │                 │
     │                 │ form-urlencoded │                 │
     │                 │────────────────►│                 │
     │                 │                 │                 │
     │                 │                 │ Validate        │
     │                 │                 │ credentials     │
     │                 │                 │                 │
     │                 │◄────────────────│                 │
     │                 │ JWT Token       │                 │
     │◄────────────────│                 │                 │
     │                 │                 │                 │
     │ Store token     │                 │                 │
     │ in localStorage │                 │                 │
     │                 │                 │                 │
     │ GET /api/timesheets               │                 │
     │ Authorization: Bearer {JWT}       │                 │
     │────────────────►│                 │                 │
     │                 │                 │                 │
     │                 │ Validate JWT    │                 │
     │                 │ with JWK Set    │                 │
     │                 │────────────────►│                 │
     │                 │◄────────────────│                 │
     │                 │                 │                 │
     │                 │ Add X-User-*    │                 │
     │                 │ headers         │                 │
     │                 │─────────────────────────────────►│
     │                 │                 │                 │
     │                 │◄─────────────────────────────────│
     │◄────────────────│ Response        │                 │
     │                 │                 │                 │
```

### JWT Token Structure

```json
{
  "sub": "user-uuid",
  "preferred_username": "john.doe",
  "email": "john.doe@example.com",
  "realm_access": {
    "roles": ["CASE_WORKER", "SUPERVISOR"]
  },
  "groups": ["CTA", "CTB"],
  "exp": 1234567890,
  "iat": 1234567800
}
```

### Role Hierarchy

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access, user management, configuration |
| **SUPERVISOR** | Multi-county access, approvals, analytics, batch jobs |
| **CASE_WORKER** | Case management, timesheet approval/rejection, tasks |
| **PROVIDER** | Create/submit timesheets, EVV check-in/out, view own data |
| **RECIPIENT** | View own timesheets, basic dashboard |

### Location-Based Access Control

1. **County Groups in Keycloak**: 58 county groups (CTA, CTB, CTC, etc.)
2. **User Assignment**: Users assigned to one or more county groups
3. **JWT Token**: Contains `groups` array with assigned counties
4. **Data Filtering**: Backend filters queries by user's county groups

```sql
-- Example: User with groups ["CTA", "CTB"]
SELECT * FROM timesheets WHERE location IN ('CTA', 'CTB')
```

### Method-Level Authorization

```java
@RequirePermission(resource = "Timesheet Resource", scope = "approve")
public ResponseEntity<Timesheet> approveTimesheet(@PathVariable Long id) {
    // Method only executes if Keycloak policy grants permission
}
```

### Field-Level Data Masking

| Masking Type | Example Output |
|--------------|----------------|
| NONE | `123-45-6789` |
| HIDDEN | `***HIDDEN***` |
| PARTIAL_MASK | `XXX-XX-6789` |
| HASH_MASK | `a1b2c3d4e5f6` |
| ANONYMIZE | `[ANONYMIZED]` |
| AGGREGATE | `$50,000 - $75,000` |

---

## 8. Database Schema

### Entity Relationship Diagram

```
┌─────────────────────┐       ┌─────────────────────┐
│      Person         │       │   ProviderRecipient │
├─────────────────────┤       ├─────────────────────┤
│ person_id (PK)      │◄──────│ provider_id (FK)    │
│ first_name          │       │ recipient_id (FK)   │
│ last_name           │       │ case_number         │
│ ssn                 │       │ authorized_hours    │
│ date_of_birth       │       │ status              │
│ gender              │       │ relationship        │
│ ethnicity           │       │ county              │
│ email               │       └─────────────────────┘
│ phone               │
│ address             │
└─────────────────────┘
         │
         │
         ▼
┌─────────────────────┐       ┌─────────────────────┐
│     Timesheet       │       │     EVV Record      │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │◄──────│ timesheet_id (FK)   │
│ user_id             │       │ id (PK)             │
│ employee_name       │       │ provider_id         │
│ department          │       │ recipient_id        │
│ location            │       │ check_in_time       │
│ pay_period_start    │       │ check_out_time      │
│ pay_period_end      │       │ check_in_lat/lng    │
│ regular_hours       │       │ check_out_lat/lng   │
│ overtime_hours      │       │ hours_worked        │
│ status              │       │ status              │
│ approved_by         │       │ violation_type      │
└─────────────────────┘       └─────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│       Task          │       │    Notification     │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │
│ title               │       │ user_id             │
│ description         │       │ message             │
│ status              │       │ notification_type   │
│ priority            │       │ read_status         │
│ assigned_to         │       │ action_link         │
│ created_by          │       │ created_at          │
│ due_date            │       └─────────────────────┘
│ work_queue          │
│ trigger_condition   │
└─────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│     ReportJob       │       │ WorkQueueSubscription│
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │
│ report_type         │       │ user_id             │
│ status              │       │ queue_name          │
│ parameters          │       │ subscribed_at       │
│ result_path         │       └─────────────────────┘
│ created_at          │
│ completed_at        │
└─────────────────────┘
```

### Timesheet Status Workflow

```
┌────────┐     Submit      ┌───────────┐
│ DRAFT  │────────────────►│ SUBMITTED │
└────────┘                 └─────┬─────┘
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
                    ▼            │            ▼
              ┌──────────┐      │      ┌──────────┐
              │ APPROVED │      │      │ REJECTED │
              └──────────┘      │      └──────────┘
                                │
                                ▼
                    ┌───────────────────┐
                    │ REVISION_REQUESTED │
                    └───────────────────┘
```

### EVV Record Status

| Status | Description |
|--------|-------------|
| IN_PROGRESS | Provider checked in, not yet checked out |
| COMPLETED | Check-in and check-out recorded |
| VERIFIED | EVV record verified and linked to timesheet |
| VIOLATION | Location or time violation detected |

---

## 9. API Reference

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login with username/password |
| POST | `/api/auth/refresh` | Refresh JWT token |

### Timesheet Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/timesheets` | List timesheets (paginated, filtered) |
| GET | `/api/timesheets/{id}` | Get timesheet by ID |
| POST | `/api/timesheets` | Create new timesheet |
| PUT | `/api/timesheets/{id}` | Update timesheet |
| DELETE | `/api/timesheets/{id}` | Delete timesheet |
| POST | `/api/timesheets/{id}/submit` | Submit for approval |
| POST | `/api/timesheets/{id}/approve` | Approve timesheet |
| POST | `/api/timesheets/{id}/reject` | Reject timesheet |
| GET | `/api/timesheets/actions` | Get allowed actions for user |

### EVV Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/evv/check-in` | Provider check-in with GPS |
| POST | `/evv/check-out/{evvId}` | Provider check-out |
| GET | `/evv/my-records` | Get provider's EVV records |
| GET | `/evv/active-checkin` | Get active check-in |
| GET | `/evv/timesheet/{timesheetId}` | Get EVV for timesheet |

### Task Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get user's tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task |
| PUT | `/api/tasks/{id}/status` | Update task status |
| DELETE | `/api/tasks/{id}` | Delete task |
| GET | `/api/tasks/queue/{queueName}` | Get queue tasks |

### Business Intelligence Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bi/reports/generate` | Queue report job |
| GET | `/api/bi/jobs/{jobId}/status` | Get job status |
| GET | `/api/bi/jobs/{jobId}/result` | Get job result |
| GET | `/api/bi/jobs/{jobId}/download` | Download report |
| POST | `/api/bi/jobs/{jobId}/cancel` | Cancel job |

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/keycloak/users` | Create user |
| GET | `/api/admin/keycloak/users` | List all users |
| GET | `/api/admin/keycloak/roles` | List all roles |
| POST | `/api/admin/keycloak/roles` | Create role |
| POST | `/api/admin/keycloak/policies` | Create policy |
| POST | `/api/admin/keycloak/permissions` | Create permission |
| GET | `/api/admin/keycloak/groups` | List groups |
| POST | `/api/admin/keycloak/groups` | Create group |

---

## 10. Event-Driven Architecture

### Kafka Topics

| Topic | Partitions | Description |
|-------|------------|-------------|
| cmips-case-events | 3 | Case lifecycle events |
| cmips-timesheet-events | 3 | Timesheet workflow events |
| cmips-provider-events | 3 | Provider-related events |
| cmips-task-events | 3 | Task creation/updates |
| cmips-notification-events | 3 | Notification triggers |

### Event Structure

```json
{
  "eventType": "TIMESHEET_SUBMITTED",
  "payload": {
    "timesheetId": 123,
    "userId": "user-uuid",
    "status": "SUBMITTED"
  },
  "metadata": {
    "timestamp": "2025-01-15T10:30:00Z",
    "source": "timesheet-service"
  },
  "traceId": "trace-uuid",
  "correlationId": "correlation-uuid"
}
```

### Event Flow Example

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Provider   │      │   Backend    │      │    Kafka     │
│   submits    │─────►│   Service    │─────►│    Topic     │
│  timesheet   │      │              │      │              │
└──────────────┘      └──────────────┘      └──────┬───────┘
                                                   │
                      ┌────────────────────────────┘
                      │
                      ▼
        ┌──────────────────────────┐
        │   TaskEventConsumer      │
        │   - Creates approval task│
        │   - Sends notification   │
        └──────────────────────────┘
```

---

## 11. Deployment

### Docker Compose Services

| Service | Container Name | Port | Dependencies |
|---------|----------------|------|--------------|
| postgres | cmips-postgres | 5432 | - |
| zookeeper | cmips-zookeeper | 2181 | - |
| kafka | cmips-kafka | 9092, 9093 | zookeeper |
| keycloak | cmips-keycloak | 8080 | postgres |
| cmips-backend | cmips-backend | 8081 | postgres, keycloak, kafka |
| api-gateway | api-gateway | 8090 | keycloak, cmips-backend |
| cmips-frontend-nextjs | cmips-frontend-nextjs | 3000 | api-gateway |

### Starting the Application

```bash
# Start all services
cd /Users/sajeev/Documents/CMIPS/cmipsapplication
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Environment Variables

#### Backend
| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_DATASOURCE_URL | PostgreSQL connection | jdbc:postgresql://postgres:5432/cmips_mvp |
| KEYCLOAK_AUTH_SERVER_URL | Keycloak URL | http://keycloak:8080/ |
| KEYCLOAK_REALM | Keycloak realm | cmips |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | Kafka brokers | kafka:9093 |
| BATCH_SCHEDULER_ENABLED | Enable batch jobs | true |
| REPORT_OUTPUT_DIR | Report storage path | /app/reports |

#### Frontend
| Variable | Description | Default |
|----------|-------------|---------|
| NEXT_PUBLIC_API_URL | API Gateway URL | http://localhost:8090/api |
| NEXT_PUBLIC_KEYCLOAK_URL | Keycloak URL | http://localhost:8080 |
| NEXT_PUBLIC_KEYCLOAK_REALM | Keycloak realm | cmips |
| NEXT_PUBLIC_KEYCLOAK_CLIENT_ID | Client ID | cmips-frontend |

### Health Checks

| Service | Endpoint | Interval |
|---------|----------|----------|
| Backend | `http://localhost:8081/actuator/health` | 30s |
| Gateway | `http://localhost:8090/actuator/health` | 30s |
| PostgreSQL | `pg_isready` | 10s |

---

## 12. Security Features

### Authentication Security

| Feature | Implementation |
|---------|----------------|
| Token-Based Auth | JWT with Keycloak |
| Token Expiration | Configurable TTL |
| Token Refresh | Refresh token support |
| Session Management | Stateless (no server sessions) |

### Authorization Security

| Feature | Implementation |
|---------|----------------|
| Role-Based Access | Extracted from JWT realm_access |
| Permission-Based | Keycloak policy evaluation |
| Location Isolation | County groups in JWT |
| Field Masking | Dynamic per role/report |
| Method-Level | @RequirePermission annotation |

### API Security

| Feature | Implementation |
|---------|----------------|
| HTTPS | TLS termination at load balancer |
| CORS | Restricted to allowed origins |
| CSRF | Disabled (stateless API) |
| Rate Limiting | Configurable at gateway |

### Data Security

| Feature | Implementation |
|---------|----------------|
| Encryption at Rest | Database encryption (configurable) |
| Encryption in Transit | HTTPS/TLS |
| Data Masking | Field-level masking service |
| Audit Logging | Comprehensive operation logging |

### Security Best Practices

1. **Least Privilege**: Users get minimum required permissions
2. **Token Priority**: JWT claims override request parameters
3. **No Insecure Fallbacks**: System fails explicitly
4. **Comprehensive Logging**: All access attempts logged
5. **Input Validation**: Server-side validation on all inputs

---

## Configuration Interfaces

### Web Admin Panel
- **URL**: `/admin/keycloak`
- **Features**: User management, role creation, policy configuration, group management, field masking

### REST API
- **Base URL**: `/api/admin/keycloak/*`
- **Features**: Programmatic access to all admin functions

---

## Support

For questions or issues:
- Check documentation in `/architecture diagrams` folder
- Review business rules in `BUSINESS_RULES.md`
- API testing with Postman collection in `/backend/CMIPS_API_Collection.postman_collection.json`

---

*Document generated for CMIPS Application v1.0*
