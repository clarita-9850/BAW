# 📋 System Capabilities and Features Reference

## 🎯 Executive Summary

This system is a **fully configurable, enterprise-grade case management and payroll information platform** designed with **zero hardcoding** - everything can be configured to meet specific business requirements without code changes.

### Key System Capabilities

- ✅ **Complete Role and Permission Management**: Create and configure unlimited custom roles with fine-grained permissions
- ✅ **Dynamic User Management**: Create users, assign roles, and manage user memberships dynamically
- ✅ **Configurable Field-Level Data Masking**: Apply different masking rules per role, per report type, per field
- ✅ **Method-Level API Authorization**: Control API endpoint access using configurable policies
- ✅ **Location-Based Access Control**: Implement geographic/organizational boundaries using configurable groups
- ✅ **Flexible Authorization Resources**: Define custom resources and configure field-level permissions
- ✅ **Administrative Feature Delegation**: Delegate admin capabilities to other roles with customizable limitations
- ✅ **Multi-Format Report Generation**: Generate reports in PDF, CSV, JSON, XML formats
- ✅ **Scheduled Report Automation**: Configure automated report generation with cron-based scheduling
- ✅ **Comprehensive Audit Logging**: Full audit trail for compliance and security monitoring

**Configuration Interface**: Web-based Admin Panel (`/admin/keycloak`) and REST API (`/api/admin/keycloak/*`)

**Configuration Storage**: All configurations stored in Keycloak, enabling dynamic changes without code deployment.

---

## Table of Contents
1. [System Capabilities Overview](#system-capabilities-overview)
2. [Role and Permission Management](#role-and-permission-management)
3. [Authentication and Authorization System](#authentication-and-authorization-system)
4. [Data Access and Filtering Capabilities](#data-access-and-filtering-capabilities)
5. [Field-Level Data Masking System](#field-level-data-masking-system)
6. [Location-Based Access Control](#location-based-access-control)
7. [Workflow Management System](#workflow-management-system)
8. [Batch Job and Reporting System](#batch-job-and-reporting-system)
9. [Data Validation System](#data-validation-system)
10. [Security Features](#security-features)
11. [Scheduling and Automation](#scheduling-and-automation)
12. [Additional System Features](#additional-system-features)

---

## 1. System Capabilities Overview

### 1.1 Core System Principles

**Fully Configurable Architecture**: The system provides comprehensive configuration capabilities that allow customization of all aspects without requiring code changes or deployments.

**Zero Hardcoding**: No business rules, roles, permissions, or access controls are hardcoded. Everything is configurable through the Admin Panel or REST API.

**Dynamic Configuration**: All configuration changes take effect immediately without requiring system restarts or code deployments.

**Policy-Based Authorization**: Authorization decisions are made dynamically using Keycloak's policy engine, allowing for complex, rule-based access control.

### 1.2 Configuration Capabilities

The system provides the following configuration capabilities:

#### User Management
- ✅ Create users with configurable attributes (username, email, password, name, etc.)
- ✅ Delete users from the system
- ✅ View and list all users
- ✅ Assign multiple roles to users
- ✅ View role assignments for any user
- ✅ Manage user lifecycle (enable/disable accounts)

#### Role Management
- ✅ Create unlimited custom roles with unique names
- ✅ Delete roles (with impact analysis)
- ✅ View all available roles
- ✅ Configure role descriptions and metadata
- ✅ Create composite roles (roles containing other roles)
- ✅ Assign custom attributes to roles

#### Permission Configuration
- ✅ Create role-based policies defining what roles can do
- ✅ Create scope-based permissions linking resources, scopes, and policies
- ✅ View all authorization policies and permissions
- ✅ Configure fine-grained permissions for specific operations
- ✅ Enable/disable permissions dynamically

#### Authorization Resource Management
- ✅ Create custom authorization resources (e.g., "Timesheet Resource", "Case Resource")
- ✅ Configure resource attributes for field-level permissions
- ✅ Update authorization rules dynamically
- ✅ View all authorization resources and their configurations

#### Field-Level Data Masking Configuration
- ✅ Configure masking rules per role, per report type, per field
- ✅ Select from multiple masking types (NONE, HIDDEN, PARTIAL_MASK, HASH_MASK, ANONYMIZE, AGGREGATE)
- ✅ Configure access levels (FULL_ACCESS, MASKED_ACCESS, HIDDEN_ACCESS)
- ✅ Set custom masking patterns for partial masking
- ✅ Enable/disable field masking for specific roles or report types

#### Method-Level API Authorization Configuration
- ✅ Define resources representing API endpoints or operations
- ✅ Define scopes representing actions (create, read, update, delete, etc.)
- ✅ Create policies that control method access
- ✅ Link resources, scopes, and policies to create permissions
- ✅ Enforce authorization using annotations (e.g., `@RequirePermission`)

#### Location-Based Access Control (Group Management)
- ✅ Create location groups (e.g., counties, districts, regions, departments)
- ✅ Assign users to one or multiple location groups
- ✅ View group members and manage memberships
- ✅ Delete location groups (with impact analysis)
- ✅ Configure multi-location access for users

#### Administrative Feature Delegation
- ✅ Delegate admin capabilities to other roles
- ✅ Configure limitations on delegated features (e.g., which roles can be assigned, which locations can be accessed)
- ✅ View and manage all delegations
- ✅ Revoke delegations at any time

### 1.3 Configuration Interfaces

The system provides two ways to configure the system:

**Web-Based Admin Panel**: 
- URL: `/admin/keycloak`
- Interactive UI for all configuration tasks
- Real-time preview of configuration changes
- User-friendly forms and wizards

**REST API**:
- Base URL: `/api/admin/keycloak/*`
- Programmatic access for automation
- Integration with external systems
- Batch configuration operations

### 1.4 Configuration Persistence

All configurations are stored in **Keycloak**:
- Roles: Stored in Keycloak realm roles
- Users: Stored in Keycloak user database
- Policies: Stored in Keycloak authorization resources
- Permissions: Stored in Keycloak authorization permissions
- Resources: Stored in Keycloak authorization resources
- Field Masking Rules: Stored in Keycloak role attributes
- Resource Attributes: Stored in Keycloak resource attributes

**Benefits**:
- ✅ Changes take effect immediately (no code deployment required)
- ✅ Centralized configuration management
- ✅ Complete audit trail in Keycloak
- ✅ Backup and restore capabilities
- ✅ Version control via Keycloak export/import

---

## 2. Role and Permission Management

### 2.1 Role Management Capabilities

The system supports **unlimited custom roles**. Organizations can:

- **Create Roles**: Create new roles with unique names and descriptions
- **Configure Role Attributes**: Add custom attributes to roles for metadata storage
- **Create Composite Roles**: Build hierarchical roles by combining multiple roles
- **Delete Roles**: Remove roles (with automatic impact analysis)
- **Modify Roles**: Update role configurations dynamically

### 2.2 Permission Configuration System

#### Policy Creation
Organizations can create policies that define access rules:
- **Role-Based Policies**: Grant access based on user roles
- **User-Based Policies**: Grant access to specific users
- **Group-Based Policies**: Grant access based on group membership
- **Time-Based Policies**: Grant access during specific time windows
- **IP-Based Policies**: Grant access from specific IP addresses
- **Custom Policies**: Create complex policies using JavaScript

#### Permission Creation
Organizations can create permissions that link:
- **Resources**: What is being accessed (e.g., "Timesheet Resource", "Case Resource")
- **Scopes**: What actions can be performed (e.g., "create", "read", "update", "delete")
- **Policies**: Who can perform these actions

**Decision Strategies**:
- **AFFIRMATIVE**: Any policy granting access allows access
- **UNANIMOUS**: All policies must grant access
- **CONSENSUS**: Majority of policies must grant access

### 2.3 Permission Examples

Organizations can configure permissions such as:
- **Create Operations**: Control who can create timesheets, cases, documents
- **Read Operations**: Control who can view data (with field-level restrictions)
- **Update Operations**: Control who can modify existing data
- **Delete Operations**: Control who can remove data
- **Submit Operations**: Control who can submit timesheets/cases for approval
- **Approve Operations**: Control who can approve submissions
- **Reject Operations**: Control who can reject submissions
- **Generate Reports**: Control who can generate various report types

### 2.4 Role Assignment Rules

- **Multiple Roles**: Users can have multiple roles simultaneously
- **Role Hierarchy**: Organizations can configure role hierarchies where higher roles inherit permissions
- **Dynamic Assignment**: Roles can be assigned/removed without code changes
- **Location Binding**: Roles can be bound to specific locations (via groups)

---

## 3. Authentication and Authorization System

### 3.1 Authentication Capabilities

**JWT Token-Based Authentication**:
- All API requests require valid JWT tokens
- Tokens validated against Keycloak issuer URI
- Support for token refresh mechanisms
- Configurable token expiration times
- Token format: `Bearer <token>`

**Token Claims Extraction**:
- User roles extracted from JWT token claims
- Location information extracted from JWT token groups
- User attributes extracted from token claims
- Token claims take precedence over request parameters

### 3.2 Authorization System

#### Role-Based Access Control (RBAC)
- User roles extracted from JWT token
- Access decisions based on role permissions
- Support for multiple roles per user
- Configurable role-to-permission mappings

#### Permission-Based Access Control (PBAC)
- Fine-grained permissions for specific operations
- Resource-scope-permission model
- Dynamic permission evaluation at runtime
- Policy-based authorization decisions

#### Method-Level Authorization
- API endpoints protected with `@RequirePermission` annotation
- Resources and scopes defined in Keycloak
- Policies evaluated dynamically by Keycloak
- No code deployment required for permission changes

**Example Configuration Flow**:
1. Organization creates resource: "Timesheet Resource"
2. Organization defines scopes: "create", "read", "update", "delete", "submit", "approve"
3. Organization creates policies: Role-based policies for each role
4. Organization creates permissions: Link resource + scope + policies
5. Backend methods use `@RequirePermission(resource = "Timesheet Resource", scope = "create")`
6. Keycloak evaluates authorization dynamically

### 3.3 Service Account Support

- **System Accounts**: Support for service accounts for automated operations
- **Token Exchange**: System can exchange tokens for different role contexts
- **Scheduled Operations**: Automated jobs use service account credentials

---

## 4. Data Access and Filtering Capabilities

### 4.1 Location-Based Data Access

#### Location Groups System

**Key Concept**: Groups in Keycloak represent locations (e.g., counties, districts, regions, departments) for location-based data access control.

**How It Works**:
1. Organization creates location groups (e.g., "County A", "District B", "Region C")
2. Users are assigned to one or more location groups
3. When users log in, their JWT token contains a `groups` array listing ALL location groups they belong to
4. Backend extracts location information from the JWT token's groups array
5. Database queries are filtered by location, ensuring users only see data for locations they belong to

**Multi-Location Access**:
- Users can belong to multiple location groups
- Users can access data from all assigned locations
- Organizations can configure single-location or multi-location access per role

**Example**:
- User is assigned to groups: `["County A", "County B"]`
- JWT token contains: `groups: ["County A", "County B"]`
- System extracts locations: `["County A", "County B"]`
- Database query: `SELECT * FROM timesheets WHERE location IN ('County A', 'County B')`
- User can access data from County A and County B, but NOT from other counties

#### Location Extraction Process

1. **Organization assigns users to location groups**: Organizations create location groups and assign users to these groups
2. **JWT token contains groups**: When user logs in, Keycloak includes all location groups in the JWT token
3. **Frontend extracts location from groups**: Frontend parses the JWT token and extracts location codes from the groups array
4. **Backend extracts location from groups**: Backend extracts location information from the JWT token's groups array
5. **Data filtering by location**: Backend uses the extracted location codes to filter database queries

### 4.2 Data Filtering System

#### Location-First Approach
1. **Data Filtering Priority**: Location filtering applied BEFORE field masking
2. **Location Extraction**: Location extracted from JWT token's `groups` array (or `realm_access.groups`)
3. **Location Normalization**: Location names normalized to standard format

#### Filtering Rules

**Configurable Location Access**:
- Organizations can configure which roles can access all locations vs. single location
- Organizations can configure location binding requirements per role
- Organizations can configure multi-location access for specific roles

**Filter Enforcement**:
1. **JWT Token Priority**: Location from JWT token takes precedence over request parameter
2. **Request Override**: Organizations can configure whether requested location can expand access
3. **Location Mismatch**: Organizations can configure how location mismatches are handled
4. **Missing Location**: Organizations can configure behavior when location is missing

#### Query Filtering Levels

- **Database Level**: All queries filtered by location at database level
- **Service Level**: Additional filtering in service layer
- **Response Level**: Final filtering before response delivery

### 4.3 Data Visibility Configuration

Organizations can configure data visibility rules:
- **Role-Based Filtering**: Different roles see different data sets
- **Location-Based Filtering**: Users see only data from their assigned locations
- **Ownership-Based Filtering**: Users see only their own data (configurable per role)
- **Combined Filtering**: Multiple filters applied together (location + role + ownership)

---

## 5. Field-Level Data Masking System

### 5.1 Masking Configuration Capabilities

The system provides comprehensive field-level data masking that organizations can configure:

- **Per Role Configuration**: Different masking rules for different roles
- **Per Report Type Configuration**: Different masking rules for different report types
- **Per Field Configuration**: Configure masking type for each individual field
- **Dynamic Masking Patterns**: Customizable masking patterns for partial masking
- **Enable/Disable Masking**: Turn masking on/off for specific roles or report types

### 5.2 Masking Types

The system supports **6 masking types**:

1. **NONE**: No masking applied (full visibility)
2. **HIDDEN**: Field completely hidden (replaced with "***HIDDEN***")
3. **PARTIAL_MASK**: Partial data shown (e.g., XXX-XX-1234) - customizable pattern
4. **HASH_MASK**: Data replaced with cryptographic hash
5. **ANONYMIZE**: Replaced with generic anonymized value
6. **AGGREGATE**: Data shown in aggregated ranges

### 5.3 Access Levels

Organizations can configure three access levels:

- **FULL_ACCESS**: Field visible without masking
- **MASKED_ACCESS**: Field visible with masking applied
- **HIDDEN_ACCESS**: Field completely hidden

### 5.4 Configuration Interface

**Admin Panel Location**: `/admin/keycloak` → Field Masking Tab

**Configuration Capabilities**:
- Select role and report type
- View all available fields
- Configure masking type for each field
- Set custom masking patterns
- Enable/disable field masking
- Save configurations to Keycloak
- Preview masking effects

### 5.5 Masking Application Rules

**Application Order**:
1. Location filtering applied first
2. Role-based data filtering applied second
3. Field masking applied last

**Masking Features**:
- **Consistent Masking**: Same data always masked the same way for same role
- **Performance Optimized**: Masking rules cached for performance
- **Audit Logging**: Masking operations logged for compliance
- **Dynamic Updates**: Rules can be updated without code deployment

---

## 6. Location-Based Access Control

### 6.1 Group Management Capabilities

**Location Groups = Data Access Boundaries**

In the system, **Keycloak groups represent locations** (e.g., counties, districts, regions, departments) for location-based access control to data and resources.

**Key Features**:
- **Groups = Locations**: Each group represents a specific location
- **User Assignment**: Users are assigned to one or more location groups
- **JWT Token Inclusion**: JWT token contains all location groups the user belongs to
- **Data Access Filtering**: Backend extracts location from groups array to filter data access
- **Multi-Location Access**: Users can belong to multiple location groups

### 6.2 Group Operations

Organizations can:

**Create Location Groups**:
- Create groups representing locations (e.g., "County A", "District B", "Region C")
- Configure hierarchical groups (parent-child relationships)
- Add custom attributes to groups

**Manage Group Memberships**:
- Add users to location groups (grants access to that location's data)
- Remove users from location groups (revokes access)
- View all members in a location group
- View all groups a user belongs to

**Delete Location Groups**:
- Remove location groups (users will lose access to that location's data)
- Impact analysis before deletion

### 6.3 Location-Based Data Access Flow

1. Organization creates location groups (e.g., "County A", "County B")
2. Organization assigns users to location groups
3. User logs in → JWT token contains: `groups: ["County A", "County B"]`
4. Backend extracts locations from groups array: `["County A", "County B"]`
5. Database queries filtered: `WHERE location IN ('County A', 'County B')`
6. User can access data from assigned locations only

### 6.4 Location Access Configuration

Organizations can configure:
- **Single Location Access**: Users see data from one location only
- **Multi-Location Access**: Users see data from multiple assigned locations
- **All Location Access**: Some roles can access all locations (configurable)
- **Location Binding Requirements**: Which roles must have location in JWT token

---

## 7. Workflow Management System

### 7.1 Workflow State Management

The system provides configurable workflow state management:

**Supported Workflow Features**:
- **Custom States**: Organizations can define custom workflow states
- **State Transitions**: Configure valid state transitions
- **State Validation**: Validate state transitions before allowing changes
- **Role-Based Transitions**: Different roles can trigger different state transitions
- **Audit Trail**: All state changes logged with timestamp and user

### 7.2 Workflow Configuration Capabilities

Organizations can configure:

**Workflow States**:
- Define custom states (e.g., DRAFT, SUBMITTED, APPROVED, REJECTED, PROCESSED)
- Configure state descriptions and metadata
- Set state priorities

**State Transitions**:
- Define valid transitions between states
- Configure which roles can trigger each transition
- Set transition requirements (e.g., approval required)
- Configure automatic transitions (e.g., auto-approve after X days)

**Workflow Validation**:
- Status validation (only valid transitions allowed)
- Role validation (user must have permission for action)
- Location validation (user location must match data location)
- Ownership validation (user must own the data, if configured)
- Timing validation (actions during business hours, if configured)

### 7.3 Workflow Error Handling

**Invalid Transition**:
- Operation rejected, status unchanged
- Error message returned to user

**Unauthorized Action**:
- Operation rejected with 403 Forbidden
- Access denied message

**Location Mismatch**:
- Operation rejected with 403 Forbidden
- Location mismatch message

---

## 8. Batch Job and Reporting System

### 8.1 Job Management Capabilities

The system provides comprehensive batch job management:

**Job Creation**:
- Create jobs for report generation
- Configure job parameters (report type, date range, filters)
- Set job priority
- Schedule jobs for immediate or delayed execution

**Job Status Tracking**:
- **QUEUED**: Job created and waiting for processing
- **PROCESSING**: Job currently being processed
- **COMPLETED**: Job completed successfully
- **FAILED**: Job failed with error
- **CANCELLED**: Job cancelled by user or system

**Job Visibility**:
- Organizations can configure job visibility rules
- Users see only their own jobs (configurable)
- Admins can see all jobs (configurable)
- Location-based job filtering (configurable)

### 8.2 Report Generation Capabilities

#### Report Types

Organizations can configure various report types:
- **Daily Reports**: Generated daily with configurable schedules
- **Weekly Reports**: Generated weekly with configurable schedules
- **Monthly Reports**: Generated monthly with configurable schedules
- **Quarterly Reports**: Generated quarterly with configurable schedules
- **Yearly Reports**: Generated yearly with configurable schedules
- **Ad-Hoc Reports**: Generated on-demand by users

#### Report Formats

The system supports multiple report formats:
- **PDF**: For email delivery and printing
- **CSV**: For SFTP delivery and data analysis
- **JSON**: For API consumption
- **XML**: For legacy system integration

#### Report Configuration

Organizations can configure:
- **Data Filtering**: Role-based and location-based filtering
- **Field Masking**: Apply masking rules based on user role
- **Pagination**: Large reports paginated (configurable chunk size)
- **Date Ranges**: Configurable default date ranges
- **Custom Fields**: Include/exclude specific fields

### 8.3 Report Delivery Options

**Email Delivery**:
- Configurable per report type
- PDF attachment format
- Role-based default recipients
- Customizable email templates

**SFTP Delivery**:
- Configurable per report type
- CSV file format
- Configurable file paths and naming conventions
- Encrypted file transfer

**API Delivery**:
- JSON format for programmatic access
- Secure API endpoints
- Authentication required

### 8.4 Job Processing Configuration

**Scheduler Settings**:
- Configurable poll interval
- Configurable max jobs per poll
- Configurable worker pool size
- Configurable chunk size

**Processing Priority**:
- Organizations can configure priority levels
- High-priority jobs processed first
- Priority-based queue management

---

## 9. Data Validation System

### 9.1 Validation Capabilities

The system provides comprehensive data validation:

**Required Field Validation**:
- Configure required fields per entity type
- Validate non-blank strings
- Validate date fields
- Validate numeric fields

**Format Validation**:
- String format validation (email, phone, etc.)
- Date format validation (ISO 8601)
- Numeric format validation (precision, scale)

**Business Rule Validation**:
- Duplicate detection (e.g., one timesheet per user per pay period)
- Range validation (e.g., dates, hours)
- Cross-field validation (e.g., end date after start date)

### 9.2 Validation Configuration

Organizations can configure:
- **Required Fields**: Which fields are required for each entity
- **Format Rules**: What formats are acceptable
- **Business Rules**: Custom business validation rules
- **Error Messages**: Custom error messages for validation failures

### 9.3 Validation Error Handling

**Error Response Format**:
```json
{
  "error": "Validation Error",
  "message": "Human-readable error message",
  "timestamp": "2025-01-29T10:30:00Z",
  "path": "/api/endpoint",
  "fieldErrors": {
    "fieldName": "Error message for this field"
  }
}
```

**Error Codes**:
- **400 Bad Request**: Validation errors, invalid input
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server errors

---

## 10. Security Features

### 10.1 Authentication Security

**JWT Token Security**:
- All API endpoints require valid JWT tokens
- Tokens validated against Keycloak issuer URI
- Configurable token expiration times
- Support for token refresh mechanisms
- Invalid tokens return 401 Unauthorized

### 10.2 Authorization Security

**Multi-Layer Authorization**:
- **Role-Based Access**: Access controlled by user role
- **Permission Checks**: Permissions verified for each operation
- **Location Isolation**: Strict location-based data isolation (configurable)
- **Field Masking**: Sensitive fields masked based on role (configurable)
- **Method-Level Authorization**: API endpoints protected with permissions

### 10.3 Data Security

**Data Encryption**:
- **At Rest**: Sensitive data encrypted in database (configurable)
- **In Transit**: All API communications use HTTPS/TLS
- **Key Management**: Keys managed via secure key management systems

**Data Access Controls**:
- Location isolation (users can only access their location data)
- Role-based filtering (data filtered by user role)
- Field-level masking (sensitive fields masked dynamically)
- Audit logging (all data access logged)

### 10.4 Security Best Practices

- **Least Privilege**: Users granted minimum required permissions
- **Location Binding**: Users strictly bound to their locations (configurable)
- **Token Priority**: JWT token claims take precedence over request parameters
- **No Insecure Fallbacks**: System fails explicitly rather than using insecure defaults
- **Comprehensive Audit Trail**: All access attempts and operations logged

---

## 11. Scheduling and Automation

### 11.1 Scheduled Report Generation

The system supports configurable scheduled report generation:

**Scheduling Options**:
- **Daily Reports**: Configurable cron expressions for daily execution
- **Weekly Reports**: Configurable cron expressions for weekly execution
- **Monthly Reports**: Configurable cron expressions for monthly execution
- **Quarterly Reports**: Configurable cron expressions for quarterly execution
- **Yearly Reports**: Configurable cron expressions for yearly execution
- **Custom Schedules**: Any cron expression for custom schedules

**Timezone Support**:
- Configurable timezone per schedule
- Support for multiple timezones

### 11.2 System Scheduler Configuration

**Scheduler Settings**:
- Enable/disable scheduler (configurable)
- Configurable cron expressions
- Configurable timezone
- Configurable default schedules

**Scheduler Rules**:
- Role-based job generation (system generates jobs for each configured role)
- Location-specific jobs (jobs generated for each location, if configured)
- System-generated JWT tokens for scheduled jobs
- Jobs added to queue for asynchronous processing

### 11.3 Job Dependencies

Organizations can configure:
- **Parent-Child Dependencies**: Jobs that depend on other jobs
- **Dependency Conditions**: ON_SUCCESS (only if parent succeeded) or ON_COMPLETION (always)
- **Dependency Chains**: Multiple levels of dependencies

---

## 12. Additional System Features

### 12.1 Data Processing Pipeline

The system uses a **configurable 5-stage data processing pipeline**:

1. **Role Validation**: Validate user role and permissions
2. **Rules Engine**: Apply business rules based on role (configurable)
3. **Query Building**: Build database queries with filters
4. **Data Fetching**: Fetch data from database
5. **Field Masking**: Apply role-based field masking

Organizations can configure:
- Which stages are enabled
- Order of stages
- Custom rules for each stage

### 12.2 Audit and Logging

**Comprehensive Audit Logging**:
- All data access operations logged
- User actions logged with timestamp
- Location access logged
- Field masking operations logged
- Configuration changes logged

**Log Levels**:
- **DEBUG**: Detailed debugging information
- **INFO**: General informational messages
- **WARN**: Warning messages (non-critical issues)
- **ERROR**: Error messages (critical issues)

**Log Retention**:
- Configurable log retention period
- Archive old logs
- Retrieve archived logs on demand

### 12.3 Performance Features

**Pagination**:
- Configurable default page size
- Configurable maximum page size
- Configurable chunk size for batch processing

**Caching**:
- Field masking rules cached (configurable TTL)
- Location mappings cached (configurable TTL)
- Role permissions cached (configurable TTL)
- Configurable cache invalidation strategies

### 12.4 Error Handling

**Error Response Format**:
```json
{
  "error": "Error Type",
  "message": "Human-readable error message",
  "timestamp": "2025-01-29T10:30:00Z",
  "path": "/api/endpoint"
}
```

**Error Codes**:
- **400 Bad Request**: Validation errors, invalid input
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server errors

### 12.5 Compliance Features

**Data Privacy Compliance**:
- **HIPAA Compliance**: Protected health information (PHI) masking (configurable)
- **GDPR Compliance**: Personal data anonymization (configurable)
- **Data Minimization**: Only necessary data collected and displayed
- **Access Controls**: Strict role-based access controls

**Audit Requirements**:
- Access logging (all data access logged)
- Change logging (all data changes logged)
- Configurable log retention (e.g., 7 years)
- Audit reports available on demand

---

## Summary

This system provides a **fully configurable, enterprise-grade platform** for building custom case management and payroll information systems. Key system capabilities:

1. **Fully Configurable System**: All roles, permissions, field masking, and method-level filtering are configurable through Admin Panel or REST API - nothing is hardcoded
2. **Zero-Code Configuration**: Configuration changes take effect immediately without code deployment
3. **Role-Based Access Control**: Comprehensive RBAC with unlimited custom roles (all configurable)
4. **Location-Based Access Control via Groups**: Keycloak groups represent locations for location-based data access. Users are assigned to location groups, and the JWT token contains all location groups the user belongs to. The backend extracts location information from the groups array in the JWT token to filter data access
5. **Location Isolation**: Users strictly bound to their locations (configurable per role) - enforced through group membership
6. **Field-Level Data Masking**: Sensitive data masked based on role, report type, and field (fully configurable)
7. **Method-Level API Authorization**: API endpoint access controlled via configurable Keycloak policies
8. **JWT Token Priority**: Token claims (including groups array for location) take precedence over request parameters
9. **Comprehensive Audit Trail**: All operations logged for compliance and security monitoring
10. **Multi-Format Reporting**: Generate reports in PDF, CSV, JSON, XML formats
11. **Scheduled Automation**: Configurable cron-based scheduled report generation
12. **Dynamic Configuration**: Changes take effect immediately without code deployment

**Configuration Access**:
- **Admin Panel**: `/admin/keycloak` (Web UI)
- **API Endpoints**: `/api/admin/keycloak/*` (REST API)
- **Authentication**: Requires appropriate administrative permissions

**System Architecture**:
- Built on Spring Boot (backend) and Next.js (frontend)
- Integrated with Keycloak for identity and access management
- PostgreSQL database with configurable schemas
- Docker-based deployment for easy setup and scaling

For detailed implementation guides, API documentation, and configuration examples, please refer to the system documentation or contact the support team.

---


