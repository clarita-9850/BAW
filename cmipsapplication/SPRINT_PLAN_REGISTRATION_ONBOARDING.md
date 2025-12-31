# CMIPS Registration & Onboarding Module - Sprint Plan

## Document Information
| Field | Value |
|-------|-------|
| **Document Title** | Sprint Plan - Registration & Onboarding Module |
| **Version** | 1.0 |
| **Created Date** | December 17, 2025 |
| **Last Updated** | December 17, 2025 |
| **Author** | CMIPS Development Team |
| **Status** | Draft |

---

## Executive Summary

This document outlines the sprint plan for implementing the **Registration and Onboarding Module** for the CMIPS (Case Management Information & Payroll System) application. Based on the DSD (Detailed System Design) documentation from the existing IHSS system, this plan covers the complete lifecycle of Recipient and Provider registration, from initial county intake through self-service ESP registration.

### Scope
- **Recipient Registration**: Referral creation, application processing, CIN clearance, case creation
- **Provider Registration**: Enrollment, background checks (CORI), eligibility determination
- **Self-Service Portal**: ESP registration for both Recipients and Providers
- **Integration**: Keycloak authentication, Kafka events, existing CMIPS entities

### Total Estimated Sprints: 8 Sprints (2-week sprints = 16 weeks)

---

## Current System Analysis

### Existing Infrastructure (Leverage)
| Component | Status | Notes |
|-----------|--------|-------|
| Keycloak Integration | ✅ Exists | `KeycloakAdminService` for user creation |
| Provider Entity | ✅ Exists | 1,358 lines - comprehensive enrollment tracking |
| Recipient Entity | ✅ Exists | 1,128 lines - referral and person types |
| Case Entity | ✅ Exists | 538 lines - status transitions |
| Kafka Events | ✅ Exists | `BaseEvent` framework |
| Field-Level Auth | ✅ Exists | `FieldLevelAuthorizationService` |
| Security Config | ✅ Exists | JWT + Keycloak RBAC |

### Missing Components (To Build)
| Component | Priority | Sprint Target |
|-----------|----------|---------------|
| Referral Management | High | Sprint 1 |
| Application Processing | High | Sprint 2 |
| CIN Clearance Service | High | Sprint 2 |
| Provider Enrollment Workflow | High | Sprint 3-4 |
| CORI Background Check Module | High | Sprint 4 |
| ESP Self-Service Registration | High | Sprint 5-6 |
| Provider Assignment (Self-Service) | Medium | Sprint 7 |
| Notifications & Communications | Medium | Sprint 7-8 |

---

## Sprint Breakdown

---

## Sprint 1: Recipient Referral Management
**Duration**: 2 weeks
**Theme**: Initial Contact & Referral Creation

### Goals
- Implement referral creation workflow
- Build person search to prevent duplicates
- Create referral status management
- Add person notes functionality

### User Stories

#### US-1.1: Person Search (Duplicate Prevention)
**As a** county intake worker
**I want to** search for existing persons before creating new records
**So that** I can prevent duplicate entries in the system

**Acceptance Criteria**:
- [ ] Search by SSN (full or last 4 digits)
- [ ] Search by Name (partial match with wildcard)
- [ ] Search by Date of Birth
- [ ] Search by CIN
- [ ] Search by County
- [ ] Results show: name, SSN (masked), DOB, person type, county
- [ ] Link to Person Home from search results

#### US-1.2: Create Referral
**As a** county intake worker
**I want to** create a referral for someone inquiring about IHSS
**So that** I can track initial contacts

**Acceptance Criteria**:
- [ ] Capture: Name, DOB, SSN (optional), phone, address, email
- [ ] Select referral source (Self, Family, Hospital, Medical Provider, etc.)
- [ ] Record referral date automatically
- [ ] Assign to intake worker
- [ ] Set initial status: OPEN_REFERRAL
- [ ] Create Person record if not exists

#### US-1.3: Manage Referral Status
**As a** county intake worker
**I want to** update referral status
**So that** I can track referral progression

**Acceptance Criteria**:
- [ ] Status options: OPEN_REFERRAL, CLOSED_REFERRAL
- [ ] Closed reasons: Not Interested, Referred Elsewhere, Converted to Application
- [ ] Record closed date when closing
- [ ] Prevent editing closed referrals

#### US-1.4: Person Notes
**As a** county worker
**I want to** add notes to a person record
**So that** I can document contacts and events

**Acceptance Criteria**:
- [ ] Add note with date, contact method, content
- [ ] View chronological history of notes
- [ ] Edit notes within 24 hours
- [ ] Cannot delete notes (audit requirement)

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-1.1 | Create `ReferralEntity` with status, source, dates | 4h |
| BE-1.2 | Create `PersonNoteEntity` with audit fields | 2h |
| BE-1.3 | Create `ReferralRepository` with search queries | 4h |
| BE-1.4 | Create `PersonNoteRepository` | 2h |
| BE-1.5 | Create `ReferralService` with business logic | 8h |
| BE-1.6 | Create `PersonSearchService` with duplicate detection | 6h |
| BE-1.7 | Create `ReferralController` REST endpoints | 4h |
| BE-1.8 | Create `PersonNoteController` REST endpoints | 2h |
| BE-1.9 | Add Keycloak resources: "Referral Resource" | 4h |
| BE-1.10 | Write unit tests for referral workflow | 6h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-1.1 | Person Search page with filters | 8h |
| FE-1.2 | Create Referral form | 6h |
| FE-1.3 | Referral List/Management page | 6h |
| FE-1.4 | Person Notes component | 4h |
| FE-1.5 | Referral status update modal | 2h |

### New Entities

```java
// ReferralEntity.java
@Entity
@Table(name = "referrals")
public class ReferralEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private PersonEntity person;

    @Enumerated(EnumType.STRING)
    private ReferralStatus status; // OPEN, CLOSED

    @Enumerated(EnumType.STRING)
    private ReferralSource source; // SELF, FAMILY, HOSPITAL, MEDICAL_PROVIDER, etc.

    private LocalDate referralDate;
    private LocalDate closedDate;

    @Enumerated(EnumType.STRING)
    private ReferralClosedReason closedReason;

    private String assignedWorkerId;
    private String countyCode;
    private String notes;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/persons/search` | Search persons |
| POST | `/api/referrals` | Create referral |
| GET | `/api/referrals` | List referrals with filters |
| GET | `/api/referrals/{id}` | Get referral details |
| PUT | `/api/referrals/{id}/status` | Update referral status |
| POST | `/api/persons/{id}/notes` | Add person note |
| GET | `/api/persons/{id}/notes` | Get person notes |

### Definition of Done
- [ ] All acceptance criteria met
- [ ] Unit tests passing (>80% coverage)
- [ ] API documentation updated
- [ ] Code reviewed and merged
- [ ] Deployed to dev environment

---

## Sprint 2: Application Processing & CIN Clearance
**Duration**: 2 weeks
**Theme**: Formal Application & System Integration

### Goals
- Implement application creation from referral
- Build CIN clearance simulation
- Add MEDS integration stub
- Track 45-day processing timeline

### User Stories

#### US-2.1: Create Application
**As a** county intake worker
**I want to** create an application from a referral
**So that** I can formally process IHSS requests

**Acceptance Criteria**:
- [ ] Convert referral to application (person type: APPLICANT)
- [ ] Capture required information:
  - Personal: Name, DOB, SSN, gender, language
  - Contact: Address, phone, email, emergency contact
  - Eligibility: Citizenship, residency
  - Medical: Disability info (brief)
  - Financial: Income info (brief)
- [ ] Set application date (starts 45-day clock)
- [ ] Generate application number
- [ ] Assign to eligibility worker

#### US-2.2: 45-Day Timeline Tracking
**As a** county worker
**I want to** see how many days remain in the 45-day processing window
**So that** I can prioritize applications

**Acceptance Criteria**:
- [ ] Display days remaining from application date
- [ ] Visual indicator: Green (>15 days), Yellow (8-15), Red (<8)
- [ ] Alerts when approaching deadline
- [ ] Extension capability with reason

#### US-2.3: CIN Clearance
**As a** county worker
**I want to** perform CIN clearance for applicants
**So that** I can get or verify their Client Index Number

**Acceptance Criteria**:
- [ ] Search SCI by SSN, name, DOB (simulated)
- [ ] Display match results: Exact, Possible, No Match
- [ ] Select existing CIN or request new
- [ ] Handle data mismatches
- [ ] Store CIN on recipient record

#### US-2.4: MEDS Eligibility Check
**As a** county worker
**I want to** verify Medi-Cal eligibility
**So that** I can confirm IHSS prerequisite

**Acceptance Criteria**:
- [ ] Query MEDS by CIN (simulated)
- [ ] Display: Aid code, status, effective dates, share of cost
- [ ] Block case creation if no active Medi-Cal
- [ ] Store Medi-Cal info on case

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-2.1 | Create `ApplicationEntity` with timeline tracking | 6h |
| BE-2.2 | Create `CINService` for clearance simulation | 8h |
| BE-2.3 | Create `MEDSService` for eligibility simulation | 6h |
| BE-2.4 | Create `ApplicationService` with 45-day logic | 8h |
| BE-2.5 | Create `ApplicationController` REST endpoints | 4h |
| BE-2.6 | Add timeline alert scheduler | 4h |
| BE-2.7 | Update `RecipientEntity` for CIN storage | 2h |
| BE-2.8 | Write unit tests | 6h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-2.1 | Application form (multi-step wizard) | 12h |
| FE-2.2 | 45-day timeline dashboard widget | 4h |
| FE-2.3 | CIN clearance modal | 6h |
| FE-2.4 | MEDS verification display | 4h |
| FE-2.5 | Application list with timeline indicators | 6h |

### New Entities

```java
// ApplicationEntity.java
@Entity
@Table(name = "applications")
public class ApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String applicationNumber;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private RecipientEntity recipient;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status; // PENDING, APPROVED, DENIED, WITHDRAWN

    private LocalDate applicationDate;
    private LocalDate decisionDate;
    private LocalDate deadlineDate; // applicationDate + 45 days

    private LocalDate extensionDate;
    private String extensionReason;

    private String assignedWorkerId;
    private String countyCode;

    // CIN Clearance
    private String cin;
    private String cinClearanceStatus; // CLEARED, PENDING, MISMATCH
    private LocalDateTime cinClearanceDate;

    // MEDS
    private String mediCalAidCode;
    private String mediCalStatus;
    private LocalDate mediCalEffectiveDate;

    // Decision
    private String denialReason;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/applications` | Create application |
| GET | `/api/applications` | List applications |
| GET | `/api/applications/{id}` | Get application details |
| PUT | `/api/applications/{id}` | Update application |
| POST | `/api/applications/{id}/cin-clearance` | Perform CIN clearance |
| POST | `/api/applications/{id}/meds-check` | Check MEDS eligibility |
| POST | `/api/applications/{id}/extend` | Extend 45-day deadline |
| POST | `/api/applications/{id}/approve` | Approve application |
| POST | `/api/applications/{id}/deny` | Deny application |

---

## Sprint 3: Case Creation & Provider Enrollment Start
**Duration**: 2 weeks
**Theme**: Case Establishment & Provider Intake

### Goals
- Implement case creation from approved application
- Start provider enrollment workflow
- Create SOC 426 form handling
- Build provider search

### User Stories

#### US-3.1: Create Case from Approved Application
**As a** county worker
**I want to** create an IHSS case when application is approved
**So that** the recipient can receive services

**Acceptance Criteria**:
- [ ] Generate unique case number
- [ ] Set case status: ELIGIBLE
- [ ] Transfer application data to case
- [ ] Assign case owner (social worker)
- [ ] Set authorization start date
- [ ] Update recipient person type to RECIPIENT
- [ ] Publish case creation event

#### US-3.2: Provider Person Search
**As a** county provider coordinator
**I want to** search for existing providers before enrollment
**So that** I don't create duplicate records

**Acceptance Criteria**:
- [ ] Search by SSN, name, provider number
- [ ] Show provider status and eligibility
- [ ] Display existing assignments
- [ ] Link to provider profile

#### US-3.3: Start Provider Enrollment
**As a** county provider coordinator
**I want to** begin enrolling a new provider
**So that** they can be approved to serve recipients

**Acceptance Criteria**:
- [ ] Create provider record with: name, SSN, DOB, address, phone, email
- [ ] Generate provider number (9 digits)
- [ ] Set initial status: eligible = PENDING
- [ ] Set SSN verification: NOT_YET_VERIFIED
- [ ] Capture relationship to potential recipient (if any)
- [ ] Store names in UPPERCASE (per BR PVM 20)

#### US-3.4: SOC 426 Form Tracking
**As a** county provider coordinator
**I want to** track SOC 426 Provider Enrollment form completion
**So that** I know when this requirement is met

**Acceptance Criteria**:
- [ ] Mark SOC 426 as received
- [ ] Record received date
- [ ] Store form electronically (attachment)
- [ ] Block enrollment progress if not complete

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-3.1 | Create `CaseCreationService` | 8h |
| BE-3.2 | Create `ProviderEnrollmentService` | 8h |
| BE-3.3 | Add provider number generation | 2h |
| BE-3.4 | Create `EnrollmentFormEntity` for SOC forms | 4h |
| BE-3.5 | Create enrollment tracking on `ProviderEntity` | 4h |
| BE-3.6 | Create `CaseController` endpoints | 4h |
| BE-3.7 | Create `ProviderEnrollmentController` endpoints | 4h |
| BE-3.8 | Publish Kafka events for case/enrollment | 4h |
| BE-3.9 | Write unit tests | 6h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-3.1 | Case creation confirmation page | 4h |
| FE-3.2 | Provider search page | 6h |
| FE-3.3 | Provider enrollment form | 8h |
| FE-3.4 | SOC 426 tracking component | 4h |
| FE-3.5 | Enrollment status tracker | 6h |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cases` | Create case from application |
| GET | `/api/cases/{id}` | Get case details |
| GET | `/api/providers/search` | Search providers |
| POST | `/api/providers/enroll` | Start provider enrollment |
| PUT | `/api/providers/{id}/soc426` | Update SOC 426 status |
| GET | `/api/providers/{id}/enrollment-status` | Get enrollment progress |

---

## Sprint 4: Provider Enrollment Completion & CORI
**Duration**: 2 weeks
**Theme**: Background Checks & Eligibility

### Goals
- Complete provider enrollment workflow
- Implement CORI background check module
- Build recipient waiver functionality
- Add SSN verification simulation

### User Stories

#### US-4.1: Provider Orientation Tracking
**As a** county provider coordinator
**I want to** track provider orientation completion
**So that** I know when this requirement is met

**Acceptance Criteria**:
- [ ] Mark orientation as completed
- [ ] Record completion date
- [ ] Track orientation type (in-person, online)
- [ ] Generate certificate (optional)

#### US-4.2: SOC 846 Provider Agreement
**As a** county provider coordinator
**I want to** track SOC 846 Provider Agreement signing
**So that** providers acknowledge their responsibilities

**Acceptance Criteria**:
- [ ] Mark provider agreement signed
- [ ] Mark overtime agreement signed
- [ ] Record signature dates
- [ ] Store forms electronically

#### US-4.3: CORI Background Check
**As a** county provider coordinator
**I want to** initiate and track CORI background checks
**So that** providers are properly vetted

**Acceptance Criteria**:
- [ ] Initiate CORI check request
- [ ] Simulate DOJ response (for MVP)
- [ ] Classify convictions: Tier 1 vs Tier 2
- [ ] Auto-deny for Tier 1 convictions
- [ ] Flag Tier 2 for waiver consideration
- [ ] Track CORI status: PENDING, CLEARED, DISQUALIFIED

#### US-4.4: Recipient Waiver (Tier 2)
**As a** county worker
**I want to** process recipient waivers for Tier 2 convictions
**So that** recipients can choose to hire providers with certain histories

**Acceptance Criteria**:
- [ ] Disclose conviction details to recipient
- [ ] Record recipient acknowledgment
- [ ] Capture SOC 2298 waiver form signature
- [ ] Record waiver approval/denial
- [ ] Link waiver to specific recipient-provider pair

#### US-4.5: SSN Verification
**As a** system
**I want to** verify provider SSN with Social Security Administration
**So that** provider identity is confirmed

**Acceptance Criteria**:
- [ ] Simulate weekly batch SSN verification
- [ ] Update status: VERIFIED, DECEASED, DUPLICATE_SSN, SUSPECT_SSN
- [ ] Alert on non-verified status
- [ ] Block enrollment completion if verification fails

#### US-4.6: Approve Provider Enrollment
**As a** county supervisor
**I want to** approve completed provider enrollments
**So that** providers become eligible to serve

**Acceptance Criteria**:
- [ ] Validate all requirements complete:
  - SOC 426 ✓
  - Orientation ✓
  - SOC 846 ✓
  - Background check cleared ✓
  - SSN verified ✓
- [ ] Set eligible = YES
- [ ] Set effective date
- [ ] Set original hire date
- [ ] Publish provider approved event

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-4.1 | Create `CORIEntity` for background checks | 6h |
| BE-4.2 | Create `CORIService` with tier classification | 8h |
| BE-4.3 | Create `RecipientWaiverEntity` | 4h |
| BE-4.4 | Create `RecipientWaiverService` | 6h |
| BE-4.5 | Create `SSNVerificationService` (simulation) | 4h |
| BE-4.6 | Create `EnrollmentApprovalService` | 8h |
| BE-4.7 | Create CORI Controller endpoints | 4h |
| BE-4.8 | Create Waiver Controller endpoints | 4h |
| BE-4.9 | Add Keycloak resources for CORI, Waiver | 4h |
| BE-4.10 | Write unit tests | 8h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-4.1 | Orientation tracking component | 4h |
| FE-4.2 | SOC 846 agreement component | 4h |
| FE-4.3 | CORI management page | 8h |
| FE-4.4 | Recipient waiver form | 6h |
| FE-4.5 | Enrollment approval workflow | 6h |
| FE-4.6 | Provider eligibility dashboard | 6h |

### New Entities

```java
// CORIEntity.java
@Entity
@Table(name = "provider_cori")
public class CORIEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Enumerated(EnumType.STRING)
    private CORIStatus status; // PENDING, CLEARED, TIER1_DISQUALIFIED, TIER2_DISQUALIFIED

    private LocalDate requestDate;
    private LocalDate responseDate;

    private Boolean hasConvictions;
    private String convictionDetails;

    @Enumerated(EnumType.STRING)
    private ConvictionTier convictionTier; // NONE, TIER1, TIER2

    private String dojResponseCode;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
}

// RecipientWaiverEntity.java
@Entity
@Table(name = "recipient_waivers")
public class RecipientWaiverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String recipientId;
    private String providerId;
    private String coriId;

    private LocalDate waiverRequestDate;
    private LocalDate disclosureDate;
    private LocalDate waiverSignedDate;

    @Enumerated(EnumType.STRING)
    private WaiverStatus status; // REQUESTED, DISCLOSED, SIGNED, APPROVED, DENIED

    private String convictionDetails;
    private String recipientJustification;

    private LocalDate countyDecisionDate;
    private String countyDecision; // APPROVED, DENIED
    private String countyReviewer;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
}
```

---

## Sprint 5: ESP Self-Service Registration (Recipients)
**Duration**: 2 weeks
**Theme**: Recipient Self-Service Portal

### Goals
- Build ESP registration for recipients
- Implement identity validation
- Create email verification
- Add security questions

### User Stories

#### US-5.1: Recipient ESP Registration - Identity Validation
**As a** recipient with an approved case
**I want to** register for ESP by validating my identity
**So that** I can access self-service features

**Acceptance Criteria**:
- [ ] Select "I am a Recipient"
- [ ] Enter: Case Number, Last 4 of CIN, Last Name, First Name, DOB
- [ ] System validates against CMIPS records
- [ ] Error if no match or already registered
- [ ] Error if case status not ELIGIBLE

#### US-5.2: Email Verification
**As a** recipient registering for ESP
**I want to** verify my email address
**So that** my account is secure

**Acceptance Criteria**:
- [ ] Enter email address (twice for confirmation)
- [ ] System sends 6-digit verification code
- [ ] Code expires in 15 minutes
- [ ] 3 attempts max before lockout
- [ ] Resend code option

#### US-5.3: Username Creation
**As a** recipient registering for ESP
**I want to** create my username
**So that** I can log in to the portal

**Acceptance Criteria**:
- [ ] Username: 6-20 characters
- [ ] Allowed: letters, numbers, underscore, hyphen
- [ ] Real-time availability check
- [ ] Visual indicator: ✓ available, ✗ taken
- [ ] Confirm username (enter twice)

#### US-5.4: Password Creation
**As a** recipient registering for ESP
**I want to** create a secure password
**So that** my account is protected

**Acceptance Criteria**:
- [ ] Minimum 8 characters, maximum 50
- [ ] Requirements: uppercase, lowercase, number, special char
- [ ] Password strength indicator (weak/medium/strong)
- [ ] Cannot contain username
- [ ] Confirm password (enter twice)

#### US-5.5: Security Questions
**As a** recipient registering for ESP
**I want to** set security questions
**So that** I can recover my account if needed

**Acceptance Criteria**:
- [ ] Select 3 different questions from dropdown
- [ ] Answer minimum 3 characters
- [ ] Answers case-insensitive
- [ ] Cannot use username or password as answer

#### US-5.6: Complete Registration
**As a** recipient completing registration
**I want to** receive confirmation
**So that** I know my account is ready

**Acceptance Criteria**:
- [ ] Create Keycloak user account
- [ ] Assign "recipient" role
- [ ] Update recipient record: espRegistered = true
- [ ] Send welcome email
- [ ] Display username reminder
- [ ] Redirect to login

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-5.1 | Create `ESPRegistrationService` | 8h |
| BE-5.2 | Create `IdentityValidationService` | 6h |
| BE-5.3 | Create `EmailVerificationService` | 6h |
| BE-5.4 | Create `SecurityQuestionEntity` | 4h |
| BE-5.5 | Create `ESPRegistrationController` | 6h |
| BE-5.6 | Add verification code caching (Redis/DB) | 4h |
| BE-5.7 | Integrate with Keycloak user creation | 4h |
| BE-5.8 | Create email templates | 4h |
| BE-5.9 | Write unit tests | 6h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-5.1 | ESP Registration wizard (5 steps) | 12h |
| FE-5.2 | Identity validation form | 4h |
| FE-5.3 | Email verification component | 4h |
| FE-5.4 | Username availability checker | 4h |
| FE-5.5 | Password strength meter | 4h |
| FE-5.6 | Security questions form | 4h |
| FE-5.7 | Registration success page | 2h |

### New Entities

```java
// ESPRegistrationEntity.java
@Entity
@Table(name = "esp_registrations")
public class ESPRegistrationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private UserType userType; // RECIPIENT, PROVIDER

    private String personId; // recipientId or providerId
    private String keycloakUserId;

    private String username;
    private String email;
    private Boolean emailVerified;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status; // IN_PROGRESS, COMPLETED, FAILED

    private LocalDateTime registrationStarted;
    private LocalDateTime registrationCompleted;

    private Integer currentStep; // 1-5

    // Audit
    private LocalDateTime createdAt;
}

// SecurityQuestionEntity.java
@Entity
@Table(name = "security_questions")
public class SecurityQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId; // Keycloak user ID

    private String question1;
    private String answer1Hash;

    private String question2;
    private String answer2Hash;

    private String question3;
    private String answer3Hash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// EmailVerificationEntity.java
@Entity
@Table(name = "email_verifications")
public class EmailVerificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String email;
    private String verificationCode;
    private LocalDateTime expiresAt;
    private Integer attemptCount;
    private Boolean verified;

    private String registrationId;

    private LocalDateTime createdAt;
}
```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/esp/register/start` | Start registration |
| POST | `/api/esp/register/validate-identity` | Validate recipient/provider identity |
| POST | `/api/esp/register/send-verification` | Send email verification code |
| POST | `/api/esp/register/verify-email` | Verify email code |
| POST | `/api/esp/register/check-username` | Check username availability |
| POST | `/api/esp/register/complete` | Complete registration |
| GET | `/api/esp/security-questions` | Get available security questions |

---

## Sprint 6: ESP Self-Service Registration (Providers)
**Duration**: 2 weeks
**Theme**: Provider Self-Service Portal

### Goals
- Build ESP registration for providers
- Implement provider identity validation
- Mirror recipient registration flow
- Add provider-specific features

### User Stories

#### US-6.1: Provider ESP Registration - Identity Validation
**As a** provider with approved enrollment
**I want to** register for ESP by validating my identity
**So that** I can submit timesheets electronically

**Acceptance Criteria**:
- [ ] Select "I am a Provider"
- [ ] Enter: Provider Number (9 digits), Last Name, First Name, DOB, Last 4 of SSN
- [ ] System validates against CMIPS records
- [ ] Error if no match or already registered
- [ ] Error if eligible ≠ YES

#### US-6.2-6.5: Same as Recipient
(Email verification, username, password, security questions - same acceptance criteria)

#### US-6.6: Provider Registration Complete
**As a** provider completing registration
**I want to** receive confirmation
**So that** I know my account is ready

**Acceptance Criteria**:
- [ ] Create Keycloak user account
- [ ] Assign "provider" role
- [ ] Update provider record: espRegistered = true
- [ ] Send welcome email with ESP guide
- [ ] Display username reminder
- [ ] Redirect to login

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-6.1 | Extend `ESPRegistrationService` for providers | 4h |
| BE-6.2 | Create `ProviderIdentityValidationService` | 6h |
| BE-6.3 | Update registration flow for provider path | 4h |
| BE-6.4 | Create provider welcome email template | 2h |
| BE-6.5 | Write unit tests | 6h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-6.1 | User type selection screen | 2h |
| FE-6.2 | Provider identity validation form | 4h |
| FE-6.3 | Update wizard for provider path | 4h |
| FE-6.4 | Provider registration success page | 2h |

---

## Sprint 7: Provider Assignment & Self-Service Hire
**Duration**: 2 weeks
**Theme**: Recipient-Provider Connection

### Goals
- Implement provider search for recipients
- Build hire provider workflow
- Add provider assignment management
- Create workweek agreement handling

### User Stories

#### US-7.1: Recipient Search for Provider
**As a** recipient (18+) logged into ESP
**I want to** search for available providers
**So that** I can hire someone to provide services

**Acceptance Criteria**:
- [ ] Search by: Provider Name, Provider Number, Last 4 of SSN
- [ ] Filter by: County, Gender, Language
- [ ] Results show: name, number, gender, county, languages, status
- [ ] Status badges: Available, Already Hired, Not Eligible, Restricted

#### US-7.2: View Provider Details
**As a** recipient searching for providers
**I want to** view provider details before hiring
**So that** I can make an informed decision

**Acceptance Criteria**:
- [ ] Show: name, provider number, county
- [ ] Show: eligibility status (background check, orientation, SOC 846)
- [ ] Show: current assignments count
- [ ] Hide: SSN, address, phone (privacy)

#### US-7.3: Hire Provider
**As a** recipient (18+) in ESP
**I want to** hire a selected provider
**So that** they can serve me

**Acceptance Criteria**:
- [ ] Verify recipient is 18+ years old
- [ ] Verify case status is ELIGIBLE
- [ ] Display certification statements:
  - I am 18 or older
  - I want to hire this provider
  - I will direct provider's work
  - I will review/approve timesheets
- [ ] Capture electronic signature
- [ ] Create provider assignment
- [ ] Notify county for setup completion

#### US-7.4: County Provider Assignment
**As a** county worker
**I want to** assign a provider to a recipient case
**So that** services can begin

**Acceptance Criteria**:
- [ ] Search and select provider
- [ ] Verify provider eligible
- [ ] Set assignment start date
- [ ] Assign hours
- [ ] Create ProviderRecipientRelationship record
- [ ] Notify provider of assignment

#### US-7.5: Provider Workweek Agreement (SOC 2255)
**As a** county worker
**I want to** create workweek agreements for providers
**So that** overtime is calculated correctly

**Acceptance Criteria**:
- [ ] Select workweek start day (Sun-Sat options)
- [ ] Apply to all provider's recipients
- [ ] Capture signatures
- [ ] Store agreement
- [ ] Track agreement history

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-7.1 | Create `ProviderSearchService` for recipients | 6h |
| BE-7.2 | Create `HireProviderService` | 8h |
| BE-7.3 | Create `ProviderAssignmentService` | 8h |
| BE-7.4 | Create `WorkweekAgreementEntity` | 4h |
| BE-7.5 | Create `WorkweekAgreementService` | 4h |
| BE-7.6 | Create Hire Provider Controller endpoints | 4h |
| BE-7.7 | Publish assignment events to Kafka | 4h |
| BE-7.8 | Write unit tests | 6h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-7.1 | Provider search page (ESP) | 8h |
| FE-7.2 | Provider details modal | 4h |
| FE-7.3 | Hire provider workflow | 8h |
| FE-7.4 | Assignment confirmation page | 4h |
| FE-7.5 | Workweek agreement form | 6h |
| FE-7.6 | My Providers list (recipient ESP) | 4h |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/esp/providers/search` | Search providers (recipient use) |
| GET | `/api/esp/providers/{id}` | Get provider details (limited) |
| POST | `/api/esp/hire-provider` | Hire provider (recipient initiated) |
| GET | `/api/esp/my-providers` | Get recipient's providers |
| POST | `/api/provider-assignments` | County create assignment |
| POST | `/api/workweek-agreements` | Create workweek agreement |

---

## Sprint 8: Notifications, Communications & Polish
**Duration**: 2 weeks
**Theme**: Communication & Final Integration

### Goals
- Implement notification system
- Build email/SMS communications
- Add dashboard widgets
- Polish and bug fixes

### User Stories

#### US-8.1: Registration Notifications
**As a** system
**I want to** send notifications for registration events
**So that** users stay informed

**Acceptance Criteria**:
- [ ] Email: Welcome after ESP registration
- [ ] Email: Application received confirmation
- [ ] Email: Case approved notification
- [ ] Email: Provider enrollment approved
- [ ] Email: Assignment notification (to provider)

#### US-8.2: Timeline Notifications
**As a** county worker
**I want to** receive alerts for approaching deadlines
**So that** I don't miss processing requirements

**Acceptance Criteria**:
- [ ] Alert: 45-day application deadline approaching
- [ ] Alert: Enrollment requirements incomplete
- [ ] In-app notification center
- [ ] Email digest option

#### US-8.3: Registration Dashboard
**As a** county supervisor
**I want to** see registration metrics
**So that** I can monitor workload

**Acceptance Criteria**:
- [ ] Referrals: Open, Closed, Converted
- [ ] Applications: Pending, Approved, Denied
- [ ] Providers: Pending enrollment, Active, By county
- [ ] Timeline indicators for approaching deadlines

#### US-8.4: Audit Trail
**As a** system administrator
**I want to** track all registration actions
**So that** we maintain accountability

**Acceptance Criteria**:
- [ ] Log all status changes
- [ ] Log user actions (create, update, approve)
- [ ] Include timestamps and user IDs
- [ ] Searchable audit log

### Technical Tasks

#### Backend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| BE-8.1 | Create `EmailService` with templates | 8h |
| BE-8.2 | Create notification event consumers | 6h |
| BE-8.3 | Create `AuditLogEntity` and service | 6h |
| BE-8.4 | Create dashboard metrics endpoints | 6h |
| BE-8.5 | Bug fixes and optimization | 8h |
| BE-8.6 | Integration testing | 8h |
| BE-8.7 | API documentation (OpenAPI) | 4h |

#### Frontend Tasks
| Task ID | Description | Estimate |
|---------|-------------|----------|
| FE-8.1 | Notification center component | 6h |
| FE-8.2 | Registration dashboard | 8h |
| FE-8.3 | Audit log viewer | 4h |
| FE-8.4 | UI polish and consistency | 8h |
| FE-8.5 | Bug fixes | 8h |
| FE-8.6 | E2E testing | 8h |

---

## Database Schema Summary

### New Tables to Create

```sql
-- Referral Management
CREATE TABLE referrals (
    id UUID PRIMARY KEY,
    person_id UUID REFERENCES persons(id),
    status VARCHAR(50) NOT NULL,
    source VARCHAR(100),
    referral_date DATE NOT NULL,
    closed_date DATE,
    closed_reason VARCHAR(100),
    assigned_worker_id VARCHAR(255),
    county_code VARCHAR(10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255)
);

-- Application Processing
CREATE TABLE applications (
    id UUID PRIMARY KEY,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    recipient_id UUID REFERENCES recipients(id),
    status VARCHAR(50) NOT NULL,
    application_date DATE NOT NULL,
    deadline_date DATE NOT NULL,
    decision_date DATE,
    extension_date DATE,
    extension_reason VARCHAR(255),
    assigned_worker_id VARCHAR(255),
    county_code VARCHAR(10),
    cin VARCHAR(20),
    cin_clearance_status VARCHAR(50),
    cin_clearance_date TIMESTAMP,
    medi_cal_aid_code VARCHAR(20),
    medi_cal_status VARCHAR(50),
    denial_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255)
);

-- CORI Background Checks
CREATE TABLE provider_cori (
    id UUID PRIMARY KEY,
    provider_id UUID REFERENCES providers(id),
    status VARCHAR(50) NOT NULL,
    request_date DATE NOT NULL,
    response_date DATE,
    has_convictions BOOLEAN,
    conviction_details TEXT,
    conviction_tier VARCHAR(20),
    doj_response_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Recipient Waivers
CREATE TABLE recipient_waivers (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    cori_id UUID REFERENCES provider_cori(id),
    status VARCHAR(50) NOT NULL,
    waiver_request_date DATE,
    disclosure_date DATE,
    waiver_signed_date DATE,
    conviction_details TEXT,
    recipient_justification TEXT,
    county_decision_date DATE,
    county_decision VARCHAR(20),
    county_reviewer VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- ESP Registration
CREATE TABLE esp_registrations (
    id UUID PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL,
    person_id UUID NOT NULL,
    keycloak_user_id VARCHAR(255),
    username VARCHAR(100),
    email VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    registration_started TIMESTAMP,
    registration_completed TIMESTAMP,
    current_step INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Email Verifications
CREATE TABLE email_verifications (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempt_count INTEGER DEFAULT 0,
    verified BOOLEAN DEFAULT FALSE,
    registration_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Questions
CREATE TABLE security_questions (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    question1 VARCHAR(500),
    answer1_hash VARCHAR(255),
    question2 VARCHAR(500),
    answer2_hash VARCHAR(255),
    question3 VARCHAR(500),
    answer3_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Workweek Agreements
CREATE TABLE workweek_agreements (
    id UUID PRIMARY KEY,
    provider_id UUID REFERENCES providers(id),
    workweek_start_day VARCHAR(20) NOT NULL,
    effective_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(50) NOT NULL,
    provider_signature_date DATE,
    recipient_signature_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Person Notes
CREATE TABLE person_notes (
    id UUID PRIMARY KEY,
    person_id UUID NOT NULL,
    note_date DATE NOT NULL,
    contact_method VARCHAR(50),
    content TEXT NOT NULL,
    follow_up_needed BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Audit Log
CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    user_id VARCHAR(255),
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Keycloak Resources to Add

### New Resources

| Resource Name | Scopes |
|---------------|--------|
| Referral Resource | view, create, edit, close, reopen, convert |
| Application Resource | view, create, edit, approve, deny, extend, withdraw |
| CORI Resource | view, create, edit, initiate-check |
| Waiver Resource | view, create, edit, approve, deny |
| ESP Registration Resource | start, validate, complete |
| Workweek Agreement Resource | view, create, edit, terminate |
| Audit Log Resource | view |

### New Roles

| Role | Description |
|------|-------------|
| intake-worker | Creates referrals and applications |
| eligibility-worker | Processes applications |
| provider-coordinator | Manages provider enrollment |
| esp-user | Self-service portal user |

---

## Risk Assessment

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Keycloak integration complexity | High | Medium | Leverage existing `KeycloakAdminService` |
| CIN/MEDS mock accuracy | Medium | High | Document as simulation, plan for real integration |
| Email delivery issues | Medium | Medium | Use proven email service (SendGrid, etc.) |
| Multi-step wizard abandonment | Medium | Medium | Save progress, allow resume |
| Data migration | High | Low | Not required for new module |

---

## Success Metrics

| Metric | Target |
|--------|--------|
| Referral to Application conversion | > 60% |
| Application processing within 45 days | > 95% |
| ESP registration completion rate | > 80% |
| Provider enrollment completion rate | > 70% |
| Average registration time (ESP) | < 10 minutes |
| System uptime | > 99.5% |

---

## Dependencies

### External Systems (Mocked for MVP)
- **SCI (Statewide Client Index)**: CIN clearance
- **MEDS**: Medi-Cal eligibility
- **DOJ**: CORI background checks
- **SSA**: SSN verification

### Internal Dependencies
- Keycloak server running and configured
- Kafka cluster for events
- PostgreSQL database
- Email service configured

---

## Sprint Summary

| Sprint | Theme | Key Deliverables |
|--------|-------|------------------|
| 1 | Referral Management | Person search, referral creation, person notes |
| 2 | Application Processing | Applications, CIN clearance, MEDS integration |
| 3 | Case & Provider Start | Case creation, provider enrollment start |
| 4 | Provider Completion | CORI, waivers, SSN verification, enrollment approval |
| 5 | ESP Registration (Recipients) | 5-step recipient registration wizard |
| 6 | ESP Registration (Providers) | 5-step provider registration wizard |
| 7 | Provider Assignment | Search, hire, workweek agreements |
| 8 | Notifications & Polish | Emails, alerts, dashboard, bug fixes |

**Total Duration**: 16 weeks (8 × 2-week sprints)

---

## Appendix A: Existing Code References

### Entities to Extend
- `RecipientEntity`: Add referral fields, CIN clearance tracking
- `ProviderEntity`: Already has comprehensive enrollment tracking
- `CaseEntity`: Add application reference

### Services to Leverage
- `KeycloakAdminService`: User creation and role assignment
- `KeycloakPolicyEvaluationService`: Authorization
- `FieldLevelAuthorizationService`: Field visibility
- `NotificationService`: Event-based notifications

### Repositories to Extend
- `RecipientRepository`: Add referral queries
- `ProviderRepository`: Already has enrollment queries

---

## Appendix B: Business Rules Reference

### From DSD Section 20 (Intake)
- Person search required before creating new records
- Referral source tracking mandatory
- 45-day processing requirement for applications
- CIN clearance required for case creation

### From DSD Section 23 (Provider Management)
- Provider must be 18+ years old
- SSN verification required
- CORI background check mandatory
- Tier 1 convictions: permanent disqualification
- Tier 2 convictions: waiver possible
- SOC 426, orientation, SOC 846 required for enrollment

### From ESP Registration Documentation
- 5-step registration process
- Email verification required
- Username: 6-20 characters
- Password: 8+ chars with complexity requirements
- 3 security questions required

---

*End of Sprint Plan Document*
