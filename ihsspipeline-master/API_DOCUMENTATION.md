# API Documentation

## Base URL

All API endpoints are prefixed with `/api` and require JWT authentication.

## Authentication

All endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

The JWT token must contain:
- `countyId` claim (or `attributes.countyId`) - Required for county-based filtering
- Role claims in `resource_access.trial-app.roles` or `realm_access.roles`

## Endpoints

### Analytics Endpoints

#### GET `/api/analytics/realtime-metrics`
Get real-time dashboard metrics.

**Response:**
```json
{
  "totalTimesheets": 1000,
  "pendingApproval": 50,
  "approved": 900,
  "rejected": 50
}
```

#### GET `/api/analytics/adhoc-data`
Get ad-hoc dataset for analytics.

**Query Parameters:**
- `limit` (optional) - Maximum number of records
- `location` (optional) - Filter by location/county
- `department` (optional) - Filter by department
- `status` (optional) - Filter by status

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "location": "Orange",
      "department": "Home Care",
      "status": "APPROVED",
      "employeeId": "EMP001",
      "employeeName": "John Doe",
      "regularHours": 40.0,
      "overtimeHours": 5.0
    }
  ],
  "total": 1000
}
```

#### GET `/api/analytics/adhoc-filters`
Get available filter options.

**Response:**
```json
{
  "locations": ["Orange", "Los Angeles", "San Diego"],
  "departments": ["Home Care", "Nursing", "Therapy"],
  "statuses": ["APPROVED", "PENDING", "REJECTED"]
}
```

#### GET `/api/analytics/adhoc-stats`
Get statistical aggregations.

**Query Parameters:**
- `dimension` - Group by dimension (location, department, status)
- `measure` - Measure to aggregate (regularHours, overtimeHours)

#### GET `/api/analytics/adhoc-breakdowns`
Get breakdowns by dimension.

#### GET `/api/analytics/adhoc-crosstab`
Get cross-tabulation data.

### Data Pipeline Endpoints

#### POST `/api/pipeline/extract-enhanced`
Enhanced 5-stage data processing pipeline.

**Request Body:**
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "countyFilter": "Orange",
  "statusFilter": "APPROVED"
}
```

**Response:**
```json
{
  "summary": {
    "totalRecords": 1000,
    "filteredRecords": 500,
    "maskedFields": 10
  },
  "data": [...]
}
```

### Business Intelligence Endpoints

#### POST `/api/bi/generate-report`
Generate BI report.

**Request Body:**
```json
{
  "reportType": "TIMESHEET_SUMMARY",
  "format": "PDF",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

### Case Management Endpoints

#### POST `/api/case/create`
Create new recipient case.

**Required Role:** CASE_WORKER

**Request Body:**
```json
{
  "recipientSsn": "123-45-6789",
  "recipientFirstName": "Jane",
  "recipientLastName": "Doe",
  "caseType": "IHSS"
}
```

#### POST `/api/case/verify-ssn`
Verify SSN with external service.

**Request Body:**
```json
{
  "ssn": "123-45-6789",
  "firstName": "Jane"
}
```

### Person Search Endpoints

#### POST `/api/person/search`
Search persons by name or SSN.

**Required Role:** CASE_WORKER

**Request Body:**
```json
{
  "searchTerm": "Jane Doe",
  "searchType": "NAME"
}
```

### Report Delivery Endpoints

#### POST `/api/reports/email`
Generate and email report immediately.

**Request Body:**
```json
{
  "reportType": "TIMESHEET_SUMMARY",
  "format": "PDF",
  "recipientEmail": "user@example.com",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

#### GET `/api/reports/jobs`
Get all report jobs.

**Query Parameters:**
- `status` (optional) - Filter by job status
- `userId` (optional) - Filter by user ID

#### GET `/api/reports/jobs/{jobId}`
Get specific report job details.

#### GET `/api/reports/jobs/{jobId}/download`
Download generated report.

### Field Masking Endpoints

#### GET `/api/field-masking/rules`
Get field masking rules for current user.

#### POST `/api/field-masking/rules`
Update field masking rules.

**Request Body:**
```json
{
  "role": "COUNTY_WORKER",
  "rules": [
    {
      "field": "ssn",
      "visible": false,
      "maskPattern": "***-**-****"
    }
  ]
}
```

### Batch Jobs Endpoints

#### GET `/api/batch-jobs`
Get all batch jobs.

#### GET `/api/batch-jobs/{jobId}`
Get specific batch job details.

#### POST `/api/batch-jobs`
Create new batch job.

**Request Body:**
```json
{
  "reportType": "TIMESHEET_SUMMARY",
  "format": "PDF",
  "schedule": "0 0 1 * *",
  "emailRecipients": ["user@example.com"]
}
```

## Error Responses

All endpoints return standard HTTP status codes:

- `200 OK` - Success
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

**Error Response Format:**
```json
{
  "error": "Error message",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/endpoint"
}
```

## County-Based Filtering

All data endpoints automatically filter results by the user's county (from JWT `countyId` claim):

- **COUNTY_WORKER**: Only sees data from their county
- **DISTRICT_WORKER**: Sees data from all counties in their district
- **CENTRAL_WORKER**: Sees data from all counties
- **SUPERVISOR**: Sees data from their assigned county
- **CASE_WORKER**: Sees data from their assigned county

If `countyId` is not present in the JWT token, the request will fail with 401 Unauthorized.

## Rate Limiting

API endpoints are subject to rate limiting:
- 100 requests per minute per user
- 1000 requests per hour per user

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1609459200
```

