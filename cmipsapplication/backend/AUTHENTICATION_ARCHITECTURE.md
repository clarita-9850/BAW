# CMIPS Authentication & Authorization Architecture

## Table of Contents

1. [Overview](#overview)
2. [Current Authentication Flow](#current-authentication-flow)
3. [User Storage Architecture](#user-storage-architecture)
4. [Performance Analysis for 2M Users](#performance-analysis-for-2m-users)
5. [Production-Ready Authentication Options](#production-ready-authentication-options)
6. [Recommendation](#recommendation)
7. [Implementation Guide](#implementation-guide)

---

## Overview

CMIPS (Case Management Information and Payrolling System) supports multiple user roles with different authentication and authorization requirements:

| Role | User Count | Authentication Method | Description |
|------|------------|----------------------|-------------|
| **RECIPIENT** | ~1.6M | ESP Self-Registration | IHSS service recipients |
| **PROVIDER** | ~400K | ESP Self-Registration | IHSS care providers |
| **CASEWORKER** | ~5K | County LDAP/SSO | County social workers |
| **SUPERVISOR** | ~500 | County LDAP/SSO | County supervisors |
| **ADMIN** | ~50 | Keycloak Direct | System administrators |

---

## Current Authentication Flow

### ESP (Electronic Services Portal) Registration

Recipients and Providers self-register through a 5-step ESP registration process:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ESP REGISTRATION FLOW (5 STEPS)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  STEP 1: Identity Verification                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  User provides:                                                      │   │
│  │  - SSN (last 4 digits) or Provider Number                           │   │
│  │  - Date of Birth                                                     │   │
│  │  - County Code                                                       │   │
│  │                                                                      │   │
│  │  System validates against:                                           │   │
│  │  - Recipient: CIN database                                          │   │
│  │  - Provider: Provider enrollment database                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  STEP 2: Contact Information                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  User provides:                                                      │   │
│  │  - Email address                                                     │   │
│  │  - Phone number                                                      │   │
│  │  - Preferred contact method                                          │   │
│  │                                                                      │   │
│  │  System sends verification code via email/SMS                        │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  STEP 3: Security Questions                                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  User selects 3 security questions and provides answers              │   │
│  │  - Used for account recovery                                         │   │
│  │  - Answers are hashed before storage                                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  STEP 4: Account Creation                                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  User creates:                                                       │   │
│  │  - Username (validated for uniqueness)                               │   │
│  │  - Password (must meet complexity requirements)                      │   │
│  │                                                                      │   │
│  │  Password Requirements:                                              │   │
│  │  - Minimum 8 characters                                              │   │
│  │  - At least 1 uppercase, 1 lowercase, 1 number, 1 special char      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  STEP 5: Terms Acceptance & Keycloak User Creation                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  1. User accepts Terms of Service                                    │   │
│  │  2. System creates user in Keycloak:                                 │   │
│  │     - keycloakAdminService.createUser(username, email, password)    │   │
│  │  3. System assigns role:                                             │   │
│  │     - keycloakAdminService.assignRoleToUser(userId, "RECIPIENT")    │   │
│  │     - OR keycloakAdminService.assignRoleToUser(userId, "PROVIDER")  │   │
│  │  4. Registration marked COMPLETED                                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Login Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         LOGIN AUTHENTICATION FLOW                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐         ┌──────────────┐         ┌─────────────┐             │
│  │  User    │         │ Spring Boot  │         │  Keycloak   │             │
│  │ (Browser)│         │   Backend    │         │   Server    │             │
│  └────┬─────┘         └──────┬───────┘         └──────┬──────┘             │
│       │                      │                        │                     │
│       │  POST /api/auth/login                         │                     │
│       │  {username, password}                         │                     │
│       │─────────────────────►│                        │                     │
│       │                      │                        │                     │
│       │                      │  POST /realms/cmips/   │                     │
│       │                      │  protocol/openid-connect/token              │
│       │                      │  grant_type=password   │                     │
│       │                      │─────────────────────────────────►│           │
│       │                      │                        │                     │
│       │                      │                        │  Validate           │
│       │                      │                        │  credentials        │
│       │                      │                        │                     │
│       │                      │◄─────────────────────────────────│           │
│       │                      │  {access_token, refresh_token,   │           │
│       │                      │   expires_in, token_type}        │           │
│       │                      │                        │                     │
│       │◄─────────────────────│                        │                     │
│       │  JWT Tokens          │                        │                     │
│       │                      │                        │                     │
└───────┴──────────────────────┴────────────────────────┴─────────────────────┘
```

### JWT Token Structure

```json
{
  "exp": 1765425487,
  "iat": 1765425427,
  "jti": "f66ac7e2-c8be-4892-becd-684eb8c93768",
  "iss": "http://localhost:8080/realms/cmips",
  "sub": "c1d18bad-e0c7-4e9a-9e26-22425bee6ff7",
  "typ": "Bearer",
  "azp": "cmips-backend",
  "session_state": "56549c7d-371e-4067-9f2e-452d743ffcf1",
  "acr": "1",
  "realm_access": {
    "roles": ["RECIPIENT"]
  },
  "resource_access": {
    "cmips-backend": {
      "roles": ["view_timesheet", "approve_timesheet"]
    }
  },
  "scope": "email profile",
  "email_verified": true,
  "name": "John Doe",
  "preferred_username": "johndoe",
  "email": "john.doe@example.com"
}
```

### Authorization Flow (Policy Enforcement)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      AUTHORIZATION / POLICY ENFORCEMENT                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐      ┌─────────────────┐      ┌────────────────────────────┐ │
│  │  User    │      │  Spring Boot    │      │  Keycloak Authorization    │ │
│  │  Request │      │  API Gateway    │      │  Services                  │ │
│  └────┬─────┘      └────────┬────────┘      └─────────────┬──────────────┘ │
│       │                     │                             │                 │
│       │  GET /api/timesheets                              │                 │
│       │  Authorization: Bearer <JWT>                      │                 │
│       │────────────────────►│                             │                 │
│       │                     │                             │                 │
│       │                     │  1. Extract JWT             │                 │
│       │                     │  2. Validate signature      │                 │
│       │                     │  3. Extract roles           │                 │
│       │                     │                             │                 │
│       │                     │  POST /realms/cmips/        │                 │
│       │                     │  authz/protection/          │                 │
│       │                     │  permission                 │                 │
│       │                     │  {resource: "Timesheet",    │                 │
│       │                     │   scope: "read"}            │                 │
│       │                     │────────────────────────────►│                 │
│       │                     │                             │                 │
│       │                     │                             │  Evaluate       │
│       │                     │                             │  Policies:      │
│       │                     │                             │  - Role Policy  │
│       │                     │                             │  - Time Policy  │
│       │                     │                             │  - Resource     │
│       │                     │                             │    Permissions  │
│       │                     │                             │                 │
│       │                     │◄────────────────────────────│                 │
│       │                     │  {result: "PERMIT"}         │                 │
│       │                     │                             │                 │
│       │                     │  4. Process request         │                 │
│       │                     │  5. Return data             │                 │
│       │◄────────────────────│                             │                 │
│       │  Timesheet data     │                             │                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Role-Based Permissions

```java
// RECIPIENT Permissions
if (userRoles.contains("RECIPIENT")) {
    return switch (resourceName) {
        case "Timesheet Resource" -> Set.of("read", "approve", "reject").contains(scope);
        case "Provider-Recipient Resource" -> Set.of("read").contains(scope);
        case "Case Resource" -> Set.of("read").contains(scope);
        case "Recipient Resource" -> Set.of("read", "update").contains(scope);
        default -> false;
    };
}

// PROVIDER Permissions
if (userRoles.contains("PROVIDER")) {
    return switch (resourceName) {
        case "Timesheet Resource" -> Set.of("read", "create", "update", "submit").contains(scope);
        case "EVV Resource" -> Set.of("read", "create").contains(scope);
        case "Provider-Recipient Resource" -> Set.of("read").contains(scope);
        case "Provider Resource" -> Set.of("read", "update").contains(scope);
        default -> false;
    };
}
```

---

## User Storage Architecture

### Dual Storage Model

Users in CMIPS are stored in **two separate locations**, each serving a different purpose:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           USER STORAGE ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ESP Registration (Step 5)                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  keycloakAdminService.createUser(username, password, email, ...)    │   │
│  │           │                                                          │   │
│  │           ▼                                                          │   │
│  │  Returns: keycloak_user_id = "abc-123-def-456"                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                          │                                                  │
│           ┌──────────────┴──────────────┐                                  │
│           ▼                              ▼                                  │
│  ┌─────────────────────────┐  ┌─────────────────────────────────┐         │
│  │   KEYCLOAK DATABASE     │  │   APPLICATION DATABASE          │         │
│  │   (Authentication)      │  │   (Business Data)               │         │
│  ├─────────────────────────┤  ├─────────────────────────────────┤         │
│  │                         │  │                                  │         │
│  │  user_entity            │  │  esp_registration               │         │
│  │  ├─ id (UUID)        ◄──┼──┼─► keycloak_user_id              │         │
│  │  ├─ username            │  │  ├─ registration_status         │         │
│  │  ├─ email               │  │  ├─ steps_completed             │         │
│  │  ├─ first_name          │  │  └─ user_type                   │         │
│  │  ├─ last_name           │  │                                  │         │
│  │  └─ enabled             │  │  recipient                       │         │
│  │                         │  │  ├─ id                           │         │
│  │  credential             │  │  ├─ cin (unique identifier)     │         │
│  │  ├─ password_hash       │  │  ├─ county_code                 │         │
│  │  ├─ salt                │  │  ├─ date_of_birth               │         │
│  │  └─ algorithm           │  │  ├─ address                     │         │
│  │                         │  │  └─ case_status                 │         │
│  │  user_role_mapping      │  │                                  │         │
│  │  ├─ user_id             │  │  provider                        │         │
│  │  └─ role_id             │  │  ├─ id                           │         │
│  │      (RECIPIENT/        │  │  ├─ provider_number (unique)    │         │
│  │       PROVIDER)         │  │  ├─ ssn_encrypted               │         │
│  │                         │  │  ├─ enrollment_status           │         │
│  │  user_session           │  │  ├─ certifications              │         │
│  │  ├─ session_id          │  │  └─ background_check_status     │         │
│  │  ├─ user_id             │  │                                  │         │
│  │  └─ last_access         │  │  provider_recipient             │         │
│  │                         │  │  ├─ provider_id                 │         │
│  │                         │  │  ├─ recipient_id                │         │
│  │                         │  │  └─ relationship_status         │         │
│  │                         │  │                                  │         │
│  │                         │  │  timesheet                       │         │
│  │                         │  │  ├─ provider_id                 │         │
│  │                         │  │  ├─ recipient_id                │         │
│  │                         │  │  └─ hours, status, etc.         │         │
│  └─────────────────────────┘  └─────────────────────────────────┘         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### What Goes Where?

| Data Type | Storage Location | Reason |
|-----------|------------------|--------|
| Username | Keycloak | Authentication |
| Password (hashed) | Keycloak | Security best practice |
| Email | Keycloak | Account recovery |
| Roles (RECIPIENT, PROVIDER) | Keycloak | Authorization |
| Sessions | Keycloak | Token management |
| CIN (Client Index Number) | Application DB | Business data |
| Provider Number | Application DB | Business data |
| SSN (encrypted) | Application DB | Business data |
| Address, Phone | Application DB | Business data |
| Timesheets | Application DB | Business data |
| Cases | Application DB | Business data |

### The Link Between Systems

```java
// ESPRegistrationEntity links the two systems
@Entity
@Table(name = "esp_registration")
public class ESPRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;  // Links to Keycloak user_entity.id

    @Enumerated(EnumType.STRING)
    private UserType userType;  // RECIPIENT or PROVIDER

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    // ... other registration metadata
}
```

### Current Development Configuration

Both Keycloak and the application currently share the same database (development only):

```yaml
# docker-compose.yml
keycloak:
  environment:
    KC_DB_URL: jdbc:postgresql://postgres:5432/cmips_mvp

backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cmips_mvp
```

---

## Performance Analysis for 2M Users

### Current Setup Limitations

The current development configuration has several limitations for production scale:

| Component | Current Setup | Issue for 2M Users |
|-----------|--------------|-------------------|
| Keycloak Mode | `start-dev` | No caching, no optimization |
| Keycloak Instances | 1 | Single point of failure, no horizontal scaling |
| Database | Shared | Resource contention |
| Sessions | Local memory | Lost on restart, can't scale |
| Policy Evaluation | Per-request | 50-200ms latency per API call |

### Performance Impact Analysis

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PERFORMANCE IMPACT: 2 MILLION USERS                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  SCENARIO: Peak Login Time (Monday 8 AM)                                    │
│  ─────────────────────────────────────────                                  │
│  Concurrent logins: ~50,000 users (2.5% of total)                          │
│  Login requests/second: ~833 (50K over 60 seconds)                         │
│                                                                             │
│  Current Single Keycloak:                                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Capacity: ~100-200 logins/second                                    │   │
│  │  Result: 4-8x OVERLOADED                                            │   │
│  │  User Experience: 30-60 second login times, timeouts                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  SCENARIO: Authorization Checks                                             │
│  ─────────────────────────────────────────                                  │
│  Active users: ~200,000 (10% concurrent)                                   │
│  API calls/user/minute: ~10                                                │
│  Total auth checks/second: ~33,333                                         │
│                                                                             │
│  Current Policy Evaluation (50-200ms per check):                           │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Required capacity: 33,333 × 100ms = 3,333 seconds of compute/sec   │   │
│  │  Single Keycloak capacity: ~100 evaluations/second                  │   │
│  │  Result: 333x OVERLOADED - System would collapse                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Database Scaling Requirements

```sql
-- Critical indexes for 2M users

-- Keycloak database indexes (usually auto-created)
CREATE INDEX idx_user_entity_username ON user_entity(username);
CREATE INDEX idx_user_entity_email ON user_entity(email);
CREATE INDEX idx_user_entity_realm ON user_entity(realm_id);
CREATE INDEX idx_credential_user ON credential(user_id);
CREATE INDEX idx_user_role_mapping ON user_role_mapping(user_id);

-- Application database indexes
CREATE INDEX idx_esp_reg_keycloak_id ON esp_registration(keycloak_user_id);
CREATE INDEX idx_esp_reg_status ON esp_registration(status);
CREATE INDEX idx_recipient_cin ON recipient(cin);
CREATE INDEX idx_recipient_county ON recipient(county_code);
CREATE INDEX idx_provider_number ON provider(provider_number);
CREATE INDEX idx_provider_ssn ON provider(ssn_encrypted);
CREATE INDEX idx_provider_recipient_rel ON provider_recipient(provider_id, recipient_id);
CREATE INDEX idx_timesheet_provider ON timesheet(provider_id);
CREATE INDEX idx_timesheet_recipient ON timesheet(recipient_id);
CREATE INDEX idx_timesheet_date ON timesheet(service_date);
```

### Database Size Estimates

| Table | Rows | Est. Size | Notes |
|-------|------|-----------|-------|
| user_entity | 2M | ~500 MB | Keycloak users |
| credential | 2M | ~300 MB | Password hashes |
| user_role_mapping | 2M | ~100 MB | Role assignments |
| user_session | 200K | ~200 MB | Active sessions (peak) |
| recipient | 1.6M | ~800 MB | Recipient business data |
| provider | 400K | ~200 MB | Provider business data |
| timesheet | 50M+ | ~10 GB | Historical timesheets |
| **Total** | - | **~15-20 GB** | Initial, grows over time |

---

## Production-Ready Authentication Options

### Option 1: Keycloak Clustering (Current Approach - Scaled)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 1: KEYCLOAK CLUSTERING                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USER STORAGE: Keycloak's PostgreSQL Database                               │
│  POLICY STORAGE: Keycloak Authorization Services                            │
│                                                                             │
│                        ┌─────────────────┐                                  │
│                        │  Load Balancer  │                                  │
│                        │  (HAProxy/ALB)  │                                  │
│                        └────────┬────────┘                                  │
│                                 │                                           │
│              ┌──────────────────┼──────────────────┐                       │
│              │                  │                  │                        │
│              ▼                  ▼                  ▼                        │
│     ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                  │
│     │ Keycloak 1  │    │ Keycloak 2  │    │ Keycloak 3  │                  │
│     │ (Primary)   │    │ (Secondary) │    │ (Secondary) │                  │
│     └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                  │
│            │                  │                  │                          │
│            └──────────────────┼──────────────────┘                          │
│                               │                                             │
│                    ┌──────────┴──────────┐                                  │
│                    │                     │                                  │
│                    ▼                     ▼                                  │
│           ┌─────────────────┐   ┌─────────────────┐                        │
│           │   Infinispan    │   │   PostgreSQL    │                        │
│           │   (Sessions)    │   │   (Users)       │                        │
│           │   Distributed   │   │   Primary +     │                        │
│           │   Cache         │   │   Read Replicas │                        │
│           └─────────────────┘   └─────────────────┘                        │
│                                                                             │
│  PROS:                                                                      │
│  ✓ Single source of truth for users and policies                           │
│  ✓ Native Keycloak features (MFA, social login, etc.)                      │
│  ✓ Built-in session clustering                                             │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Every authorization check requires network call to Keycloak             │
│  ✗ 50-200ms latency per policy evaluation                                  │
│  ✗ Complex cluster management                                              │
│                                                                             │
│  COST: Infrastructure only (~$2,000-5,000/month for 3-node cluster)        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Configuration:**

```yaml
# docker-compose.production.yml
version: '3.8'

services:
  keycloak-1:
    image: quay.io/keycloak/keycloak:23.0.0
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres-primary:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: ${KC_DB_PASSWORD}
      KC_CACHE: ispn
      KC_CACHE_STACK: kubernetes
      KC_HOSTNAME: auth.cmips.ca.gov
      KC_PROXY: edge
      KC_HTTP_ENABLED: "true"
      JAVA_OPTS: "-Xms2g -Xmx4g"
    command: start --optimized
    deploy:
      resources:
        limits:
          memory: 6G
          cpus: '4'

  postgres-primary:
    image: postgres:15
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: ${KC_DB_PASSWORD}
    volumes:
      - keycloak_data:/var/lib/postgresql/data
    deploy:
      resources:
        limits:
          memory: 8G
          cpus: '4'
```

---

### Option 2: Hybrid Model (Keycloak + Local Cache) - RECOMMENDED

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 2: HYBRID MODEL (RECOMMENDED)                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USER STORAGE: Keycloak PostgreSQL (source of truth)                        │
│  CACHE STORAGE: Redis (roles + permissions copy)                            │
│  POLICY STORAGE: Keycloak (synced to Redis)                                 │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                        SPRING BOOT APPLICATION                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                    CACHED POLICY SERVICE                         │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │                    REDIS CACHE                             │  │  │ │
│  │  │  │  ┌─────────────────────────────────────────────────────┐  │  │  │ │
│  │  │  │  │  policy:RECIPIENT:Timesheet:read = PERMIT           │  │  │  │ │
│  │  │  │  │  policy:RECIPIENT:Timesheet:approve = PERMIT        │  │  │  │ │
│  │  │  │  │  policy:PROVIDER:Timesheet:create = PERMIT          │  │  │  │ │
│  │  │  │  │  policy:PROVIDER:Timesheet:submit = PERMIT          │  │  │  │ │
│  │  │  │  │  policy:PROVIDER:EVV:create = PERMIT                │  │  │  │ │
│  │  │  │  │  ...                                                  │  │  │  │ │
│  │  │  │  └─────────────────────────────────────────────────────┘  │  │  │ │
│  │  │  │                          ▲                                 │  │  │ │
│  │  │  │                          │ 0.1ms lookup                    │  │  │ │
│  │  │  └──────────────────────────┼─────────────────────────────────┘  │  │ │
│  │  │                             │                                    │  │ │
│  │  │  Authorization Check:       │                                    │  │ │
│  │  │  1. Check Redis cache ──────┘                                    │  │ │
│  │  │  2. If cache hit → return (0.1ms)                               │  │ │
│  │  │  3. If cache miss → call Keycloak → cache result               │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  │                                    ▲                                   │ │
│  │                                    │ Background sync every 5 min      │ │
│  └────────────────────────────────────┼───────────────────────────────────┘ │
│                                       │                                     │
│                                       ▼                                     │
│                        ┌─────────────────────────┐                         │
│                        │        KEYCLOAK         │                         │
│                        │  ┌─────────────────┐    │                         │
│                        │  │ 2M Users        │    │  ◄── Source of truth   │
│                        │  │ + Roles         │    │                         │
│                        │  │ + Policies      │    │                         │
│                        │  └─────────────────┘    │                         │
│                        └─────────────────────────┘                         │
│                                                                             │
│  PROS:                                                                      │
│  ✓ 0.1ms authorization checks (500x faster than direct Keycloak)          │
│  ✓ Keycloak remains source of truth                                        │
│  ✓ Policy changes sync automatically                                       │
│  ✓ No migration needed from current setup                                  │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Cache can be up to 5 minutes stale                                     │
│  ✗ Additional Redis infrastructure                                         │
│                                                                             │
│  COST: Infrastructure only (~$3,000-6,000/month)                           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Implementation:**

```java
@Service
@Slf4j
public class CachedPolicyEvaluationService {

    private final RedisTemplate<String, PolicyDecision> redisTemplate;
    private final KeycloakPolicyEvaluationService keycloakService;

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String CACHE_PREFIX = "policy:";

    public boolean hasPermission(String userId, Set<String> roles,
                                  String resource, String scope) {
        // Build cache key
        String cacheKey = buildCacheKey(roles, resource, scope);

        // Try cache first (0.1ms)
        PolicyDecision cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT for {}", cacheKey);
            return cached.isPermitted();
        }

        // Cache miss - call Keycloak (50-200ms)
        log.debug("Cache MISS for {}, calling Keycloak", cacheKey);
        boolean permitted = keycloakService.hasPermission(userId, roles, resource, scope);

        // Cache the result
        redisTemplate.opsForValue().set(
            cacheKey,
            new PolicyDecision(permitted),
            CACHE_TTL
        );

        return permitted;
    }

    private String buildCacheKey(Set<String> roles, String resource, String scope) {
        String roleKey = roles.stream().sorted().collect(Collectors.joining(","));
        return CACHE_PREFIX + roleKey + ":" + resource + ":" + scope;
    }

    // Background sync job
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void syncPoliciesFromKeycloak() {
        log.info("Starting policy sync from Keycloak");

        // Sync all role-resource-scope combinations
        List<String> roles = List.of("RECIPIENT", "PROVIDER", "CASEWORKER", "SUPERVISOR");
        List<String> resources = List.of("Timesheet", "Case", "Provider", "Recipient", "EVV");
        List<String> scopes = List.of("read", "create", "update", "delete", "submit", "approve");

        for (String role : roles) {
            for (String resource : resources) {
                for (String scope : scopes) {
                    try {
                        boolean permitted = keycloakService.evaluatePolicy(
                            Set.of(role), resource + " Resource", scope);
                        String cacheKey = CACHE_PREFIX + role + ":" + resource + ":" + scope;
                        redisTemplate.opsForValue().set(
                            cacheKey,
                            new PolicyDecision(permitted),
                            CACHE_TTL.multipliedBy(2) // Longer TTL for pre-fetched
                        );
                    } catch (Exception e) {
                        log.warn("Failed to sync policy: {}", e.getMessage());
                    }
                }
            }
        }

        log.info("Policy sync completed");
    }
}
```

---

### Option 3: Okta / Auth0 (External Identity Provider)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 3: OKTA / AUTH0                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USER STORAGE: Okta/Auth0 Cloud (external, managed)                         │
│  POLICY STORAGE: Okta Authorization Server                                  │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      OKTA / AUTH0 CLOUD                                │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                  OKTA'S INFRASTRUCTURE                           │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  2M Users stored in Okta's database                       │  │  │ │
│  │  │  │  - Username, Email, Password (managed by Okta)            │  │  │ │
│  │  │  │  - Groups: RECIPIENT, PROVIDER                            │  │  │ │
│  │  │  │  - Custom attributes: county_code, cin, provider_number   │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  │                                                                  │  │ │
│  │  │  Authorization Server:                                          │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  Scopes:                                                   │  │  │ │
│  │  │  │  - read:timesheet, create:timesheet, approve:timesheet    │  │  │ │
│  │  │  │  - read:case, update:case                                 │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  Access Policies:                                          │  │  │ │
│  │  │  │  - IF group=RECIPIENT THEN grant read:timesheet           │  │  │ │
│  │  │  │  - IF group=PROVIDER THEN grant create:timesheet          │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ OIDC / OAuth2                          │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      SPRING BOOT APPLICATION                           │ │
│  │  - Validates JWT from Okta                                             │ │
│  │  - Extracts roles/scopes from token claims                            │ │
│  │  - No local user storage (only business data in PostgreSQL)           │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  PROS:                                                                      │
│  ✓ No infrastructure to manage                                             │
│  ✓ Built for massive scale (Okta handles billions of auths)              │
│  ✓ Built-in MFA, social login, adaptive authentication                    │
│  ✓ 99.99% SLA                                                             │
│                                                                             │
│  CONS:                                                                      │
│  ✗ EXPENSIVE: $2-5/user/month = $4M-$10M/year for 2M users               │
│  ✗ Less policy flexibility than Keycloak                                  │
│  ✗ Vendor lock-in                                                         │
│  ✗ Data residency concerns (users stored outside your infrastructure)    │
│                                                                             │
│  COST: $4,000,000 - $10,000,000 per year                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Option 4: Open Policy Agent (OPA)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 4: OPEN POLICY AGENT (OPA)                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USER STORAGE: Keycloak (authentication only)                               │
│  POLICY STORAGE: OPA (Rego files in Git)                                    │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                          KEYCLOAK                                      │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  2M Users (authentication only)                                  │  │ │
│  │  │  - Username, Password, Email                                     │  │ │
│  │  │  - Basic roles: RECIPIENT, PROVIDER                              │  │ │
│  │  │  - NO authorization policies (moved to OPA)                      │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ JWT Token (contains roles)             │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      SPRING BOOT APPLICATION                           │ │
│  │                                │                                       │ │
│  │                                ▼                                       │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                    OPA SIDECAR CONTAINER                         │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  REGO POLICIES (stored in Git, deployed via CI/CD)        │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  # cmips_authz.rego                                        │  │  │ │
│  │  │  │  package cmips.authz                                       │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  default allow = false                                     │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  # RECIPIENT can read and approve timesheets              │  │  │ │
│  │  │  │  allow {                                                   │  │  │ │
│  │  │  │    input.user.roles[_] == "RECIPIENT"                     │  │  │ │
│  │  │  │    input.action in ["read", "approve", "reject"]          │  │  │ │
│  │  │  │    input.resource == "timesheet"                          │  │  │ │
│  │  │  │  }                                                         │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  # PROVIDER can create and submit timesheets              │  │  │ │
│  │  │  │  allow {                                                   │  │  │ │
│  │  │  │    input.user.roles[_] == "PROVIDER"                      │  │  │ │
│  │  │  │    input.action in ["read", "create", "update", "submit"] │  │  │ │
│  │  │  │    input.resource == "timesheet"                          │  │  │ │
│  │  │  │  }                                                         │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  # PROVIDER can only see their own recipients             │  │  │ │
│  │  │  │  allow {                                                   │  │  │ │
│  │  │  │    input.user.roles[_] == "PROVIDER"                      │  │  │ │
│  │  │  │    input.action == "read"                                 │  │  │ │
│  │  │  │    input.resource == "recipient"                          │  │  │ │
│  │  │  │    input.resource_owner == input.user.provider_id         │  │  │ │
│  │  │  │  }                                                         │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  │                                                                  │  │ │
│  │  │  Performance: <1ms per policy evaluation                        │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  PROS:                                                                      │
│  ✓ Sub-millisecond policy evaluation                                       │
│  ✓ Policies as code (Git versioned, PR reviews, CI/CD)                    │
│  ✓ Extremely flexible policy language (Rego)                              │
│  ✓ Can enforce complex business rules                                      │
│  ✓ Free and open source                                                    │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Need to migrate Keycloak policies to Rego                              │
│  ✗ Learning curve for Rego syntax                                         │
│  ✗ Policy changes require deployment (not runtime like Keycloak)          │
│                                                                             │
│  COST: Infrastructure only (~$2,000-4,000/month)                           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Option 5: AWS Cognito

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 5: AWS COGNITO                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USER STORAGE: AWS Cognito User Pool (AWS-managed)                          │
│  POLICY STORAGE: Custom Lambda Authorizer                                   │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                          AWS CLOUD                                     │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                    COGNITO USER POOL                             │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  2M Users (AWS-managed storage)                           │  │  │ │
│  │  │  │  - Username, Password (SRP protocol)                      │  │  │ │
│  │  │  │  - Email, Phone (for MFA)                                 │  │  │ │
│  │  │  │  - Custom attributes: county_code, cin                    │  │  │ │
│  │  │  │  - Groups: RECIPIENT, PROVIDER                            │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                        │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                  COGNITO IDENTITY POOL                           │  │ │
│  │  │  - Maps groups to IAM roles                                      │  │ │
│  │  │  - RECIPIENT → arn:aws:iam::xxx:role/cmips-recipient-role       │  │ │
│  │  │  - PROVIDER → arn:aws:iam::xxx:role/cmips-provider-role         │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                        │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                  LAMBDA AUTHORIZER                               │  │ │
│  │  │  (For fine-grained policies not supported by Cognito)           │  │ │
│  │  │                                                                  │  │ │
│  │  │  exports.handler = async (event) => {                           │  │ │
│  │  │    const token = event.authorizationToken;                      │  │ │
│  │  │    const decoded = jwt.verify(token, publicKey);                │  │ │
│  │  │    const groups = decoded['cognito:groups'];                    │  │ │
│  │  │                                                                  │  │ │
│  │  │    if (groups.includes('RECIPIENT')) {                          │  │ │
│  │  │      return generatePolicy('Allow', ['GET:/timesheets/*']);     │  │ │
│  │  │    }                                                             │  │ │
│  │  │    if (groups.includes('PROVIDER')) {                           │  │ │
│  │  │      return generatePolicy('Allow', [                           │  │ │
│  │  │        'GET:/timesheets/*',                                     │  │ │
│  │  │        'POST:/timesheets',                                      │  │ │
│  │  │        'PUT:/timesheets/*'                                      │  │ │
│  │  │      ]);                                                         │  │ │
│  │  │    }                                                             │  │ │
│  │  │  };                                                              │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ JWT Token                              │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      SPRING BOOT APPLICATION                           │ │
│  │  - Validates JWT from Cognito                                          │ │
│  │  - Business data in RDS PostgreSQL                                     │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  PROS:                                                                      │
│  ✓ AWS-native, auto-scales                                                 │
│  ✓ Cheapest cloud option                                                   │
│  ✓ Built-in MFA, email/SMS verification                                   │
│  ✓ Integrates with other AWS services                                      │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Limited authorization features (need custom Lambda)                    │
│  ✗ Less flexible than Keycloak for complex policies                       │
│  ✗ Vendor lock-in to AWS                                                   │
│                                                                             │
│  COST CALCULATION:                                                          │
│  - First 50,000 MAU: Free                                                  │
│  - Next 50,000 MAU: $0.0055/user = $275/month                              │
│  - Next 1,900,000 MAU: $0.0046/user = $8,740/month                         │
│  - Lambda authorizer: ~$500/month                                          │
│  - Total: ~$9,500/month (~$114,000/year)                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Option 6: User Federation / External Identity Store (LDAP/Custom DB)

This option stores users in an **external system** (LDAP, Active Directory, or Custom Database) while Keycloak handles **only authorization**. Users authenticate against the external store, but Keycloak issues JWT tokens with roles and permissions.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│         OPTION 6: USER FEDERATION (External User Store + Keycloak Authz)    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USER STORAGE: External System (LDAP / Active Directory / Custom DB)        │
│  AUTHENTICATION: External System validates credentials                      │
│  AUTHORIZATION: Keycloak (policies, roles, permissions)                     │
│  TOKEN ISSUANCE: Keycloak (JWT with roles from external + KC policies)     │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    EXTERNAL USER STORE                                 │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  Option A: LDAP / Active Directory                              │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  2M Users stored in LDAP                                  │  │  │ │
│  │  │  │  - uid: johndoe                                           │  │  │ │
│  │  │  │  - userPassword: {SSHA}xxxxx                              │  │  │ │
│  │  │  │  - mail: john@example.com                                 │  │  │ │
│  │  │  │  - memberOf: cn=RECIPIENT,ou=groups,dc=cmips              │  │  │ │
│  │  │  │  - employeeType: RECIPIENT                                │  │  │ │
│  │  │  │  - customAttr: countyCode=SAC, cin=1234567890             │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  │                              OR                                  │  │ │
│  │  │  Option B: Custom PostgreSQL Database                           │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  cmips_users table:                                       │  │  │ │
│  │  │  │  - id, username, password_hash, email                     │  │  │ │
│  │  │  │  - user_type (RECIPIENT/PROVIDER)                         │  │  │ │
│  │  │  │  - county_code, cin, provider_number                      │  │  │ │
│  │  │  │  - is_active, last_login, mfa_enabled                     │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  │                              OR                                  │  │ │
│  │  │  Option C: State Identity Management System (IDM)               │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  California's existing identity infrastructure            │  │  │ │
│  │  │  │  - Federated via SAML/OIDC                               │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ User Federation SPI                    │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                          KEYCLOAK                                      │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                   USER FEDERATION PROVIDER                       │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  - Connects to external user store                        │  │  │ │
│  │  │  │  - Validates credentials against external system          │  │  │ │
│  │  │  │  - Maps external groups → Keycloak roles                  │  │  │ │
│  │  │  │  - NO user data stored in Keycloak                        │  │  │ │
│  │  │  │  - Creates "federated identity" link only                 │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  │                                                                  │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │              AUTHORIZATION SERVICES                        │  │  │ │
│  │  │  │  Resources:                                                │  │  │ │
│  │  │  │  - Timesheet Resource                                     │  │  │ │
│  │  │  │  - Case Resource                                          │  │  │ │
│  │  │  │  - Provider Resource                                      │  │  │ │
│  │  │  │  - Recipient Resource                                     │  │  │ │
│  │  │  │                                                            │  │  │ │
│  │  │  │  Policies:                                                 │  │  │ │
│  │  │  │  - RECIPIENT can read/approve timesheets                  │  │  │ │
│  │  │  │  - PROVIDER can create/submit timesheets                  │  │  │ │
│  │  │  │  - County-based access control                            │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ JWT Token with roles + permissions     │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      SPRING BOOT APPLICATION                           │ │
│  │  - Receives JWT with roles (RECIPIENT/PROVIDER)                       │ │
│  │  - Token contains Keycloak authorization permissions                  │ │
│  │  - Enforces policies using token claims                               │ │
│  │  - Business data in application PostgreSQL                            │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### How User Federation Works

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    USER FEDERATION LOGIN FLOW                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐      ┌──────────────┐      ┌────────────┐    ┌────────────┐  │
│  │  User    │      │  Keycloak    │      │  External  │    │  Spring    │  │
│  │ (Browser)│      │  Server      │      │  User Store│    │  Boot API  │  │
│  └────┬─────┘      └──────┬───────┘      └─────┬──────┘    └─────┬──────┘  │
│       │                   │                    │                  │         │
│       │  1. Login Request │                    │                  │         │
│       │  (username/pass)  │                    │                  │         │
│       │──────────────────►│                    │                  │         │
│       │                   │                    │                  │         │
│       │                   │  2. Federate Auth  │                  │         │
│       │                   │  (LDAP bind or     │                  │         │
│       │                   │   DB query)        │                  │         │
│       │                   │───────────────────►│                  │         │
│       │                   │                    │                  │         │
│       │                   │  3. Auth Result +  │                  │         │
│       │                   │  User Attributes   │                  │         │
│       │                   │  (groups, county)  │                  │         │
│       │                   │◄───────────────────│                  │         │
│       │                   │                    │                  │         │
│       │                   │  4. Map to KC Roles│                  │         │
│       │                   │  LDAP:cn=RECIPIENT │                  │         │
│       │                   │  → KC:RECIPIENT    │                  │         │
│       │                   │                    │                  │         │
│       │                   │  5. Evaluate       │                  │         │
│       │                   │  Authorization     │                  │         │
│       │                   │  Policies          │                  │         │
│       │                   │                    │                  │         │
│       │  6. JWT Token     │                    │                  │         │
│       │  containing:      │                    │                  │         │
│       │  - roles          │                    │                  │         │
│       │  - permissions    │                    │                  │         │
│       │  - custom claims  │                    │                  │         │
│       │◄──────────────────│                    │                  │         │
│       │                   │                    │                  │         │
│       │  7. API Request with JWT              │                  │         │
│       │───────────────────────────────────────────────────────────►│        │
│       │                   │                    │                  │         │
│       │                   │                    │                  │  8. Validate JWT    │
│       │                   │                    │                  │  Check permissions  │
│       │                   │                    │                  │  from token claims  │
│       │                   │                    │                  │         │
│       │  9. Response (if authorized)          │                  │         │
│       │◄───────────────────────────────────────────────────────────│        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### JWT Token Structure with Federation

```json
{
  "exp": 1765425487,
  "iat": 1765425427,
  "iss": "http://keycloak.cmips.ca.gov/realms/cmips",
  "sub": "f:ldap:johndoe",  // Federated identity (not stored in KC)
  "typ": "Bearer",
  "azp": "cmips-backend",

  // Roles mapped from external system
  "realm_access": {
    "roles": ["RECIPIENT"]  // Mapped from LDAP group
  },

  // Keycloak Authorization permissions
  "authorization": {
    "permissions": [
      {
        "rsname": "Timesheet Resource",
        "scopes": ["read", "approve", "reject"]
      },
      {
        "rsname": "Case Resource",
        "scopes": ["read"]
      }
    ]
  },

  // Custom claims from external system
  "county_code": "SAC",        // From LDAP attribute
  "cin": "1234567890",         // From LDAP attribute
  "user_type": "RECIPIENT",    // From LDAP employeeType

  "preferred_username": "johndoe",
  "email": "john@example.com"
}
```

#### Implementation Options

**Option 6A: LDAP Federation (Built-in)**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 6A: LDAP USER FEDERATION                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Keycloak has BUILT-IN support for LDAP/Active Directory federation.       │
│                                                                             │
│  Configuration in Keycloak Admin Console:                                   │
│  1. User Federation → Add Provider → LDAP                                   │
│  2. Configure connection:                                                   │
│     - Connection URL: ldap://ldap.cmips.ca.gov:389                         │
│     - Bind DN: cn=admin,dc=cmips,dc=ca,dc=gov                              │
│     - Users DN: ou=users,dc=cmips,dc=ca,dc=gov                             │
│                                                                             │
│  LDAP Schema for CMIPS Users:                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  dn: uid=johndoe,ou=recipients,ou=users,dc=cmips,dc=ca,dc=gov      │   │
│  │  objectClass: inetOrgPerson                                         │   │
│  │  objectClass: cmipsUser                                             │   │
│  │  uid: johndoe                                                       │   │
│  │  cn: John Doe                                                       │   │
│  │  sn: Doe                                                            │   │
│  │  givenName: John                                                    │   │
│  │  mail: john.doe@example.com                                         │   │
│  │  userPassword: {SSHA}xxxxxxxxxxxxxxxx                               │   │
│  │  memberOf: cn=RECIPIENT,ou=groups,dc=cmips,dc=ca,dc=gov            │   │
│  │  cmipsCountyCode: SAC                                               │   │
│  │  cmipsCIN: 1234567890                                               │   │
│  │  cmipsUserType: RECIPIENT                                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  Group-to-Role Mapping:                                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  LDAP Group                    →    Keycloak Role                   │   │
│  │  cn=RECIPIENT,ou=groups,...   →    RECIPIENT                        │   │
│  │  cn=PROVIDER,ou=groups,...    →    PROVIDER                         │   │
│  │  cn=CASEWORKER,ou=groups,...  →    CASEWORKER                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  Attribute Mapping (for custom claims in JWT):                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  LDAP Attribute        →    JWT Claim                               │   │
│  │  cmipsCountyCode       →    county_code                             │   │
│  │  cmipsCIN              →    cin                                     │   │
│  │  cmipsProviderNumber   →    provider_number                         │   │
│  │  cmipsUserType         →    user_type                               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  PROS:                                                                      │
│  ✓ Zero code required - Keycloak built-in feature                          │
│  ✓ Keycloak never stores user passwords                                    │
│  ✓ LDAP scales to millions of users easily                                │
│  ✓ Full Keycloak authorization policies still work                        │
│  ✓ Can use existing state LDAP infrastructure                             │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Need to set up/maintain LDAP server                                    │
│  ✗ LDAP schema must include CMIPS-specific attributes                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Option 6B: Custom User Storage Provider (Custom Database)**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 6B: CUSTOM USER STORAGE PROVIDER                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  For storing users in YOUR OWN PostgreSQL database (not Keycloak's).       │
│  Keycloak calls your custom provider to authenticate users.                 │
│                                                                             │
│  Architecture:                                                              │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      YOUR APPLICATION DATABASE                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  cmips_external_users table:                                    │  │ │
│  │  │  ┌───────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │  id            BIGSERIAL PRIMARY KEY                      │  │  │ │
│  │  │  │  username      VARCHAR(100) UNIQUE NOT NULL               │  │  │ │
│  │  │  │  password_hash VARCHAR(255) NOT NULL                      │  │  │ │
│  │  │  │  email         VARCHAR(255)                               │  │  │ │
│  │  │  │  first_name    VARCHAR(100)                               │  │  │ │
│  │  │  │  last_name     VARCHAR(100)                               │  │  │ │
│  │  │  │  user_type     VARCHAR(20) -- RECIPIENT or PROVIDER       │  │  │ │
│  │  │  │  county_code   VARCHAR(10)                                │  │  │ │
│  │  │  │  cin           VARCHAR(20)                                │  │  │ │
│  │  │  │  provider_num  VARCHAR(20)                                │  │  │ │
│  │  │  │  is_active     BOOLEAN DEFAULT true                       │  │  │ │
│  │  │  │  mfa_secret    VARCHAR(255)                               │  │  │ │
│  │  │  │  created_at    TIMESTAMP                                  │  │  │ │
│  │  │  │  last_login    TIMESTAMP                                  │  │  │ │
│  │  │  └───────────────────────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    ▲                                        │
│                                    │ JDBC Connection                        │
│                                    │                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │              KEYCLOAK CUSTOM USER STORAGE SPI                          │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  @Component                                                      │  │ │
│  │  │  public class CmipsUserStorageProvider                          │  │ │
│  │  │      implements UserStorageProvider,                             │  │ │
│  │  │                 UserLookupProvider,                              │  │ │
│  │  │                 CredentialInputValidator {                       │  │ │
│  │  │                                                                  │  │ │
│  │  │    // Called when user tries to login                           │  │ │
│  │  │    public UserModel getUserByUsername(String username) {        │  │ │
│  │  │      // Query YOUR database                                     │  │ │
│  │  │      CmipsUser user = userRepository.findByUsername(username); │  │ │
│  │  │      return new CmipsUserAdapter(user, session, realm);        │  │ │
│  │  │    }                                                            │  │ │
│  │  │                                                                  │  │ │
│  │  │    // Validate password against YOUR database                   │  │ │
│  │  │    public boolean isValid(CredentialInput input) {              │  │ │
│  │  │      String password = input.getChallengeResponse();           │  │ │
│  │  │      return passwordEncoder.matches(password, user.getHash()); │  │ │
│  │  │    }                                                            │  │ │
│  │  │                                                                  │  │ │
│  │  │    // Map user_type to Keycloak role                            │  │ │
│  │  │    public Stream<RoleModel> getRoleMappingsStream() {          │  │ │
│  │  │      if ("RECIPIENT".equals(user.getUserType())) {             │  │ │
│  │  │        return Stream.of(realm.getRole("RECIPIENT"));           │  │ │
│  │  │      }                                                          │  │ │
│  │  │      return Stream.of(realm.getRole("PROVIDER"));              │  │ │
│  │  │    }                                                            │  │ │
│  │  │  }                                                               │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  PROS:                                                                      │
│  ✓ Complete control over user storage                                      │
│  ✓ Users stored in YOUR database (full ownership)                         │
│  ✓ Can integrate with existing user tables                                │
│  ✓ Keycloak authorization policies still work                             │
│  ✓ No LDAP infrastructure needed                                          │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Requires custom Java code (Keycloak SPI)                               │
│  ✗ Must deploy custom JAR to Keycloak                                     │
│  ✗ More maintenance than built-in LDAP                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Option 6C: Identity Brokering (External IdP)**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPTION 6C: IDENTITY BROKERING                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Use an EXTERNAL Identity Provider for authentication, Keycloak for authz. │
│  Users authenticate with California state IdP, Keycloak adds permissions.  │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │              EXTERNAL IDENTITY PROVIDER                                │ │
│  │  (California State SSO / Login.gov / Custom IdP)                      │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  - 2M Users stored here                                         │  │ │
│  │  │  - Handles authentication (username/password/MFA)               │  │ │
│  │  │  - Issues SAML assertion or OIDC token                         │  │ │
│  │  │  - Contains user attributes (county, CIN, etc.)                │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ SAML / OIDC                            │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                          KEYCLOAK                                      │ │
│  │  (Identity Broker + Authorization Server)                             │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  1. Receives assertion from external IdP                        │  │ │
│  │  │  2. Creates "brokered" user identity (no password stored)       │  │ │
│  │  │  3. Maps external attributes to Keycloak roles                  │  │ │
│  │  │  4. Applies Keycloak authorization policies                     │  │ │
│  │  │  5. Issues Keycloak JWT with permissions                        │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ JWT with roles + permissions           │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      SPRING BOOT APPLICATION                           │ │
│  │  - Validates Keycloak JWT                                             │ │
│  │  - Enforces authorization from token                                  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  Flow:                                                                      │
│  1. User clicks "Login" → Redirected to external IdP                       │
│  2. User authenticates with external IdP                                   │
│  3. External IdP redirects to Keycloak with SAML/OIDC token               │
│  4. Keycloak maps user to roles, evaluates policies                       │
│  5. Keycloak issues its own JWT with CMIPS permissions                    │
│  6. Application receives Keycloak JWT (same as any other flow)            │
│                                                                             │
│  PROS:                                                                      │
│  ✓ Zero user management in Keycloak                                        │
│  ✓ Leverage existing state identity infrastructure                        │
│  ✓ SSO across multiple state applications                                 │
│  ✓ Full Keycloak authorization features available                         │
│                                                                             │
│  CONS:                                                                      │
│  ✗ Depends on external IdP availability                                   │
│  ✗ More complex login flow (redirects)                                    │
│  ✗ Must coordinate with external IdP team                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Custom User Storage Provider Implementation

```java
/**
 * Keycloak Custom User Storage Provider
 * Stores users in external PostgreSQL, Keycloak handles authorization only
 */
@RequiredArgsConstructor
public class CmipsUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,
        CredentialInputValidator,
        CredentialInputUpdater {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final CmipsUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ============ USER LOOKUP ============

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        String externalId = StorageId.externalId(id);
        CmipsExternalUser user = userRepository.findById(Long.parseLong(externalId));
        return user != null ? new CmipsUserAdapter(session, realm, model, user) : null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        CmipsExternalUser user = userRepository.findByUsername(username);
        return user != null ? new CmipsUserAdapter(session, realm, model, user) : null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        CmipsExternalUser user = userRepository.findByEmail(email);
        return user != null ? new CmipsUserAdapter(session, realm, model, user) : null;
    }

    // ============ CREDENTIAL VALIDATION ============

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType())) return false;

        CmipsExternalUser externalUser = userRepository.findByUsername(user.getUsername());
        if (externalUser == null) return false;

        String password = input.getChallengeResponse();
        boolean valid = passwordEncoder.matches(password, externalUser.getPasswordHash());

        if (valid) {
            // Update last login in external DB
            userRepository.updateLastLogin(externalUser.getId());
        }

        return valid;
    }

    // ============ USER COUNT (for admin console) ============

    @Override
    public int getUsersCount(RealmModel realm) {
        return userRepository.countAll();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm,
            Map<String, String> params, Integer firstResult, Integer maxResults) {
        String search = params.get(UserModel.SEARCH);
        return userRepository.search(search, firstResult, maxResults)
            .stream()
            .map(user -> new CmipsUserAdapter(session, realm, model, user));
    }
}

/**
 * Adapter that wraps external user as Keycloak UserModel
 */
public class CmipsUserAdapter extends AbstractUserAdapterFederatedStorage {

    private final CmipsExternalUser user;

    public CmipsUserAdapter(KeycloakSession session, RealmModel realm,
                           ComponentModel model, CmipsExternalUser user) {
        super(session, realm, model);
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();
    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    // ============ ROLE MAPPING FROM EXTERNAL USER TYPE ============

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        // Map external user_type to Keycloak role
        String userType = user.getUserType(); // RECIPIENT or PROVIDER
        RoleModel role = realm.getRole(userType);
        return role != null ? Stream.of(role) : Stream.empty();
    }

    // ============ CUSTOM ATTRIBUTES (for JWT claims) ============

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = new HashMap<>();

        // These will appear as claims in the JWT token
        attrs.put("county_code", List.of(user.getCountyCode()));
        attrs.put("user_type", List.of(user.getUserType()));

        if (user.getCin() != null) {
            attrs.put("cin", List.of(user.getCin()));
        }
        if (user.getProviderNumber() != null) {
            attrs.put("provider_number", List.of(user.getProviderNumber()));
        }

        return attrs;
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(model, String.valueOf(user.getId()));
    }
}
```

#### External User Database Schema

```sql
-- External user storage (NOT in Keycloak database)
-- This can be in your application database or a dedicated auth database

CREATE TABLE cmips_external_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,  -- BCrypt hash
    email           VARCHAR(255) UNIQUE,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    user_type       VARCHAR(20) NOT NULL,   -- 'RECIPIENT' or 'PROVIDER'
    county_code     VARCHAR(10),
    cin             VARCHAR(20),            -- For recipients
    provider_number VARCHAR(20),            -- For providers
    is_active       BOOLEAN DEFAULT true,
    mfa_enabled     BOOLEAN DEFAULT false,
    mfa_secret      VARCHAR(255),
    failed_attempts INTEGER DEFAULT 0,
    locked_until    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP
);

-- Indexes for performance with 2M users
CREATE INDEX idx_ext_users_username ON cmips_external_users(username);
CREATE INDEX idx_ext_users_email ON cmips_external_users(email);
CREATE INDEX idx_ext_users_type ON cmips_external_users(user_type);
CREATE INDEX idx_ext_users_county ON cmips_external_users(county_code);
CREATE INDEX idx_ext_users_cin ON cmips_external_users(cin);
CREATE INDEX idx_ext_users_provider ON cmips_external_users(provider_number);
CREATE INDEX idx_ext_users_active ON cmips_external_users(is_active) WHERE is_active = true;

-- Partition by user_type for better performance
-- Recipients: 1.6M users
-- Providers: 400K users
CREATE TABLE cmips_external_users_recipients PARTITION OF cmips_external_users
    FOR VALUES IN ('RECIPIENT');

CREATE TABLE cmips_external_users_providers PARTITION OF cmips_external_users
    FOR VALUES IN ('PROVIDER');
```

#### Keycloak Configuration for User Federation

```yaml
# realm-export.json (partial)
{
  "realm": "cmips",
  "userFederationProviders": [
    {
      "name": "cmips-external-users",
      "providerId": "cmips-user-storage",  // Your custom SPI
      "providerType": "org.keycloak.storage.UserStorageProvider",
      "priority": 0,
      "config": {
        "jdbcUrl": ["jdbc:postgresql://user-db.cmips.ca.gov:5432/cmips_users"],
        "dbUsername": ["cmips_readonly"],
        "dbPassword": ["${CMIPS_USER_DB_PASSWORD}"],
        "cachePolicy": ["NO_CACHE"],  // Always fetch from external DB
        "importEnabled": ["false"],   // Don't import users to KC
        "syncRegistrations": ["false"]
      }
    }
  ],

  // Authorization still configured in Keycloak
  "clients": [
    {
      "clientId": "cmips-backend",
      "authorizationServicesEnabled": true,
      "authorizationSettings": {
        "resources": [
          { "name": "Timesheet Resource", "scopes": ["read", "create", "approve"] },
          { "name": "Case Resource", "scopes": ["read", "update"] }
        ],
        "policies": [
          {
            "name": "RECIPIENT Policy",
            "type": "role",
            "logic": "POSITIVE",
            "config": { "roles": ["RECIPIENT"] }
          },
          {
            "name": "PROVIDER Policy",
            "type": "role",
            "logic": "POSITIVE",
            "config": { "roles": ["PROVIDER"] }
          }
        ],
        "permissions": [
          {
            "name": "Recipient Timesheet Permission",
            "type": "scope",
            "resources": ["Timesheet Resource"],
            "scopes": ["read", "approve"],
            "policies": ["RECIPIENT Policy"]
          },
          {
            "name": "Provider Timesheet Permission",
            "type": "scope",
            "resources": ["Timesheet Resource"],
            "scopes": ["read", "create"],
            "policies": ["PROVIDER Policy"]
          }
        ]
      }
    }
  ]
}
```

#### Option 6 Summary

| Sub-Option | User Storage | Best For | Complexity |
|------------|--------------|----------|------------|
| **6A: LDAP** | LDAP/Active Directory | Orgs with existing LDAP | Low (built-in) |
| **6B: Custom DB** | Your PostgreSQL | Full control, no LDAP | Medium (custom SPI) |
| **6C: Identity Broker** | External IdP | State SSO integration | Low-Medium |

**Why Option 6 is Great for CMIPS:**

1. **Keycloak doesn't store 2M users** - External system handles the scale
2. **Full authorization policies** - Keycloak still enforces all permissions
3. **JWT contains everything** - Roles + permissions in token
4. **Separation of concerns** - Auth in one place, authz in another
5. **Flexibility** - Can use LDAP, custom DB, or external IdP

---

## Recommendation

### Decision Matrix

| Criteria | Weight | Keycloak Cluster | Hybrid (KC+Cache) | Okta/Auth0 | OPA | AWS Cognito | User Federation |
|----------|--------|------------------|-------------------|------------|-----|-------------|-----------------|
| **Performance** | 25% | 2/5 | 5/5 | 4/5 | 5/5 | 4/5 | 4/5 |
| **Policy Flexibility** | 25% | 5/5 | 5/5 | 3/5 | 5/5 | 2/5 | 5/5 |
| **Cost (2M users)** | 20% | 4/5 | 4/5 | 1/5 | 4/5 | 5/5 | 5/5 |
| **Operational Complexity** | 15% | 2/5 | 3/5 | 5/5 | 3/5 | 4/5 | 3/5 |
| **Migration Effort** | 15% | 5/5 | 5/5 | 2/5 | 3/5 | 2/5 | 4/5 |
| **Scalability (2M users)** | - | 3/5 | 4/5 | 5/5 | 4/5 | 5/5 | 5/5 |
| **Weighted Score** | 100% | **3.35** | **4.45** | **2.85** | **4.05** | **3.40** | **4.35** |

### Updated Recommendation

Based on your requirement to **not store users in Keycloak** while still enforcing Keycloak policies, here are the top recommendations:

| Rank | Option | Best For |
|------|--------|----------|
| **1st** | **Hybrid + User Federation** | Maximum performance + external user storage |
| **2nd** | **User Federation (6B: Custom DB)** | Full control, users in your PostgreSQL |
| **3rd** | **User Federation (6A: LDAP)** | If you have existing LDAP infrastructure |
| **4th** | **Hybrid (Option 2)** | If you must store users in Keycloak |

### Best Architecture: Hybrid + User Federation (Combined)

This combines **Option 6 (User Federation)** with **Option 2 (Hybrid Caching)** for the best of both worlds:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│          RECOMMENDED: HYBRID + USER FEDERATION ARCHITECTURE                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                 EXTERNAL USER DATABASE (Your PostgreSQL)               │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  cmips_external_users: 2M users                                 │  │ │
│  │  │  - Credentials (username, password_hash)                        │  │ │
│  │  │  - User type (RECIPIENT/PROVIDER)                               │  │ │
│  │  │  - Attributes (county_code, cin, provider_number)               │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ User Federation SPI                    │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                          KEYCLOAK                                      │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  - NO users stored in Keycloak                                  │  │ │
│  │  │  - User Federation Provider connects to external DB             │  │ │
│  │  │  - Authorization policies ONLY                                  │  │ │
│  │  │  - Issues JWT with roles + permissions                          │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ Sync policies every 5 min              │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                        REDIS CACHE                                     │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  policy:RECIPIENT:Timesheet:read = PERMIT                       │  │ │
│  │  │  policy:RECIPIENT:Timesheet:approve = PERMIT                    │  │ │
│  │  │  policy:PROVIDER:Timesheet:create = PERMIT                      │  │ │
│  │  │  policy:PROVIDER:EVV:create = PERMIT                            │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ 0.1ms policy lookup                    │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      SPRING BOOT APPLICATION                           │ │
│  │  1. Receives JWT from Keycloak                                        │ │
│  │  2. Extracts roles (RECIPIENT/PROVIDER) from token                    │ │
│  │  3. Checks Redis cache for permissions (0.1ms)                        │ │
│  │  4. Enforces authorization                                            │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  BENEFITS:                                                                  │
│  ✓ Users NOT stored in Keycloak (external DB handles 2M users)            │
│  ✓ Keycloak policies still enforced (via JWT + Redis cache)              │
│  ✓ 0.1ms authorization checks (500x faster)                               │
│  ✓ Full control over user data                                            │
│  ✓ No expensive per-user licensing                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**How Authorization Works with User Federation:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│            AUTHORIZATION FLOW WITH EXTERNAL USER STORE                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. RECIPIENT logs in:                                                      │
│     - Keycloak federates auth to external PostgreSQL                       │
│     - External DB validates password                                        │
│     - Returns user_type = "RECIPIENT", county_code = "SAC"                 │
│                                                                             │
│  2. Keycloak issues JWT:                                                    │
│     {                                                                       │
│       "sub": "f:external:johndoe",                                         │
│       "realm_access": { "roles": ["RECIPIENT"] },  // From external DB     │
│       "county_code": "SAC",                        // From external DB     │
│       "cin": "1234567890",                         // From external DB     │
│       "authorization": {                                                    │
│         "permissions": [                           // From Keycloak        │
│           { "rsname": "Timesheet", "scopes": ["read", "approve"] }        │
│         ]                                                                  │
│       }                                                                     │
│     }                                                                       │
│                                                                             │
│  3. RECIPIENT hits API: GET /api/timesheets                                │
│     - Spring Boot extracts roles from JWT                                  │
│     - Checks Redis: "policy:RECIPIENT:Timesheet:read" = PERMIT            │
│     - Request allowed (0.1ms check)                                        │
│                                                                             │
│  4. RECIPIENT hits API: POST /api/timesheets (create)                      │
│     - Checks Redis: "policy:RECIPIENT:Timesheet:create" = DENY            │
│     - Request blocked (403 Forbidden)                                      │
│                                                                             │
│  5. PROVIDER logs in and hits: POST /api/timesheets                        │
│     - Checks Redis: "policy:PROVIDER:Timesheet:create" = PERMIT           │
│     - Request allowed                                                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Why This Architecture is Best for CMIPS:**

1. **Users NOT in Keycloak**: 2M users stored in your PostgreSQL, not Keycloak
2. **Full Policy Enforcement**: Keycloak policies still work (roles mapped from external DB)
3. **Blazing Fast**: 0.1ms authorization via Redis cache
4. **Cost Effective**: No per-user licensing, just infrastructure (~$5K/month)
5. **Separation of Concerns**:
   - External DB = User credentials + business attributes
   - Keycloak = Authorization policies only
   - Redis = Fast policy cache
6. **Flexibility**: Can use LDAP, custom DB, or external IdP as user store

### Architecture Summary

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              RECOMMENDED PRODUCTION ARCHITECTURE                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                         ┌─────────────────┐                                 │
│                         │  Load Balancer  │                                 │
│                         │  (AWS ALB)      │                                 │
│                         └────────┬────────┘                                 │
│                                  │                                          │
│            ┌─────────────────────┼─────────────────────┐                   │
│            │                     │                     │                    │
│            ▼                     ▼                     ▼                    │
│   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐         │
│   │  Spring Boot 1  │   │  Spring Boot 2  │   │  Spring Boot 3  │         │
│   │  + Redis Client │   │  + Redis Client │   │  + Redis Client │         │
│   └────────┬────────┘   └────────┬────────┘   └────────┬────────┘         │
│            │                     │                     │                    │
│            └─────────────────────┼─────────────────────┘                   │
│                                  │                                          │
│                    ┌─────────────┴─────────────┐                           │
│                    │                           │                            │
│                    ▼                           ▼                            │
│         ┌───────────────────┐      ┌───────────────────┐                   │
│         │   Redis Cluster   │      │  Keycloak Cluster │                   │
│         │   (Policy Cache)  │      │  (Source of Truth)│                   │
│         │                   │      │                   │                   │
│         │  - 0.1ms lookups  │◄─────│  - 2M users       │                   │
│         │  - 5 min TTL      │ sync │  - Policies       │                   │
│         │  - HA with        │      │  - 3 nodes        │                   │
│         │    Sentinel       │      │                   │                   │
│         └───────────────────┘      └─────────┬─────────┘                   │
│                                              │                              │
│                                              ▼                              │
│                                   ┌───────────────────┐                    │
│                                   │    PostgreSQL     │                    │
│                                   │  (Keycloak DB)    │                    │
│                                   │  Primary + 2 Read │                    │
│                                   │  Replicas         │                    │
│                                   └───────────────────┘                    │
│                                                                             │
│  METRICS:                                                                   │
│  ├─ Authentication: 500-1000 logins/second                                 │
│  ├─ Authorization: 100,000+ checks/second                                  │
│  ├─ Latency: <1ms for 99% of auth checks                                  │
│  └─ Availability: 99.9% (multi-AZ deployment)                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Implementation Guide

### Phase 1: Add Redis Caching (Week 1-2)

1. Add Redis dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. Configure Redis in `application.yml`:

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    lettuce:
      pool:
        max-active: 50
        max-idle: 10
        min-idle: 5
```

3. Implement `CachedPolicyEvaluationService` (see Option 2 code above)

4. Update `SecurityConfig` to use cached service

### Phase 2: Keycloak Production Setup (Week 2-3)

1. Switch to production mode:

```yaml
keycloak:
  command: start --optimized --hostname=auth.cmips.ca.gov
  environment:
    KC_PROXY: edge
    KC_HTTP_ENABLED: "false"
    KC_HOSTNAME_STRICT: "true"
```

2. Add database indexes (see SQL above)

3. Configure Infinispan for session clustering

### Phase 3: Monitoring & Optimization (Week 3-4)

1. Add Prometheus metrics:

```java
@Bean
MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config()
        .commonTags("application", "cmips-backend");
}
```

2. Create Grafana dashboards for:
   - Cache hit/miss ratio
   - Authentication latency
   - Policy evaluation times
   - Active sessions

3. Set up alerts for:
   - Cache miss rate > 10%
   - Auth latency > 500ms
   - Failed login spike

---

## Appendix

### A. Database Schema for User Storage

```sql
-- Keycloak tables (managed by Keycloak)
-- user_entity, credential, user_role_mapping, etc.

-- Application tables
CREATE TABLE esp_registration (
    id BIGSERIAL PRIMARY KEY,
    keycloak_user_id VARCHAR(36) UNIQUE,
    user_type VARCHAR(20) NOT NULL, -- RECIPIENT or PROVIDER
    status VARCHAR(20) NOT NULL,
    step_completed INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recipient (
    id BIGSERIAL PRIMARY KEY,
    cin VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    county_code VARCHAR(10),
    address TEXT,
    case_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE provider (
    id BIGSERIAL PRIMARY KEY,
    provider_number VARCHAR(20) UNIQUE NOT NULL,
    ssn_encrypted VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    enrollment_status VARCHAR(20),
    background_check_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### B. Cost Comparison Summary

| Option | Monthly Cost | Annual Cost | Notes |
|--------|-------------|-------------|-------|
| Keycloak Cluster | $2,000-5,000 | $24K-60K | Infrastructure only |
| Hybrid (Recommended) | $3,000-6,000 | $36K-72K | KC + Redis |
| Okta | $333K-833K | $4M-10M | Per-user licensing |
| OPA | $2,000-4,000 | $24K-48K | KC + OPA sidecar |
| AWS Cognito | $8,000-12,000 | $96K-144K | MAU pricing |

### C. Related Files

| File | Purpose |
|------|---------|
| `ESPRegistrationService.java` | 5-step ESP registration implementation |
| `AuthController.java` | Login endpoint, Keycloak token exchange |
| `KeycloakPolicyEvaluationService.java` | Current policy enforcement |
| `SecurityConfig.java` | Spring Security configuration |
| `docker-compose.yml` | Current infrastructure setup |
| `application.yml` | Application configuration |

---

*Document Version: 1.0*
*Last Updated: December 2024*
*Author: CMIPS Development Team*
