## Registration & Onboarding Sprint Plan

### 1. Goal & Scope
- **Goal**: Implement end-to-end **Recipient** and **Provider** registration + onboarding flows in the unified CMIPS application, aligned with DSD Section 20/23 and the HTML journey docs.
- **Scope**:
  - County-side intake/enrollment flows (already partially implemented in backend) wired cleanly to APIs and UI.
  - ESP-style self-service registration for **Recipients** and **Providers** (5-step flows: validate identity, verify email, create username/password, security Qs).
  - Recipient self-service **Hire Provider** flow via ESP-style UI.
  - Integration with **Keycloak** for authentication and roles, using existing `AuthController` and `KeycloakAdminService` patterns.

---

### 2. Current Backend State (Summary)

- **Recipient domain** (`RecipientEntity`, `RecipientRepository`, `RecipientController`, `CaseManagementService`):
  - Already supports **referral creation**, **case creation**, **case approval/denial**, **status transitions**, and **referral reopen/close**.
  - `RecipientEntity` has **`personType`** (OPEN_REFERRAL/APPLICANT/RECIPIENT), **CIN**, **countyCode**, and **`espRegistered`** + `eTimesheetOption`.
  - `RecipientRepository` supports search, `findByCin`, `findBySsn`, `findByEmail`, `findByEspRegisteredTrue`, etc.
  - `RecipientController` exposes **search**, **CRUD**, **address**, **accessibility**, and a simple `/api/recipients/{id}/esp-registration` endpoint that only toggles `espRegistered` (no full 5-step flow).

- **Case domain** (`CaseEntity`, `CaseRepository`, `CaseManagementService`):
  - `CaseManagementService.createReferral`, `.createCase`, `.approveCase`, `.denyCase`, `.terminateCase`, etc., already model Section 20/21 rules.
  - Approval sets case to **ELIGIBLE** and the `RecipientEntity.personType` to **RECIPIENT**.

- **Provider domain** (`ProviderEntity`, `ProviderRepository`, `ProviderManagementService`, `ProviderManagementController`):
  - `ProviderEntity` contains enrollment flags (SOC 426/846, orientation, background, CORI, etc.), `eligible`, `providerStatus`, `espRegistered`, `eTimesheetStatus`, overtime + sick leave fields.
  - `ProviderManagementService.createProvider` and `.approveEnrollment` implement core **enrollment** and **eligibility** logic per Section 23.
  - `ProviderManagementService.assignProviderToCase` creates `ProviderAssignmentEntity` records and enforces `isEligibleToServe()`.
  - `ProviderRepository` already has search by **providerNumber/SSN/name/county**, and `findByEspRegisteredTrue()`.

- **Auth & Keycloak** (`AuthController`, `SecurityConfig`, `KeycloakAdminService`, `CaseManagementKeycloakInitializer`):
  - `AuthController` **only handles login/refresh** against Keycloak; there is **no self-service registration API** yet.
  - `SecurityConfig` uses Keycloak JWT as a resource server.
  - `KeycloakAdminService` is available to create/manage users, groups, roles via the Keycloak Admin API.

- **Gaps for Registration/Onboarding**:
  - No public **self-service endpoints** for Recipient/Provider ESP registration (5-step flows).
  - No identity validation APIs for:
    - Recipient: **caseNumber + CIN + name + DOB**.
    - Provider: **providerNumber + last4 SSN + name + DOB**.
  - No backend orchestration to create **Keycloak users**, link them to `RecipientEntity`/`ProviderEntity`, set roles, and flip `espRegistered` in a controlled way.
  - No Recipient-facing APIs to **search providers** and **hire provider** via ESP (currently only county-side assignment).

---

### 3. High-Level Epics

- **Epic 1 – Backend Identity & Registration Foundations**
  - Build the core backend building blocks (DTOs, services, repositories, endpoints) for validating Recipient/Provider identity and tracking ESP registration state.

- **Epic 2 – Recipient ESP Self-Service Registration Flow**
  - Implement the full 5-step registration wizard for Recipients (public API layer + Keycloak user creation + `espRegistered` updates).

- **Epic 3 – Provider ESP Self-Service Registration Flow**
  - Implement the full 5-step registration wizard for Providers, including eligibility checks and Keycloak integration.

- **Epic 4 – Recipient “Hire Provider” Onboarding Flow**
  - Enable logged-in Recipients to search for Providers and hire them online, driving `ProviderAssignmentEntity` creation.

- **Epic 5 – Frontend (Next.js) Flows & UX**
  - Implement multi-step forms, validation, and client-side orchestration for the above backend APIs.

- **Epic 6 – Security, Observability, and Operational Hardening**
  - Keycloak config, token/role mapping, audit logging, metrics, and error handling for all registration/onboarding flows.

Below is a suggested **3-sprint plan** (~2 weeks per sprint).

---

### 4. Sprint 1 – Backend Foundations & County-Side Registration

**Objective**: Solid backend base for registration & identity validation, and confirm that county-driven intake/enrollment flows cover the DSD rules we rely on.

#### 4.1 Stories

- **Story S1.1 – Document Current Intake & Enrollment Coverage**
  - Review `CaseManagementService` and `RecipientController` against DSD Section 20/21 to confirm:
    - Referral → Application → Case → ELIGIBLE → RECIPIENT lifecycle is correct.
    - Required fields for identity validation (caseNumber, CIN, DOB, names, county) are present on entities.
  - Review `ProviderManagementService` and `ProviderManagementController` against Section 23 to confirm:
    - Provider enrollment flow (createProvider, approveEnrollment) is complete.
    - All fields needed for provider identity validation (providerNumber, SSN, DOB, name) exist.

- **Story S1.2 – Add Identity Validation Service Layer**
  - Create a new service class, e.g. `RegistrationIdentityService`, with methods:
    - `validateRecipientIdentity(caseNumber, cinLast4, lastName, firstName, dob)` using `CaseRepository` + `RecipientRepository`.
    - `validateProviderIdentity(providerNumber, ssnLast4, lastName, firstName, dob)` using `ProviderRepository`.
  - Define detailed **error codes** (e.g., `CASE_NOT_FOUND`, `CIN_MISMATCH`, `NAME_DOB_MISMATCH`, `PROVIDER_NOT_ELIGIBLE`, `ALREADY_REGISTERED`).

- **Story S1.3 – Public Registration Controller (Backend API Shell)**
  - Add a new controller, e.g. `PublicRegistrationController` under `/api/public/registration` (permit-all in `SecurityConfig`).
  - Endpoints (no Keycloak integration yet, just stubs + identity validation):
    - `POST /recipient/identity-check` – wraps `validateRecipientIdentity`, returns success + masked recipient info.
    - `POST /provider/identity-check` – wraps `validateProviderIdentity`, returns success + masked provider info.
  - Return structured responses: `{ success, errorCode, message, correlationId }`.

- **Story S1.4 – Registration State & Audit Entities**
  - Introduce new entities/tables to track registration attempts and verification codes, e.g.:
    - `EspRegistrationSessionEntity` (id, roleType, personId, status, createdAt, expiresAt, lastStepCompleted).
    - `VerificationCodeEntity` (id, sessionId, channelType=EMAIL/PHONE, codeHash, expiresAt, attempts, consumed).
  - Implement JPA repositories for these.

- **Story S1.5 – Update SecurityConfig for Public Registration APIs**
  - Ensure `/api/public/registration/**` is allowed anonymously while everything else remains JWT-protected.
  - Add basic rate limiting / abuse controls at gateway level (to be detailed later).

#### 4.2 Deliverables

- New service layer for identity validation.
- Public registration controller with identity check endpoints.
- Database migrations (Liquibase/Flyway or SQL) for registration sessions and verification codes.
- Updated `SecurityConfig` allowing anonymous access to registration endpoints.

---

### 5. Sprint 2 – Recipient ESP Self-Service Registration (5-Step Flow)

**Objective**: Implement full Recipient ESP registration flow, from identity validation through Keycloak user creation and `espRegistered` update.

#### 5.1 Stories

- **Story S2.1 – Email Verification Flow (Shared)**
  - Add service `VerificationService` using existing `NotificationService`/email infra.
  - API endpoints (under `/api/public/registration`):
    - `POST /recipient/send-email-code` – given a valid registration session ID + email, create and send 6-digit code.
    - `POST /recipient/verify-email-code` – verify code, mark email as verified on session.
  - Enforce expiry (e.g., 15 minutes) and max attempts.

- **Story S2.2 – Recipient Username & Password Rules Enforcement**
  - Define DTOs and validation logic for:
    - `POST /recipient/username` – check uniqueness and syntax (6–20 chars, allowed chars).
    - `POST /recipient/password` – enforce password policy (length, complexity, no username, etc.).
  - Decide where to store password during registration:
    - Prefer **not** storing raw passwords; build directly into Keycloak user in S2.4.

- **Story S2.3 – Security Questions Storage**
  - Design a minimal `SecurityQuestionEntity` / `RecipientSecurityQuestionEntity` (recipientId, questionId, answerHash, createdAt).
  - API endpoint: `POST /recipient/security-questions` – accepts list of 3 Q/A, hashes answers (e.g., using BCrypt) before persisting.

- **Story S2.4 – Keycloak User Creation for Recipients**
  - Use `KeycloakAdminService` to create a Keycloak user when Recipient finishes all steps:
    - Username from S2.2, email from S2.1.
    - Assign **realm role** `RECIPIENT`.
    - Link user to `RecipientEntity` via new fields (if you choose to add them): `keycloakUserId`, `keycloakUsername`.
  - API endpoint: `POST /recipient/complete-registration`:
    - Validates registration session, email verified, username/password/security questions provided.
    - Calls Keycloak Admin API to create user.
    - Sets `RecipientEntity.espRegistered = true` and optionally `eTimesheetOption = 'IHSS_WEBSITE'`.

- **Story S2.5 – Enhance RecipientController ESP Endpoints**
  - Deprecate or internally reuse `/api/recipients/{id}/esp-registration` so it’s used only by **county staff**, not by ESP public flow.
  - Ensure only caseworkers/admins with proper permissions can call the internal toggle.

- **Story S2.6 – Error Handling & Logging**
  - Standardize error responses for all Recipient registration APIs.
  - Add structured logging (correlationId, recipientId/personId, sessionId) for observability.

#### 5.2 Deliverables

- Fully functional Recipient ESP registration flow (backend-only).
- Keycloak users created for Recipients with proper roles.
- `RecipientEntity.espRegistered` accurately reflects ESP registration.
- Unit/integration tests for the Recipient registration APIs.

---

### 6. Sprint 3 – Provider ESP Registration & Recipient “Hire Provider” Flow

**Objective**: Implement Provider self-service ESP registration and the Recipient self-service Hire Provider onboarding flow.

#### 6.1 Stories – Provider ESP Registration

- **Story S3.1 – Provider Identity Validation Endpoint**
  - Reuse `RegistrationIdentityService` to implement `POST /provider/identity-check`:
    - Input: providerNumber, last4 SSN, lastName, firstName, DOB.
    - Checks `ProviderEntity.eligible = 'YES'` and not already ESP-registered.

- **Story S3.2 – Provider Email Verification, Username & Password**
  - Mirror S2.1 & S2.2 for Providers with separate endpoints:
    - `POST /provider/send-email-code`, `/provider/verify-email-code`.
    - `POST /provider/username`, `/provider/password`.
  - Share underlying services where possible.

- **Story S3.3 – Provider Keycloak User Creation & espRegistered Update**
  - Endpoint: `POST /provider/complete-registration`:
    - Create Keycloak user with **realm role** `PROVIDER`.
    - Optionally store `keycloakUserId`/`keycloakUsername` on `ProviderEntity`.
    - Set `ProviderEntity.espRegistered = true`, `eTimesheetStatus = 'ENROLLED'`.

- **Story S3.4 – Security Questions for Provider (Optional / Shared)**
  - Decide whether to reuse Recipient security questions table (with roleType) or separate table.
  - Implement analogous `/provider/security-questions` endpoint.

#### 6.2 Stories – Recipient “Hire Provider” Flow (Onboarding)

- **Story S3.5 – Provider Search API for Recipients**
  - Add a new controller (or extend an existing one) with endpoints secured by Keycloak role `RECIPIENT`:
    - `GET /api/esp/providers/search` with filters:
      - providerName (first/last), providerNumber, last4 SSN, county, language, gender.
    - Use `ProviderRepository.searchProviders` + any additional filters (e.g., only `eligible = 'YES'`, not deceased, not ineligible).
    - Return statuses matching DSD UI: **Available**, **Already Hired**, **Not Eligible**, **Restricted (CORI)**.

- **Story S3.6 – Hire Provider API (Create Assignment)**
  - Endpoint: `POST /api/esp/providers/hire` (role `RECIPIENT`):
    - Input: providerId/providerNumber, caseId (or derived from recipient’s primary case), relationship, optional assignedHours.
    - Validate:
      - Recipient is **18+** and has an **ELIGIBLE** or **PRESUMPTIVE_ELIGIBLE** case.
      - Provider `isEligibleToServe()`.
      - Business rules on max providers per case, CORI restrictions, etc.
    - Internally call `ProviderManagementService.assignProviderToCase` and return assignment details.

- **Story S3.7 – Notifications & Tasks After Hire**
  - When hire completes:
    - Create notification(s) via `NotificationService` for:
      - County worker (new assignment / hire request).
      - Provider (new recipient assignment).
      - Recipient (confirmation with confirmation number).
    - Optionally create a **Task** in `TaskService` for the caseworker to finalize pay rate/hours.

- **Story S3.8 – ESP Role-Based Access & Routing**
  - Ensure Keycloak realm config and `SecurityConfig` support:
    - `RECIPIENT` users can only access `/api/esp/**` recipient endpoints.
    - `PROVIDER` users can only access provider-specific ESP endpoints.
    - Internal staff (CASE_WORKER, SUPERVISOR, ADMIN) continue to use existing `/api/recipients`, `/api/providers`, `/api/cases` endpoints.

#### 6.3 Deliverables

- Provider ESP registration backend APIs complete.
- Recipient “Hire Provider” flow implemented and integrated with `ProviderAssignmentEntity`.
- Notification and task side-effects wired into existing `NotificationService` and `TaskService`.

---

### 7. Epic 5 – Frontend (Next.js) – High-Level Tasks (Parallel / Later Sprints)

> These are top-level tasks; you can split them further into separate UI sprints.

- **Recipient Registration Wizard UI**
  - Next.js pages/components for:
    - Step 1 – Identity: form for caseNumber, CIN last4, name, DOB → calls `/recipient/identity-check`.
    - Step 2 – Email verification (send + verify code).
    - Step 3 – Username/password selection.
    - Step 4 – Security questions.
    - Step 5 – Confirmation.
  - Error handling and messaging matching the HTML Recipient Journey doc.

- **Provider Registration Wizard UI**
  - Same pattern as Recipient, with provider-specific wording and fields.

- **Hire Provider UI (Recipient)**
  - Search filters and results UI matching `1 -RECIPIENT_JOURNEY.html` (statuses, filters for county/language/gender).
  - Hire confirmation screen with electronic signature and legal statements.

- **Shared UX & Design System**
  - Reuse components for step indicators, error toasts, form fields, validation messages.

---

### 8. Epic 6 – Security, Observability, and Hardening (Ongoing)

- **Keycloak Configuration**
  - Ensure realm roles `RECIPIENT`, `PROVIDER`, `CASE_WORKER`, `SUPERVISOR`, `ADMIN` are configured (already present in config JSON).
  - Create separate **public client(s)** for ESP flows if needed (`cmips-esp`), with appropriate redirect URIs.

- **Audit & Logging**
  - Log all registration and hire-provider events with correlation IDs.
  - Optionally publish `CaseEvent` / `TaskEvent` / `TimesheetEvent` to Kafka for downstream analytics.

- **Rate Limiting & Abuse Protection**
  - At gateway level, implement basic rate limits on:
    - Identity checks.
    - Email code sends.
    - Hire provider calls.

- **Monitoring & Alerts**
  - Metrics: number of registrations per day, failure reasons, provider hire success/failure.
  - Alerts on spikes in errors or suspicious activity.

---

### 9. Summary

This sprint plan incrementally builds on your existing **Case Management** and **Provider Management** services:
- **Sprint 1**: Backend foundation – identity validation, public registration API shell, registration state tracking.
- **Sprint 2**: Recipient ESP registration – 5-step flow + Keycloak user creation.
- **Sprint 3**: Provider ESP registration + Recipient “Hire Provider” onboarding.

Frontend and security hardening epics can run in parallel once the backend contracts are stable.
