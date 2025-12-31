# Client Developer Q&A Document
## Comprehensive Technical Questions & Answers for IHSS/CMIPS Timesheet Management System

This document anticipates and answers technical questions that client developers may have about the system's architecture, features, and implementation details.

---

## Table of Contents
1. [Architecture Questions](#1-architecture-questions)
2. [Authentication & Security Questions](#2-authentication--security-questions)
3. [Dashboard Analytics Questions](#3-dashboard-analytics-questions)
4. [Report Generation Questions](#4-report-generation-questions)
5. [Batch Processing Questions](#5-batch-processing-questions)
6. [Data Flow & Integration Questions](#6-data-flow--integration-questions)
7. [Database & Performance Questions](#7-database--performance-questions)
8. [Deployment & Operations Questions](#8-deployment--operations-questions)

---

## 1. Architecture Questions

### Q1.1: What is the overall system architecture?
**Answer:** The system follows a microservices architecture with clear separation of concerns:
- **Frontend**: Next.js 14 with React and TypeScript (Port 3000)
- **Backend API**: Spring Boot 3.2.0 with Java 17 (Port 8080)
- **API Gateway**: Routes requests between frontend and backend (Port 8090)
- **Database**: PostgreSQL 13 (Port 5432)
- **Authentication**: Keycloak OAuth2/OIDC (Port 8085)
- **Notification Service**: Separate microservice for email/SMS (Port 8086)
- **External Validation API**: SSN and data validation service (Port 8082)

### Q1.2: What is the complete technology stack?
**Answer:**
| Layer | Technology |
|-------|------------|
| Frontend | Next.js 14, React 18, TypeScript, CSS Modules |
| Backend | Spring Boot 3.2.0, Java 17, Spring Data JPA |
| Database | PostgreSQL 13, Hibernate ORM |
| Authentication | Keycloak 22.0.5, OAuth2/OIDC, JWT |
| Report Generation | OpenPDF (iText fork), Apache POI |
| Template Engine | FreeMarker |
| Containerization | Docker, Docker Compose |
| File Transfer | SFTP via JSch library |
| Email | Spring Mail, MailHog (development) |

### Q1.3: How does the system handle horizontal scalability?
**Answer:** The system is designed for horizontal scalability:
- **Stateless Backend**: All session state is stored in JWT tokens, allowing any instance to handle any request
- **Database Connection Pooling**: HikariCP manages connection pools efficiently
- **Batch Job Processing**: Thread pool executor with configurable worker pool size (default: 2 workers)
- **Job Queue**: Database-backed job queue with optimistic locking prevents duplicate processing
- **Containerized Deployment**: Docker containers can be replicated behind a load balancer

### Q1.4: What design patterns are used in the application?
**Answer:**
1. **Repository Pattern**: Spring Data JPA repositories abstract database access
2. **Service Layer Pattern**: Business logic encapsulated in service classes
3. **Factory Pattern**: Report generators use factories for different formats
4. **Strategy Pattern**: Field masking strategies vary by role
5. **Observer Pattern**: Event service publishes events for audit logging
6. **Template Method Pattern**: Report templates define structure, subclasses fill content
7. **Optimistic Locking**: Prevents race conditions in job claiming

### Q1.5: How is the codebase organized?
**Answer:** The Spring Boot backend follows standard layering:
```
src/main/java/com/example/kafkaeventdrivenapp/
├── config/           # Configuration classes (BatchProcessingConfig, SecurityConfig)
├── controller/       # REST API endpoints (ReportDeliveryController, AnalyticsController)
├── dto/              # Data Transfer Objects (BIReportRequest, ReportGenerationResponse)
├── entity/           # JPA entities (ReportJobEntity, TimesheetEntity)
├── repository/       # Spring Data repositories (ReportJobRepository)
├── service/          # Business logic services (JobQueueService, ReportGenerationService)
└── security/         # Security configurations and filters
```

### Q1.6: How do the frontend and backend communicate?
**Answer:** Communication follows REST API patterns:
1. Frontend makes HTTP requests via Axios client (`lib/services/api.ts`)
2. All requests include JWT token in `Authorization: Bearer <token>` header
3. API Gateway (port 8090) routes to backend services
4. Backend validates JWT, extracts user role and countyId
5. Response includes JSON data with role-appropriate field masking applied

### Q1.7: What is the role of the API Gateway?
**Answer:** The API Gateway serves as a single entry point:
- Routes frontend requests to appropriate backend services
- Handles CORS configuration
- Provides a unified endpoint (port 8090) for the frontend
- Can be extended for rate limiting, request logging, and circuit breaking

### Q1.8: How does the system handle configuration management?
**Answer:** Configuration is managed through:
- `application.yml` - Main Spring Boot configuration
- Environment variables - Override defaults for different environments
- `@ConfigurationProperties` classes - Type-safe configuration binding
- Docker Compose `.env` files - Container-specific settings

### Q1.9: What external services does the application integrate with?
**Answer:**
| Service | Purpose | Integration Method |
|---------|---------|-------------------|
| Keycloak | Authentication/Authorization | OAuth2/OIDC, JWT validation |
| PostgreSQL | Data persistence | JDBC/JPA |
| SFTP Server | Report file delivery | JSch library |
| SMTP Server | Email notifications | Spring Mail |
| External Validation API | SSN verification | REST API calls |

### Q1.10: How is error handling implemented across the system?
**Answer:**
- **Backend**: Global exception handler (`@ControllerAdvice`) catches and formats errors
- **Service Layer**: Try-catch blocks with specific exception types
- **Job Processing**: Failed jobs update status to "FAILED" with error message stored
- **Frontend**: Axios interceptors catch API errors, display user-friendly messages
- **Logging**: SLF4J with detailed error logging including stack traces

### Q1.11: What logging strategy is used?
**Answer:**
- **Framework**: SLF4J with Logback
- **Log Levels**: DEBUG for development, INFO/WARN/ERROR for production
- **Audit Logging**: EventService logs all data access operations
- **Emoji Indicators**: Console logs use emojis for quick visual scanning (📊, ✅, ❌)
- **Structured Logging**: Key operations log parameters for debugging

### Q1.12: How is the system monitored in production?
**Answer:**
- **Health Checks**: Spring Actuator exposes `/actuator/health` endpoint
- **Docker Health Checks**: Each container has health check configured
- **Job Monitoring**: Batch job status tracked in database (QUEUED, PROCESSING, COMPLETED, FAILED)
- **Progress Tracking**: Jobs report 0-100% progress during execution

### Q1.13: What is the data model overview?
**Answer:** Core entities:
- **Timesheets**: Employee work records with hours, status, location
- **Report Jobs**: Batch processing queue with status tracking
- **Cases**: Recipient case management
- **Persons**: Provider/recipient personal information
- **Events**: Audit log entries

### Q1.14: How does the system handle concurrent requests?
**Answer:**
- **Thread Pool**: Configurable thread pool for batch processing (default: 2 workers)
- **Connection Pool**: HikariCP manages database connections
- **Optimistic Locking**: `@Version` field on entities prevents lost updates
- **Job Claiming**: `markJobAsProcessing()` uses database transaction to claim jobs atomically

### Q1.15: What are the key configuration parameters?
**Answer:**
```yaml
batch:
  scheduler:
    enabled: true          # Enable/disable job scheduler
    interval-ms: 5000      # Poll interval (5 seconds)
    max-jobs-per-poll: 3   # Max jobs picked up per poll
    worker-pool-size: 2    # Parallel worker threads

job-processing:
  default-chunk-size: 1000   # Records per chunk
  dependent-chunk-size: 2000 # Chunk size for dependent jobs
```

---

## 2. Authentication & Security Questions

### Q2.1: How does JWT-based authentication work?
**Answer:**
1. User logs in via Keycloak login page
2. Keycloak validates credentials and issues JWT token
3. JWT contains: user ID, username, roles, countyId (custom claim)
4. Frontend stores token and includes in all API requests
5. Backend validates token signature against Keycloak public key
6. Backend extracts role and countyId from token claims

### Q2.2: What user roles are supported?
**Answer:**
| Role | Description | Data Access |
|------|-------------|-------------|
| CENTRAL_WORKER | State-level administrator | All counties, all data |
| DISTRICT_WORKER | District-level worker | Assigned district only |
| COUNTY_WORKER | County-level worker | Assigned county only |
| SUPERVISOR | County supervisor | Assigned county only |
| CASE_WORKER | Case management worker | Assigned county only |
| PROVIDER | Service provider | Own data only |
| RECIPIENT | Service recipient | Own data only |

### Q2.3: How is role-based access control (RBAC) implemented?
**Answer:**
1. **Role Extraction**: JWT token parsed to extract user role
2. **Query Filtering**: All database queries filter by user's county (except CENTRAL_WORKER)
3. **Field Masking**: Sensitive fields masked based on role
4. **UI Restrictions**: Frontend hides/disables features based on role
5. **API Validation**: Controllers validate role has permission for requested action

### Q2.4: How does county-level data isolation work?
**Answer:**
```java
// Backend extracts county from JWT
String userCounty = extractCountyFromJWT(jwtToken);

// All queries filtered by county
if (!userRole.equals("CENTRAL_WORKER")) {
    query.setParameter("county", userCounty);
}
```
- County ID stored as custom claim in JWT token
- No fallback mechanisms - system fails if county not in token
- Central workers bypass county filtering

### Q2.5: What is field-level masking and how does it work?
**Answer:** Field masking protects sensitive data based on user role:

| Masking Type | Example | Description |
|--------------|---------|-------------|
| NONE | Original value | Full visibility |
| HIDDEN | `***HIDDEN***` | Complete redaction |
| PARTIAL_MASK | `XXX-XX-1234` | Last 4 digits visible |
| HASH_MASK | `HASH_1234567890` | Hashed identifier |
| ANONYMIZE | `User 123` | Generic replacement |
| AGGREGATE | `0-20 hours` | Range instead of value |

### Q2.6: How is field masking configured per role?
**Answer:** Example masking matrix:

| Field | Central Worker | County Worker | Provider |
|-------|---------------|---------------|----------|
| SSN | PARTIAL_MASK | HIDDEN | HIDDEN |
| Provider Email | NONE | HIDDEN | NONE |
| Total Amount | NONE | AGGREGATE | HIDDEN |
| Recipient Name | NONE | NONE | ANONYMIZE |

Configuration is maintained in `FieldMaskingService` and loaded from database.

### Q2.7: How are API endpoints secured?
**Answer:**
1. **Spring Security**: All endpoints require authentication by default
2. **JWT Filter**: Custom filter validates JWT on every request
3. **Method Security**: `@PreAuthorize` annotations on controller methods
4. **CORS**: Configured to allow only trusted origins
5. **HTTPS**: All production traffic encrypted (TLS)

### Q2.8: How is the JWT token validated?
**Answer:**
1. Extract token from `Authorization` header
2. Verify signature using Keycloak's public key (JWKS endpoint)
3. Check expiration time (`exp` claim)
4. Validate issuer matches Keycloak realm URL
5. Extract custom claims (role, countyId)

### Q2.9: What happens when a token expires?
**Answer:**
- Frontend receives 401 Unauthorized response
- Auth context detects expired token
- User redirected to login page
- Keycloak issues new token after re-authentication
- Refresh tokens can extend session without re-login

### Q2.10: How is sensitive data protected in transit and at rest?
**Answer:**
- **In Transit**: HTTPS/TLS encryption for all API calls
- **At Rest**: PostgreSQL encryption, sensitive fields masked in responses
- **Logging**: Sensitive data excluded from logs
- **Database**: Connection strings use SSL mode in production

---

## 3. Dashboard Analytics Questions

### Q3.1: How does real-time analytics data flow from backend to frontend?
**Answer:**
```
Frontend (React) → API Request → Backend Analytics Controller
                                        ↓
                              AnalyticsService
                                        ↓
                              Database Query
                                        ↓
                              Field Masking
                                        ↓
                              JSON Response → Frontend Display
```

### Q3.2: What analytics endpoints are available?
**Answer:**
| Endpoint | Purpose |
|----------|---------|
| `/api/analytics/summary` | Aggregate statistics |
| `/api/analytics/adhoc-filters` | Available filter options |
| `/api/analytics/adhoc-stats` | Filtered statistics |
| `/api/analytics/adhoc-data` | Raw data with filters |
| `/api/analytics/adhoc-breakdowns` | Demographic breakdowns |
| `/api/analytics/adhoc-crosstab` | Cross-tabulation data |

### Q3.3: What dimensions are supported in analytics?
**Answer:**
- Location (County)
- Department
- Status
- Employee ID/Name
- User ID
- Pay Period Start/End
- Submitted By / Approved By
- Provider Demographics (Gender, Ethnicity, Age Group)
- Recipient Demographics (Gender, Ethnicity, Age Group)

### Q3.4: What measures can be analyzed?
**Answer:**
- ID Count (record count)
- Total Hours
- Regular Hours
- Overtime Hours
- Sick Hours
- Vacation Hours
- Holiday Hours

### Q3.5: How does multi-dimensional grouping work?
**Answer:**
Users can select up to 8 dimensions to group data:
```javascript
const dimensions = {
  dimension1: 'location',      // Group by county
  dimension2: 'department',    // Then by department
  dimension3: 'status',        // Then by status
  // ... up to dimension8
};
```
Backend groups data by all selected dimensions and aggregates measures.

### Q3.6: How are filters applied to analytics queries?
**Answer:**
1. User selects filters in UI (county, department, status, demographics)
2. Frontend sends filters as query parameters
3. Backend builds dynamic WHERE clause
4. County filter auto-applied based on user's JWT token
5. Results filtered, aggregated, and returned

### Q3.7: How does county restriction work in analytics?
**Answer:**
```javascript
// Frontend checks user role
const isCountyRestricted = userRole.includes('SUPERVISOR') || 
                           userRole.includes('CASE_WORKER');

if (isCountyRestricted) {
  // County dropdown disabled, auto-set from JWT
  setCountyFilter(user.countyId);
}
```

### Q3.8: What key metrics are displayed on the dashboard?
**Answer:**
- **Individuals**: Total unique records
- **Population**: Reference population (California)
- **Per Capita Rate**: Records per population percentage
- **Total Authorized Hours**: Sum of all hours
- **Average Hours**: Mean hours per record

### Q3.9: How is the pivot table functionality implemented?
**Answer:**
Frontend transforms raw data client-side:
1. Fetch data with selected dimensions
2. Group records by dimension values
3. Calculate aggregates for each group
4. Display in tabular format with dimension columns and measure columns

### Q3.10: How does the Details Table differ from Pivot Table?
**Answer:**
- **Details Table**: Raw record-level data with all columns
- **Pivot Table**: Aggregated/grouped data by selected dimensions
- User switches between views via tabs
- Same underlying data, different presentation

### Q3.11: How are demographic filters populated?
**Answer:**
```javascript
// API returns available options
const response = await analyticsService.getAdhocFilters();
// Returns:
{
  providerGenders: ['Male', 'Female', 'Non-Binary'],
  providerEthnicities: ['Hispanic', 'Asian', 'White', ...],
  providerAgeGroups: ['18-25', '26-35', '36-45', ...],
  // ... recipient demographics
}
```

### Q3.12: How is analytics data refreshed?
**Answer:**
- Data refreshes on filter change (useEffect dependency)
- No automatic polling (user-triggered refresh)
- Loading state shown during data fetch
- Error handling with status banner notifications

---

## 4. Report Generation Questions

### Q4.1: What report formats are supported?
**Answer:**
| Format | Library | Use Case |
|--------|---------|----------|
| PDF | OpenPDF (iText fork) | Formatted reports with charts |
| CSV | Custom generator | Spreadsheet analysis |
| JSON | Jackson | API/BI tool integration |
| XML | JAXB | Enterprise system integration |

### Q4.2: How is a PDF report generated?
**Answer:**
```java
// PDFReportGeneratorService.java
public byte[] generatePDFReport(String reportType, String userRole, 
                                List<Map<String, Object>> data, 
                                Map<String, Object> additionalData, 
                                String jwtToken) {
    // 1. Get visible fields for role
    List<String> visibleFields = fieldVisibilityService.getVisibleFields(userRole, jwtToken);
    
    // 2. Create PDF document
    Document document = new Document(PageSize.A4);
    
    // 3. Add sections
    addCoverPage(document, reportType, dateRange);
    addExecutiveSummary(document, data, userRole);
    addAnalyticsSection(document, data, userRole);
    addDetailedDataSection(document, data, visibleFields);
    addAppendices(document, data, additionalData);
    
    // 4. Return byte array
    return outputStream.toByteArray();
}
```

### Q4.3: What sections are included in PDF reports?
**Answer:**
1. **Cover Page**: Report title, date range, generated timestamp
2. **Executive Summary**: Key metrics, high-level statistics
3. **Analytics & Insights**: Charts, trend analysis
4. **Detailed Data Section**: Tabular data with visible fields
5. **Appendices**: Additional context, methodology notes
6. **Footer**: Page numbers, confidentiality notice

### Q4.4: How does field masking apply to reports?
**Answer:**
Before data is written to any report format:
1. User role extracted from JWT
2. Field visibility rules loaded for role
3. Each field value processed through masking function
4. Masked data written to report
5. Masked fields cannot be unmasked from report output

### Q4.5: How are CSV reports generated?
**Answer:**
```java
// CSVReportGeneratorService.java
public String generateCSV(List<Map<String, Object>> data, List<String> columns) {
    StringBuilder csv = new StringBuilder();
    
    // Header row
    csv.append(String.join(",", columns)).append("\n");
    
    // Data rows
    for (Map<String, Object> row : data) {
        List<String> values = columns.stream()
            .map(col -> formatCSVValue(row.get(col)))
            .collect(Collectors.toList());
        csv.append(String.join(",", values)).append("\n");
    }
    
    return csv.toString();
}
```

### Q4.6: How does report delivery work?
**Answer:**
Reports can be delivered via:
1. **Download**: User downloads directly from browser
2. **Email**: Attached to email via Spring Mail
3. **SFTP**: Uploaded to configured SFTP server
4. **Storage**: Saved to local/S3 storage for later retrieval

### Q4.7: How are scheduled reports generated?
**Answer:**
```java
// ScheduledReportService.java
@Scheduled(cron = "0 30 5 * * ?")  // Daily at 5:30 AM
public void generateDailyReports() {
    String systemToken = generateSystemJwtToken();
    
    for (String role : REPORT_ROLES) {
        generateUnifiedReportForRole(role, "DAILY_REPORT", systemToken);
    }
}
```

### Q4.8: What report types are available?
**Answer:**
- DAILY_REPORT - Previous day's data
- WEEKLY_REPORT - Previous week's data
- MONTHLY_REPORT - Previous month's data
- QUARTERLY_REPORT - Previous quarter's data
- YEARLY_REPORT - Previous year's data
- TIMESHEET_REPORT - Custom date range
- COUNTY_ANALYTICS - County-specific analysis
- CONSOLIDATED_REPORT - Multi-source aggregation

### Q4.9: How is the report date range determined?
**Answer:**
```java
// For scheduled reports
private DateRange getPreviousDayRange() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    return new DateRange(yesterday, yesterday);
}

// For manual reports
request.setStartDate(LocalDate.parse(startDate));
request.setEndDate(LocalDate.parse(endDate));
```

### Q4.10: How does SFTP delivery work?
**Answer:**
```java
// SFTPDeliveryService.java
public void uploadReport(byte[] fileContent, String filename) {
    JSch jsch = new JSch();
    Session session = jsch.getSession(username, host, port);
    session.setPassword(password);
    session.connect();
    
    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    channel.connect();
    channel.put(new ByteArrayInputStream(fileContent), remotePath + filename);
    channel.disconnect();
    session.disconnect();
}
```

### Q4.11: How are email reports sent?
**Answer:**
```java
// EmailReportService.java
public void sendReportEmail(String to, String subject, byte[] pdfAttachment) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText("Please find attached your report.");
    helper.addAttachment("report.pdf", new ByteArrayResource(pdfAttachment));
    
    mailSender.send(message);
}
```

### Q4.12: How is report data fetched for generation?
**Answer:**
1. ReportGenerationService receives request with filters
2. DataFetchingService builds query with role-based filtering
3. Query executed with pagination for large datasets
4. Results passed through FieldMaskingService
5. Masked data returned for report formatting

### Q4.13: How are large reports handled?
**Answer:**
- **Chunked Processing**: Data processed in configurable chunks (default: 1000 records)
- **Streaming**: Results streamed to file rather than held in memory
- **Background Jobs**: Large reports processed asynchronously
- **Progress Tracking**: Job progress updated incrementally

### Q4.14: What happens if report generation fails?
**Answer:**
1. Exception caught in service layer
2. Job status updated to "FAILED"
3. Error message stored in job record
4. User notified (if notification enabled)
5. Retry can be triggered manually

### Q4.15: How is report access audited?
**Answer:**
- EventService logs all report generation requests
- Log includes: user, role, report type, date range, timestamp
- Audit trail stored in events table
- Compliance reports can be generated from audit data

---

## 5. Batch Processing Questions

### Q5.1: How does the job queue system work?
**Answer:**
```
Job Creation → Database (QUEUED) → Scheduler Poll → Worker Thread → Processing → COMPLETED/FAILED
```
1. Jobs created with status "QUEUED" in `report_jobs` table
2. BatchJobScheduler polls every 5 seconds
3. Up to 3 jobs claimed per poll
4. Worker threads process jobs asynchronously
5. Status updated on completion/failure

### Q5.2: What is the job lifecycle?
**Answer:**
```
QUEUED → PROCESSING → COMPLETED
           ↓
         FAILED
           ↓
       CANCELLED (manual)
```

### Q5.3: How does the BatchJobScheduler work?
**Answer:**
```java
@Scheduled(fixedDelayString = "${batch.scheduler.interval-ms:5000}")
public void pollAndDispatchJobs() {
    if (!properties.isEnabled()) return;
    
    // Get up to 3 queued jobs
    List<ReportJobEntity> queuedJobs = jobRepository.findTopQueuedJobs(3);
    
    for (ReportJobEntity job : queuedJobs) {
        // Atomically claim job (optimistic locking)
        jobQueueService.markJobAsProcessing(job.getJobId())
            .ifPresent(claimedJob -> 
                batchJobExecutor.execute(() -> 
                    backgroundProcessingService.processJob(claimedJob.getJobId())
                )
            );
    }
}
```

### Q5.4: How is job claiming implemented to prevent duplicates?
**Answer:**
```java
@Transactional
public Optional<ReportJobEntity> markJobAsProcessing(String jobId) {
    ReportJobEntity job = jobRepository.findById(jobId).orElse(null);
    
    if (job == null || !"QUEUED".equals(job.getStatus())) {
        return Optional.empty();  // Already claimed or doesn't exist
    }
    
    job.setStatus("PROCESSING");
    job.setStartedAt(LocalDateTime.now());
    
    try {
        return Optional.of(jobRepository.save(job));  // Optimistic lock check
    } catch (OptimisticLockingFailureException e) {
        return Optional.empty();  // Another thread claimed it
    }
}
```

### Q5.5: How does the worker thread pool work?
**Answer:**
```java
@Bean(name = "batchJobExecutor")
public TaskExecutor batchJobExecutor(BatchSchedulerProperties properties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("batch-worker-");
    executor.setCorePoolSize(properties.getWorkerPoolSize());  // Default: 2
    executor.setMaxPoolSize(properties.getWorkerPoolSize());
    executor.setQueueCapacity(properties.getMaxJobsPerPoll());
    executor.initialize();
    return executor;
}
```

### Q5.6: How does chunked processing work?
**Answer:**
```java
public void processDataInChunksStreaming(String jobId, int totalRecords, int chunkSize) {
    int processedRecords = 0;
    
    while (processedRecords < totalRecords) {
        // Fetch chunk
        List<Record> chunk = dataFetchingService.fetchChunk(offset, chunkSize);
        
        // Process chunk
        processChunk(chunk);
        
        // Update progress
        processedRecords += chunk.size();
        int progress = (processedRecords * 100) / totalRecords;
        jobQueueService.updateJobProgress(jobId, progress, processedRecords);
    }
}
```

### Q5.7: How do job dependencies work?
**Answer:**
Configuration in `application.yml`:
```yaml
job-dependencies:
  enabled: true
  dependencies:
    - parent-report-type: "DAILY_REPORT"
      parent-role: "CASE_WORKER"
      dependent-report-type: "DAILY_SUMMARY"
      condition: "ON_SUCCESS"
```
When parent job completes successfully, dependent job is automatically queued.

### Q5.8: How are multiple dependencies handled?
**Answer:**
```yaml
- parent-report-types: ["WEEKLY_REPORT", "MONTHLY_REPORT"]
  dependent-report-type: "CONSOLIDATED_REPORT"
  condition: "ON_SUCCESS"
```
System waits for ALL parent jobs to complete before triggering dependent job.

### Q5.9: How is job progress tracked?
**Answer:**
```javascript
// Frontend polls job status
const { data: jobs } = useQuery({
  queryKey: ['jobs', selectedStatus],
  queryFn: () => jobService.getAllJobs(),
  refetchInterval: 5000,  // Poll every 5 seconds
});

// Display progress bar
<div className="progress-bar" style={{ width: `${job.progress}%` }}>
  {job.progress}%
</div>
```

### Q5.10: What information is stored for each job?
**Answer:**
```sql
report_jobs table:
├── job_id              -- Unique identifier (JOB_XXXXXXXX)
├── status              -- QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED
├── report_type         -- DAILY_REPORT, WEEKLY_REPORT, etc.
├── user_role           -- Role that created the job
├── parent_job_id       -- NULL or parent job ID (for dependencies)
├── jwt_token           -- Stored token for processing
├── request_data        -- Serialized request JSON
├── result_path         -- Path to generated report file
├── progress            -- 0-100 percentage
├── total_records       -- Total records to process
├── processed_records   -- Records processed so far
├── error_message       -- Error details if FAILED
├── created_at          -- Job creation timestamp
├── started_at          -- Processing start timestamp
├── completed_at        -- Completion timestamp
├── estimated_completion_time
└── version             -- Optimistic locking version
```

### Q5.11: How can a job be cancelled?
**Answer:**
```java
// Controller endpoint
@PostMapping("/jobs/{jobId}/cancel")
public ResponseEntity<?> cancelJob(@PathVariable String jobId) {
    jobQueueService.cancelJob(jobId);
    return ResponseEntity.ok(Map.of("status", "CANCELLED"));
}

// Service method
public void cancelJob(String jobId) {
    ReportJobEntity job = jobRepository.findById(jobId).orElseThrow();
    if ("QUEUED".equals(job.getStatus()) || "PROCESSING".equals(job.getStatus())) {
        job.setStatus("CANCELLED");
        jobRepository.save(job);
    }
}
```

### Q5.12: How is job result retrieved?
**Answer:**
```javascript
// Get job result
const result = await jobService.getJobResult(jobId);
// Returns: { jobId, status, resultPath, totalRecords, processedRecords, completedAt }

// Download report file
const blob = await jobService.downloadReport(jobId);
const url = URL.createObjectURL(blob);
// Trigger download...
```

### Q5.13: What triggers job creation?
**Answer:**
1. **Manual**: User clicks "Generate Report" in dashboard
2. **Scheduled**: Cron jobs trigger at configured times
3. **Dependency**: Parent job completion triggers dependent jobs
4. **API**: Direct POST to `/api/bi/reports/generate`

### Q5.14: How are scheduled jobs configured?
**Answer:**
```java
// ScheduledReportService.java
@Scheduled(cron = "0 30 5 * * ?")      // Daily at 5:30 AM
public void generateDailyReports() { ... }

@Scheduled(cron = "0 30 5 * * MON")    // Weekly on Monday
public void generateWeeklyReports() { ... }

@Scheduled(cron = "0 30 5 1 * ?")      // Monthly on 1st
public void generateMonthlyReports() { ... }

@Scheduled(cron = "0 30 5 1 1,4,7,10 ?")  // Quarterly
public void generateQuarterlyReports() { ... }
```

### Q5.15: How does error handling work in batch jobs?
**Answer:**
```java
public void processJob(String jobId) {
    try {
        // Processing logic...
        jobQueueService.setJobResult(jobId, resultPath);
    } catch (Exception e) {
        log.error("Job failed: {}", e.getMessage());
        jobQueueService.updateJobStatus(jobId, "FAILED", e.getMessage());
        // Dependent jobs NOT triggered on failure
    }
}
```

---

## 6. Data Flow & Integration Questions

### Q6.1: How does the data extraction pipeline work?
**Answer:**
5-stage pipeline:
1. **Role Validation**: Verify user permissions
2. **Rules Engine Trigger**: Determine filtering rules
3. **Query Building**: Construct dynamic SQL
4. **Data Fetching**: Execute query with pagination
5. **Field Masking**: Apply role-based masking

### Q6.2: How does Business Objects integration work?
**Answer:**
- System provides JSON/CSV/XML exports compatible with BO
- Scheduled exports can be delivered to BO-accessible locations via SFTP
- API endpoints available for direct BO data connector integration
- Field masking ensures BO users only see authorized data

### Q6.3: How is data formatted for different BI tools?
**Answer:**
| BI Tool | Format | Delivery |
|---------|--------|----------|
| Business Objects | JSON/CSV | API/SFTP |
| Tableau | CSV/JSON | API/File |
| Power BI | JSON | REST API |
| Crystal Reports | CSV | SFTP |

### Q6.4: How does the notification service integrate?
**Answer:**
```java
// NotificationService.java
public void sendNotification(String type, String recipient, Map<String, Object> data) {
    switch (type) {
        case "EMAIL":
            emailService.send(recipient, formatEmail(data));
            break;
        case "SMS":
            smsService.send(recipient, formatSMS(data));
            break;
    }
}
```

### Q6.5: How does external SSN validation work?
**Answer:**
```java
// ExternalValidationService.java
public ValidationResult validateSSN(String ssn) {
    RestTemplate restTemplate = new RestTemplate();
    String url = externalValidationUrl + "/ssn";
    
    Map<String, String> request = Map.of("ssn", ssn);
    ResponseEntity<ValidationResult> response = 
        restTemplate.postForEntity(url, request, ValidationResult.class);
    
    return response.getBody();
}
```

### Q6.6: What data export formats are supported?
**Answer:**
- **JSON**: Structured data with nested objects
- **CSV**: Flat tabular data, Excel-compatible
- **XML**: Enterprise system integration
- **PDF**: Formatted reports with visuals

### Q6.7: How does the frontend service layer work?
**Answer:**
```typescript
// lib/services/api.ts - Base Axios client
const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: { 'Content-Type': 'application/json' }
});

// Interceptor adds JWT token
apiClient.interceptors.request.use((config) => {
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Q6.8: How is API versioning handled?
**Answer:**
Currently single version at `/api/*`. For future versioning:
- URL path versioning: `/api/v1/*`, `/api/v2/*`
- Header versioning: `Accept: application/vnd.api.v1+json`
- Both approaches supported by Spring

### Q6.9: How does the system handle network failures?
**Answer:**
- **Retry Logic**: Axios can be configured with retry interceptors
- **Timeout**: Configurable request timeouts
- **Circuit Breaker**: Can be added with Resilience4j
- **Fallback**: Graceful degradation with user-friendly messages

### Q6.10: How is data consistency maintained across services?
**Answer:**
- **Single Database**: All services share PostgreSQL instance
- **Transactions**: Spring `@Transactional` for atomic operations
- **Optimistic Locking**: Prevents concurrent update conflicts
- **Event Logging**: Audit trail for data changes

---

## 7. Database & Performance Questions

### Q7.1: What is the database schema structure?
**Answer:**
Key tables:
- `timesheets` - Work records with hours, status, location
- `report_jobs` - Batch job queue
- `cases` - Recipient case management
- `persons` - Provider/recipient information
- `events` - Audit log

### Q7.2: How are database queries optimized?
**Answer:**
- **Indexing**: Indexes on frequently queried columns (location, status, date)
- **Pagination**: All list queries support pagination
- **Lazy Loading**: JPA lazy loading for related entities
- **Query Optimization**: Native queries for complex analytics

### Q7.3: How is connection pooling configured?
**Answer:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

### Q7.4: How are large result sets handled?
**Answer:**
- **Pagination**: Default page size of 25-100 records
- **Streaming**: Large exports use streaming writes
- **Chunking**: Batch jobs process in chunks
- **Async Processing**: Large reports processed in background

### Q7.5: What database indexes exist?
**Answer:**
```sql
CREATE INDEX idx_timesheets_location ON timesheets(location);
CREATE INDEX idx_timesheets_status ON timesheets(status);
CREATE INDEX idx_timesheets_pay_period ON timesheets(pay_period_start, pay_period_end);
CREATE INDEX idx_report_jobs_status ON report_jobs(status);
CREATE INDEX idx_report_jobs_created ON report_jobs(created_at);
```

### Q7.6: How is database migration handled?
**Answer:**
- SQL migration scripts in `db-migrations/` folder
- Manual execution for schema changes
- Version-controlled migration files
- Can be automated with Flyway/Liquibase

### Q7.7: How is data backed up?
**Answer:**
- PostgreSQL native backup tools (pg_dump)
- AWS RDS automated backups in production
- Point-in-time recovery available
- Daily backup schedule recommended

---

## 8. Deployment & Operations Questions

### Q8.1: How is the application containerized?
**Answer:**
Each service has its own Dockerfile:
```dockerfile
# Backend Dockerfile
FROM openjdk:17-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Frontend Dockerfile
FROM node:18-alpine
COPY . .
RUN npm install && npm run build
CMD ["npm", "start"]
```

### Q8.2: How does Docker Compose orchestrate services?
**Answer:**
```yaml
services:
  postgres:
    image: postgres:13
    ports: ["5432:5432"]
    healthcheck: ...
    
  spring-app:
    build: .
    ports: ["8080:8080"]
    depends_on:
      postgres:
        condition: service_healthy
    
  frontend:
    build: ./timesheet-frontend
    ports: ["3000:3000"]
```

### Q8.3: What are the system requirements?
**Answer:**
- **Memory**: Minimum 4GB RAM for all containers
- **CPU**: 2+ cores recommended
- **Disk**: 10GB+ for Docker images and data
- **Network**: Ports 3000, 8080, 8085, 8090, 5432

### Q8.4: How is the system monitored?
**Answer:**
- **Health Endpoints**: `/actuator/health` on each service
- **Docker Health Checks**: Container-level health monitoring
- **Logging**: Centralized logging via Docker
- **Metrics**: Spring Actuator metrics available

### Q8.5: How are secrets managed?
**Answer:**
- **Development**: `.env` files (gitignored)
- **Production**: AWS Secrets Manager
- **Configuration**: Environment variable injection
- **Keycloak**: Separate secrets for client credentials

### Q8.6: How is the application updated?
**Answer:**
```bash
# Rebuild and restart
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Q8.7: What is the startup sequence?
**Answer:**
1. PostgreSQL starts and becomes healthy
2. Keycloak starts (if local)
3. External Validation API starts
4. Spring Boot backend starts (waits for dependencies)
5. Notification service starts
6. Frontend starts

### Q8.8: How are logs accessed?
**Answer:**
```bash
# View all logs
docker-compose logs -f

# View specific service
docker-compose logs -f spring-app

# View last 100 lines
docker-compose logs --tail=100 spring-app
```

### Q8.9: How is the system scaled in production?
**Answer:**
- **Horizontal Scaling**: Multiple Spring Boot instances behind load balancer
- **Database**: AWS RDS with read replicas
- **Caching**: Redis can be added for session/data caching
- **CDN**: Static frontend assets via CloudFront

### Q8.10: What is the disaster recovery plan?
**Answer:**
- **Database Backups**: Daily automated backups
- **Infrastructure as Code**: Docker Compose for reproducibility
- **Multi-AZ**: AWS RDS Multi-AZ for database failover
- **Documentation**: Runbooks for common failure scenarios

---

## Summary

This Q&A document covers 80+ technical questions across 8 categories that client developers are likely to ask during the system presentation. Each answer provides technical depth while remaining accessible, with code examples and configuration snippets where appropriate.

For live demonstration, focus on:
1. **Analytics Dashboard**: Show filter interactions and real-time data
2. **Report Generation**: Generate PDF/CSV with visible field masking
3. **Batch Jobs**: Create a job and show progress tracking
4. **Security**: Demonstrate role-based restrictions

---

*Document Version: 1.0*
*Last Updated: December 2024*

