# IHSS CMIPS - Provider Management Screens Documentation
## Release 2025.03.01 - DSD Section 23

---

## TABLE OF CONTENTS
1. [Provider Search and Registration](#provider-search-registration)
2. [Provider Enrollment Management](#provider-enrollment)
3. [Provider Assignment and Case Management](#provider-assignment)
4. [Provider Hours Management](#provider-hours)
5. [Provider CORI Management](#provider-cori)
6. [Provider Benefits and Waivers](#provider-benefits)
7. [Provider Workweek Agreements](#workweek-agreements)
8. [Provider Travel Time](#travel-time)
9. [Provider Overtime Violations](#overtime-violations)
10. [Provider Sick Leave](#sick-leave)
11. [Document Import](#document-import)

---

## 1. PROVIDER SEARCH AND REGISTRATION {#provider-search-registration}

### Person Search – Provider

**Access:** My Workspace → My Shortcuts → Register a Provider

**Purpose:** Search for existing person records or begin provider registration process

**Search Criteria Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Last Name | Text | Conditional* | Minimum 3 characters for partial search |
| SX (Soundex) | Checkbox | No | Soundex search for Last Name |
| First Name | Text | No | |
| SSN | Number | Conditional* | Full SSN |
| All SSNs | Checkbox | No | Shows Duplicate SSN types |
| Last 4 SSN | Checkbox | No | Search by last 4 digits only |
| CIN | Number | Conditional* | Client Index Number |
| Person Type | Dropdown | No | |
| Provider Number | Text | Conditional* | 9-digit CMIPS number |
| Date of Birth | Date MM/DD/YYYY | No | |
| Gender | Dropdown | No | |
| County | Dropdown | No | Default: All |
| District Office | Dropdown | No | |
| Street Number | Number | Conditional* | |
| Street Name | Text | Conditional* | |
| Unit Type | Dropdown | No | |
| Unit Number | Number | Yes (if Unit Type) | |
| City | Text | Conditional* | |

**Required Search Rules:** At least ONE of the following:
- Social Security Number (SSN)
- Full or partial last name (3+ characters + additional criteria)
- Client Index Number (CIN)
- Address (Street Number + Street Name + City)
- Provider Number

**Search Results Display:**

| Column | Description |
|--------|-------------|
| Full Name | Last Name, First Name (hyperlink to Create Provider) |
| SSN | Masked as XXX-XX-9999 |
| Type | Blank, Duplicate SSN, or Suspect SSN |
| CIN | Client Index Number (blank for Provider only) |
| Date of Birth | MM/DD/YYYY |
| Gender | Male, Female, Other |
| Person Type | Provider, Recipient, Applicant, etc. |
| Residence Address | Full street address |
| City | Residence city |
| County | Residence county |

**Actions:**
- **Search:** Execute search with criteria
- **Reset:** Clear all search fields
- **Continue Registration:** Open Create Provider screen (new provider)
- **<<Previous / Next>>:** Navigate through results (50 per page)
- **<Full Name>:** Open Create Provider screen with person data pre-populated

**Sort Order:** Alphabetical by person name (last name, first name)

**Business Rules:**
- Default SSN search excludes Duplicate SSN types (unless "All SSNs" checked)
- "Last 4 SSN" requires exactly 4 digits
- Results display maximum 50 records per page

---

### Create Provider

**Access:** Person Search → Continue Registration OR Click person name

**Purpose:** Enter basic provider information to begin enrollment process

**Screen Type:** Pop-up

**Data Fields:**

| Section | Field | Type | Required | Notes |
|---------|-------|------|----------|-------|
| **Basic Info** | Last Name | Text | Yes | Auto-uppercase |
| | First Name | Text | Yes | Auto-uppercase |
| | Middle Name | Text | No | Auto-uppercase |
| | Suffix | Dropdown | No | Jr., Sr., II, III, IV |
| | Gender | Radio | Yes | Male, Female |
| | Date of Birth | Date | Yes | MM/DD/YYYY |
| **SSN** | SSN | Number | Yes* | 9 digits |
| | Applied for SSN | Checkbox | No | Alternative to SSN |
| **Contact** | Primary Phone | Phone | No | (XXX) XXX-XXXX |
| | Email Address | Email | No | |
| | Preferred Language | Dropdown | No | Spoken language |
| | Written Language | Dropdown | No | For correspondence |
| **Address** | Residence Address | Address | Yes | Use Address Look-up |
| | County | Dropdown | Auto | Set from address |
| **Enrollment** | DOJ County | Dropdown | Yes | County for background check |
| | Effective Date | Date | No | Default: Current date |
| | DOJ Background Check | Checkbox | No | Check when completed |
| | Provider Orientation | Checkbox | No | Check when completed |
| | SOC 846 - Provider Agreement | Checkbox | No | Required for eligibility |
| | SOC 846 - Overtime Agreement | Checkbox | No | Required after FLSA date |

*Note: Either SSN or "Applied for SSN" checkbox required

**Actions:**
- **Save:** Create provider record and display Person Home screen
- **Cancel:** Return to Person Search screen
- **Residence Address Look-up:** Open address validation screen

**Business Rules:**
- BR PVM 03: SSN Verification set to "Not Yet Verified" on save
- BR PVM 20: Names converted to all uppercase on save
- BR PVM 27: DOJ County sets provider's county (doesn't affect Person Home county if person is also Applicant/Recipient)
- BR PVM 69: After FLSA date, checking either SOC 846 checkbox automatically checks both
- If person already exists: Pre-populate fields from existing record
- Effective Date defaults to current date if not entered

---

### View Provider Details

**Access:** Person Home → Provider link in Person Type section

**Purpose:** View comprehensive provider information and access all provider management functions

**Screen Sections:**

#### **General Section**
| Field | Description |
|-------|-------------|
| Provider Number | 9-digit CMIPS Provider Number |
| Eligible | Yes, No, Pending, Pending Reinstatement |
| Effective Date | Date eligibility status effective |
| Ineligible Reason | Reason code if Eligible = "No" |
| SSN Verification | Not Yet Verified, Verified, Deceased, Invalid |
| Deceased Entered Date | Date deceased status entered |
| Number of Active Cases | Count of active recipient assignments |

**Ineligible Reasons:**
- Suspended or Ineligible
- Tier 1 Conviction / Subsequent Tier 1 Conviction
- Tier 2 Conviction / Subsequent Tier 2 Conviction
- Duplicate SSN / Suspect SSN
- Death
- Inactive/No Payroll for 1 Year
- Third Overtime Violation
- Fourth Overtime Violation
- SOC 846 Not Completed
- Provider Enrollment Ineligible

#### **Medi-Cal Section**
| Field | Description |
|-------|-------------|
| Suspended/Ineligible | Yes/No |
| Suspended/Ineligible Begin Date | Date suspension began |
| Suspended/Ineligible End Date | Date suspension ended |

#### **Public Authority Section**
| Field | Description |
|-------|-------------|
| PA Registered | Yes/No |
| Training | Yes/No |
| Fingerprinting | Yes/No |
| Background Check | Yes/No |

#### **Enrollment Information Section**
| Field | Description |
|-------|-------------|
| Effective Date | Provider eligibility effective date |
| DOJ County | County handling background check |
| DOJ Background Check | Completed checkbox |
| Provider Orientation | Completed checkbox |
| SOC 846 - Provider Agreement | Completed checkbox |
| SOC 846 - Overtime Agreement | Completed checkbox (FLSA) |

#### **FLSA (Fair Labor Standards Act) Section**
| Field | Description |
|-------|-------------|
| Provider Has Workweek Agreement | Yes/No |
| Provider Has Workweek Agreement with Travel | Yes/No |
| Provider Weekly Maximum | HH:MM calculated hours |
| Provider Monthly Overtime Maximum | HH:MM calculated hours |
| Recipient Weekly Maximum | HH:MM for recipient |

**Action Links:**

| Link | Function | Availability |
|------|----------|--------------|
| **Modify** | Edit enrollment information | Always |
| **Reinstate** | Restore previous eligibility | Within 30 days of "Yes" to "No" change (except OT Violations 3/4) |
| **Re-enroll** | Start new enrollment | When Eligible = "No" |
| **Approve** | Approve pending enrollment | When Eligible = "Pending Reinstatement" |
| **Reject** | Reject pending enrollment | When Eligible = "Pending Reinstatement" |

**Navigation Links:**
- Cases (view all cases provider assigned to)
- Enrollment History
- CORI
- Benefits Deduction
- Workweek Agreement
- Travel Time
- Overtime Violations
- Sick Leave
- Attachments

**Business Rules:**
- BR PVM 25: Reinstate link only displays if eligible changed to "No" within 30 days AND Ineligible Reason is NOT "Third/Fourth Overtime Violation"
- BR PVM 26: Re-enroll link only displays if current Eligible status is "No"
- Number of Active Cases calculated nightly via batch job (BR PVM 71)

---

### Modify Enrollment

**Access:** View Provider Details → Modify link

**Purpose:** Update provider enrollment information and eligibility status

**Screen Type:** Pop-up

**Editable Fields:**

| Field | Type | Notes |
|-------|------|-------|
| Effective Date | Date | Required |
| DOJ County | Dropdown | Sets provider county |
| DOJ Background Check | Checkbox | |
| Provider Orientation | Checkbox | |
| SOC 846 - Provider Agreement | Checkbox | Required for eligibility |
| SOC 846 - Overtime Agreement | Checkbox | Required after FLSA (auto-checks both) |
| Eligible | Dropdown | Yes, No, Pending |
| Ineligible Reason | Dropdown | Required if Eligible = "No" |

**Actions:**
- **Save:** Update enrollment record
- **Cancel:** Close without saving

**Key Business Rules:**

**BR PVM 15: Eligible "Yes" to "No"**
When Eligible changed from "Yes" to "No":
- Save enrollment data
- Set Effective Date (current date if not provided)
- If provider Active or On Leave on any case:
  - Set Provider Status to Terminated
  - Set Termination Effective Date = Enrollment Effective Date
  - Set Termination Reason based on Ineligible Reason:
    - "Provider Not Eligible to Work" if reason is: Suspended/Ineligible, Duplicate SSN, Suspect SSN
    - Otherwise: "Terminated Provider"
  - End-date any General Exception Waivers
  - End-date any Recipient Waivers (End Date = Effective Date - 1)
  - Generate notification to Case Owner
  - Generate task to WPCS queue (if applicable)
  - End-date Provider Workweek Agreement
  - End-date Recipient Workweek Agreement
  - Recalculate overtime maximums
  - Update active cases/providers counts

**BR PVM 17: Eligible to "Yes" from "Pending Reinstatement"**
- Clear Ineligible Reason
- Reset Sick Leave eligibility end date if needed

**BR PVM 21: Re-enroll - Default DOJ County**
- When Re-enroll action: DOJ County defaults to user's county (unless user is CDSS)

**BR PVM 29: DOJ County Update**
- DOJ County for provider sets County on Person Home screen (if no Applicant/Recipient status)

**BR PVM 55: DOJ Background Check Checkbox**
- When DOJ Background Check checked (and wasn't before): Set DOJ County to user's county

**BR PVM 68-70: SOC 846 Requirements After FLSA**
After FLSA effective date:
- Checking either SOC 846 checkbox automatically checks both
- Both Provider Agreement AND Overtime Agreement required for eligibility

**BR PVM 88: Enrollment History**
- Every save creates history record with Last Updated By and Last Updated Date

---

### Approve/Reject Provider Enrollment

**Access:** View Provider Details → Approve/Reject links (when Eligible = "Pending Reinstatement")

**Purpose:** Approve or reject provider reinstatement request

**Approve Provider Enrollment:**
- Screen Type: Confirmation pop-up
- Actions: Yes, No
- Effect: Sets Eligible to "Yes", clears Ineligible Reason

**Reject Provider Enrollment:**
- Screen Type: Pop-up with rejection reason
- Fields: Rejection Reason (required text field)
- Actions: Save, Cancel
- Effect: Sets Eligible to "No", records rejection reason

---

### Provider Enrollment History

**Access:** View Provider Details → Enrollment History link

**Purpose:** View all changes to provider enrollment data

**Display Format:** Table with columns:

| Column | Description |
|--------|-------------|
| Effective Date | Enrollment effective date |
| DOJ County | County handling background check |
| DOJ Background Check | Yes/No |
| Provider Orientation | Yes/No |
| SOC 846 - Provider Agreement | Yes/No |
| SOC 846 - Overtime Agreement | Yes/No |
| Eligible | Yes/No/Pending/Pending Reinstatement |
| Ineligible Reason | Reason code if applicable |
| Last Updated By | User ID |
| Last Updated Date | Date/Time timestamp |

**Sort Order:** Most recent first (by Last Updated Date)

**No Actions:** View only

---

## 2. PROVIDER ASSIGNMENT AND CASE MANAGEMENT {#provider-assignment}

### Cases List Screen

**Access:** View Provider Details → Cases link

**Purpose:** View all recipient cases provider is assigned to or has been assigned to

**Display Columns:**

| Column | Description |
|--------|-------------|
| Case Name | Recipient Last Name, First Name (hyperlink to Case Home) |
| Case Number | 7-digit case number (hyperlink to Case Home) |
| Provider Type | IHSS, WPCS, or IHSS/WPCS |
| Provider Status | Active, On Leave, Terminated |
| Begin Date | Provider assignment start date |
| End Date | Provider assignment end date (blank if active) |
| Termination Reason | Reason code if terminated |

**Default Sort Order:** Provider Status (Active, On Leave, Terminated)

**Actions:**
- Click Case Name or Case Number: Navigate to Case Home
- No add/edit functions (read-only list)

---

### Case Providers (from Recipient Case perspective)

**Access:** Case Home → Evidence & Authorization tab → Case Providers link

**Purpose:** View and manage all providers assigned to recipient case

**Display Sections:**

#### **Active Providers**
Shows all providers with Status = "Active"

#### **Inactive Providers**
Shows providers with Status = "On Leave" or "Terminated"

**Provider Columns:**

| Column | Description |
|--------|-------------|
| Provider Name | Last Name, First Name (hyperlink to View Case Provider) |
| Provider Number | 9-digit CMIPS number |
| Provider Type | IHSS, WPCS |
| Relationship | Relationship to recipient |
| Provider Status | Active, On Leave, Terminated |
| Begin Date | Assignment start date |
| Pay Rate | Provider hourly rate |

**Actions:**
- **Assign Provider:** Add new provider to case
- **View:** View provider assignment details (click provider name)
- **Modify:** Edit provider assignment
- **Leave/Terminate:** End or pause provider assignment

**Default Sort Order (BR PVM 06):** Provider Status

---

### Assign Case Provider

**Access:** Case Home → Case Providers → Assign Provider

**Purpose:** Assign provider to recipient case with hours, pay rate, and relationship

**Screen Type:** Full page

**Data Sections:**

#### **Provider Selection**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Provider Search | Search icon | Yes | Opens Person Search |
| Provider Name | Auto-fill | Read-only | From search selection |
| Provider Number | Auto-fill | Read-only | From search selection |

#### **Assignment Details**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Provider Type | Auto-select | Read-only | IHSS or WPCS based on case |
| Relationship to Recipient | Dropdown | Yes | Affects funding source |
| Begin Date | Date | Yes | Assignment start date |
| IHSS Hours Pay Rate | Dropdown | Yes | From County Pay Rate table |
| Print Option | Radio | No | Print/Mail, Electronic, or Local Print |

**Relationship Options:**
- Spouse
- Parent
- Adult Child
- Other Relative
- Neighbor/Friend
- Organization Employee
- Other

**Print Options:**
- Print/Mail from a Centralized Print Center
- Electronic (E-Timesheet)
- Generate for Local Print

#### **Assigned Hours Section** (Optional)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Assigned Hours Form Indicated | Checkbox | No | Enables assigned hours |
| Assigned Hours | Time HH:MM | Yes (if checkbox) | Cannot exceed recipient authorized |
| Back-up Provider | Checkbox | No | Designates as back-up |

#### **Timesheet Options**
| Field | Type | Notes |
|-------|------|-------|
| Generate Timesheets | Radio | Arrears (past) or Advance Pay (future) |
| From Pay Period Begin Date | Date | Start of timesheet generation |
| To Pay Period Begin Date | Date | End of timesheet generation |

**Actions:**
- **Save:** Create provider assignment
- **Cancel:** Return to Case Providers screen
- **Pay Rate Search:** View available county pay rates
- **Provider Search Icon:** Search for provider to assign

**Key Business Rules:**

**BR PVM 01: Automatically Build Provider Hours**
- System copies recipient authorized hours to provider hours (if no Assigned Hours)
- Creates segments matching authorization periods

**BR PVM 02: Automatically Build Pay Rate Changes**
- System creates new provider hour segments when county pay rate changes

**BR PVM 07: Create Initial Timesheets**
If save successful AND pay period >= Travel Claim Start Date AND Print Option is Print/Mail or Electronic:
- **Standard Timesheet:** Generate if no EVV and not large font
- **Electronic Timesheet:** Generate if EVV effective
- **Travel Claim:** Generate if "Provider has Workweek Agreement with Travel" = Yes
- **Arrears:** Up to 24 pay periods (12 months)
- **Advance Pay:** Up to 6 pay periods (3 months)

**BR PVM 13: Update Recipient Funding Source**
When provider relationship is Spouse or Parent (of minor <18):
- Re-determine case funding source (may change to IPO - IHSS Plus Option)
- If funding changes from PCSP to IPO3/IPO4: Update Reassessment Due Date to 1 year from Home Visit Date
- Create Case Note documenting funding source change

**BR PVM 40-43: Provider Notification (SOC 2271)**
Generate SOC 2271 form when:
- Provider assigned for first time
- Provider returns after Leave
- Provider returns after Termination
Form includes:
- Recipient name
- Provider number
- Monthly authorized hours
- Weekly authorized hours
- Service types (marked with X)
- Separate form for each authorization segment if service types change

**BR PVM 48-49: Labor Market Adjustment (LMA)**
If LMA change occurs during provider assignment:
- End-date current provider hours one day before LMA start
- Create new segment with LMA begin date
- Apply 7% increase to assigned hours

**BR PVM 50-53: Inter-County Transfer**
Sending county can assign provider when ICT in "Completed" status:
- Assign Provider screen displays
- Pay rate defaults to Sending County default
- Save action allowed

---

### View Case Provider

**Access:** Case Providers → Click Provider Name

**Purpose:** View complete provider assignment details and manage provider hours

**Screen Sections:**

#### **Provider Assignment Details**
| Field | Display |
|-------|---------|
| Provider Name | Full name |
| Provider Number | 9-digit number |
| Provider Type | IHSS, WPCS, IHSS/WPCS |
| Relationship to Recipient | Dropdown value |
| Provider Status | Active, On Leave, Terminated |
| Begin Date | Assignment start |
| End Date | Blank if active |
| Termination Reason | If terminated |
| IHSS Eligible | Yes/No (from provider details) |
| Pay Rate | Current rate |

#### **Provider Hours List**
Table showing all provider hour segments:

| Column | Description |
|--------|-------------|
| Begin Date | Segment start date |
| End Date | Segment end date (blank if current) |
| Status | Active, On Leave, Terminated |
| Assigned Hours | HH:MM if assigned |
| Pay Rate | Hourly rate |
| Assigned Hours Form | Yes/No indicator |

**Actions:**
- **Modify:** Edit assignment details
- **Leave/Terminate:** End or pause assignment
- **New (Provider Hours):** Create new hour segment
- **View:** View provider hour segment details
- **Modify (Hours):** Edit provider hour segment

#### **WPCS Details Section** (if WPCS provider)
Shows WPCS-specific hour segments

**Actions:**
- **New (WPCS):** Create WPCS hours
- **View (WPCS):** View WPCS details
- **Modify (WPCS):** Edit WPCS hours

**Navigation:**
- Back to Case Providers list
- Provider Hours History
- WPCS Provider Hours History

---

### Modify Case Provider

**Access:** View Case Provider → Modify button

**Purpose:** Update provider assignment details

**Screen Type:** Pop-up

**Editable Fields:**

| Field | Type | Notes |
|-------|------|-------|
| Relationship to Recipient | Dropdown | Affects funding source |
| IHSS Hours Pay Rate | Dropdown | From county pay rate table |

**Non-Editable:**
- Provider Name
- Provider Number
- Provider Type
- Begin Date
- Provider Status

**Actions:**
- **Save:** Update assignment
- **Cancel:** Close without saving

**Business Rule:**
- BR PVM 13: Relationship change triggers funding source re-determination

---

### Leave/Terminate Case Provider

**Access:** View Case Provider → Leave/Terminate button

**Purpose:** End or pause provider assignment to case

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Status | Radio | Yes | On Leave or Terminated |
| Effective Date | Date | Yes | Leave/Termination date |
| Termination Reason | Dropdown | Yes (if Terminated) | Required for terminated status |
| End Date Workweek Agreement | Checkbox | No | End associated workweek agreement |

**Termination Reasons:**
- Provider Not Eligible to Work
- Provider Enrollment Ineligible
- Recipient Not Eligible
- Recipient Request
- Provider Request
- Provider Deceased
- Medi-Cal Suspended or Ineligible
- Terminated Provider
- (Others as defined in code tables)

**Actions:**
- **Save:** Process leave/termination
- **Cancel:** Close without saving

**Key Business Rules:**

**BR PVM 44-47: Provider Notification Cleanup**
- If Effective Date <= current date: Inactivate all Pending SOC 2271 notifications
- If Effective Date > current date: Inactivate Pending SOC 2271 with Effective Date >= Begin Date

**BR PVM 72-73: End Date Workweek Agreements**
If "End Date Workweek Agreement" checked:
- **Provider Workweek Agreement:**
  - If Effective Date before most recent agreement Begin Date: Set agreement status to "Inactive"
  - If Effective Date before agreement End Date: Set agreement End Date to Effective Date
  - If Effective Date is past date: Set agreement End Date to current date
- **Recipient Workweek Agreement:**
  - If Effective Date before most recent agreement Begin Date: Set agreement status to "Inactive"
  - If Effective Date before agreement End Date: Set agreement End Date to Effective Date - 1 day
  - If Effective Date is past date: Set agreement End Date to current date

**BR PVM 35: Terminate Provider if Recipient Waiver Terminated**
- When Recipient Waiver end-dated: Provider termination date = Waiver End Date + 1
- Termination Reason: "Provider Enrollment Ineligible"

**BR PVM 81-84: Inter-County Transfer Leave/Terminate**
Sending County or WPCS worker can leave/terminate provider prior to ICT:
- If Effective Date before ICT Authorization Start Date
- Retain active segment in Receiving County
- Create leave/terminate segment in Sending County ending one day before ICT Start

---

## 3. PROVIDER HOURS MANAGEMENT {#provider-hours}

### Create Provider Hours

**Access:** View Case Provider → New (Provider Hours section)

**Purpose:** Add new provider hour segment with specific dates and hours

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Begin Date | Date | Yes | Segment start date |
| End Date | Date | No | Blank for open-ended |
| Assigned Hours Form Indicated | Checkbox | No | |
| Assigned Hours | Time HH:MM | Yes (if checkbox) | Cannot exceed recipient authorized |
| Back-up Provider | Checkbox | No | |
| IHSS Hours Pay Rate | Dropdown | Yes | From county pay rate table |

**Actions:**
- **Save:** Create hour segment
- **Cancel:** Close without saving
- **Pay Rate Search:** View county pay rates

**Key Business Rules:**

**BR PVM 18: Override Terminated/On-Leave Segments**
- If new Begin Date <= Effective Date of Terminated/On Leave segment
- New segment overrides the terminated/on-leave segment

**BR PVM 88: Create Hours Prior to Oldest Segment**
- If Begin Date prior to oldest segment but >= "Eligible" case authorization
- System creates segments matching case authorization periods
- Includes "Not Eligible" periods as Leave/Terminate segments

**BR PVM 51-53: Inter-County Transfer Permissions**
Sending County can create provider hours when ICT "Completed":
- Display Create Provider Hours screen
- Default pay rate to Sending County rate
- Save action allowed

---

### Modify Provider Hours

**Access:** View Case Provider → Modify (Provider Hours row)

**Purpose:** Update existing provider hour segment

**Screen Type:** Pop-up

**Editable Fields:**

| Field | Type | Notes |
|-------|------|-------|
| Begin Date | Date | Usually not editable |
| End Date | Date | Can add/change |
| Assigned Hours Form Indicated | Checkbox | |
| Assigned Hours | Time HH:MM | Cannot exceed recipient authorized |
| Back-up Provider | Checkbox | |
| IHSS Hours Pay Rate | Dropdown | From county pay rate table |

**Actions:**
- **Save:** Update hour segment
- **Cancel:** Close without saving
- **Pay Rate Search:** View county pay rates

**Key Business Rule:**

**BR PVM 54: Inter-County Transfer - Modify Hours**
Sending County can modify hours when ICT "Completed":
- Note: Only Assign Hours can be changed

---

### View IHSS Provider Hours History

**Access:** View Case Provider → Provider Hours History link

**Purpose:** View all provider hour segments for this case assignment

**Display Format:** Table with columns:

| Column | Description |
|--------|-------------|
| Begin Date | Segment start date |
| End Date | Segment end date |
| Status | Active, On Leave, Terminated |
| Assigned Hours Form | Yes/No |
| Assigned Hours | HH:MM if assigned |
| Pay Rate | Hourly rate |
| Back-up Provider | Yes/No |
| Last Updated By | User ID |
| Last Updated Date | Date/Time |

**Sort Order:** Most recent first (by Begin Date descending)

**No Actions:** View only

---

### WPCS Details (Create/View/Modify)

**Access:** View Case Provider → New/View/Modify (WPCS Details section)

**Purpose:** Manage Waiver Personal Care Services provider hours

**Fields:** Similar to IHSS Provider Hours with WPCS-specific attributes

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Begin Date | Date | Yes | |
| End Date | Date | No | |
| Assigned Hours Form Indicated | Checkbox | No | |
| Assigned Hours | Time HH:MM | Yes (if checkbox) | |
| WPCS Hours Pay Rate | Dropdown | Yes | WPCS pay rate table |

**Note:** WPCS providers may have different pay rates and overtime calculations than IHSS providers

---

## 4. PROVIDER CORI MANAGEMENT {#provider-cori}

### Provider CORI Details

**Access:** View Provider Details → CORI link

**Purpose:** View all Criminal Offender Record Information for provider

**Display Format:** Table with CORI records:

| Column | Description |
|--------|-------------|
| Conviction Date | Date of conviction |
| Tier | 1 or 2 |
| CORI End Date | Date conviction no longer applies |
| General Exception Begin Date | Waiver start date |
| General Exception End Date | Waiver end date |
| Status | Active, Inactive |

**Actions:**
- **New:** Create new CORI record
- **View:** View CORI details
- **Modify:** Edit CORI record
- **Inactivate:** End-date CORI record

**Sort Order:** Most recent Conviction Date first

---

### Create Provider CORI

**Access:** Provider CORI Details → New

**Purpose:** Document criminal conviction for provider

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Conviction Date | Date | Yes | Date of conviction |
| Tier | Dropdown | Yes | Tier 1 or Tier 2 |
| Crime Description | Text | No | Description of offense |
| CORI End Date | Date | No | When conviction no longer applies |
| Comments | Text Area | No | Additional notes |

**Actions:**
- **Save:** Create CORI record
- **Cancel:** Close without saving

**Key Business Rule - BR PVM 31: Create Provider CORI**

When CORI saved with conviction date different from existing records:
- Set Provider Eligible status to "No"
- Set Effective Date to 20 calendar days from current date
- Set Ineligible Reason:
  - **First CORI:**
    - Tier 1: "Tier 1 Conviction"
    - Tier 2: "Tier 2 Conviction"
  - **Additional CORI:**
    - Tier 1: "Subsequent Tier 1 Conviction"
    - Tier 2: "Subsequent Tier 2 Conviction"
- Terminate Provider Hours:
  - Termination Reason: "Provider Enrollment Ineligible"
  - If most recent segment End Date < 20 days from current: Termination Effective Date = End Date + 1
  - Else: Termination Effective Date = 20 days from current
- If General Exception exists with no End Date: Set End Date to 20 days from current
- If Recipient Waiver exists with blank End Date: Set End Date to 20 days from current - 1 day
- End-date Provider/Recipient Workweek Agreements
- Recalculate overtime maximums
- Update active cases/providers counts

---

### View Provider CORI

**Access:** Provider CORI Details → Click CORI record

**Purpose:** View complete CORI details including waivers

**Display Sections:**

#### **CORI Information**
| Field | Display |
|-------|---------|
| Conviction Date | Date |
| Tier | 1 or 2 |
| Crime Description | Text |
| CORI End Date | Date or blank |
| Status | Active, Inactive |
| Comments | Text |

#### **General Exception Waiver** (Tier 2 only)
| Field | Display |
|-------|---------|
| General Exception Begin Date | Date |
| General Exception End Date | Date or blank |
| Waiver Details | Text |

#### **Associated Recipient Waivers** (Tier 2 only)
List of recipient waivers for this CORI:

| Column | Description |
|--------|-------------|
| Recipient Name | Case name |
| Case Number | 7-digit number |
| Waiver Begin Date | Start date |
| Waiver End Date | End date or blank |
| Status | Active, Inactive |

**Actions:**
- **Modify:** Edit CORI record
- **Inactivate:** End-date CORI

---

### Modify Provider CORI

**Access:** View Provider CORI → Modify

**Purpose:** Update CORI details and manage waivers

**Screen Type:** Pop-up

**Editable Fields:**

| Field | Type | Notes |
|-------|------|-------|
| Conviction Date | Date | Usually not editable |
| Tier | Dropdown | Can change 1↔2 |
| Crime Description | Text | |
| CORI End Date | Date | Ends CORI applicability |
| General Exception Begin Date | Date | Tier 2 only |
| General Exception End Date | Date | Tier 2 only |
| Comments | Text Area | |

**Actions:**
- **Save:** Update CORI record
- **Cancel:** Close without saving

**Key Business Rules:**

**BR PVM 33: Adding General Exception**
When General Exception Begin Date populated AND no End Date AND Provider meets all enrollment criteria:
- Set Provider Eligible to "Yes"

**BR PVM 34: Adding General Exception End Date**
When General Exception End Date populated AND Provider Eligible = "Yes" AND End Date before CORI End Date:
- Set Provider Eligible to "No"
- Set Effective Date = General Exception End Date + 1
- Set Ineligible Reason: "Tier 2 Conviction"
- Terminate provider hours (20 days from current)
- End-date workweek agreements
- Recalculate overtime maximums

**BR PVM 36: Updating Tier 2 to Tier 1**
When Tier changed from 2 to 1:
- Set Provider Eligible to "No" (if currently "Yes")
- Update Ineligible Reason: "Tier 1 Conviction"
- Terminate any Recipient Waivers (effective current + 20 days)
- Set Waiver Termination Reason: "Terminated Provider"
- Follow BR PVM 15 actions (terminate provider, end agreements, etc.)

**BR PVM 39: Updating Tier 1 to Tier 2**
When Tier changed from 1 to 2:
- Update Ineligible Reason: "Tier 2 Conviction" or "Subsequent Tier 2 Conviction"
- Provider may now obtain waivers to serve

**BR PVM 37: End Date CORI - Recipient Waivers**
When CORI End Date populated AND active Recipient Waiver exists:
- Set Recipient Waiver End Date = CORI End Date
- Termination Reason: "Waiver No Longer Required"
- Provider remains Eligible "No"

**BR PVM 38: End Date CORI - General Exception**
When CORI End Date populated AND active General Exception exists:
- Set General Exception End Date = CORI End Date
- Provider remains Eligible "Yes"
- Do not terminate provider

---

### Inactivate Provider CORI

**Access:** View Provider CORI → Inactivate

**Purpose:** Remove CORI record from active status

**Screen Type:** Confirmation pop-up

**Display:** "Are you sure you want to inactivate this CORI record?"

**Actions:**
- **Yes:** Inactivate CORI (set status to Inactive)
- **No:** Cancel action

**Note:** Inactivation typically only allowed when CORI End Date has been set

---

## 5. PROVIDER BENEFITS AND WAIVERS {#provider-benefits}

### Benefit Deduction

**Access:** View Provider Details → Benefits Deduction link

**Purpose:** View and manage provider benefit deductions

**Display Format:** Table of benefit deductions:

| Column | Description |
|--------|-------------|
| Benefit Type | Type of deduction |
| Begin Date | Deduction start date |
| End Date | Deduction end date |
| Amount | Deduction amount |
| Status | Active, Inactive |

**Actions:**
- **New:** Create benefit deduction
- **Modify:** Edit benefit deduction

---

### Create/Modify Provider Benefits Deduction

**Access:** Benefits Deduction → New/Modify

**Purpose:** Add or update provider benefit deductions

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Benefit Type | Dropdown | Yes | Health, Dental, Vision, etc. |
| Begin Date | Date | Yes | Deduction start |
| End Date | Date | No | Deduction end (blank for ongoing) |
| Deduction Amount | Currency | Yes | $ amount per pay period |
| Frequency | Dropdown | Yes | Per Pay Period, Monthly, etc. |

**Actions:**
- **Save:** Create/update deduction
- **Cancel:** Close without saving

---

### Recipient Waiver (for Tier 2 CORI)

**Access:** View Provider Details → Recipient Waiver link (for Tier 2 providers)

**Purpose:** Manage recipient-specific waivers allowing Tier 2 provider to serve

**Display Format:** Table of waivers:

| Column | Description |
|--------|-------------|
| Recipient Name | Case name |
| Case Number | 7-digit number |
| CORI Conviction Date | Associated CORI |
| Waiver Begin Date | Start date |
| Waiver End Date | End date or blank |
| Status | Active, Terminated |
| Termination Reason | If terminated |

**Actions:**
- **New:** Create recipient waiver
- **View:** View waiver details
- **Modify:** Edit waiver
- **Inactivate:** End-date waiver

**Key Concept:** Recipient Waiver allows specific recipient to waive Tier 2 CORI restriction for specific provider. Waiver is case-specific and does not apply to other cases.

---

### Create Recipient Waiver

**Access:** Recipient Waiver list → New

**Purpose:** Create waiver for recipient to allow Tier 2 provider to serve

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Recipient Case | Search/Select | Yes | Case granting waiver |
| CORI Record | Dropdown | Yes | Which conviction being waived |
| Waiver Begin Date | Date | Yes | Waiver start |
| Waiver End Date | Date | No | Blank for ongoing |
| Waiver Comments | Text Area | No | Reason for waiver |

**Actions:**
- **Save:** Create waiver
- **Cancel:** Close without saving
- **Case Search:** Search for recipient case

---

### View/Modify/Inactivate Recipient Waiver

**Access:** Recipient Waiver list → View/Modify/Inactivate

**Purpose:** View details, update, or end recipient waiver

**View Screen:** Display all waiver details (read-only)

**Modify Screen:** 
- Edit Waiver End Date
- Update Comments

**Inactivate Screen:**
- Confirmation pop-up
- Sets Waiver End Date to current date
- Sets Termination Reason

**Key Business Rule - BR PVM 35:**
When Recipient Waiver terminated (End Date entered):
- Set Case Provider Termination Date = Waiver End Date + 1
- Set Termination Reason: "Provider Enrollment Ineligible"
- End-date workweek agreements
- Recalculate overtime maximums
- Update active counts

---

## 6. PROVIDER WORKWEEK AGREEMENTS {#workweek-agreements}

### Provider Workweek Agreement

**Access:** View Provider Details → Workweek Agreement link

**Purpose:** View and manage provider's regular work schedule across all recipients

**Display Format:** Table of agreements:

| Column | Description |
|--------|-------------|
| Begin Date | Agreement start date |
| End Date | Agreement end date |
| Hours | Total weekly hours (HH:MM) |
| With Travel | Yes/No indicator |
| Status | Active, Inactive |

**Actions:**
- **New:** Create workweek agreement
- **View:** View agreement details
- **Modify:** Edit agreement
- **Inactivate:** End-date agreement
- **History:** View inactive agreements

**Sort Order:** Most recent Begin Date first

**Key Concept:** Workweek agreements document provider's committed work schedule and are used to calculate weekly maximum hours and determine if provider can claim travel time.

---

### Create Provider Workweek Agreement

**Access:** Provider Workweek Agreement → New

**Purpose:** Document provider's regular work schedule for one or more recipients

**Screen Type:** Pop-up

**Data Sections:**

#### **Agreement Details**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Begin Date | Date | Yes | Agreement start |
| End Date | Date | No | Blank for ongoing |
| With Travel | Checkbox | No | Indicates travel time included |

#### **Recipients Selection**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Select Recipients | Multi-select | Yes | Choose one or more recipients |

Use "Select Recipient" search icon to display list of all recipients (regardless of status) sorted by:
1. Status (Eligible, Presumptive Eligible, Leave, Terminated)
2. Case Name (Last Name, First Name)

#### **Work Schedule** (for each day)
| Day | Hours Field | Minutes Field |
|-----|-------------|---------------|
| Sunday | HH | MM |
| Monday | HH | MM |
| Tuesday | HH | MM |
| Wednesday | HH | MM |
| Thursday | HH | MM |
| Friday | HH | MM |
| Saturday | HH | MM |

**Total Hours:** Automatically calculated as user enters daily hours/minutes (BR PVM 58)

**Actions:**
- **Save:** Create agreement
- **Cancel:** Close without saving
- **Select Recipient:** Search for recipients

**Key Business Rules:**

**BR PVM 58: Dynamic Hours Calculation**
- As user enters/modifies hours/minutes for any day
- System dynamically updates "Hours" field showing total weekly hours

**BR PVM 76: End Date Previous Agreement**
When new agreement saved AND previous agreement had End Date as 12/31/9999 OR date future to new Begin Date:
- Set previous agreement End Date to new Begin Date - 1 day

---

### View Provider Workweek Agreement

**Access:** Provider Workweek Agreement → Click agreement row

**Purpose:** View complete workweek agreement details

**Display Sections:**

#### **Agreement Information**
| Field | Display |
|-------|---------|
| Begin Date | Agreement start |
| End Date | Agreement end or blank |
| Total Hours | HH:MM for week |
| With Travel | Yes/No |
| Status | Active, Inactive |

#### **Associated Recipients**
List of recipients included in agreement:
- Recipient Name
- Case Number
- Provider Status for that case

#### **Work Schedule**
Table showing hours for each day:

| Day | Hours | Minutes |
|-----|-------|---------|
| Sunday | HH | MM |
| Monday | HH | MM |
| Tuesday | HH | MM |
| Wednesday | HH | MM |
| Thursday | HH | MM |
| Friday | HH | MM |
| Saturday | HH | MM |

**Actions:**
- **Modify:** Edit agreement
- **Inactivate:** End-date agreement

---

### Modify Provider Workweek Agreement

**Access:** View Provider Workweek Agreement → Modify

**Purpose:** Update workweek agreement schedule or end date

**Editable Fields:**
- End Date (can add/change)
- Hours/Minutes for each day
- With Travel checkbox
- Cannot change: Begin Date, Recipients

**Actions:**
- **Save:** Update agreement
- **Cancel:** Close without saving

---

### Inactivate Provider Workweek Agreement

**Access:** View Provider Workweek Agreement → Inactivate

**Purpose:** End workweek agreement

**Screen Type:** Confirmation pop-up

**Display:** "Are you sure you want to inactivate this workweek agreement?"

**Actions:**
- **Yes:** Set End Date to current date, Status to Inactive
- **No:** Cancel action

---

### Provider Workweek Agreement History

**Access:** Provider Workweek Agreement → History

**Purpose:** View all inactive workweek agreements

**Display Format:** Table of inactive agreements with same columns as active list

**Sort Order:** Most recent End Date first

**No Actions:** View only

---

### Recipient Workweek Agreement (from Case perspective)

**Note:** Similar to Provider Workweek Agreement but accessed from recipient case perspective

**Access:** Case Home → Evidence & Authorization → Provider section

**Purpose:** View/manage workweek agreement for provider serving this specific recipient

**Functionality:** Same as Provider Workweek Agreement but filtered to show only agreements for this recipient

---

## 7. PROVIDER TRAVEL TIME {#travel-time}

### Travel Time Recipient Case

**Access:** View Provider Details → Travel Time link

**Purpose:** View all recipient cases with travel time arrangements

**Display Format:** Table of recipients:

| Column | Description |
|--------|-------------|
| Recipient Name | Last Name, First Name |
| Case Number | 7-digit number |
| Provider Type | IHSS, WPCS, or IHSS/WPCS |
| Provider Status | Active, On-Leave, Terminated |
| Weekly Travel Time Hours | HH:MM per week |
| Workweek Agreement | Most recent agreement reference |

**Sort Order (BR PVM 59):**
1. Status (Active, On-Leave, Terminated)
2. Recipient Last Name

**Note:** If provider has served as both IHSS and WPCS for one recipient, two rows display

**Actions:**
- Click Recipient Name: Navigate to Travel Time Hours for that recipient
- No add/edit at this level

---

### Provider Travel Time Hours

**Access:** Travel Time Recipient Case → Click Recipient Name

**Purpose:** View and manage travel time arrangements for specific recipient

**Display Format:** Table of travel time segments:

| Column | Description |
|--------|-------------|
| Traveling From | Source recipient name |
| Traveling To | Current recipient name |
| Begin Date | Travel time start |
| End Date | Travel time end |
| Hours per Week | HH:MM weekly travel |
| Status | Active, On Leave, Terminated |

**Actions:**
- **New:** Create travel time arrangement
- **View:** View travel time details
- **Modify:** Edit travel time
- **Leave/Terminate:** End travel time
- **History:** View inactive travel time

**Weekly Maximum:** System enforces 7:00 hours per week maximum across all recipients

---

### Create Travel Time

**Access:** Provider Travel Time Hours → New

**Purpose:** Document travel time between recipient locations

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Traveling From Recipient | Search/Select | Yes | Source location |
| Traveling To Recipient | Auto-fill | Read-only | Current recipient (destination) |
| Begin Date | Date | Yes | Travel time start |
| End Date | Date | No | Blank for ongoing |
| Hours per Week | Time HH:MM | Yes | Weekly travel hours |

**Actions:**
- **Save:** Create travel time
- **Cancel:** Close without saving
- **Traveling From Search:** Select source recipient

**Key Business Rule - BR PVM 60:**
When "Traveling From" search icon selected:
- Display list of all recipients EXCEPT current recipient
- Provider must be associated with traveling-from recipient

**Validation:**
- Total travel time across all recipients cannot exceed 7:00 hours per week
- If exceeds: Display error message

---

### View Travel Time

**Access:** Provider Travel Time Hours → Click travel time row

**Purpose:** View complete travel time arrangement details

**Display Fields:**

| Field | Display |
|-------|---------|
| Traveling From Recipient | Recipient name and case number |
| Traveling To Recipient | Recipient name and case number |
| Begin Date | Start date |
| End Date | End date or blank |
| Hours per Week | HH:MM |
| Status | Active, On Leave, Terminated |
| Termination Reason | If terminated |
| Last Updated By | User ID |
| Last Updated Date | Date/Time |

**Actions:**
- **Modify:** Edit travel time
- **Leave/Terminate:** End travel time
- **Inactivate:** Remove travel time (if appropriate)

---

### Modify Travel Time

**Access:** View Travel Time → Modify

**Purpose:** Update travel time hours or end date

**Editable Fields:**
- End Date (can add/change)
- Hours per Week (subject to 7-hour maximum)
- Cannot change: Traveling From, Traveling To, Begin Date

**Actions:**
- **Save:** Update travel time
- **Cancel:** Close without saving

**Key Business Rule - BR PVM 61:**
When Modify Travel Time saved:
- Populate saved information to View Travel Time screen
- Save previous View Travel Time data to Travel Time History

**Validation:**
- Total travel time across all recipients cannot exceed 7:00 hours per week

---

### Leave/Terminate Travel Time

**Access:** View Travel Time → Leave/Terminate

**Purpose:** End or pause travel time arrangement

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Status | Radio | Yes | On Leave or Terminated |
| Effective Date | Date | Yes | Leave/termination date |
| Termination Reason | Dropdown | Yes (if Terminated) | |

**Termination Reasons:**
- Provider Not Eligible to Work
- Recipient Not Eligible
- Provider Request
- Route Changed
- Other

**Actions:**
- **Save:** Process leave/termination
- **Cancel:** Close without saving

**Key Business Rule - BR PVM 64:**
When travel time leave/terminate saved AND Effective Date entered AND current travel time hours exist:
- End date current travel time hours: Effective Date - 1
- Create leave/terminate segment using data from screen
- Set Status and Termination Reason as indicated

---

### Inactivate Travel Time

**Access:** View Travel Time → Inactivate

**Purpose:** Supersede terminated/on-leave travel time segment

**Screen Type:** Confirmation pop-up

**Key Business Rule - BR PVM 65:**
When new travel time segment added AND Begin Date equals Begin Date on existing Terminated/On Leave segment:
- Supersede the previous segment
- Display only the active segment

---

### Travel Time History

**Access:** Provider Travel Time Hours → History

**Purpose:** View all inactive travel time arrangements

**Display Format:** Table with same columns as active list

**Sort Order:** Most recent End Date first

**No Actions:** View only

---

### Automatic Travel Time Termination

**Key Business Rules:**

**BR PVM 62: Recipient Case Terminated**
When recipient case terminated AND current travel time exists:
- End date travel time with case Authorization End Date
- Create leave/terminate segment with:
  - Effective Date = Authorization End Date + 1
  - End Date = 12/31/9999
  - Termination Reason = "Recipient Not Eligible"

**BR PVM 63: Provider Hours Terminated**
When provider hours terminated/on leave AND travel time exists AND travel time End Date is after Provider termination date:
- Set travel time End Date = Provider termination date - 1
- Create leave/terminate segment with:
  - Effective Date = Provider termination date
  - End Date = 12/31/9999
  - Termination Reason = "Provider Not Eligible to Work"

**BR PVM 66: Provider Eligible Changes to "No"**
When any business rule sets Provider Eligible from "Yes" to "No" AND travel time exists:
- If current End Date ≠ 12/31/9999: Create new leave/terminate segment with Begin Date = current End Date + 1
- Else: Create segment with Begin Date = current date
- Set End Date = 12/31/9999
- Set Status = Terminated
- Set Termination Reason = "Provider Not Eligible to Work"

---

## 8. PROVIDER OVERTIME VIOLATIONS {#overtime-violations}

### Overtime Violations

**Access:** View Provider Details → Overtime Violations link

**Purpose:** View and manage provider overtime violations

**Display Format:** Table of violations:

| Column | Description |
|--------|-------------|
| Violation Number | System-generated unique ID (hyperlink) |
| Violation Date | Date violation occurred |
| Violation Type | Exceeds Weekly Maximum, Exceeds Travel Maximum |
| Violation Count | 1, 2, 3, 4 |
| Violation Status | Pending Review, Active, Inactive, Inactive-Exemption |
| Next Possible Violation Date | Date provider can incur next violation |

**Actions:**
- Click Violation Number: View Overtime Violation details
- No add function (violations triggered automatically by system)

**Sort Order:** Most recent Violation Date first

**Key Concept:** Violations automatically triggered when payment (timesheet, special transaction, payment correction) causes hours to exceed limits

---

### View Overtime Violations

**Access:** Overtime Violations → Click Violation Number

**Purpose:** View complete violation details and manage review process

**Screen Sections:**

#### **Violation Information** (Top of screen)

| Field | Display |
|-------|---------|
| Violation Number | Unique identifier |
| Violation Date | Date violation occurred |
| Service Month | Month of service causing violation |
| Pay Period | Pay period of violation |
| Violation Type | Exceeds Weekly Maximum or Exceeds Travel Maximum |
| Violation Count | 1, 2, 3, or 4 |
| Violation Status | Current status |
| Next Possible Violation Date | Date for next possible violation |

**Violation Statuses:**
- Pending Review (County review pending)
- Active (Violation upheld, counts toward total)
- Inactive (Violation overridden)
- Inactive – Exemption (Provider has exemption for service month)
- Inactive – No Violations for one year (Violation count reset)
- Inactive – Provider One Year Termination (4th violation termination)

#### **County Review Section**

**Fields:**

| Field | Display/Entry |
|-------|---------------|
| County Review Outcome | Pending Review, Pending Uphold, Upheld, Pending Override, Override |
| County Review Date | Date review completed |
| County Review By | User ID who completed review |
| County Review Letter Date | Date letter sent to provider |
| Comments | Text (view/edit via Comments link) |

**Actions:**
- **County Review:** Open Modify County Review screen
- **Comments:** View/add review comments
- **History:** View County Review changes

**Outcomes:**
- **Pending Uphold** → **Upheld** (nightly batch): Violation becomes Active, letters issued
- **Pending Override** → Supervisor Review: Task to Supervisor, waits approval

#### **Supervisor Review Section** (appears if County Review = Override)

**Fields:**

| Field | Display/Entry |
|-------|---------------|
| Supervisor Review Outcome | Supervisor Review, Pending Approval, Approved, Pending Rejection, Rejected |
| Supervisor Review Date | Date review completed |
| Supervisor Review By | User ID |
| Comments | Text |

**Actions:**
- **Supervisor Review:** Open Modify Supervisor Review screen
- **Comments:** View supervisor comments
- **History:** View Supervisor Review changes

**Outcomes:**
- **Pending Approval** → **Approved** (nightly batch): Violation becomes Inactive
- **Pending Rejection** → **Rejected** (nightly batch): Violation becomes Active, letters issued

#### **County Dispute Section** (appears after provider requests dispute)

**Provider has 10 calendar days after County Review Letter Date to request dispute**

**Fields:**

| Field | Display/Entry |
|-------|---------------|
| County Dispute Filed Date | Date provider filed dispute |
| County Dispute Outcome | Pending Review, Pending Uphold, Upheld, Pending Override, Override |
| County Dispute Date | Date dispute reviewed |
| County Dispute By | User ID |
| County Dispute Letter Date | Date letter sent |
| Comments | Text |

**Actions:**
- **County Dispute:** Open Modify County Dispute screen
- **Comments:** View dispute comments
- **History:** View dispute changes

**Timeline:** 10 business days total for county to complete dispute review

**Tasks Generated:**
- Day 6: Alert to worker and supervisor (4 days remaining)
- Day 8: Alert to supervisor (2 days remaining)

#### **Supervisor Dispute Section** (if County Dispute = Override)

**Fields:**

| Field | Display/Entry |
|-------|---------------|
| Supervisor Dispute Outcome | Supervisor Review, Pending Approval, Approved, Pending Rejection, Rejected |
| Supervisor Dispute Date | Date |
| Supervisor Dispute By | User ID |
| Comments | Text |

**Actions:**
- **Supervisor Dispute:** Open Modify Supervisor Dispute screen
- **Comments:** View comments
- **History:** View changes

#### **CDSS Appeal Section** (Violations 3 & 4 only)

**Only available for Overtime Violation #3 or #4 (result in provider suspension)**

**Fields:**

| Field | Display/Entry |
|-------|---------------|
| CDSS Appeal Outcome | Pending, Upheld, Override |
| CDSS Appeal Date | Date appeal decided |
| CDSS Appeal By | User ID |
| Comments | Text |

**Actions:**
- **CDSS Appeal:** Open Modify CDSS Appeal screen (CDSS staff only)
- **Comments:** View appeal comments

**Outcomes:**
- **Upheld:** Provider must wait suspension period (90 or 365 days)
- **Override:** Provider can be reinstated immediately

#### **Training Section** (Violation #2 only, first time)

**When provider incurs 2nd violation for first time, may complete training to remove violation**

**Fields:**

| Field | Display/Entry |
|-------|---------------|
| Training Available | Yes/No |
| Date Training Completed | Date provider completed self-certification |
| Training Completed By | User ID who entered date |

**Timeline:**
- Provider: 14 calendar days from notification (extended to 21 days with CDSS approval)
- County: Additional 5 calendar days to enter completion date
- Total: 19-26 calendar days before system auto-generates SOC 2257B

**Actions:**
- Enter Date Training Completed (if provider submits self-certification within timeframe)

**Note:** Training option available only ONCE. If another violation occurs after training completion, training is NOT available again.

---

### Modify County Review

**Access:** View Overtime Violations → County Review link

**Purpose:** Enter county worker's review decision on violation

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| County Review Outcome | Radio | Yes | Pending Uphold or Pending Override |
| County Review Date | Date | Auto | Current date |
| Comments | Text Area | Yes | Required for outcome (1,000 chars) |

**Outcome Options:**
- **Pending Uphold:** Worker believes violation should stand
- **Pending Override:** Worker believes violation should be removed

**Actions:**
- **Save:** Record review decision
- **Cancel:** Close without saving

**Timeline:** 3 business days from violation trigger

**Edit Window:** Can edit until end of business day (nightly batch locks record)

**Key Concept:** Comments required to document why outcome decision was made. Anyone reviewing violation later can understand rationale.

---

### Modify County Supervisor Review

**Access:** View Overtime Violations → Supervisor Review link (if County Review = Override)

**Purpose:** Supervisor reviews county worker's override request

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Supervisor Review Outcome | Radio | Yes | Pending Approval or Pending Rejection |
| Supervisor Review Date | Date | Auto | Current date |
| Comments | Text Area | Yes | Required (1,000 chars) |

**Outcome Options:**
- **Pending Approval:** Supervisor agrees with override
- **Pending Rejection:** Supervisor disagrees with override

**Actions:**
- **Save:** Record supervisor decision
- **Cancel:** Close without saving

**Timeline:** 2 business days (additional) after County Review Override

**Edit Window:** Can edit until end of business day

---

### Modify County Dispute

**Access:** View Overtime Violations → County Dispute link (after provider files dispute)

**Purpose:** Review and decide on provider's dispute of violation

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| County Dispute Filed Date | Date | Yes | Date provider filed (must be within 10 calendar days of letter) |
| County Dispute Outcome | Radio | Yes | Pending Uphold or Pending Override |
| County Dispute Date | Date | Auto | Current date |
| Comments | Text Area | Yes | Required (1,000 chars) |

**Actions:**
- **Save:** Record dispute decision
- **Cancel:** Close without saving

**Timeline:** 10 business days total (worker + supervisor combined)

**Tasks:**
- Task triggered when Dispute Filed Date entered
- Alerts at Day 6 and Day 8 if not resolved

**Validation:** Dispute Filed Date cannot be more than 10 business days from County Review Letter Date

---

### Modify Supervisor Dispute

**Access:** View Overtime Violations → Supervisor Dispute link (if County Dispute = Override)

**Purpose:** Supervisor reviews dispute override request

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Supervisor Dispute Outcome | Radio | Yes | Pending Approval or Pending Rejection |
| Supervisor Dispute Date | Date | Auto | Current date |
| Comments | Text Area | Yes | Required (1,000 chars) |

**Actions:**
- **Save:** Record supervisor decision
- **Cancel:** Close without saving

**Timeline:** Must complete within 10-day total dispute timeline

---

### County Review History / Supervisor Review History

**Access:** View Overtime Violations → History link (in County/Supervisor Review sections)

**Purpose:** View all changes made to review outcomes

**Display Format:** Table with columns:

| Column | Description |
|--------|-------------|
| Review Outcome | Previous outcome value |
| Review Date | Date of review |
| Review By | User ID |
| Comments | Review comments |
| Last Updated By | User who made change |
| Last Updated Date | Date/Time of change |

**Sort Order:** Most recent change first

**No Actions:** View only

**Key Concept:** History captures every change to violation outcomes so full timeline can be reviewed

---

### Violation Details History

**Access:** View Overtime Violations → Violation Details History link (if available)

**Purpose:** View all violation detail changes

**Display Format:** Similar to County Review History

**Shows:** All field changes for violation record (dates, statuses, counts, etc.)

---

## 9. PROVIDER SICK LEAVE {#sick-leave}

### Sick Leave

**Access:** View Provider Details → Sick Leave link

**Purpose:** View provider's sick leave accrual, eligibility, and usage by fiscal year

**Display Format:** Table of fiscal years:

| Column | Description |
|--------|-------------|
| Fiscal Year | July 1 - June 30 period |
| Accrued Date | Date 100 hours paid (first day of pay period) |
| Eligible Date | Date eligible to claim (first day of pay period) |
| Accrued Hours | Total hours for fiscal year (8, 16, or 24) |
| Remaining Hours | Hours available to claim |
| Paid Hours | Hours claimed and paid |
| Status | Accruing, Eligible, Expired |

**Fiscal Year Allocations:**
- **2018-2020:** 8 hours per fiscal year
- **2020-2022:** 16 hours per fiscal year
- **2022-ongoing:** 24 hours per fiscal year

**Actions:**
- **View:** View sick leave details for fiscal year
- No add/edit functions (accrual and eligibility automatic)

**Sort Order:** Most recent fiscal year first

**Key Concepts:**
- Accrual: Provider must work 100 paid service hours
- Eligibility: After accrual, must work additional 200 hours OR wait 60 calendar days
- Hours do NOT carry over between fiscal years

---

### View Sick Leave (Fiscal Year Details)

**Access:** Sick Leave list → Click fiscal year row

**Purpose:** View complete sick leave information for specific fiscal year

**Display Sections:**

#### **Fiscal Year Information**

| Field | Display |
|-------|---------|
| Fiscal Year | July 1, YYYY - June 30, YYYY |
| Accrued Date | Date (first day of pay period when 100th hour paid) |
| Eligible Date | Date (first day of pay period when eligible) |
| Accrued Hours | HH:MM total for year |
| Remaining Hours | HH:MM available to claim |
| Paid Hours | HH:MM claimed and paid |
| Status | Accruing, Eligible, Expired |

#### **Eligibility Tracking**

| Field | Display |
|-------|---------|
| Service Hours Paid | Count toward 100-hour accrual |
| Additional Hours Paid | Count toward 200-hour eligibility (after accrual) |
| Days Since Accrual | Calendar days since accrual date |

#### **Sick Leave Claims** (list of claims for this fiscal year)

Table showing:

| Column | Description |
|--------|-------------|
| Claim Number | System-generated ID |
| Recipient Case | Case name and number |
| Service Date(s) | Date(s) sick leave claimed |
| Hours Claimed | HH:MM |
| Claim Date | Date claim submitted |
| Pay Date | Date paid |
| Payment Amount | $ amount |
| Status | Pending, Paid, Voided |

**Actions:**
- **View Claim:** View sick leave claim details
- **Reissue Sick Leave:** Reissue voided payment (if applicable)

**No add function:** Sick leave claimed via paper form or ESP (Electronic Service Portal)

---

### View Sick Leave Claim

**Access:** View Sick Leave → Click claim row

**Purpose:** View complete sick leave claim details

**Display Fields:**

| Field | Display |
|-------|---------|
| Claim Number | System-generated ID |
| Fiscal Year | FY of claim |
| Recipient Case | Case name and number |
| Service Date(s) | Date(s) sick leave taken |
| Hours Claimed | HH:MM total |
| Claim Submitted Date | Date provider submitted claim |
| Claim Received Date | Date county received claim |
| Pay Rate | County wage rate for service date(s) |
| Payment Amount | $ calculated |
| Pay Date | Date paid |
| Warrant Number | Payment warrant number |
| Status | Pending, Paid, Voided |
| Void Reason | If voided (e.g., Non-Deliverable) |

**Actions:**
- **Reissue Sick Leave:** Reissue if voided (only for voided claims)

**Key Concept:** Sick leave paid at county wage rate for that recipient case on service date(s). Never paid at overtime rate.

---

### Reissue Sick Leave

**Access:** View Sick Leave Claim → Reissue Sick Leave link (voided claims only)

**Purpose:** Reissue sick leave payment after warrant voided as non-deliverable

**Screen Type:** Confirmation pop-up

**Display:** 
- Claim details
- Original hours and amount
- Current remaining hours
- Warning if remaining hours < original claim

**Actions:**
- **Yes, Reissue:** Process reissue
- **No, Cancel:** Cancel action

**Business Logic:**
- Voided warrant: Hours credited back to fiscal year
- County should verify/update provider mailing address first
- If original hours > remaining hours: Reissue pays only up to remaining
- Payment processed against original service date(s)

**Important:** Sick leave claims must be submitted by end of month following service dates (e.g., September 23 claim by October 31)

**Fiscal Year End Claims:** Unclaimed hours at 6/30 must be claimed by 7/31 or lost

---

## 10. DOCUMENT IMPORT {#document-import}

### Provider Attachments

**Access:** View Provider Details → Attachments link

**Purpose:** Upload, view, and manage scanned provider documents

**Display Format:** Table of uploaded documents:

| Column | Description |
|--------|-------------|
| Document Type | Form type (from allowed list) |
| File Name | Original file name |
| Upload Date | Date uploaded |
| Uploaded By | User ID |
| Status | Active, Archived, Pending Archive |

**Actions:**
- **Upload:** Upload new document
- **View:** View/download document
- **Archive:** Mark for archiving (nightly batch)
- **Restore:** Restore pending archive (same day only)

**Allowed Provider Forms:**
- SOC 426 - Provider Enrollment Form
- SOC 846 - Provider Enrollment Agreement
- SOC 2305 - Request for Exemption from Workweek Limits (Exemption 2)
- SOC 2308 - Exemption from Workweek Limits Approved Provider Agreement
- SOC 2313 - Exemption State Administrative Review Request Form

**File Requirements:**
- **Types Allowed:** PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG
- **Maximum Size:** 5 MB per file
- **Quantity:** One copy of each form type (except forms that allow multiples)

**Sort Order:** Most recent Upload Date first

---

### Upload Provider Document

**Access:** Provider Attachments → Upload

**Purpose:** Upload scanned provider form to case management system

**Screen Type:** Pop-up

**Data Fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Document Type | Dropdown | Yes | Select from allowed forms list |
| File | File Browser | Yes | Select file to upload |
| Comments | Text Area | No | Optional description |

**Actions:**
- **Upload:** Upload file to system
- **Cancel:** Close without uploading

**Validation:**
- File type must be in allowed list
- File size must be ≤ 5 MB
- Only one copy of each form type allowed (except specified forms)
- System cannot validate form contents (user responsible for correct form selection)

**Processing:**
- File uploaded to repository
- Available immediately for viewing
- Retained as long as provider is active

---

### View Provider Document

**Access:** Provider Attachments → Click document row OR View link

**Purpose:** View or download uploaded document

**Display:** Document opens in browser viewer or downloads based on file type

**Actions:**
- **Download:** Download file
- **Close:** Close viewer

**No edit function:** Documents cannot be modified once uploaded. Must archive and upload new version if correction needed.

---

### Archive Provider Document

**Access:** Provider Attachments → Archive link

**Purpose:** Mark document for archival (removal from active view)

**Screen Type:** Confirmation pop-up

**Display:** "Are you sure you want to archive this document?"

**Actions:**
- **Yes:** Mark as Pending Archive
- **No:** Cancel action

**Processing:**
- Status changed to "Pending Archive"
- Document archived during nightly batch cycle
- Can be restored only on same day before batch runs

**Use Cases:**
- Uploaded wrong document
- Uploaded corrected version
- Document no longer needed

---

### Restore Provider Document

**Access:** Provider Attachments → Restore link (Pending Archive documents only)

**Purpose:** Restore document marked for archive (same day only)

**Screen Type:** Confirmation pop-up

**Availability:** Only before nightly batch runs (same business day as archive action)

**Actions:**
- **Yes:** Restore to Active status
- **No:** Cancel

**Important:** Once nightly batch runs, document permanently archived and cannot be restored

---

## SUMMARY OF KEY SCREEN CONCEPTS

### Screen Access Patterns

**Provider-Centric Screens:** Access via View Provider Details
- Enrollment History
- CORI Management
- Benefits Deduction
- Workweek Agreements
- Travel Time
- Overtime Violations
- Sick Leave
- Attachments

**Case-Centric Screens:** Access via Case Home
- Case Providers
- Assign Case Provider
- View/Modify Case Provider
- Provider Hours
- Recipient Workweek Agreement

**Search Entry Points:**
- Person Search (Register Provider)
- Case search (to access Case Providers)

### Common Screen Patterns

**List → View → Modify → History**
Most provider management features follow this pattern:
1. List screen: Shows all records in table
2. View screen: Displays complete details for selected record
3. Modify screen: Pop-up for editing (Save/Cancel)
4. History screen: Shows all changes or inactive records

**Pop-up Screens:**
- Most data entry and modification screens
- Confirmation screens for delete/inactivate actions
- Standardized Save/Cancel buttons

**Actions/Links:**
- Blue hyperlinked text: Navigation to related screens
- Buttons: Primary actions (Save, Modify, New)
- Icons: Search functionality, help text

### Data Validation

**Required Fields:** Indicated with red asterisk (*) or validation on save

**Field-Level Validation:**
- SSN: 9 digits
- Phone: (XXX) XXX-XXXX format
- Email: Valid email format
- Dates: MM/DD/YYYY format
- Times: HH:MM format (hours:minutes)

**Business Rule Validation:**
- Provider eligibility checks before assignment
- Hour maximums (weekly, monthly, travel)
- Timeframe validations (e.g., 30 days for reinstatement)
- Relationship-based funding source changes

### Screen Security

**Role-Based Access:**
- County workers: Standard provider management
- Supervisors: Approval and override functions
- WPCS staff: WPCS-specific screens
- CDSS staff: Statewide functions, appeals

**County-Based Access:**
- DOJ County: Determines which county can modify provider
- Sending County: Special permissions during Inter-County Transfer

### Batch Processing Integration

**Screens Affected by Nightly Batch:**
- Provider Eligibility status updates (SSN verification, suspended providers)
- Overtime violation outcome processing
- Sick Leave fiscal year records
- Provider inactivity checks
- Document archival

**Edit Windows:**
- Most modification screens: Editable until end of business day
- After batch runs: Record locked, history created

---

## NAVIGATION SUMMARY

### Provider Management Main Navigation Flow

```
My Workspace
└── Register a Provider
    └── Person Search
        ├── Continue Registration → Create Provider → Person Home
        └── Select Person → Create Provider → Person Home
            └── Provider Link
                └── View Provider Details
                    ├── Modify Enrollment
                    ├── Reinstate / Re-enroll / Approve / Reject
                    ├── Cases → Cases List
                    │   └── Click Case → Case Home
                    ├── Enrollment History
                    ├── CORI → Provider CORI Details
                    │   ├── Create/View/Modify/Inactivate CORI
                    │   └── Associated Recipient Waivers
                    ├── Benefits Deduction → Create/Modify Deductions
                    ├── Workweek Agreement → Create/View/Modify/History
                    ├── Travel Time → Travel Time Recipient Case
                    │   └── Provider Travel Time Hours
                    │       └── Create/View/Modify/Leave/Terminate/History
                    ├── Overtime Violations → View Violations
                    │   └── County Review / Supervisor Review / Dispute / Appeal
                    ├── Sick Leave → View Fiscal Year Details
                    │   └── View Claims / Reissue
                    └── Attachments → Upload/View/Archive/Restore
```

### Case-Based Provider Access Flow

```
Case Home
└── Evidence & Authorization Tab
    └── Case Providers
        ├── Assign Provider → Assign Case Provider
        └── View Provider → View Case Provider
            ├── Modify
            ├── Leave/Terminate
            ├── Provider Hours → Create/Modify/View/History
            └── WPCS Details → Create/Modify/View/History
```

---

**Document Created:** December 10, 2025  
**Source:** _Release_2025_03_01__DSD_Section_23.pdf  
**Total Screens Documented:** 60+ screens across 10 functional areas
