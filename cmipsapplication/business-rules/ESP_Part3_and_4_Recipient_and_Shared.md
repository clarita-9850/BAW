# IHSS CMIPS - Electronic Services Portal (ESP) Documentation
## Parts 3 & 4: Recipient Functionality and Shared Features
### Release 2025.03.01 - DSD Section 4

---

## PART 3: RECIPIENT FUNCTIONALITY

### TABLE OF CONTENTS - PART 3
1. [Recipient Home](#recipient-home)
2. [Timesheet Review](#timesheet-review)
3. [Timesheet Approval/Rejection](#approval-rejection)
4. [Hire Provider](#hire-provider)
5. [Payment History](#recipient-payments)

---

## 1. RECIPIENT HOME {#recipient-home}

### Recipient Home Screen

**Access:** After login

**Quick Actions:**
- **Review Timesheets** (primary action)
- View Payment History
- Hire Provider (if age 18+)
- My Preferences

**Pending Reviews:**

| Provider | Pay Period | Hours | Submitted | Action |
|----------|------------|-------|-----------|--------|
| Smith, J | 01/01-01/15 | 80:00 | 01/16 | Review |
| Jones, M | 01/01-01/15 | 40:00 | 01/15 | Review |

**Urgency Indicator:**
- Red: Overdue
- Yellow: Due soon (1-2 days)
- Green: Recently submitted

**Recent Activity:**
- Last 3 timesheet approvals
- Last 3 payments to providers
- Account updates

**Notifications:**
- New timesheet ready
- Payment processed
- Account alerts

---

## 2. TIMESHEET REVIEW {#timesheet-review}

### Provider Selection (if multiple providers)

**Access:** Recipient Home → Review Timesheets

**Table:**

| Provider | Provider # | Pay Period | Hours | Status | Action |
|----------|------------|------------|-------|--------|--------|
| Smith, J | 123456789 | 01/01-01/15 | 80:00 | Submitted | Review |
| Jones, M | 987654321 | 01/01-01/15 | 40:00 | Submitted | Review |

**Status:**
- Submitted (ready for review)
- Approved (you approved)
- Rejected (you rejected)
- Paid (payment processed)

**Filters:**
- Pending Review (default)
- All Timesheets
- By provider
- By pay period

**Single Provider:** Bypass to timesheet review

---

### Timesheet Review Screen

**Access:** Provider Selection → Review

**Header:**

| Field | Value |
|-------|-------|
| Provider Name | Full name, provider # |
| Recipient Name | Your name, case # |
| Pay Period | Dates |
| Authorized Hours | Weekly/Monthly |
| Submitted Date | When submitted |
| Status | Current status |

**Time Entries Table:**

| Date | Day | Service Type | Hours | Min | Total | Check-In | Check-Out |
|------|-----|--------------|-------|-----|-------|----------|-----------|
| 01/01 | Mon | Domestic | 04 | 30 | 4:30 | 9:00 AM | 1:30 PM |
| 01/02 | Tue | Domestic | 05 | 00 | 5:00 | 9:00 AM | 2:00 PM |
| 01/03 | Wed | Paramedical | 01 | 00 | 1:00 | 3:00 PM | 4:00 PM |

**Service Type Color Coding:**
- Different colors for easy scanning
- Legend displayed

**EVV Data (if applicable):**
- GPS coordinates
- Address verified
- Distance from home
- Map icon to view location

---

### Timesheet Summary

| Category | Provider Entered | Authorized | Difference |
|----------|------------------|------------|------------|
| Total Hours | 80:00 | 80:00 | 0:00 ✓ |
| Domestic | 60:00 | 60:00 | 0:00 ✓ |
| Paramedical | 15:00 | 15:00 | 0:00 ✓ |
| Accompaniment | 5:00 | 5:00 | 0:00 ✓ |

**Status Indicators:**
- ✓ Green: Matches authorized
- ⚠️ Yellow: Slightly over
- ❌ Red: Significantly over/under

**Overtime Info:**
- Regular Hours: XX:XX
- Overtime Hours: XX:XX
- Total Compensation: $X,XXX (estimated)

---

### Review Instructions

**What to Check:**

1. **Dates and Times**
   - Were these actual dates provider worked?
   - Do hours match when provider was at home?
   - Any dates you were not home or provider didn't come?

2. **Service Types**
   - Did provider perform services listed?
   - Are service types correct?

3. **Hours**
   - Do total hours seem accurate?
   - Compare to your own records
   - Check for unusual patterns

4. **Location (EVV)**
   - Verify check-in/out locations correct
   - Provider should check in at your home

**If Correct:** Approve Timesheet

**If Wrong:** Reject Timesheet with reason

---

### Recipient Options

**Buttons:**

| Button | Function |
|--------|----------|
| Approve Timesheet | Approve and send to payroll |
| Reject Timesheet | Reject with reason, return to provider |
| View Details | Expanded view |
| Print Timesheet | Print for records |
| Email Copy | Send to email |
| Contact Provider | Send message |
| Need Help? | Help desk/county |

**Status Tracking:**
- Time remaining to review: [X] days
- Auto-approval if not reviewed: [Date] (if applicable)
- Last reviewed by: [Date/Time] (if multiple signatories)

---

## 3. TIMESHEET APPROVAL/REJECTION {#approval-rejection}

### Approve Timesheet – Electronic Signature

**Access:** Timesheet Review → Approve Timesheet

**Summary:**
- Provider: [Name] (#[Number])
- Pay Period: [Dates]
- Total Hours: XX:XX
- Estimated Payment: $X,XXX

**Service Breakdown:**

| Service Type | Hours | Rate | Amount |
|--------------|-------|------|--------|
| Domestic | 60:00 | $18.00 | $1,080 |
| Paramedical | 15:00 | $18.00 | $270 |
| Accompaniment | 5:00 | $18.00 | $90 |
| **Total** | **80:00** | | **$1,440** |

**Certification:**
```
I certify under penalty of perjury that:

1. [Provider Name] worked the hours shown
2. Services were actually provided to me
3. Dates and times are accurate to best of my knowledge
4. Service types match services provided
5. I am approving this timesheet for payment
6. False certification may result in benefit termination
7. Electronic submission constitutes my legal signature

I approve this timesheet for payment.
```

**Fields:**

| Field | Type | Required |
|-------|------|----------|
| Full Name | Text | Yes (match recipient or signatory) |
| Relationship | Dropdown | Yes (if signatory) |
| Date | Date | Auto (read-only) |
| Electronic Signature | Text | Yes (type name) |

**Relationship Options (if authorized signatory):**
- Parent/Guardian
- Spouse
- Authorized Representative
- POA (Power of Attorney)
- Conservator
- Other

**Checkbox:** [ ] I certify information is true and accurate

**Buttons:** Approve and Submit, Back, Cancel

---

### Approval Confirmation

**Success:** "You have successfully approved this timesheet!"

**Details:**
- Provider: [Name]
- Pay Period: [Dates]
- Hours Approved: XX:XX
- Estimated Payment: $X,XXX
- Approval Date: [Date/Time]
- Approved By: [Your name]

**Next Steps:**

1. **Timesheet Sent to Payroll**
2. **Payment Timeline:**
   - Direct Deposit: 2-3 business days after pay date
   - Paper Warrant: 5-7 business days after pay date
3. **Provider Notification:** Email confirmation sent
4. **Payment Tracking:** View in Payment History

**Buttons:**
- View Timesheet
- Review Another Timesheet
- Return to Home
- Print Confirmation

**Email:** Confirmation sent

---

### Reject Timesheet

**Access:** Timesheet Review → Reject Timesheet

**Timesheet Being Rejected:**
- Provider: [Name]
- Pay Period: [Dates]
- Hours: XX:XX

**Warning:**
```
You are about to reject this timesheet. The provider will need 
to correct and resubmit. Please provide clear explanation so 
provider can make necessary corrections.
```

**Select Primary Reason:**
- ○ Hours do not match services provided
- ○ Service dates are incorrect
- ○ Service types are incorrect
- ○ Provider was not present on dates listed
- ○ Hours claimed exceed actual time worked
- ○ Provider did not provide some services claimed
- ○ I do not remember provider working these hours
- ○ Check-in/check-out locations are incorrect
- ○ Other (please explain below)

**Additional Comments (required):**
- Text area: Provide specific details
- Example: "Provider did not come on 01/05 and 01/12. Hours on 01/08 should be 4 hours, not 8."
- Character limit: 1,000

**Helpful Tips:**
```
Be specific:
✓ Mention specific dates that are wrong
✓ State correct hours if you remember
✓ Explain which services were/weren't provided
✗ Don't just say "wrong"
✗ Don't reject without discussing first if possible
```

**Contact Provider First:**
- Consider contacting provider to discuss
- Many issues resolved with phone call
- Provider may have made simple mistake

**Buttons:**
- Reject Timesheet
- Contact Provider First (link)
- Cancel

---

### Rejection Confirmation

**Timesheet Rejected**

**Details:**
- Provider: [Name]
- Pay Period: [Dates]
- Rejected By: [Your name]
- Rejection Date: [Date/Time]
- Reason: [Selected reason]
- Comments: [Your comments]

**What Happens Next:**

1. **Provider Notified:** Email/text about rejection
2. **Provider Reviews:** Reviews your comments
3. **Provider Corrects:** Makes necessary corrections
4. **Resubmission:** Resubmits corrected timesheet
5. **Your Review:** You review and approve again
6. **Communication:** Provider may contact you

**If You Need Help:**
- Social Worker: [Name] at [Phone]
- IHSS Service Desk: 1-866-376-7066
- County can help resolve disputes

**Buttons:**
- View Rejected Timesheet
- Return to Timesheet List
- Return to Home
- Print Confirmation

**Email:** Confirmation sent

---

### Timesheet History (Recipient View)

**Access:** Recipient Home → Timesheet History

**Table:**

| Pay Period | Provider | Hours | Submitted | Status | Action |
|------------|----------|-------|-----------|--------|--------|
| 01/01-01/15 | Smith, J | 80:00 | 01/16 | Submitted | Review |
| 12/16-12/31 | Smith, J | 75:30 | 12/31 | Approved | View |
| 12/16-12/31 | Jones, M | 40:00 | 12/30 | Approved | View |
| 12/01-12/15 | Smith, J | 78:00 | 12/15 | Paid | View |

**Status Definitions:**

| Status | Meaning | Action |
|--------|---------|--------|
| Submitted | Ready for review | Review and approve/reject |
| Approved | You approved | Wait for payment |
| Paid | Payment issued | View payment details |
| Rejected | You rejected | Wait for resubmission |
| Cancelled | County cancelled | Contact county |

**Filters:**
- Pending Review / All / Approved / Rejected / Paid
- By provider
- By pay period
- By date range

**Sort:**
- By pay period (newest first)
- By provider
- By status
- By submission date

---

## 4. HIRE PROVIDER {#hire-provider}

### Hire Provider Overview

**Purpose:** Search for and hire providers directly through ESP

**Eligibility:**
- Recipient age 18 or older
- Case in Eligible/Presumptive Eligible status
- Accessed from Recipient Menu

**Restrictions:**
- Cannot hire if case terminated/on leave
- Cannot hire if at maximum providers
- Some providers ineligible due to:
  - CORI without proper waivers
  - Not eligible status
  - Already assigned to case

**Help Desk:** Can assist with search but cannot complete hire

---

### Step 1: Search for Provider

**Access:** Recipient Home → Hire Provider

**Search By:**
- ○ Provider Name
- ○ Provider Number
- ○ SSN (last 4 digits)

**If Provider Name:**

| Field | Type | Required |
|-------|------|----------|
| Last Name | Text | Yes |
| First Name | Text | No |
| Use Soundex | Checkbox | No |

**If Provider Number:**
- Provider Number (9 digits)

**If SSN:**
- Last 4 of SSN (4 digits)

**Filters (optional):**
- County (defaults to yours)
- Gender
- Language

**Buttons:** Search, Reset, Cancel

---

### Step 2: Search Results

**Found [X] Provider(s)**

**Table:**

| Provider Name | Provider # | Gender | County | Languages | Status | Action |
|---------------|------------|--------|--------|-----------|--------|--------|
| Smith, John | 123456789 | M | LA | English, Spanish | Available | Select |
| Jones, Mary | 987654321 | F | LA | English | Available | Select |
| Davis, Bob | 456789123 | M | LA | English | Already Hired | N/A |

**Status:**
- **Available:** Can be hired
- **Already Hired:** Already on your case
- **Not Eligible:** Cannot be hired (see details)
- **Restricted:** Has CORI restrictions

**Status Details:** Click to see why cannot hire

**Sort:** By name, county, availability

**Pagination:** 10/25/50 per page

**Buttons:**
- Select Provider
- New Search
- Cancel

**No Results:**
```
No providers found matching criteria.

Suggestions:
- Try different spelling
- Use Soundex for similar names
- Expand county search
- Contact county for recommendations
```

---

### Step 3: Confirm Provider

**Access:** Search Results → Select

**Provider Information:**

| Field | Value |
|-------|-------|
| Provider Name | Full name |
| Provider Number | 9 digits |
| Gender | M/F |
| County | County name |
| Languages | List |

**Eligibility Status:**

| Field | Value |
|-------|-------|
| Eligible to Work | Yes |
| Background Check | Yes |
| Orientation | Yes |
| SOC 846 | Yes |

**Current Assignments:**
- Active Cases: [X]
- Available: Yes

**Important Information:**
```
Before hiring this provider:

1. Interview or speak with provider
2. Verify available for your schedule
3. Discuss services needed and capabilities
4. Confirm comfortable with your needs
5. Check references if desired

Your Social Worker can help you:
- Interview providers
- Verify qualifications
- Ensure appropriate for needs
```

**Contact Provider:** Link if ESP account

**Buttons:**
- Continue to Hire
- Back to Search
- Cancel

---

### Step 4: Confirm Hire

**Access:** Confirm Provider → Continue

**You are hiring:**
- Provider: [Name] (#[Number])
- Your Case: [Name] (#[Case #])

**Your Case Information:**

| Field | Value |
|-------|-------|
| Case Name | Your name |
| Case Number | 7 digits |
| County | County |
| Case Status | Eligible |
| Authorized Services | List |
| Monthly Hours | XX:XX |

**What Happens:**

1. **Provider Assignment:** Immediate
2. **County Notification:** Social Worker notified
3. **Provider Setup:** County will:
   - Set pay rate
   - Assign hours
   - Generate timesheets
   - Set up E-Timesheet
4. **Timeline:**
   - Effective immediately
   - Can begin working right away
   - First timesheet next pay period
5. **Next Steps:**
   - Coordinate schedule
   - Discuss care needs
   - Provider receives notification

**Important Notes:**
```
⚠️ Please Note:

- You direct the provider's work
- Discuss your care needs
- Provider must complete timesheets
- You must review and approve timesheets
- Contact Social Worker with questions

If provider not suitable:
- Contact county to remove
- Can hire different provider
```

**Certification:**
```
I certify that:
1. I am age 18 or older
2. I want to hire this provider
3. Provider will be assigned immediately
4. I will direct the provider's work
5. I will review and approve timesheets
6. Electronic submission constitutes my legal signature
```

**Fields:**
- Full Name (match recipient)
- Date (auto)
- Electronic Signature

**Checkbox:** [ ] I agree and want to hire

**Buttons:**
- Complete Hire
- Back
- Cancel

---

### Hire Confirmation

**Success! Provider Hired**

**Details:**
- Provider: [Name] (#[Number])
- Your Case: [Name] (#[Case #])
- Hire Date: [Date/Time]
- Confirmation: HPXXXXXXXXXXXX

**What Happens Next:**

1. **County Processing:**
   - Social Worker notified
   - Setup within 1-2 business days
   - Provider contacted

2. **Provider Notification:**
   - Email sent
   - Instructions given

3. **Your Next Steps:**
   - Contact provider for schedule
   - Discuss care needs
   - Agree on work schedule

4. **Timesheet Setup:**
   - Generated for next pay period
   - Provider submits electronically
   - You review and approve

5. **Questions:**
   - Social Worker: [Name] at [Phone]
   - IHSS Service Desk: 1-866-376-7066

**Buttons:**
- View My Providers
- Hire Another Provider
- Return to Home
- Print Confirmation

**Emails:**
- To You: Confirmation
- To Provider: Assignment notification
- To County: Processing alert

---

### Hire Provider Restrictions

**If Under 18:**
```
You must be 18 or older to hire provider through ESP.

Contact your Social Worker for assistance.
```

**If Case Not Eligible:**
```
Your case is not currently in eligible status.

Cannot hire providers at this time. Contact Social Worker.
```

**If At Maximum:**
```
You have reached maximum providers for your case.

To hire new provider, must first terminate existing provider.
Contact Social Worker.
```

**If Provider Restricted:**
```
This provider cannot be hired due to:
- [Reason, e.g., "Tier 1 CORI conviction"]

Search for different provider or contact Social Worker.
```

---

## 5. PAYMENT HISTORY {#recipient-payments}

### Payment History (Recipient View)

**Access:** Recipient Home → Payment History

**Date Range:** Last 6 months

**Table:**

| Payment Date | Provider | Pay Period | Hours | Gross Amount | Action |
|--------------|----------|------------|-------|--------------|--------|
| 01/25 | Smith, J | 01/01-01/15 | 80:00 | $1,440 | View |
| 01/25 | Jones, M | 01/01-01/15 | 40:00 | $720 | View |
| 01/10 | Smith, J | 12/16-12/31 | 75:30 | $1,359 | View |

**Summary:**
- Total Paid (6 months): $XX,XXX
- Total Hours: XXX:XX
- Payments: XX
- Providers: X

**Filters:**
- By date range
- By provider
- By pay period

**Sort:**
- By payment date (newest)
- By provider
- By amount
- By hours

---

### Payment Details (Recipient View)

**Access:** Payment History → View

**Payment Information:**

| Field | Value |
|-------|-------|
| Provider | Name, provider # |
| Recipient | Your name, case # |
| Pay Period | Dates |
| Payment Date | Date issued |
| Payment Method | Direct Deposit / Warrant |

**Hours Breakdown:**

| Service Type | Hours | Rate | Amount |
|--------------|-------|------|--------|
| Domestic | 60:00 | $18.00 | $1,080 |
| Paramedical | 15:00 | $18.00 | $270 |
| Accompaniment | 5:00 | $18.00 | $90 |
| **Total** | **80:00** | | **$1,440** |

**Note:** Recipient view shows gross only (not deductions)

**Timesheet Reference:**
- Approved By: Your name
- Approved Date: Date/Time
- Method: ESP / TTS

**Buttons:**
- View Timesheet
- Print Payment Details
- Back

---

## PART 4: SHARED FUNCTIONALITY

### TABLE OF CONTENTS - PART 4
1. [Login and Password](#login-password)
2. [Account Information](#account-info)
3. [My Preferences](#preferences)
4. [Message Center](#message-center)
5. [Help and Support](#help)

---

## 1. LOGIN AND PASSWORD {#login-password}

### Login Screen

**URL:** https://www.etimesheets.ihss.ca.gov

**Fields:**
- Username
- Password (masked)

**Buttons:**
- Sign In
- Show Password (toggle)

**Links:**
- Register as New User
- Forgot Username or Password?
- Need Help?

**Remember Me:** [ ] Remember username (not password)

**Limits:**
- Max 5 failed attempts
- Account locked 30 minutes after 5 failures
- Session expires after 15 minutes inactivity
- Warning at 13 minutes

**Security:**
- HTTPS encrypted
- Session timeout
- Account lockout

---

### Forgot Username or Password

**Access:** Login → "Forgot Username or Password?"

**Options:**
- ○ I forgot my username
- ○ I forgot my password
- ○ Both

---

### Get Username

**If forgot username:**

**Fields:** Email Address

**Process:**
1. Enter email
2. System searches account
3. Sends email with username
4. Email arrives within minutes

**Email Content:**
```
Your ESP username: [username]

Login at: https://www.etimesheets.ihss.ca.gov

If didn't request, disregard or contact service desk.

Questions? Call 1-866-376-7066
```

---

### Password Reset (3 Steps)

**Step 1: Email and Security Question**

1. Enter email
2. Answer one security question (system selects)
3. Verify answer

**Validation:**
- Email must match registered
- Answer must match (case-insensitive)
- 3 failed attempts locks account 30 minutes

---

**Step 2: Verification Code**

1. System sends 6-digit code to email
2. Enter code
3. Code expires in 15 minutes
4. Can resend (max 3 times/hour)

---

**Step 3: Create New Password**

**Fields:**
- New Password
- Confirm Password

**Requirements:**
- Min 8 characters, max 50
- At least one uppercase
- At least one lowercase
- At least one number
- At least one special (!@#$%^&*)
- Cannot contain username
- Cannot match last 5 passwords

**Strength Indicator:** Weak/Medium/Strong

**Success:** "Password reset successfully!"

**Email:** Confirmation sent

---

### Change Password (from within ESP)

**Access:** Header Menu → Change Password

**Fields:**
- Current Password
- New Password
- Confirm Password

**Validation:**
- Current must be correct
- New must meet requirements
- Cannot reuse last 5

**Success:** "Password successfully changed"

**Email:** Confirmation sent

---

### Password Expiration

**Expires:** 180 days (6 months) after last change

**Warning:** Starting 14 days before:
```
⚠️ Password Expiring Soon

Your password will expire in [X] days.

Change password soon to avoid lockout.

[Change Now] [Remind Later]
```

**On Expiration:**
```
⚠️ Password Expired

Must create new password to continue using ESP.

[Change Password Now]
```

**Forced Change:**
- Redirected to Change Password
- Cannot access until password changed
- Can cancel and log out

---

### Update Security Questions

**Access:** Header Menu → Update Security Questions

**Current Questions:** Listed (answers not shown)

**New Questions:** For each of 3:
- Security Question (dropdown)
- Answer (text, min 3 chars)

**Rules:**
- Must select 3 different
- Cannot reuse current questions
- All answers required

**Success:** "Security questions updated"

---

### Session Timeout

**Inactivity:** 15 minutes

**Warning at 13 minutes:**
```
⚠️ Session Timeout Warning

Will be logged out in 2 minutes due to inactivity.

Stay logged in?

[Stay Logged In] [Log Out Now]

Time Remaining: 1:59
```

**If "Stay":** Session extended 15 minutes

**If timeout:** Logged out, redirected to login

**Activity Extends Session:**
- Clicking buttons
- Entering text
- Navigating pages
- Submitting forms

---

## 2. ACCOUNT INFORMATION {#account-info}

### Account Information Screen

**Access:** Header Menu → Account Information

**Personal Information (Read-Only):**
- Name
- Provider # / Case #
- Date of Birth
- County

**Note:** Cannot change in ESP. Contact county.

**Contact Information (Editable):**
- Email Address [Change Email]
- Primary Phone [Update Phone Numbers]
- Secondary Phone
- Mailing Address [Update Address]

**Account Security (Editable):**
- Username (cannot change)
- Password [Change Password]
- Security Questions [Update Questions]

**Communication Preferences:** See My Preferences

---

### Change Email

**Access:** Account Info → Change Email

**Current:** [email@example.com]

**Fields:**
- New Email
- Confirm Email
- Current Password (for security)

**Process:**
1. Enter new email (twice)
2. Enter current password
3. System sends verification code to new email
4. Enter code
5. Email updated

**Verification:** 6-digit code, 15-min expiration

**Emails:**
- To Old: "Email changed"
- To New: "Verification code"
- To Both: "Change confirmed"

---

### Update Phone Numbers

**Fields:**
- Primary Phone (required)
- Phone Type (Mobile/Home/Work)
- Can Receive Texts (mobile only)
- Secondary Phone (optional)
- Secondary Type

**Text Preference:**
- Only for mobile numbers
- Used for text notifications

**Used For:**
- Password reset
- Timesheet notifications (TTS)
- Account alerts
- Text notifications (if enabled)

---

### Update Address

**Fields:**
- Street Address (required)
- Apartment/Unit
- City (required)
- State (CA default)
- ZIP (5 or 9 digits)

**Address Validation:**
- Validates against USPS
- Suggests corrections
- Can override if confident

**If Doesn't Match USPS:**
```
⚠️ Address Verification

You Entered:          USPS Suggests:
123 Main Street       123 Main St
Los Angeles, CA       Los Angeles, CA 90001

Which address?
○ Use suggested (recommended)
○ Use address I entered

[Continue]
```

**Used For:**
- Mailing paper forms
- Warrant delivery (no direct deposit)
- Official correspondence

---

## 3. MY PREFERENCES {#preferences}

### My Preferences – Provider

**Access:** Header Menu → My Preferences

**Notification Preferences:**

**Email Notifications:** (checkboxes)
- Timesheet submitted (by me)
- Timesheet approved
- Timesheet rejected
- Payment processed
- Direct deposit completed
- Sick leave claim status
- Career Pathways claim status
- Account updates
- System announcements

**Text Notifications:** (if mobile)
- Timesheet approved
- Timesheet rejected
- Payment processed
- Urgent account alerts

**Text Opt-In:**
```
By checking, you consent to receive text messages from CDSS.
Message and data rates may apply. Opt out anytime.
```

**Language Preferences:**
- Display Language (website)
- Correspondence Language (emails/forms)

**Timesheet Preferences:**
- Default View: Calendar / List
- Time Entry Format: 12-hour / 24-hour
- Email Copy of Timesheets

**Display Preferences:**
- Dashboard Layout: Card / Compact
- Show Tips and Tutorials
- Date Format: MM/DD/YYYY / DD/MM/YYYY

**Privacy Preferences:**
- Show recent activity
- Remember recipient selection

**Session Settings:**
- Log out after: 15 / 30 / 60 minutes inactivity

**Warning:** Longer sessions less secure on shared devices

**Buttons:** Save Preferences, Reset to Defaults, Cancel

---

### My Preferences – Recipient

**Similar to Provider with differences:**

**Timesheet Communication:**
How to be notified when timesheets ready?
- ○ Email notification
- ○ Phone call (automated)
- ○ Both email and phone

**Note for TTS:**
```
Preference applies to both ESP and TTS.

If phone calls, you'll get automated call when timesheets ready.
```

**Timesheet Review:**
- Default View: Detailed / Summary
- History: Last 3/6/12 months
- Auto-Approve: [ ] Do NOT auto-approve (recommended)

**Warning:**
```
⚠️ Auto-Approve Not Recommended

Strongly recommend manual review of each timesheet.

Only use if unable to review and trust provider completely.
```

---

## 4. MESSAGE CENTER {#message-center}

### Message Center

**Access:** Header Menu → Messages (icon with unread count badge)

**Inbox Table:**

| Date | Subject | Category | Status | Action |
|------|---------|----------|--------|--------|
| 01/15 | System Maintenance | Announcement | Unread | View |
| 01/10 | New Feature: Career Pathways | Update | Read | View |
| 01/05 | Direct Deposit Confirmation | Notification | Read | View |

**Categories:**

| Category | Description | Icon |
|----------|-------------|------|
| Announcement | Important system announcements | 📢 |
| Update | New features/changes | 🆕 |
| Notification | Account activity | 🔔 |
| Alert | Urgent attention | ⚠️ |
| Reminder | Due dates/deadlines | ⏰ |

**Status:**
- Unread (bold, badge)
- Read (normal)

**Filters:**
- All Messages
- Unread Only
- By Category
- By Date Range

**Sort:**
- By date (newest first)
- By category
- By status (unread first)

**Actions:**
- Mark as Read/Unread
- Delete
- Archive (keeps 90 days)

---

### View Message

**Header:**
- From: IHSS ESP
- Date: Date/Time
- Category: Icon and name
- Subject: Subject line

**Body:** Full message content (HTML formatted)

**Attachments:** (if any) Download links

**Related Links:** (if any)

**Actions:**
- Mark as Unread
- Delete
- Archive
- Print
- Close

---

### Common Message Types

**System Maintenance:**
```
IHSS ESP Scheduled Maintenance

ESP will be unavailable for maintenance:

Date: [Date]
Time: [Start] - [End]
Duration: Approximately [X] hours

During this time:
- Cannot access ESP
- Timesheet submissions unavailable
- Plan accordingly

For urgent issues, contact county or service desk.
```

**Password Expiration:**
```
⚠️ Password Expiring in [X] Days

Your password expires on [Date].

Change password to avoid lockout.

[Change Password Now]

Password Requirements:
- Minimum 8 characters
- At least one uppercase
- At least one lowercase
- At least one number
- At least one special character

Questions? Call 1-866-376-7066
```

**Payment Processed:**
```
Payment Processed

Your payment for [Dates] has been processed.

Provider: [Name]
Recipient: [Name]
Hours: [XX:XX]
Amount: $[X,XXX]
Method: [Direct Deposit / Warrant]
Date: [Date]

View Payment Details: [Link]

Questions? Contact county or view Payment History.
```

**New Feature:**
```
🆕 New Feature Available

Career Pathways claims now available in ESP!

What's New:
- Submit training time claims online
- Submit incentive claims electronically
- Track your training progress
- View training report

How to Access:
1. Log in to ESP
2. Select "Career Pathways" from menu
3. Follow instructions

Training Videos: [Link]

Questions? Call 1-866-376-7066
```

---

### Message Center Settings

**Access:** Message Center → Settings icon

**Email Notifications:**
- [ ] Email me when I receive new message
- [ ] Daily digest of all messages

**Message Retention:**
- Keep messages for: 30/60/90/180 days
- Auto-delete after: Never/90/180/365 days

**Archive Settings:**
- Move read messages to archive after: Never/7/30/60 days

---

## 5. HELP AND SUPPORT {#help}

### Help Resources

**Access:** Menu → Help / Training / Contact Us

**Video Tutorials:**
- Getting Started with ESP
- Submitting First Timesheet
- Reviewing Provider Timesheets (Recipients)
- Using Direct Deposit
- Submitting Sick Leave Claims
- Career Pathways Claims
- Mobile Check-In/Check-Out

**User Guides:**
- Provider User Guide (PDF)
- Recipient User Guide (PDF)
- Quick Start Guide
- FAQs

**Training:**
- Upcoming webinars
- County training sessions
- Online classes

---

### Contact Options

**IHSS Service Desk:**
- Phone: 1-866-376-7066
- Hours: Mon-Fri, 7 AM - 5 PM PST
- Email: [Email address]
- Chat: During business hours

**Your County:**
- County: [Your county]
- Social Worker: [Name]
- Phone: [Phone]
- Email: [Email]
- Hours: [Office hours]

**Technical Support:**
- For technical issues with ESP
- Submit help desk ticket online
- Include screenshots
- Response: 1-2 business days

---

### Submit Help Desk Request

**Access:** Contact → Submit Help Desk Request

**Fields:**

| Field | Type | Required |
|-------|------|----------|
| Your Name | Text | Auto-filled |
| Email | Email | Auto-filled |
| Phone | Phone | Auto-filled |
| Issue Category | Dropdown | Yes |
| Issue Description | Text Area | Yes (min 50 chars) |
| Screenshots | File Upload | No (max 3, 5MB each) |
| Priority | Radio | Yes |

**Issue Categories:**
- Cannot log in
- Forgot username/password
- Timesheet not displaying
- Payment issue
- Direct deposit problem
- Sick leave claim issue
- Career Pathways issue
- Error message
- Other technical issue

**Priority:**
- ○ Low (general question)
- ○ Normal (need help soon)
- ○ High (cannot complete important task)
- ○ Urgent (critical, cannot work)

**Buttons:** Submit Request, Cancel

**Confirmation:**
```
Help Desk Request Submitted

Ticket Number: HD-XXXXXXXXXXXX

We will respond via email within 1-2 business days.

For urgent issues, call:
IHSS Service Desk: 1-866-376-7066
```

---

**End of Parts 3 & 4**

**Complete ESP Documentation Series:**
- Part 1: Overview, Business Processes, Registration
- Part 2: Provider Functionality
- Part 3: Recipient Functionality  
- Part 4: Shared Features
