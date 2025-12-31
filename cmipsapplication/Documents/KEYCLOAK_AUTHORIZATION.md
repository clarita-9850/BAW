# CMIPS Keycloak Authorization System Documentation

## Overview

The CMIPS application implements a configurable, zero-code authorization system using Keycloak's Authorization Services. This system provides:

1. **Resource-Based Access Control** - Define who can access which resources
2. **Scope-Based Permissions** - Define what actions users can perform on resources
3. **Field-Level Authorization** - Control which fields different roles can read/write
4. **Declarative Configuration** - All authorization rules are configured in Keycloak, not hardcoded

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CMIPS Authorization Flow                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────┐    ┌──────────────────┐    ┌─────────────────────────┐   │
│  │ Frontend │───>│ Spring Boot API  │───>│ Keycloak Authorization  │   │
│  │ (Next.js)│    │ + @RequirePermis │    │ Services                │   │
│  └──────────┘    └──────────────────┘    └─────────────────────────┘   │
│       │                   │                          │                  │
│       │                   │                          │                  │
│       ▼                   ▼                          ▼                  │
│  JWT Token          AuthorizationAspect       Policy Evaluation         │
│  (roles, groups)    FieldLevelAuthzService    Resource Attributes       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. `@RequirePermission` Annotation
**File:** `backend/src/main/java/com/cmips/annotation/RequirePermission.java`

Declarative annotation for method-level authorization:

```java
@RequirePermission(resource = "Timesheet Resource", scope = "create",
                   message = "You don't have permission to create timesheets")
public ResponseEntity<?> createTimesheet(...)
```

**Parameters:**
- `resource` - The Keycloak resource name (must match exactly)
- `scope` - The action/scope being performed (create, read, update, delete, submit, approve, etc.)
- `message` - Custom error message when access is denied

### 2. `AuthorizationAspect`
**File:** `backend/src/main/java/com/cmips/aspect/AuthorizationAspect.java`

AOP aspect that intercepts `@RequirePermission` annotated methods and evaluates permissions via Keycloak's policy engine.

### 3. `FieldLevelAuthorizationService`
**File:** `backend/src/main/java/com/cmips/service/FieldLevelAuthorizationService.java`

Provides automatic field filtering based on Keycloak resource attributes.

**Key Methods:**
- `filterFields(Map data, String resourceName, String scope)` - Filters response data
- `getAllowedFields(String resourceName, String scope)` - Gets accessible fields for current user
- `getAllowedActions(String resourceName)` - Gets allowed actions for current user
- `canPerformAction(String resourceName, String action)` - Checks if user can perform action

### 4. `CaseManagementKeycloakInitializer`
**File:** `backend/src/main/java/com/cmips/config/CaseManagementKeycloakInitializer.java`

Programmatically creates Keycloak resources, roles, and permissions at startup when `cmips.keycloak.init-resources=true`.

---

## Resource Configurations

### 1. Timesheet Resource

**Keycloak Resource Name:** `Timesheet Resource`
**Type:** `Timesheet`
**URIs:** `/api/timesheets/*`, `/api/timesheets/*/submit`, `/api/timesheets/*/approve`, `/api/timesheets/*/reject`

#### Scopes (Actions)

| Scope | Description | Allowed Roles |
|-------|-------------|---------------|
| `create` | Create new timesheet | PROVIDER |
| `read` | View timesheets | PROVIDER, RECIPIENT, CASE_WORKER |
| `update` | Modify timesheet | PROVIDER (own), CASE_WORKER |
| `delete` | Delete timesheet | CASE_WORKER |
| `submit` | Submit for approval | PROVIDER |
| `approve` | Approve timesheet | CASE_WORKER |
| `reject` | Reject timesheet | CASE_WORKER |

#### Field-Level Scopes

| Scope | Description |
|-------|-------------|
| `timesheet:employee_id` | Access to employeeId field |
| `timesheet:employee_name` | Access to employeeName field |
| `timesheet:department` | Access to department field |
| `timesheet:location` | Access to location field |
| `timesheet:regular_hours` | Access to regularHours field |
| `timesheet:overtime_hours` | Access to overtimeHours field |
| `timesheet:holiday_hours` | Access to holidayHours field |
| `timesheet:sick_hours` | Access to sickHours field |
| `timesheet:vacation_hours` | Access to vacationHours field |
| `timesheet:comments` | Access to comments field |
| `timesheet:supervisor_comments` | Access to supervisorComments field |
| `timesheet:approval_info` | Access to approvedBy, approvedAt fields |

#### Resource Attributes (Field Permissions)

```json
{
  "provider_read_fields": ["employeeName", "location", "overtimeHours", "regularHours",
                          "payPeriodStart", "payPeriodEnd", "totalHours", "status", "comments"],
  "provider_write_fields": ["employeeId", "employeeName", "department", "location",
                           "payPeriodStart", "payPeriodEnd", "regularHours", "overtimeHours",
                           "holidayHours", "sickHours", "vacationHours", "comments"],
  "provider_actions": ["create", "edit", "submit"],

  "recipient_read_fields": ["employeeName", "payPeriodStart", "payPeriodEnd", "totalHours",
                           "status", "comments"],
  "recipient_actions": ["approve", "reject"],

  "case_worker_read_fields": ["id", "userId", "employeeId", "employeeName", "department",
                             "location", "payPeriodStart", "payPeriodEnd", "regularHours",
                             "overtimeHours", "holidayHours", "sickHours", "vacationHours",
                             "totalHours", "status", "comments", "supervisorComments",
                             "approvedBy", "approvedAt", "submittedBy", "submittedAt",
                             "createdAt", "updatedAt"],
  "case_worker_write_fields": ["supervisorComments", "status"],
  "case_worker_actions": ["read", "approve", "reject", "delete"]
}
```

#### Controller Implementation

**File:** `backend/src/main/java/com/cmips/controller/TimesheetController.java`

```java
@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    @Autowired
    private FieldLevelAuthorizationService fieldLevelAuthzService;

    @PostMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "create")
    public ResponseEntity<?> createTimesheet(@RequestBody Map<String, Object> requestData) {
        // 1. Filter incoming request fields based on user's write permissions
        Map<String, Object> filteredRequest = fieldLevelAuthzService.filterFields(
            requestData, "Timesheet Resource", "create");

        // 2. Process the filtered request
        TimesheetResponse response = timesheetService.createTimesheet(userId, filteredRequest);

        // 3. Filter response fields based on user's read permissions
        Map<String, Object> responseMap = convertToMap(response);
        Map<String, Object> filteredResponse = fieldLevelAuthzService.filterFields(
            responseMap, "Timesheet Resource", "read");

        return ResponseEntity.ok(filteredResponse);
    }

    @GetMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "read")
    public ResponseEntity<?> getTimesheets(Pageable pageable) {
        // Fetch timesheets based on user role
        Page<TimesheetResponse> timesheets = getTimesheetsForUserRole();

        // Convert to maps for field filtering
        List<Map<String, Object>> timesheetMaps = timesheets.getContent().stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());

        // Apply field-level filtering
        List<Map<String, Object>> filtered = fieldLevelAuthzService.filterFields(
            timesheetMaps, "Timesheet Resource", "read");

        // Include allowed actions in response
        Set<String> allowedActions = fieldLevelAuthzService.getAllowedActions("Timesheet Resource");

        return ResponseEntity.ok(Map.of(
            "content", filtered,
            "allowedActions", allowedActions
        ));
    }

    @PostMapping("/{id}/approve")
    @RequirePermission(resource = "Timesheet Resource", scope = "approve")
    public ResponseEntity<?> approveTimesheet(@PathVariable Long id) {
        // Only users with 'approve' scope can execute this
        return timesheetService.approveTimesheet(id, userId);
    }
}
```

#### Entity Definition

**File:** `backend/src/main/java/com/cmips/entity/Timesheet.java`

```java
@Entity
@Table(name = "timesheets")
public class Timesheet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String employeeId;
    private String employeeName;
    private String department;
    private String location;

    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;

    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal holidayHours;
    private BigDecimal sickHours;
    private BigDecimal vacationHours;
    private BigDecimal totalHours;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    private String comments;
    private String supervisorComments;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private String submittedBy;
    private LocalDateTime submittedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

### 2. Case Resource

**Keycloak Resource Name:** `Case Resource`
**Type:** `urn:cmips:resources:case-resource`

#### Scopes

| Scope | Description | Allowed Roles |
|-------|-------------|---------------|
| `view` | View case information | case-viewer, case-supervisor, state-admin |
| `create` | Create new case | case-creator, case-supervisor |
| `edit` | Edit case information | case-editor, case-supervisor |
| `approve` | Approve case | case-approver, case-supervisor |
| `deny` | Deny case | case-approver, case-supervisor |
| `terminate` | Terminate case | case-supervisor |
| `assign` | Assign case to worker | case-supervisor |
| `transfer` | Inter-county transfer | case-transfer-manager |

#### Field Permissions by Role

```json
{
  "fields.basic": ["id", "caseNumber", "caseStatus", "caseType", "countyCode", "caseOwnerId"],

  "fields.case-viewer": ["id", "caseNumber", "caseStatus", "caseType", "countyCode",
                         "caseOwnerId", "recipientId", "cin", "referralDate",
                         "applicationDate", "eligibilityDate", "authorizedHoursMonthly",
                         "authorizedHoursWeekly", "assessmentType"],

  "fields.case-supervisor": ["id", "caseNumber", "caseStatus", "caseType", "countyCode",
                            "caseOwnerId", "recipientId", "cin", "referralDate",
                            "applicationDate", "eligibilityDate", "authorizationStartDate",
                            "authorizationEndDate", "authorizedHoursMonthly",
                            "authorizedHoursWeekly", "assessmentType", "healthCareCertStatus",
                            "healthCareCertDueDate", "mediCalAidCode", "fundingSource",
                            "shareOfCost", "waiverProgram", "denialReason", "terminationReason"],

  "fields.state-admin": ["*"]
}
```

---

### 3. Case Notes Resource

**Keycloak Resource Name:** `Case Notes Resource`
**Type:** `urn:cmips:resources:case-notes-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View case notes |
| `create` | Create new notes |
| `edit` | Edit existing notes |
| `delete` | Delete notes |

#### Field Permissions

```json
{
  "fields.basic": ["id", "caseId", "noteType", "subject", "createdDate", "createdBy"],

  "fields.case-viewer": ["id", "caseId", "noteType", "subject", "content",
                        "createdDate", "createdBy", "updatedDate", "updatedBy"]
}
```

---

### 4. Case Contacts Resource

**Keycloak Resource Name:** `Case Contacts Resource`
**Type:** `urn:cmips:resources:case-contacts-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View case contacts |
| `create` | Create new contact |
| `edit` | Edit contact information |
| `delete` | Delete contact |

---

### 5. Recipient Resource

**Keycloak Resource Name:** `Recipient Resource`
**Type:** `urn:cmips:resources:recipient-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View recipient information |
| `create` | Create new recipient |
| `edit` | Edit recipient information |

#### Field Permissions (Protecting PII)

```json
{
  "fields.basic": ["id", "personType", "countyCode", "firstName", "lastName"],

  "fields.recipient-viewer": ["id", "personType", "countyCode", "firstName", "lastName",
                              "middleName", "dateOfBirth", "gender", "cin", "residenceCity",
                              "residenceState", "primaryLanguage", "interpreterNeeded"],

  "fields.recipient-editor": ["id", "personType", "countyCode", "firstName", "lastName",
                              "middleName", "dateOfBirth", "gender", "cin", "ssn",
                              "ssnVerificationStatus", "residenceAddress", "residenceCity",
                              "residenceState", "residenceZip", "mailingAddress",
                              "mailingCity", "mailingState", "mailingZip", "phoneNumber",
                              "alternatePhone", "email", "primaryLanguage",
                              "interpreterNeeded", "espRegistered"],

  "fields.state-admin": ["*"]
}
```

**Note:** SSN access requires `recipient-editor` role or higher - this protects sensitive PII.

---

### 6. Provider Resource

**Keycloak Resource Name:** `Provider Resource`
**Type:** `urn:cmips:resources:provider-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View provider information |
| `create` | Create/enroll new provider |
| `edit` | Edit provider information |
| `approve` | Approve provider enrollment |
| `enroll` | Complete enrollment process |
| `reinstate` | Reinstate terminated provider |

#### Field Permissions

```json
{
  "fields.basic": ["id", "providerNumber", "firstName", "lastName", "providerStatus",
                   "dojCountyCode", "eligible"],

  "fields.provider-viewer": ["id", "providerNumber", "firstName", "lastName", "middleName",
                             "providerStatus", "dojCountyCode", "eligible",
                             "orientationCompleted", "enrollmentDate", "terminationDate"],

  "fields.provider-editor": ["id", "providerNumber", "firstName", "lastName", "middleName",
                             "ssn", "ssnVerificationStatus", "dateOfBirth", "providerStatus",
                             "dojCountyCode", "eligible", "ineligibleReason", "soc426Signed",
                             "orientationCompleted", "soc846Signed", "workweekAgreementSigned",
                             "enrollmentDate", "terminationDate", "sickLeaveEligible",
                             "sickLeaveHoursAccrued", "lastSickLeaveAccrualDate"]
}
```

---

### 7. Provider CORI Resource

**Keycloak Resource Name:** `Provider CORI Resource`
**Type:** `urn:cmips:resources:provider-cori-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View CORI status |
| `create` | Submit CORI check |
| `edit` | Update CORI information |

#### Field Permissions (Protecting Criminal Background Information)

```json
{
  "fields.basic": ["id", "providerId", "coriStatus", "coriTier", "coriExpirationDate"],

  "fields.cori-viewer": ["id", "providerId", "coriStatus", "coriTier", "coriSubmissionDate",
                         "coriExpirationDate", "hasRecipientWaiver"],

  "fields.cori-manager": ["id", "providerId", "coriStatus", "coriTier", "coriSubmissionDate",
                          "coriExpirationDate", "coriClearanceDate", "coriDenialDate",
                          "coriDenialReason", "hasRecipientWaiver", "recipientWaiverId",
                          "recipientWaiverEndDate", "hasGeneralException",
                          "generalExceptionBeginDate", "generalExceptionEndDate",
                          "generalExceptionNotes"]
}
```

---

### 8. Provider-Recipient Relationship Resource

**Keycloak Resource Name:** `Provider-Recipient Resource`
**Type:** `Provider-Recipient`

#### Scopes

| Scope | Description |
|-------|-------------|
| `read` | View relationships |
| `create` | Create new relationship |
| `update` | Update relationship |
| `delete` | Terminate relationship |

#### Controller Implementation

**File:** `backend/src/main/java/com/cmips/controller/ProviderRecipientController.java`

```java
@RestController
@RequestMapping("/api/provider-recipient")
public class ProviderRecipientController {

    @GetMapping("/my-recipients")
    public ResponseEntity<List<ProviderRecipientRelationship>> getMyRecipients() {
        String providerId = getCurrentUserId();
        return ResponseEntity.ok(
            providerRecipientRepository.findByProviderIdAndStatus(providerId, "ACTIVE"));
    }

    @GetMapping("/my-providers")
    public ResponseEntity<List<ProviderRecipientRelationship>> getMyProviders() {
        String recipientId = getCurrentUserId();
        return ResponseEntity.ok(
            providerRecipientRepository.findByRecipientIdAndStatus(recipientId, "ACTIVE"));
    }
}
```

---

### 9. EVV (Electronic Visit Verification) Resource

**Keycloak Resource Name:** `EVV Resource`
**Type:** `EVV`

#### Scopes

| Scope | Description |
|-------|-------------|
| `read` | View EVV records |
| `create` | Create check-in/check-out |
| `update` | Modify EVV record |
| `delete` | Delete EVV record |

#### Controller Implementation

**File:** `backend/src/main/java/com/cmips/controller/EVVController.java`

```java
@RestController
@RequestMapping("/evv")
public class EVVController {

    @PostMapping("/check-in")
    public ResponseEntity<EVVRecord> checkIn(@RequestBody Map<String, Object> request) {
        String providerId = getCurrentUserId();
        // Creates EVV check-in with GPS coordinates
        return ResponseEntity.ok(evvService.checkIn(providerId, recipientId,
                                                     serviceType, latitude, longitude));
    }

    @PostMapping("/check-out/{evvId}")
    public ResponseEntity<EVVRecord> checkOut(@PathVariable Long evvId,
                                              @RequestBody Map<String, Object> request) {
        // Completes EVV visit with check-out coordinates
        return ResponseEntity.ok(evvService.checkOut(evvId, latitude, longitude));
    }
}
```

---

### 10. Service Eligibility Resource

**Keycloak Resource Name:** `Service Eligibility Resource`
**Type:** `urn:cmips:resources:service-eligibility-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View eligibility assessments |
| `create` | Create new assessment |
| `edit` | Edit assessment details |
| `approve` | Approve assessment |
| `calculate` | Calculate service hours |

#### Field Permissions

```json
{
  "fields.basic": ["id", "caseId", "assessmentType", "assessmentStatus", "totalAssessedNeed"],

  "fields.eligibility-viewer": ["id", "caseId", "assessmentType", "assessmentStatus",
                                "totalAssessedNeed", "domesticHours", "personalCareHours",
                                "paramedicalHours", "protectiveSupervisionHours",
                                "transportationHours"],

  "fields.eligibility-assessor": ["id", "caseId", "assessmentType", "assessmentStatus",
                                  "assessmentDate", "homeVisitDate", "reassessmentDueDate",
                                  "totalAssessedNeed", "domesticHours", "domesticHtgIndicator",
                                  "personalCareHours", "personalCareHtgIndicator",
                                  "paramedicalHours", "paramedicalHtgIndicator",
                                  "protectiveSupervisionHours", "protectiveSupervisionHtgIndicator",
                                  "transportationHours", "transportationHtgIndicator",
                                  "mealPrepHours", "relatedServicesHours", "teachingDemoHours",
                                  "mobilityRank", "houseworkRank", "mealPrepRank", "eatingRank",
                                  "bathingRank", "dressingRank", "bowelBladderRank",
                                  "mentalFunctionRank", "memoryRank", "orientationRank",
                                  "judgmentRank", "shareOfCostAmount", "shareOfCostMet",
                                  "waiverProgram", "advancePayEligible"]
}
```

#### Controller Implementation

**File:** `backend/src/main/java/com/cmips/controller/ServiceEligibilityController.java`

```java
@RestController
@RequestMapping("/api/eligibility")
public class ServiceEligibilityController {

    @GetMapping("/case/{caseId}")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "view")
    public ResponseEntity<List<ServiceEligibilityEntity>> getAssessmentsForCase(
            @PathVariable Long caseId) {
        return ResponseEntity.ok(eligibilityRepository.findByCaseId(caseId));
    }

    @PostMapping
    @RequirePermission(resource = "Service Eligibility Resource", scope = "create")
    public ResponseEntity<ServiceEligibilityEntity> createAssessment(
            @RequestBody CreateAssessmentRequest request) {
        return ResponseEntity.ok(eligibilityService.createAssessment(
            request.getCaseId(), request.getAssessmentType(), userId));
    }

    @PutMapping("/{id}/approve")
    @RequirePermission(resource = "Service Eligibility Resource", scope = "approve")
    public ResponseEntity<ServiceEligibilityEntity> approveAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(eligibilityService.approveAssessment(id, userId));
    }
}
```

---

### 11. Health Care Certification Resource

**Keycloak Resource Name:** `Health Care Certification Resource`
**Type:** `urn:cmips:resources:health-care-certification-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View health certifications |
| `create` | Create new certification (SOC 873) |
| `edit` | Edit certification details |
| `approve` | Approve exceptions/extensions |

---

### 12. Overtime Violation Resource

**Keycloak Resource Name:** `Overtime Violation Resource`
**Type:** `urn:cmips:resources:overtime-violation-resource`

#### Scopes

| Scope | Description |
|-------|-------------|
| `view` | View overtime violations |
| `create` | Create violation record |
| `edit` | Update violation |
| `review` | First-level review |
| `supervisor-review` | Supervisor review |

---

## Keycloak Policies

### Role-Based Policies

| Policy Name | Description | Associated Roles |
|-------------|-------------|------------------|
| `Case Worker Policy` | Grants access to case worker functions | CASE_WORKER |
| `Provider Policy` | Grants access to provider functions | PROVIDER |
| `Recipient Policy` | Grants access to recipient functions | RECIPIENT |
| `Supervisor Policy` | Grants elevated access | SUPERVISOR |

### Aggregate Policies

| Policy Name | Description | Combines |
|-------------|-------------|----------|
| `Provider or Case Worker Policy` | Either role grants access | Provider Policy OR Case Worker Policy |
| `Recipient or Case Worker Policy` | Either role grants access | Recipient Policy OR Case Worker Policy |

### Resource-Specific Permissions

| Permission Name | Resource | Scopes | Policy |
|-----------------|----------|--------|--------|
| `CASE_WORKER Timesheet Access` | Timesheet Resource | read, approve, reject | Case Worker Policy |
| `PROVIDER Timesheet Access` | Timesheet Resource | create, read, submit | Provider Policy |
| `CASE_WORKER Case Access` | Case Resource | view, create, edit | Case Worker Policy |

---

## How to Add a New Resource

### Step 1: Define the Resource in Keycloak

Using Keycloak Admin Console or programmatically via `CaseManagementKeycloakInitializer`:

```java
createResource("New Resource Name", "Description",
    Arrays.asList("view", "create", "edit", "delete"),
    getFieldPermissions());
```

### Step 2: Define Field Permissions

```java
private Map<String, List<String>> getFieldPermissions() {
    Map<String, List<String>> permissions = new HashMap<>();

    permissions.put("fields.basic", Arrays.asList("id", "name", "status"));
    permissions.put("fields.viewer", Arrays.asList("id", "name", "status", "details"));
    permissions.put("fields.editor", Arrays.asList("*"));

    return permissions;
}
```

### Step 3: Create Controller with Annotations

```java
@RestController
@RequestMapping("/api/new-resource")
public class NewResourceController {

    @Autowired
    private FieldLevelAuthorizationService fieldAuthService;

    @GetMapping
    @RequirePermission(resource = "New Resource Name", scope = "view")
    public ResponseEntity<?> getAll() {
        List<Map<String, Object>> data = repository.findAll();
        // Apply field filtering
        return ResponseEntity.ok(fieldAuthService.filterFields(data, "New Resource Name", "read"));
    }

    @PostMapping
    @RequirePermission(resource = "New Resource Name", scope = "create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        // Filter request fields
        Map<String, Object> filtered = fieldAuthService.filterFields(
            request, "New Resource Name", "write");
        // Process filtered data
        return ResponseEntity.ok(service.create(filtered));
    }
}
```

### Step 4: Create Keycloak Policies

Using Admin API or Console:
1. Create a role-based policy for each role that needs access
2. Create permissions linking the resource scopes to policies
3. Configure resource attributes for field-level permissions

---

## Testing Authorization

### Test Permission Evaluation

```bash
# Get admin token
ADMIN_TOKEN=$(curl -s -X POST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
  -d "username=admin&password=admin123&grant_type=password&client_id=admin-cli" | jq -r '.access_token')

# Get user token
USER_TOKEN=$(curl -s -X POST "http://localhost:8080/realms/cmips/protocol/openid-connect/token" \
  -d "username=provider1&password=password123&grant_type=password&client_id=cmips-frontend" \
  | jq -r '.access_token')

# Test API endpoint
curl -s "http://localhost:8081/api/timesheets" \
  -H "Authorization: Bearer $USER_TOKEN" | jq
```

### Verify Field Filtering

The response will only contain fields the user is authorized to see based on their role and the resource attributes configured in Keycloak.

---

## Configuration Properties

```yaml
# application.yml
keycloak:
  auth-server-url: http://localhost:8080/
  realm: cmips
  resource: cmips-backend
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}

# Enable resource initialization at startup
cmips:
  keycloak:
    init-resources: true  # Set to false in production
```

---

## Benefits of This Approach

1. **Zero-Code Authorization** - Add new permissions without code changes
2. **Centralized Management** - All authorization rules in Keycloak
3. **Audit Trail** - Keycloak logs all authorization decisions
4. **Fine-Grained Control** - Field-level and action-level permissions
5. **Role Hierarchy** - Support for complex role relationships
6. **Dynamic Updates** - Change permissions without redeploying

---

## Related Files

- `backend/src/main/java/com/cmips/annotation/RequirePermission.java`
- `backend/src/main/java/com/cmips/aspect/AuthorizationAspect.java`
- `backend/src/main/java/com/cmips/service/FieldLevelAuthorizationService.java`
- `backend/src/main/java/com/cmips/service/KeycloakPolicyEvaluationService.java`
- `backend/src/main/java/com/cmips/service/KeycloakAuthorizationService.java`
- `backend/src/main/java/com/cmips/config/CaseManagementKeycloakInitializer.java`
