# IHSS CMIPS - Electronic Services Portal (ESP) Documentation
## Part 2: Provider Functionality
### Release 2025.03.01 - DSD Section 4

---

## TABLE OF CONTENTS - PART 2
1. [Provider Time Entry](#time-entry)
2. [Provider Timesheet Submission](#submission)
3. [Provider Payment History](#payments)
4. [Provider Sick Leave](#sick-leave)
5. [Provider Direct Deposit](#direct-deposit)
6. [Provider Travel Claims](#travel-claims)
7. [Provider Career Pathways](#career-pathways)

---

## 1. PROVIDER TIME ENTRY {#time-entry}

### Provider Home Screen

**Access:** After login

**Quick Actions:**
- **Enter Time** (primary action)
- Review Timesheets
- Payment History
- Sick Leave
- Career Pathways
- Direct Deposit

**Recent Activity:**
- Last 3 timesheets with status
- Last 3 payments
- Action items

**Notifications:**
- Timesheet due dates
- Payment processed
- Account updates

---

### Timesheet Entry – Recipient Selection

**Purpose:** Select recipient (if provider serves multiple)

**Display:** Table of recipients

| Recipient | Case # | Authorized Hours | Status | Action |
|-----------|--------|------------------|--------|--------|
| Smith, John | 1234567 | 80:00/month | Submitted | Enter Time |
| Jones, Mary | 2345678 | 40:00/month | Not Started | Enter Time |

**Status Options:**
- Not Started
- In Progress
- Submitted
- Approved
- Rejected
- Paid

**Single Recipient:** Bypass to time entry

---

### Timesheet Entry Screen

**Screen Header:**

| Field | Display |
|-------|---------|
| Provider Name | Full name, provider # |
| Recipient Name | Full name, case # |
| Pay Period | Dates |
| Authorized Hours | Weekly/Monthly |
| Service Types | List authorized |

**Entry Formats:**

#### Daily Entry Format

| Date | Service Type | Hours | Minutes | Total | Actions |
|------|--------------|-------|---------|-------|---------|
| 01/01 | Domestic | 04 | 30 | 4:30 | Edit/Delete |
| 01/02 | Domestic | 05 | 00 | 5:00 | Edit/Delete |

**Add Entry:**
- Date (calendar, within pay period)
- Service Type (dropdown based on authorization)
- Hours (0-24)
- Minutes (00, 15, 30, 45)

**Service Types:**
- Domestic Services
- Personal Care Services
- Accompaniment Services
- Paramedical Services
- Protective Supervision
- Meal Preparation
- Respite Services (WPCS)

**Entry Rules:**
- 15-minute increments minimum
- Cannot exceed 24 hours/day
- Cannot exceed authorized hours
- Cannot enter future dates
- Cannot enter >12 months past

#### Weekly Entry Format (Live-In)

| Week | Start | End | Hours | Minutes | Total |
|------|-------|-----|-------|---------|-------|
| 1 | 01/01 | 01/07 | 40 | 00 | 40:00 |
| 2 | 01/08 | 01/15 | 40 | 00 | 40:00 |

---

### Timesheet Summary

| Category | Value |
|----------|-------|
| Total Hours This Period | XX:XX |
| Authorized Hours | XX:XX |
| Remaining Hours | XX:XX |
| Overtime Hours | XX:XX |
| Travel Hours | XX:XX |

**Color Coding:**
- Green: Within authorized
- Yellow: 90%+ of authorized
- Red: Exceeds authorized

**Warnings:**
- Overtime violation warnings
- Weekly maximum warnings
- Share of Cost warnings
- EVV compliance warnings

---

### EVV Time Entry (Check-In/Check-Out)

**Mobile Check-In:**

1. Provider arrives at location
2. Opens ESP on mobile
3. Taps "Check In"
4. System captures:
   - GPS coordinates
   - Time stamp
   - Recipient
   - Service type
5. Confirmation displayed

**Check-In Confirmation:**
- "You are checked in for [Recipient]"
- Service start time
- Current location (address)
- "Check Out" button

**After Service:**

1. Provider completes services
2. Taps "Check Out"
3. System captures:
   - GPS coordinates
   - Time stamp
   - Calculates duration
4. Confirmation displayed

**Check-Out Confirmation:**
- "You are checked out"
- Service end time
- Total duration
- "View Timesheet" button

**Location Verification:**
- GPS vs recipient address
- Warning if outside geofence
- Can attest if GPS inaccurate
- County can review discrepancies

**Rules:**
- Must check out before checking in again
- Cannot check in twice simultaneously
- Check-out must be after check-in
- Maximum shift length warnings

---

### Validation

**Real-Time:**
- Hours don't exceed 24/day
- Hours don't exceed weekly maximum
- Total doesn't exceed authorized
- Service types match authorization
- Dates within allowed range

**Validation Messages:**

| Type | Example |
|------|---------|
| Error | "Cannot enter more than 24 hours for single day" |
| Warning | "Approaching weekly maximum hours" |
| Info | "Timesheet will require recipient approval" |

**Submit Validation:**
- At least one entry
- All required fields complete
- No validation errors
- No duplicates
- EVV requirements met

---

### Save Options

**Buttons:**

| Button | Function |
|--------|----------|
| Save Draft | Save without submitting (return later) |
| Submit Timesheet | E-signature and submit |
| Cancel | Discard and return |
| Print | Print for records |

**Save Draft:**
- Status: "In Progress"
- Edit anytime before submission
- Not visible to recipient
- Saved 90 days

**Submit:** Leads to electronic signature

---

## 2. PROVIDER TIMESHEET SUBMISSION {#submission}

### Electronic Signature Screen

**Timesheet Summary:**
- Provider/Recipient names
- Pay period
- Total hours
- Service breakdown
- Estimated compensation

**Certification Statement:**
```
I certify under penalty of perjury under the laws of California that:

1. I certify the hours I worked providing IHSS/WPCS services
2. I certify I provided services to the recipient listed
3. I understand falsifying may result in criminal prosecution
4. Electronic submission constitutes my legal signature
5. I have reviewed the timesheet for accuracy

I agree and submit this timesheet for payment.
```

**Fields:**

| Field | Type | Required |
|-------|------|----------|
| Full Name | Text | Yes (must match) |
| Date | Date | Auto (read-only) |
| Electronic Signature | Text | Yes (type name) |

**Checkbox:** [ ] I agree to terms

**Buttons:**
- Submit Timesheet
- Back (return to time entry)
- Cancel

**Validation:**
- Signature must match name
- Must check agreement box
- No validation errors

---

### Submission Confirmation

**Success Message:** "Your timesheet has been successfully submitted!"

**Details:**
- Confirmation number
- Recipient name
- Pay period
- Total hours
- Submission date/time

**Next Steps:**
1. Recipient will be notified
2. Recipient must approve
3. After approval → payroll
4. Payment per normal schedule
5. Notification of approval/rejection

**Buttons:**
- View Timesheet
- Enter Another Timesheet
- Return to Home
- Print Confirmation

**Email:** Confirmation sent with details

---

### Timesheet Status Tracking

**Access:** Provider Home → Review Timesheets

**Display:** Table of timesheets

| Pay Period | Recipient | Status | Submitted | Hours | Action |
|------------|-----------|--------|-----------|-------|--------|
| 01/01-01/15 | Smith, J | Submitted | 01/16 | 80:00 | View |
| 12/16-12/31 | Smith, J | Approved | 12/31 | 75:30 | View |
| 12/01-12/15 | Smith, J | Paid | 12/15 | 78:00 | View |

**Status Definitions:**

| Status | Meaning | Provider Action |
|--------|---------|-----------------|
| Draft | Not submitted | Complete and submit |
| Submitted | Awaiting recipient | Wait for approval |
| Approved | Recipient approved | Wait for payment |
| Processing | In payroll | Wait for payment |
| Paid | Payment issued | View payment details |
| Rejected | Recipient rejected | Review and resubmit |
| Voided | County cancelled | Contact county |

**Filters:**
- All / Current Period / Pending / Rejected / Paid
- By Recipient
- By Date Range

**Sort:**
- By pay period (newest first)
- By recipient
- By status

---

### Rejected Timesheet Handling

**Rejection Notification:**
- Email (if preference set)
- Text (if preference set)
- ESP notification at login

**Rejection Display:**

**Rejection Information:**
- Rejected by: Recipient name
- Rejected on: Date/Time
- Reason: Dropdown selection
- Comments: Recipient explanation

**Common Reasons:**
- Hours don't match services provided
- Service dates incorrect
- Service types incorrect
- Provider not present on dates
- Hours claimed exceed actual time
- Other (see comments)

**Edit Rejected:**
1. Review reason and comments
2. Make corrections
3. Modify/delete/add entries
4. Resubmit with e-signature
5. Recipient must approve again

**Resubmission:**
- New submission date recorded
- Previous rejection history maintained
- Recipient notified
- Process repeats

**Dispute:**
If provider disagrees:
1. Contact recipient to discuss
2. Contact Social Worker if needed
3. County can investigate
4. County can override (exceptional)

---

## 3. PROVIDER PAYMENT HISTORY {#payments}

### Payment History Screen

**Access:** Provider Home → Payment History

**Date Range:** Last 3 months

**Display:** Table of payments

| Payment Date | Recipient | Pay Period | Gross | Net | Status | Action |
|--------------|-----------|------------|-------|-----|--------|--------|
| 01/25 | Smith, J | 01/01-01/15 | $1,200 | $1,050 | Paid | View |
| 01/10 | Smith, J | 12/16-12/31 | $1,150 | $1,010 | Paid | View |

**Status:**
- Paid (direct deposit or warrant issued)
- Processing (in payroll)
- Pending (approved, awaiting processing)
- Voided (cancelled)
- Reissued (original voided, reissued)

**Filters:**
- By date range
- By recipient
- By status
- By payment method

**Sort:**
- By payment date (newest first)
- By recipient
- By amount

**Summary:**
- Total Paid (3 months): $XX,XXX
- Number of Payments: XX
- Average Payment: $XXX

---

### Payment Detail Screen

**Payment Information:**

| Field | Value |
|-------|-------|
| Provider Name | Name, provider # |
| Recipient Name | Name, case # |
| Pay Period | Dates |
| Payment Date | Date issued |
| Payment Method | Direct Deposit / Warrant |
| Warrant Number | (if warrant) |
| Status | Paid/Voided/Reissued |

**Earnings Breakdown:**

| Description | Rate | Hours | Amount |
|-------------|------|-------|--------|
| Regular Hours | $18.00/hr | 75:00 | $1,350.00 |
| Overtime Hours | $27.00/hr | 5:00 | $135.00 |
| Travel Time | $18.00/hr | 3:00 | $54.00 |
| Sick Leave | $18.00/hr | 8:00 | $144.00 |
| Career Pathways | $18.00/hr | 15:00 | $270.00 |
| **Gross Earnings** | | **106:00** | **$1,953.00** |

**Deductions:**

| Description | Amount |
|-------------|--------|
| Federal Income Tax | $195.30 |
| State Income Tax | $97.65 |
| Social Security (FICA) | $121.09 |
| Medicare | $28.32 |
| State Disability (SDI) | $19.53 |
| Health Insurance | $50.00 |
| **Total Deductions** | **$511.89** |

**Payment Summary:**

| Category | Amount |
|----------|--------|
| Gross Earnings | $1,953.00 |
| Total Deductions | $511.89 |
| **Net Payment** | **$1,441.11** |

**Year-to-Date:**
- YTD Gross: $23,436.00
- YTD Deductions: $6,142.68
- YTD Net: $17,293.32

**Buttons:**
- Download Pay Stub (PDF)
- Email Pay Stub
- Print
- View Timesheet
- Back

---

### EVV Payment Detail

**Additional for EVV:**

**EVV Compliance:**
- EVV Required: Yes/No
- EVV Method: Check-In/Out / Manual
- Compliance Status: Compliant / Non-Compliant
- Location Verified: Yes/No

**Visit Details:**

| Date | Check-In | Check-Out | Duration | Location | Service |
|------|----------|-----------|----------|----------|---------|
| 01/01 | 9:00 AM | 1:30 PM | 4:30 | 123 Main St | Domestic |
| 01/02 | 9:00 AM | 2:00 PM | 5:00 | 123 Main St | Domestic |

**Location Map:**
- Interactive map
- Check-in/out locations marked
- Recipient address marked
- Color-coded (green/yellow/red)

---

## 4. PROVIDER SICK LEAVE {#sick-leave}

### Sick Leave Overview

**California SB 3:** Paid sick leave for IHSS/WPCS providers

**Eligibility:**
- Work 100 hours to accrue (initial)
- Work additional 200 hours OR wait 60 days to claim
- Once eligible, automatically eligible each year

**Accrual Rates:**

| Fiscal Year | Hours Accrued |
|-------------|---------------|
| 7/1/2018 - 6/30/2020 | 8 hours |
| 7/1/2020 - 6/30/2022 | 16 hours |
| 7/1/2022 - ongoing | 24 hours |

**Fiscal Year:** July 1 - June 30

**Claiming Rules:**
- Minimum 1 hour/day
- 30-minute increments after first hour
- Claim by end of month following service dates
- Unclaimed by July 31 are lost (no carryover)
- Paid at county wage rate (never overtime)
- Separate claim per recipient

---

### Sick Leave Claim Screen

**Access:** Provider Home → Sick Leave Claim

**Eligibility Check:**
1. Has accrued hours
2. Eligible to claim (300 hours or 60 days)
3. Has available hours
4. Was assigned during claim period

**If Not Eligible:** Message explaining why

**If Eligible:**

**Availability Display:**

**Current Fiscal Year:**

| Field | Value |
|-------|-------|
| Fiscal Year | 7/1/2024 - 6/30/2025 |
| Hours Accrued | 24:00 |
| Hours Claimed | 8:00 |
| **Hours Available** | **16:00** |

**Prior FY (July only):**
- Shows prior year available
- Claim deadline July 31

**Claim Selection:**

| Field | Type | Required |
|-------|------|----------|
| Pay Period | Dropdown | Yes |
| Recipient | Dropdown | Yes |

**Pay Period Options:**
- Current month
- Prior month
- (July: Prior FY also)

**Recipient Options:**
- Only recipients served during pay period
- Only with sick leave eligibility

**Buttons:** Next, Cancel

---

### Sick Leave Time Entry

**Display Header:**

| Field | Value |
|-------|-------|
| Provider | Name, number |
| Recipient | Name, case # |
| Pay Period | Dates |
| Available Hours | HH:MM |
| Already Claimed | HH:MM (this period) |
| Remaining | HH:MM |

**Time Entry:**

| Date | Hours | Minutes | Total | Actions |
|------|-------|---------|-------|---------|
| | | | | Add Entry |

**Add Entry:**
- Date (calendar, within pay period)
- Hours (0-8 typically)
- Minutes (00, 30 after first hour)

**Rules:**
- Minimum 1:00
- 30-minute increments
- Max per day typically 8:00
- Cannot exceed available
- Cannot claim same date twice
- Cannot claim already-paid dates

**Real-Time Calculation:**
- Total Claimed This Entry
- Available Hours
- Remaining

**Buttons:**
- Submit Claim (to e-signature)
- Save Draft
- Cancel

---

### Sick Leave Electronic Signature

**Claim Summary:**
- Provider/Recipient
- Pay Period
- Fiscal Year
- Total Hours
- Estimated Payment

**Dates Claimed:**
- List with hours
- Total hours

**Certification:**
```
I certify under penalty of perjury that:

1. I was unable to work the hours claimed due to illness/injury
2. I did not provide services on dates claimed
3. Information is true and correct
4. Falsifying may result in repayment, prosecution, ineligibility

I submit this claim for payment.
```

**Fields:**
- Full Name (must match)
- Date (auto)
- Electronic Signature

**Checkbox:** [ ] I agree

**Buttons:** Submit Claim, Back, Cancel

---

### Sick Leave Confirmation

**Success:** "Sick leave claim successfully submitted!"

**Details:**
- Confirmation Number: SLXXXXXXXXXXXX
- Recipient
- Pay Period
- Total Hours
- Estimated Payment
- Submission Date

**Processing:**
1. County reviews
2. If approved, payment within 2-3 pay periods
3. At county wage rate
4. Notification when issued
5. Check Payment History

**Important:**
- Hours deducted from available
- If denied, hours credited back
- View status in Claim History
- Contact county with questions

**Buttons:**
- View Claim Details
- Submit Another Claim
- View Claim History
- Return to Home
- Print Confirmation

---

### Sick Leave Claim History

**Access:** Provider Home → Sick Leave Claim History

**Fiscal Year Selector:** Dropdown

**FY Summary:**

| Field | Value |
|-------|-------|
| Fiscal Year | Dates |
| Accrued Date | Date 100 hrs paid |
| Eligible Date | Date eligible to claim |
| Accrued Hours | Total for year |
| Remaining Hours | Available |
| Paid Hours | Claimed and paid |
| Status | Accruing/Eligible/Expired |

**Claims Table:**

| Claim Date | Recipient | Pay Period | Hours | Status | Payment | Action |
|------------|-----------|------------|-------|--------|---------|--------|
| 01/20 | Smith, J | 01/01-01/15 | 16:00 | Paid | $288 | View |
| 12/15 | Smith, J | 12/01-12/15 | 8:00 | Processing | Pending | View |

**Status:**
- Pending (awaiting county review)
- Approved (approved, awaiting payment)
- Processing (in payroll)
- Paid (payment issued)
- Denied (county denied)
- Voided (claim cancelled)

**Filters:**
- All / Pending/Processing / Paid / Denied
- By recipient
- By pay period

---

## 5. PROVIDER DIRECT DEPOSIT {#direct-deposit}

### Direct Deposit Overview

**Purpose:** Receive payments via bank account instead of paper warrant

**Benefits:**
- Faster access (no mail delay)
- No manual deposit
- More secure
- Automatic every pay period
- Free service

**Eligibility:** All providers

**Processing Time:**
- New enrollment: 2-3 pay periods
- Changes: 1-2 pay periods
- Cancellation: 1-2 pay periods

---

### Direct Deposit Screen

**Access:** Provider Home → Direct Deposit

**If No Direct Deposit:**
- Message: "You are not currently enrolled"
- "Enroll Now" button

**If Enrolled:**

**Table by Recipient:**

| Recipient | Status | Bank | Account (Last 4) | Effective Date | Action |
|-----------|--------|------|------------------|----------------|--------|
| Smith, J | Active | Bank of America | XX-1234 | 01/01 | Change/Cancel |
| Jones, M | Pending | Wells Fargo | XX-5678 | 02/01 | Cancel |

**Status:**
- Pending (submitted, not effective)
- Active (in effect)
- Cancelled (processed)
- Change Pending (update submitted)

**Buttons:**
- Enroll in Direct Deposit
- Change Bank Information
- Cancel Direct Deposit
- View Enrollment History

---

### Recipient Selection (Enroll)

**Purpose:** Select which recipient(s) to enroll

**Table:**

| ☐ | Recipient | Case # | Current Status | Avg Monthly Payment |
|---|-----------|--------|----------------|---------------------|
| ☐ | Smith, J | 1234567 | Not Enrolled | $1,200 |
| ☑ | Jones, M | 2345678 | Not Enrolled | $800 |
| ☐ | Davis, B | 3456789 | Already Enrolled | $950 |

**Select All:** [ ] Select All Recipients

**Validation:**
- Must select at least one
- Cannot select already enrolled
- Cannot select terminated

**Buttons:** Next, Cancel

---

### Bank Details Entry

**Selected Recipients:** [List displayed]

**Fields:**

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| Bank Name | Text | Yes | Free text |
| Bank Routing Number | Number | Yes | 9 digits, valid |
| Confirm Routing | Number | Yes | Must match |
| Account Type | Radio | Yes | Checking / Savings |
| Account Number | Number | Yes | Up to 17 digits |
| Confirm Account | Number | Yes | Must match |

**Account Type:**
- ○ Checking Account
- ○ Savings Account

**Instructions:**
- Routing number: 9 digits at bottom of check
- Account number: Varies, at bottom of check
- Verify all information carefully
- Changes take 2-3 pay periods
- You'll receive warrants until active

**Sample Check Image:** Shows where to find numbers

**Buttons:** Next (to e-signature), Back, Cancel

**Validation:**
- Routing number valid (checksum)
- Account number numeric
- Confirmations match
- All fields required

---

### Direct Deposit Electronic Signature

**Summary:**
- Recipients: [List]
- Bank: [Name]
- Account Type: [Checking/Savings]
- Routing: [XXXXXXXXX]
- Account: [XXXXXXX1234] (last 4 shown)
- Estimated Effective: [Date]
- First Direct Deposit: [Date]

**Certification:**
```
I certify that:

1. I am owner/authorized signer on account
2. Bank information is correct and complete
3. I authorize CDSS/MFI to deposit IHSS/WPCS payments
4. I understand:
   - Begins within 2-3 pay periods
   - I'll receive warrants until active
   - I can change or cancel anytime
   - Incorrect info may delay payment
5. I will notify CDSS if account closed/changed

I request direct deposit enrollment.
```

**Fields:**
- Full Name (must match)
- Date (auto)
- Electronic Signature

**Checkbox:** [ ] I agree

**Buttons:** Submit Request, Back, Cancel

---

### Direct Deposit Confirmation

**Success:** "Direct deposit request successfully submitted!"

**Details:**
- Confirmation Number: DDXXXXXXXXXXXX
- Recipients Enrolled: [List]
- Bank Account: [Last 4]
- Processing Time: 2-3 pay periods
- First Direct Deposit: [Date]

**Information:**
1. Request processed within 2-3 pay periods
2. Continue receiving warrants until then
3. Email confirmation when active
4. Bank statement shows "CDSS IHSS" or "EDS IHSS"
5. Questions: Contact county or service desk

**Buttons:**
- View Direct Deposit Status
- Enroll Another Recipient
- Return to Home
- Print Confirmation

**Email:** Confirmation sent

---

### Change/Cancel Direct Deposit

**Change Bank Info:**
1. Select recipients to update
2. Enter new bank information
3. E-signature
4. Processing 1-2 pay periods

**Cancel:**
1. Select recipients to remove
2. Reason (optional): Account closed / Prefer warrants / Other
3. Confirm: [ ] I understand I'll receive warrants
4. E-signature
5. Processing 1-2 pay periods

---

## 6. PROVIDER TRAVEL CLAIMS {#travel-claims}

### Travel Claim Overview

**Purpose:** Claim travel time between recipient locations

**Eligibility:**
- Serve 2+ recipients
- Have Workweek Agreement with Travel
- Maximum 7:00 hours/week

**Rules:**
- Paid at regular rate (not overtime)
- No recipient approval needed
- Separate from timesheet
- Same timeframe as timesheet
- Career Pathways training exempt from 7-hour max

---

### Travel Recipient Selection

**Access:** Provider Home → Time Entry → Travel Claim

**Purpose:** Select "traveling TO" recipient

**Table:**

| ○ | Recipient | Case # | Address | Current Travel (Week) |
|---|-----------|--------|---------|----------------------|
| ○ | Smith, J | 1234567 | 123 Main St | 3:00 |
| ○ | Jones, M | 2345678 | 456 Oak Ave | 2:00 |

**Weekly Summary:**
- Total Travel This Week: 5:00
- Maximum Allowed: 7:00
- Remaining: 2:00

**Validation:**
- Must have Workweek Agreement with Travel
- Cannot exceed 7:00/week
- Must select one recipient

**Buttons:** Next, Cancel

---

### Travel Time Entry

**Header:**

| Field | Value |
|-------|-------|
| Provider | Name, number |
| Traveling TO | Selected recipient |
| Pay Period | Dates |
| Weekly Used | X:XX |
| Weekly Max | 7:00 |
| Weekly Available | X:XX |

**Entry Table:**

| Date | From Recipient | Hours | Minutes | Total | Actions |
|------|----------------|-------|---------|-------|---------|
| | | | | | Add Entry |

**Add Entry:**
- Date (within pay period)
- Traveling FROM (dropdown - all other recipients)
- Hours (0-7)
- Minutes (00, 15, 30, 45)

**Rules:**
- Minimum 0:15
- 15-minute increments
- Max per entry typically 2:00
- Cannot exceed 7:00 weekly
- Cannot claim same route twice/day

**Current Entries:**

| Date | From | To | Hours | Actions |
|------|------|----|-------|---------|
| 01/02 | Smith, J | Jones, M | 0:45 | Edit/Delete |
| 01/03 | Jones, M | Davis, B | 1:00 | Edit/Delete |

**Summary:**
- Total This Claim: 1:45
- Total This Week: 5:00
- Remaining: 2:00

**Buttons:**
- Save Draft
- Submit Claim (to e-signature)
- Cancel

---

### Travel Claim Electronic Signature

**Summary:**
- Provider: [Name]
- Pay Period: [Dates]
- Total Travel: X:XX

**Entries:**

| Date | From | To | Hours |
|------|------|----|-------|
| 01/02 | Smith, J | Jones, M | 0:45 |
| Total | | | 0:45 |

**Estimated Payment:** $XX.XX

**Certification:**
```
I certify that:

1. I traveled between recipients listed
2. Travel was necessary to provide services
3. I have Workweek Agreement with travel
4. Travel time is accurate and truthful
5. I have not claimed these times elsewhere
6. Falsifying may result in repayment, prosecution, ineligibility

I submit this travel claim for payment.
```

**Fields:** Name, Date, Signature

**Checkbox:** [ ] I agree

**Buttons:** Submit Claim, Back, Cancel

---

### Travel Claim Confirmation

**Success:** "Travel claim successfully submitted!"

**Details:**
- Confirmation: TCXXXXXXXXXXXX
- Pay Period: [Dates]
- Total Hours: X:XX
- Estimated Payment: $XX.XX
- Submission Date: [Date]

**Processing:**
1. No recipient approval needed
2. Processed with regular timesheet
3. Payment in next pay period
4. View status in Payment History

**Buttons:**
- View Payment History
- Submit Another Travel Claim
- Return to Home
- Print Confirmation

---

## 7. PROVIDER CAREER PATHWAYS {#career-pathways}

### Career Pathways Overview

**SB 172:** Training opportunities and incentives for career advancement

**Program Types:**
1. **Training Time Claims** - Payment for time in courses
2. **Completion Incentive Claims** - Bonus for 15+ hours in pathway
3. **Mentor Incentive Claims** - Payment for mentoring
4. **Applied Skills Incentive Claims** - Bonus for skill demonstration

**Career Pathways:**
1. Basic Skills:
   - General Health and Safety
   - Adult Education

2. Health Pathway:
   - Foundations in Healthcare
   - Direct Support
   - Home Health Aide
   - Certified Nursing Assistant

3. Care Pathway:
   - Foundations in Caregiving
   - Direct Support
   - Home Health Aide

**Approval:** Claims → CDSS review → Approved claims → Payroll

---

### Claim Type Selection

**Access:** Provider Home → Career Pathways

**Options:**

1. **Training Time Claim**
   - Payment for time in courses
   - At provider hourly rate
   - No limit

2. **Completion Incentive Claim**
   - One-time bonus for 15+ hours
   - $[Amount] per pathway
   - One per pathway

3. **Mentor Incentive Claim**
   - Payment for mentoring
   - At hourly rate
   - Must be approved mentor

4. **Applied Skills Incentive Claim**
   - Bonus for demonstrating skills
   - $[Amount]
   - Assessment period required

**Select:** Radio buttons

**Buttons:** Next, Cancel

---

### Training Time Claim Entry

**Header:**
- Provider: [Name]
- Claim Type: Training Time
- Pay Period: [Dropdown]
- Total Hours: Auto-calculated

**Entry Table:**

| Career Pathway | Class # | Class Name | Training Date | Hours | Min | Actions |
|----------------|---------|------------|---------------|-------|-----|---------|
| | | | | | | Add Course |

**Add Course:**
- Career Pathway (dropdown - 5 options)
- Class Number (text - from provider)
- Class Name (text - official name)
- Training Date (date - within pay period)
- Hours (0-8)
- Minutes (00, 15, 30, 45)

**Pathway Options:**
1. General Health and Safety
2. Adult Education
3. Health Pathway
4. Care Pathway

**Rules:**
- Multiple courses per claim
- Mix pathways
- Unique class numbers
- Within pay period
- Hours reasonable (≤8 typical)

**Current Entries:**

| Pathway | Class# | Name | Date | Hours | Actions |
|---------|--------|------|------|-------|---------|
| Health | H-101 | Basic Anatomy | 01/05 | 3:00 | Edit/Delete |
| Health | H-102 | Vital Signs | 01/07 | 2:00 | Edit/Delete |

**Claim Summary:**
- Total Training Hours: 5:00
- Estimated Payment: $90.00
- Courses: 2

**Documentation:**
- Completion certificates required
- Verified by CDSS

**Buttons:**
- Save Draft
- Submit Claim (to e-signature)
- Cancel

---

### Career Pathways Electronic Signature

**Claim Summary:**
- Claim Type: [Type]
- Provider: [Name]
- Details: [Courses/incentive/mentoring]
- Total Amount: $XX.XX

**Certification:**
```
I certify that:

1. Information in this Career Pathways claim is true and accurate
2. I completed training/mentoring/skills as claimed
3. I have documentation to support claim
4. Claim will be reviewed and approved by CDSS
5. False claims may result in repayment, prosecution, ineligibility, disqualification
6. Payment only issued after CDSS approval

I submit this Career Pathways claim.
```

**Fields:** Name, Date, Signature

**Checkbox:** [ ] I agree

**Buttons:** Submit Claim, Back, Cancel

---

### Career Pathways Confirmation

**Success:** "Career Pathways claim successfully submitted!"

**Details:**
- Confirmation: CPXXXXXXXXXXXX
- Claim Type: [Type]
- Amount: $XX.XX
- Submission Date: [Date]

**CDSS Approval Process:**
1. Submitted → queued for review
2. Under Review → CDSS reviewing
3. Approved → sent to payroll
4. Paid → payment issued
5. Denied → reason provided

**Timeline:**
- Review: 2-4 weeks
- Approval to Payment: 1-2 pay periods
- Total: 4-8 weeks typical

**Tracking:**
- Check status in Training Report
- Email when approved/denied
- View payments in Payment History

**Buttons:**
- View Training Report
- Submit Another Claim
- Return to Home
- Print Confirmation

---

### Career Pathways Training Report

**Access:** Provider Home → Career Pathways Training Report

**Training Summary by Pathway:**

| Pathway | Completed | Pending | Total | Completion Incentive |
|---------|-----------|---------|-------|---------------------|
| General H&S | 18:00 | 3:00 | 21:00 | Claimed ($500) |
| Health | 12:00 | 0:00 | 12:00 | Available |
| Care | 0:00 | 0:00 | 0:00 | N/A |

**Training Time Claims:**

| Claim Date | Pay Period | Pathway | Courses | Hours | Status | Payment | Action |
|------------|------------|---------|---------|-------|--------|---------|--------|
| 01/15 | 01/01-01/15 | Health | 3 | 9:00 | Approved | $162 | View |
| 12/20 | 12/16-12/31 | Care | 2 | 6:00 | Pending | Pending | View |

**Claim Status:**
- Submitted / Under Review / Approved / Paid / Denied / Returned

**Incentive Claims:**

| Claim Date | Type | Description | Amount | Status | Payment Date |
|------------|------|-------------|--------|--------|--------------|
| 01/20 | Completion | Health Pathway | $500 | Approved | 02/15 |
| 12/10 | Mentor | 20 hrs mentoring | $360 | Paid | 01/10 |

**Course Details:** Expandable list showing all courses with:
- Course info
- Training date
- Hours
- Provider
- Completion certificate link
- Status
- Payment

**Filters/Sort:**
- All / Approved / Pending / Denied
- By pathway
- By date range
- By status

**Buttons:**
- Export to PDF
- Submit New Claim
- Training Resources

---

**End of Part 2**

**See Also:**
- Part 1: Overview and Registration
- Part 3: Recipient Functionality
- Part 4: Shared Features
