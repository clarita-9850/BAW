# IHSS CMIPS - Electronic Services Portal (ESP) Documentation
## Part 1: Overview, Business Processes, and Registration
### Release 2025.03.01 - DSD Section 4

---

## TABLE OF CONTENTS - PART 1
1. [ESP Overview](#overview)
2. [User Types](#user-types)
3. [Registration Process](#registration)
4. [E-Timesheet System](#e-timesheets)
5. [EVV Requirements](#evv)

---

## 1. ESP OVERVIEW {#overview}

### Purpose
The IHSS Electronic Services Portal (ESP) is a web-based self-service application for IHSS/WPCS providers and recipients to manage timesheets, payments, and program activities electronically.

### Access
- **URL:** https://www.etimesheets.ihss.ca.gov
- **Browsers:** Chrome, Firefox, Safari, Edge
- **Mobile:** Responsive design
- **Accessibility:** Section 508 compliant

### Key Features

#### For Providers:
1. **Electronic Timesheets** - Submit time electronically, view status, EVV support
2. **Payment Management** - View 3-month history, download pay stubs, W-2 forms
3. **Sick Leave** - Submit claims, view available hours
4. **Direct Deposit** - Enroll, update, cancel
5. **Travel Claims** - Submit travel time (7 hours/week max)
6. **Live-In Certification** - Certify/cancel live-in status
7. **Career Pathways** - Training time and incentive claims
8. **Account Management** - Update contact info, preferences

#### For Recipients:
1. **Timesheet Review** - Approve/reject provider timesheets
2. **Hire Provider** - Search and hire providers
3. **Payment History** - View payments to providers
4. **Account Management** - Update contact info

### Integration
- CMIPS Case Management (user authentication, data validation)
- MAS Payroll (timesheet processing, payments)
- CDSS Career Pathways (claim approval)
- TTS (Telephone Timesheet System - alternative to ESP)

---

## 2. USER TYPES {#user-types}

### Provider User

**Eligibility:**
- Active IHSS/WPCS provider
- Valid provider number
- Valid SSN or Applied for SSN
- Can register even if not assigned to recipient

**Access Requirements:**
- Email address
- Security questions/answers
- Unique username/password

**Dual Role:**
Providers who are also recipients need TWO accounts:
- One for provider functions
- One for recipient functions

### Recipient User

**Eligibility:**
- IHSS/WPCS recipient
- Case in Eligible/Presumptive Eligible status
- Age 18+ (for Hire Provider function)

**Access Requirements:**
- Email or phone number
- Security questions/answers
- Unique username/password

**Timesheet Review Options:**
1. **ESP:** Web-based review
2. **TTS:** Telephone Timesheet System
Can use either or both interchangeably

### Timesheet Communication Preference
Recipients choose notification method:
- **Email:** Receive email when timesheet ready
- **Telephone:** Receive automated call

Set in CMIPS Case Management

### Timesheet Accommodation (TTS)
For TTS users:
- **Press or Say Line:** English/Spanish only
- **DIALEVV Line:** All other recipients

---

## 3. REGISTRATION PROCESS {#registration}

### Provider Registration - 5 Steps

#### STEP 1: Validate Identity

**Screen:** Register Step 1

**Fields:**

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| I am a | Radio | Yes | Provider or Recipient |
| Provider Number | Text | Yes | 9 digits |
| Last Name | Text | Yes | Must match CMIPS |
| First Name | Text | Yes | Must match CMIPS |
| Date of Birth | Date | Yes | MM/DD/YYYY |
| Last 4 of SSN | Number | Yes | 4 digits |

**Validation:**
- All fields must match CMIPS exactly
- Provider must exist in system
- Provider cannot already be registered (unless inactive)

**Error Messages:**
- "Provider information does not match our records"
- "This provider is already registered. Please use Forgot Username"
- "Provider number must be 9 digits"

**Buttons:** Next, Cancel

---

#### STEP 2: Verify Email

**Screen:** Register Step 2

**Fields:**

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| Email Address | Email | Yes | Valid format |
| Confirm Email | Email | Yes | Must match |

**Process:**
1. User enters email twice
2. System sends 6-digit verification code
3. User enters code
4. System validates code
5. Proceed to Step 3

**Verification Code Entry:**
- 6-digit code
- 15-minute expiration
- Can resend (max 3 times/hour)

**Buttons:** Verify, Resend Code, Back, Cancel

**Error Messages:**
- "Verification code is incorrect"
- "Verification code has expired"
- "Email addresses do not match"

---

#### STEP 3: Create Username

**Screen:** Register Step 3

**Fields:**

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| Username | Text | Yes | 6-20 characters |
| Confirm Username | Text | Yes | Must match |

**Username Rules:**
- 6-20 characters
- Letters, numbers, underscore, hyphen only
- No spaces or special characters
- Must be unique
- Case-insensitive

**Real-Time Validation:**
- ✓ Green checkmark if available
- ✗ Red X if unavailable

**Buttons:** Next, Back, Cancel

**Error Messages:**
- "Username is already taken"
- "Username must be 6-20 characters"
- "Username can only contain letters, numbers, underscores, and hyphens"

---

#### STEP 4: Create Password

**Screen:** Register Step 4

**Fields:**

| Field | Type | Required |
|-------|------|----------|
| Password | Password | Yes |
| Confirm Password | Password | Yes |

**Password Requirements:**
- Minimum 8 characters, maximum 50
- At least one uppercase (A-Z)
- At least one lowercase (a-z)
- At least one number (0-9)
- At least one special (!@#$%^&*)
- Cannot contain username
- Cannot match last 5 passwords

**Password Strength Indicator:**
- Weak (red) / Medium (yellow) / Strong (green)
- Checkmarks for each requirement met

**Buttons:** Next, Back, Cancel

**Error Messages:**
- "Password must be at least 8 characters"
- "Password must contain at least one uppercase letter"
- "Passwords do not match"

---

#### STEP 5: Security Questions

**Screen:** Register Step 5

**Purpose:** Set up account recovery

**Fields:** For each of 3 questions:

| Field | Type | Required |
|-------|------|----------|
| Security Question | Dropdown | Yes |
| Answer | Text | Yes (min 3 chars) |

**Question Options:**
- What is your mother's maiden name?
- What is the name of your first pet?
- What city were you born in?
- What is your favorite color?
- What is the name of your elementary school?
- What is your favorite food?
- What is the street you grew up on?
- What is your father's middle name?
- What is the name of your first employer?
- What is your favorite movie?

**Rules:**
- Must select 3 different questions
- Answers min 3 characters
- Answers case-insensitive
- Answers cannot be username/password

**Buttons:** Complete Registration, Back, Cancel

**Validation:**
- All 3 questions must be different
- All answers required
- Answers cannot be blank

---

### Registration Complete

**Success Screen:**
- Confirmation message
- Username reminder
- Login link

**Email Sent:** Welcome email (ETSE22)
- Welcome message
- Username
- Login link
- Help desk contact

**Privacy Email:** Annual privacy policy (ETSE57)
- Sent each January
- Personal information collection notice
- Data usage
- User rights

---

### Recipient Registration

**Same 5-step process with differences:**

**Step 1 Fields:**
- Case Number (instead of Provider Number)
- Last 4 of CIN (instead of SSN)

**Validation:**
- Case must be Eligible/Presumptive Eligible
- All fields match CMIPS recipient record

**Registration Status in CMIPS:**
- Not Registered
- Registered – ESP
- Registered – TTS
- Registered – Both
- Inactive

---

## 4. E-TIMESHEET SYSTEM {#e-timesheets}

### Overview
E-Timesheets replace paper timesheets when both provider and recipient are registered electronically.

### Prerequisites

**Provider Must Be:**
- Registered for ESP OR
- Registered for TTS

**Recipient Must Be:**
- Registered for ESP OR
- Registered for TTS

**Both Must Be Registered:** E-Timesheets only work when BOTH are registered for at least one method.

### Transition from Paper to Electronic

**Paper Stops When:**
1. Provider submits AND recipient approves electronically, OR
2. CaseProviderEVVEffectiveDate reached

**Until Then:**
- Paper timesheets auto-generated
- Can submit paper OR electronic (paid once only)
- Outstanding paper can be submitted in ESP

### E-Timesheet vs EVV Timesheet

**E-Timesheet (Non-EVV):**
- Standard electronic timesheet
- ESP or TTS
- No location verification required

**EVV Timesheet:**
- Electronic Visit Verification
- Must submit electronically
- Requires check-in/check-out with location
- ESP or TTS interchangeable

**EVV Effective Date:**
- CaseProviderEVVEffectiveDate per relationship
- After date: paper not accepted
- Electronic submission required

### Timesheet Workflow

**1. Provider Time Entry**
- Enter hours for pay period
- Daily or weekly entry
- System validates vs authorized hours
- Checks overtime violations

**2. Provider Submission**
- Electronic signature
- Certification of accuracy
- Status: "Submitted"
- Recipient notified

**3. Recipient Review**
- Logs into ESP or calls TTS
- Reviews hours
- Compares to services received

**4. Recipient Approval**
- Electronic signature
- Status: "Approved"
- Sent to payroll
- Payment per normal schedule

**5. Alternative: Rejection**
- Recipient rejects with reason
- Provider notified
- Provider corrects and resubmits

### Timesheet Types

**Standard Timesheet:**
- Regular pay period (1-15th, 16th-end)
- Auto-generated each period

**Supplemental Timesheet:**
- Requested for missed/corrected time
- Prior 12 months
- County approval required

**EVV Timesheet:**
- Standard with EVV requirements
- Check-in/check-out data
- Location verification

**Travel Claim:**
- Separate from timesheet
- Travel between recipients
- 7 hours/week max
- No recipient approval needed

---

## 5. EVV REQUIREMENTS {#evv}

### EVV Overview
Electronic Visit Verification (21st Century Cures Act) - federal requirement for electronic capture of service delivery data.

### EVV Data Captured

**Required Elements:**
1. Type of Service
2. Individual Receiving Service
3. Individual Providing Service
4. Date of Service
5. Location of Service (GPS/address)
6. Time Service Begins (check-in)
7. Time Service Ends (check-out)

### EVV Methods in ESP

#### Method 1: Check-In/Check-Out (Mobile)

**Process:**
1. Provider arrives at recipient location
2. Opens ESP on smartphone
3. Clicks "Check In"
4. ESP captures GPS location and time
5. Provider performs services
6. Clicks "Check Out"
7. ESP captures end time and location

**Requirements:**
- Smartphone with GPS
- Location services enabled
- Internet connection (at check-in/out)

#### Method 2: Manual Time Entry with Location

**Process:**
1. Provider enters time on timesheet
2. System prompts for location verification
3. Provider confirms or enters address
4. Data associated with entry

**Use Cases:**
- No smartphone
- GPS unavailable
- After-the-fact entry

### EVV Effective Date

**CaseProviderEVVEffectiveDate:**
- Set per provider-recipient relationship
- Date EVV requirement begins
- After date:
  - Paper not accepted
  - Electronic submission required
  - EVV data must be captured

### EVV Exceptions

**Good Cause Exception:**
Recipient can request if:
- Geographic location prevents GPS
- Technology barriers
- Other valid reasons

**Exception Process:**
1. Recipient requests through county
2. County evaluates
3. If approved: Attestation instead of GPS
4. Exception period tracked

---

## BUSINESS PROCESS FLOWS

### E-Timesheet Entry Flow
```
Provider → ESP Login
→ Select "Enter Time"
→ Select Recipient (if multiple)
→ Select Pay Period
→ Enter hours for each day/service type
→ Review entries
→ Submit (electronic signature)
→ Recipient notified
→ Recipient approves/rejects
```

### E-Timesheet Review Flow
```
Recipient notified (email/phone)
→ Login ESP or call TTS
→ View timesheet details
→ Compare to services received
→ Decision:
   APPROVE → E-signature → To payroll
   REJECT → Enter reason → Notify provider
```

### Password Reset Flow
```
"Forgot Username/Password"
→ Enter email
→ Answer security question
→ Receive code via email
→ Enter code
→ Create new password
→ Login
```

### Hire Provider Flow
```
Recipient → ESP Login
→ "Hire Provider"
→ Search (name/SSN/provider #)
→ View results
→ Select provider
→ Confirm details
→ Submit hire
→ System validates eligibility
→ Provider assigned
→ Confirmation email
```

---

## KEY CONCEPTS SUMMARY

### Registration
- **Self-Service:** 24/7 online registration
- **5 Steps:** Identity, Email, Username, Password, Security Questions
- **Verification:** Email verification code required
- **Security:** Password complexity, security questions

### E-Timesheets
- **Prerequisites:** Both provider and recipient registered
- **Transition:** Paper stops after first electronic approval or EVV date
- **Workflow:** Provider entry → Recipient approval → Payroll
- **Methods:** ESP (web) or TTS (telephone)

### EVV
- **Federal Requirement:** 21st Century Cures Act
- **7 Data Elements:** Service type, individuals, date, location, times
- **Methods:** Mobile check-in/out or manual entry with location
- **Effective Date:** Per provider-recipient relationship

### User Types
- **Provider:** Submit time, view payments, manage account
- **Recipient:** Review time, hire providers, view payments
- **Dual Role:** Separate accounts for each role

---

**End of Part 1**

**See Also:**
- Part 2: Provider Functionality (Time Entry, Payments, Sick Leave)
- Part 3: Recipient Functionality (Review, Approval, Hire Provider)
- Part 4: Shared Features (Account, Preferences, Support)
