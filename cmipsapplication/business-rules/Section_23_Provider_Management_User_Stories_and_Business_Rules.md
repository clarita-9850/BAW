# IHSS CMIPS - Provider Management User Stories and Business Rules
## Release 2025.03.01 - DSD Section 23

---

## EXECUTIVE SUMMARY

**Section:** DSD Section 23 - Provider Management  
**Total Business Rules:** 190+ (BR PVM 01 through BR PVM 190+)  
**Implementation Status:** Majority IMPLEMENTED  
**Key Features:** Provider enrollment, assignment, overtime violation tracking, workweek agreements, travel time, sick leave, CORI management

---

## TABLE OF CONTENTS
1. [User Stories / Functional Requirements](#user-stories)
2. [Key Business Rules Overview](#business-rules-overview)
3. [Critical Business Rules (1-50)](#business-rules-1-50)
4. [Business Rules Summary (51-190+)](#business-rules-summary)

---

## USER STORIES / FUNCTIONAL REQUIREMENTS {#user-stories}

### 1. Provider Management and Enrollment

**As a** County case worker  
**I want to** register and manage individual providers (IP) in the IHSS program  
**So that** providers can be assigned to recipient cases and receive payment for services

**Acceptance Criteria:**

#### **Provider Registration Requirements:**
- Complete SOC 426 Provider Enrollment Form
- Complete Provider Orientation
- Sign Provider Agreement (SOC 846)
  - After FLSA implementation: Must sign both Provider Agreement and Overtime Agreement
- Complete criminal background check through Department of Justice (DOJ)
- Document Criminal Offender Record Information (CORI) if applicable

#### **Provider Eligibility Determination:**
**Tier 1 Crimes (Automatic Ineligibility):**
- Provider convicted or incarcerated for Tier 1 crime: NOT eligible to serve as IHSS Provider

**Tier 2 Crimes (Conditional Eligibility):**
- Provider may still serve with:
  - General Exception Waiver from CDSS Caregiver Background Check Bureau (CBCB), OR
  - Recipient Waiver for the specified crime

#### **Provider Data Tracking:**
- Social Security Number (SSN) verification through weekly batch process with Social Security Administration (SSA)
- Provider eligibility verified against Medi-Cal suspended and ineligible providers table
- Original hire date tracked based on initial date of eligibility
- Provider enrollment status: Pending, Yes (Eligible), No (Ineligible), Pending Reinstatement

#### **Provider Status Management:**
- Active: Currently serving recipients
- On Leave: Temporarily not serving
- Terminated: No longer able to serve
- Ineligibility Reasons include:
  - Suspended or Ineligible
  - Tier 1 Conviction
  - Tier 2 Conviction
  - Subsequent Tier 1/2 Conviction
  - Duplicate SSN / Suspect SSN
  - Death
  - Inactive/No Payroll for 1 Year
  - Third/Fourth Overtime Violation
  - SOC 846 Not Completed
  - Provider Enrollment Ineligible

---

### 2. Provider Assignment to Recipient Cases

**As a** County case worker  
**I want to** assign providers to recipient cases  
**So that** recipients can receive authorized IHSS services

**Acceptance Criteria:**

#### **Assignment Process:**
- Provider cannot be assigned until all enrollment requirements completed
- Track From/To dates of service period
- Select provider pay rate based on county rates available for time period
- Assign hours to provider for specific case (upon recipient request and receipt of signed Assigned Hours form)
- Provider assigned hours may not exceed recipient authorized hours for time period

#### **Provider Hour Types:**
- **Regular IHSS Hours:** Standard in-home supportive services
- **WPCS Hours:** Waiver Personal Care Services
- **Assigned Hours:** Specific hours allocated to provider (limits remaining hours on timesheet)
- **Back-up Provider Hours:** Tracked against recipient's remaining back-up hours for fiscal year

#### **Automatic Provider Hours Updates:**
- System automatically builds provider hours based on Recipient IP Mode of Service hours
- When no assigned hours indicated: Copy changes to recipient authorized hours to provider hours
- Automatically build new provider hours segments when pay rate changes in County Pay Rate table

#### **Funding Source Updates:**
- System automatically updates recipient funding source based on provider relationship:
  - Spouse of recipient → IPO (IHSS Plus Option) eligible
  - Parent of minor child (under 18) → IPO eligible
  - System re-determines funding source when provider relationship changes

#### **Provider Notification (SOC 2271):**
System-generated when:
- IHSS provider assigned to recipient case for first time
- Provider previously terminated or on leave returns to serve case
- Case Auth to Purchase after Adjusted Hours changes
- Service Types change

---

### 3. Provider Overtime Violation Tracking and Outcomes

**As a** County supervisor and CDSS staff  
**I want to** track and manage provider overtime violations  
**So that** providers comply with workweek limits and receive appropriate consequences

**Background:**
With FLSA implementation, providers are subject to weekly hour limitations. Violations trigger a multi-level review process that can result in provider suspension.

**Acceptance Criteria:**

#### **Overtime Violation Types:**

**1. Exceeds Weekly Maximum:**
- **One-to-One (Single Recipient):** Provider with only one active recipient exceeds recipient weekly authorized hours by more than allowable threshold
- **One-to-Many (Multiple Recipients):** Provider with multiple active recipients exceeds 66:00 per week by more than allowable threshold
- **WPCS One-to-One:** Provider with WPCS hours or combined IHSS/WPCS for one recipient exceeds 70:45 per week by more than allowable threshold

**2. Exceeds Travel Maximum:**
- More than 7:00 hours of travel claimed in a workweek
- Includes travel across all recipients for both IHSS and WPCS programs

**Exemption:** Career Pathways training time hours are exempt from overtime violation processing

#### **One Violation Per Service Month Rule:**
- Provider may incur only ONE violation per service month
- "Next Possible Violation Date" field controls this:
  - If final process completed in same month as violation: Next Possible Violation Date = 1st day of next calendar month
  - If final process completed in month after violation: Next Possible Violation Date = 1st day of next pay period (1st or 16th)

#### **Violation Statuses:**
- **Pending Review:** Initial violation triggered, awaiting county review
- **Active:** Violation upheld, counts toward provider violation total
- **Inactive:** Violation overridden/rescinded
- **Inactive – No Violations for one year:** Provider violation count reset after 1 year clean record
- **Inactive – Provider One Year Termination:** Provider terminated due to 4th violation
- **Inactive – Exemption:** Provider has overtime exemption for service month

#### **Review Process Hierarchy:**

**Level 1: County Review (3 business days)**
- Task triggered to County Overtime Violation Work Queue
- County worker reviews violation
- Outcomes:
  - **Upheld (Pending Uphold):** Violation stands → becomes Active, letters issued
  - **Override (Pending Override):** Request supervisor review

**Level 2: Supervisor Review (2 business days)**
- Task triggered to Supervisor Overtime Violation Work Queue
- Supervisor reviews county worker override request
- Outcomes:
  - **Approved:** Override accepted → Violation becomes Inactive
  - **Rejected:** Override denied → Violation becomes Active, letters issued

**Level 3: County Dispute (10 business days total)**
- Provider has 10 calendar days to request county review after notification
- County Dispute Filed Date must be within 10 business days of County Review Letter Date
- Same outcomes as County Review (Upheld or Override → Supervisor Review)
- Tasks triggered at:
  - Day 6: Alert to county worker and supervisor (4 days remaining)
  - Day 8: Alert to supervisor only (2 days remaining)

**Level 4: CDSS State Administrative Review (Violations 3 & 4 only)**
- Only available for Overtime Violation #3 or #4 (result in provider suspension)
- Outcomes:
  - **Upheld:** Provider must wait suspension period before reinstatement/re-enrollment
  - **Override:** Provider can be reinstated immediately

#### **Violation Consequences:**

**Violation #1:** Active violation on record

**Violation #2 (First Time):**
- **Optional Training:** Provider may review training materials and self-certify completion
- **Timeframe:** 14 calendar days from violation notification (extended to 21 days to accommodate extension requests)
- **County Entry:** Additional 5 calendar days for county to enter training completion
- **If Training Completed:** Second violation removed (Inactive status)
- **If Training Not Completed:** Second violation stands
- **If Dispute Filed:** 14-day training period suspended until dispute outcome
  - If dispute upheld: 14-day period resets after dispute
  - If dispute overridden: Violation removed, no training needed
- **Important:** Training option only available ONCE. If another violation occurs after training completion, it's treated as a new second violation but training is NOT available again.

**Violation #3:**
- **Consequence:** Provider terminated for 90 calendar days
- **Termination Timing:** 20 calendar days after upheld action processed
- **Reinstatement:** Automatic after 90 days (batch job 600QINDN restores eligibility)
- **No Re-enrollment Required:** Provider can resume serving without going through enrollment process again

**Violation #4:**
- **Consequence:** Provider terminated for 365 calendar days  
- **Termination Timing:** 20 calendar days after upheld action processed
- **Re-enrollment Required:** Provider must complete entire provider enrollment process to become IHSS or WPCS provider again

#### **Work Queues and Tasks:**

**County Overtime Violation Work Queue:**
- Review Overtime Violation for Provider [Name] [Number]
- County Dispute Overtime Violation for Provider [Name] [Number]
- County Dispute Resolution due in 2/4 business days

**Supervisor Overtime Violation Work Queue:**
- Supervisor Review for Provider Overtime Violation
- Dispute Supervisor Review for Provider Overtime Violation
- Supervisor Dispute Outcome due in 2 business days

**WPCS Overtime Violation:**
- WPCS violations trigger task to existing WPCS Work Queue
- Follow same County Review and County Dispute processes

#### **Comments and History:**
- Comments required whenever violation outcome is determined (1,000 character limit)
- Comments ensure anyone reviewing violation later knows why outcome decision was made
- History link shows all changes to violation data (Last Update Date/By tracked)
- Most recent information displays on View Overtime Violation screen, previous data pushed to History

#### **Edits Allowed:**
- County Review or Supervisor Review screens: Editable until end of business day, then locked
- County Dispute or Supervisor Dispute screens: Editable until end of business day, then locked

---

### 4. Provider Overtime Exemption Payment Processing

**As a** CDSS or WPCS staff member  
**I want to** manage provider overtime exemptions  
**So that** providers caring for severely impaired recipients can work up to 360 hours per month

**Acceptance Criteria:**

#### **Exemption Overview:**
- Provider self-certifies request for exemption from existing overtime rules and weekly maximums
- Exemption rules limit provider to 360:00 hours per month across all recipients
- Exemptions apply to entire calendar month regardless of Begin/End dates
- Example: If exemption Begin Date is 5/28/2016, provider exempt for entire month of May

#### **Exemption Requirements:**
- Must be associated with two or more recipients (multiple IHSS/WPCS assignments OR single recipient with both IHSS and WPCS services)
- CDSS/WPCS staff manage exemptions based on county-submitted requests
- Exemptions apply to current/future service months only (not retroactive)

#### **Payment Processing with Exemption:**
- System aggregates claimed hours across all recipients for entire service month
- If claimed hours exceed 360:00 per month limit:
  - Hours in excess of 360:00 are cut back
  - Cut-back hours indicated as "Exemption Cutback"
  - View on Monthly Provider Paid Hours screen
- Overtime violations triggered during exemption period → Violation Status = "Inactive – Exemption"

#### **Adding Recipient to Provider with Active Exemption:**
- System displays warning: "The Provider has a current Overtime Violation Exemption. If you continue with this action the Overtime Exemption will be ended with an End Date (MM/DD/YYYY). Do you want to continue?"
- If user continues: Existing exemption is end-dated
- End Date calculation depends on Provider Hours Begin Date:
  - If Begin Date in current month: End Date = one day before Provider Hours Begin Date
  - If Begin Date in future month: End Date = last day of month prior to Provider Hours Begin Date
  - If Begin Date in past month: End Date = last day of most recent paid month
- CDSS advises counties to contact CDSS before continuing this action

#### **Terminating Provider with Active Exemption:**
- Exemption automatically assigned End Date based on Provider Hours Termination Effective Date
- Exemption records become non-editable after add/terminate actions

#### **Exemption Inactivation Rules:**
- Can only be inactivated by user if NO overtime violations have been affected by exemption
- If any violations inactivated due to exemption: Must assign End Date, cannot inactivate record

---

### 5. Provider Sick Leave

**As a** IHSS or WPCS provider  
**I want to** accrue and claim paid sick leave  
**So that** I can receive payment when I'm ill and unable to care for recipients

**Background:**
California Senate Bill No. 3 provides paid sick leave benefit for IHSS and WPCS providers beginning July 1, 2018.

**Acceptance Criteria:**

#### **Sick Leave Accrual Rates (by Fiscal Year):**
- **7/1/2018 - 6/30/2020:** 8 hours per fiscal year
- **7/1/2020 - 6/30/2022:** 16 hours per fiscal year
- **7/1/2022 - ongoing:** 24 hours per fiscal year

#### **Accrual and Eligibility Criteria:**

**Initial Accrual:**
- Provider must work 100 service hours paid in first fiscal year
- When 100 hours worked: ALL sick leave hours for fiscal year are accrued
- Accrued Date = first day of pay period when 100th hour processed

**Eligibility to Claim:**
After initial 100 hours accrued, provider must meet ONE of:
- Work additional 200 service hours (total 300 hours), OR
- Allow 60 calendar days to elapse from Accrued Date

When eligible: Sick Leave Hours Eligible Date set to first day of pay period when qualifying action occurs

**Ongoing Eligibility:**
- Once eligible, provider automatically eligible each subsequent fiscal year for that year's allocation
- Exception: If provider becomes ineligible for "No payroll activity of one year"

#### **Claiming Sick Leave:**

**Claim Requirements:**
- Provider must have accrued and met eligibility criteria
- Submit paper claim or electronic claim through Electronic Service Portal (ESP)
- **Minimum claim:** 1 hour per day
- **Increments:** After initial 1 hour, claim in 30-minute increments
- **Partial hours:** If remaining sick leave < 1 hour, provider may claim remaining time
- **Deadline:** Submit by end of month following service dates (e.g., September 23 claim must be received by October 31)
- **Separate claims:** If provider serves multiple recipients, separate claim required for each recipient

**Payment Details:**
- Paid at county wage rate assigned to provider for recipient case for day(s) claimed
- **Never paid at overtime rate** regardless of hours worked in week/month
- Generally paid as separate payment
- Sick leave hours do NOT count toward overtime violations

**Fiscal Year Carryover:**
- Sick leave hours do NOT carry over from one fiscal year to another
- Unclaimed hours at end of fiscal year (6/30) must be claimed by end of July (7/31) or lost

#### **Tracking and Display:**
- Hours appear on Remittance Advice (RA) once provider meets eligibility criteria
- Shows "Avail" (available) and "Paid" hours for current fiscal year
- As sick leave claimed/paid: "Avail" decreases, "Paid" increases
- Viewable by provider through E-Timesheet application
- **Note:** RA only shows current fiscal year hours, not previous fiscal years

#### **Sick Leave Ineligibility:**
- "Provider Sick Leave Eligibility Period" tracks eligibility at person level
- Existing providers as of 7/1/2018: Period starts 7/1/2018
- Exception: Providers with "Eligible" = "No" and Ineligible Reason = "Inactive/No Payroll for 1 Year"
- New providers: Period starts on Provider Details "Effective Date"
- Provider becomes ineligible if: "Eligible" changes to "No" with Ineligible Reason "Inactive/No Payroll for 1 Year"
  - Provider Sick Leave Eligibility Period End Date set to last day of eligibility
  - Must re-qualify if provider goes through enrollment again

#### **Voided Sick Leave Payments:**
- Voided warrant (non-deliverable): Sick leave time credited back to fiscal year in which it was accrued
- County should verify/update provider mailing address before reissuing
- Reissue via "Reissue Sick Leave" link on View Sick Leave screen
- If originally claimed hours exceed remaining hours after void: Reissued payment only pays up to remaining hours

---

### 6. Provider Workweek Agreements

**As a** County case worker  
**I want to** create and manage provider workweek agreements  
**So that** provider work schedules are documented and weekly maximums calculated correctly

**Acceptance Criteria:**

#### **Workweek Agreement Purpose:**
- Documents provider's regular work schedule across all recipients
- Used to calculate provider weekly maximum hours
- May include travel time provisions
- Tracks provider's committed hours per recipient per day

#### **Agreement Management:**
- Create Provider Workweek Agreement: Associate with one or more recipients
- Begin Date and End Date tracked
- Hours dynamically calculated as user enters daily time entries (Sunday through Saturday)
- Total hours displayed automatically
- Can indicate if agreement includes travel time

#### **Agreement End Dating:**
- Automatically end-dated when:
  - Provider placed on Leave/Terminated
  - Provider Eligible status changes from "Yes" to "No"
  - New Provider Workweek Agreement created (previous agreement end-dated one day before new Begin Date)
- Manual end-dating available when "End Date Workweek Agreement" checkbox selected

#### **Recipient Workweek Agreements:**
- Separate agreements track recipient-specific work schedules
- End-dated when Provider Leave/Terminated for that recipient
- End-dated when Provider Eligible status changes to "No"

#### **Weekly Maximum Calculations:**
- Provider Weekly Maximum based on workweek agreements across all recipients
- Recipient Weekly Maximum based on authorized hours
- System recalculates maximums when workweek agreements change

---

### 7. Provider Travel Time

**As a** County case worker  
**I want to** track and manage provider travel time between recipients  
**So that** providers can be compensated for travel and travel maximums enforced

**Acceptance Criteria:**

#### **Travel Time Rules:**
- Maximum 7:00 hours per week across all recipients
- Tracked for both IHSS and WPCS services
- Travel time between recipient locations
- "Traveling From" recipient must be designated

#### **Travel Time Management:**
- Create Travel Time: Specify recipient, traveling from recipient, hours per week
- View Travel Time: See current travel arrangements
- Modify Travel Time: Update travel hours or routes
- Leave/Terminate Travel Time: End-date travel arrangements
- Travel Time History: Track all changes

#### **Automatic Travel Time End-Dating:**
- When recipient case terminated: Travel time end-dated with case Authorization End Date
- When provider hours terminated/on leave: Travel time end-dated
- When provider Eligible changes to "No": Travel time automatically terminated with reason "Provider Not Eligible to Work"

#### **Travel Time Display:**
- Travel Time Recipient Case screen shows:
  - All recipient cases by Provider Type (IHSS, WPCS, IHSS/WPCS)
  - Provider Status (Active, On-Leave, Terminated)
  - Weekly Travel Time Hours
  - Most recent Workweek Agreement
- Sort order: Status (Active, On-Leave, Terminated), then Recipient Last Name

---

### 8. Provider CORI Management

**As a** County case worker  
**I want to** document and manage Criminal Offender Record Information (CORI) for providers  
**So that** provider eligibility is properly determined based on criminal history

**Acceptance Criteria:**

#### **CORI Documentation:**
- Create Provider CORI: Document conviction information
- Fields include:
  - Conviction Date
  - Tier (1 or 2)
  - Crime details
  - CORI End Date (if applicable)
- View Provider CORI: Review conviction history
- Modify Provider CORI: Update conviction details or add General Exception

#### **Tier 1 Convictions (Automatic Ineligibility):**
- Provider Eligible status immediately set to "No"
- Ineligible Reason: "Tier 1 Conviction" or "Subsequent Tier 1 Conviction"
- Provider Effective Date: 20 calendar days from current date
- All provider hours terminated with reason "Provider Enrollment Ineligible"
- Any General Exception Waivers end-dated
- Any Recipient Waivers end-dated

#### **Tier 2 Convictions (Conditional Eligibility):**
- Provider Eligible status set to "No"
- Ineligible Reason: "Tier 2 Conviction" or "Subsequent Tier 2 Conviction"
- Provider may serve if:
  - **General Exception Waiver** granted by CDSS CBCB, OR
  - **Recipient Waiver** granted for specific crime

#### **General Exception Waiver:**
- Added via Modify Provider CORI screen
- Begin Date and End Date (if applicable)
- When added: Provider Eligible status set to "Yes" (if all other enrollment criteria met)
- When End Date before CORI End Date: Provider Eligible status returns to "No" with reason "Tier 2 Conviction"

#### **Recipient Waiver:**
- Specific to recipient case
- Allows provider with Tier 2 conviction to serve that specific recipient
- End Date can be assigned
- When terminated: Case Provider termination date = Waiver End Date + 1 day, reason "Provider Enrollment Ineligible"

#### **CORI Tier Changes:**
- **Tier 2 to Tier 1:** Provider Eligible set to "No," Ineligible Reason updated, all Recipient Waivers terminated
- **Tier 1 to Tier 2:** Ineligible Reason updated to Tier 2 Conviction, provider may seek waivers

#### **CORI Inactivation:**
- When CORI End Date entered:
  - Active Recipient Waivers end-dated with reason "Waiver No Longer Required"
  - Active General Exception end-dated
  - Provider remains eligible if waivers were in place

---

### 9. Provider Benefit Deductions

**As a** County case worker  
**I want to** manage provider benefit deductions  
**So that** appropriate deductions are taken from provider payments for benefits

**Acceptance Criteria:**
- Create Provider Benefits Deduction
- Modify Provider Benefits Deduction
- Track deduction types and amounts
- Begin and End dates for deductions
- Deductions processed with provider payments

---

### 10. Document Import

**As a** County case worker  
**I want to** upload and manage scanned provider documents  
**So that** provider enrollment forms and certifications are stored electronically

**Acceptance Criteria:**

#### **Eligible Provider Forms:**
- SOC 426 - Provider Enrollment Form
- SOC 846 - Provider Enrollment Agreement
- SOC 2305 - Request for Exemption from Workweek Limits (Exemption 2)
- SOC 2308 - Exemption from Workweek Limits Approved Exemption Provider Agreement
- SOC 2313 - Exemption from Workweek Limits State Administrative Review Request Form

#### **File Specifications:**
- **Allowed Types:** PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG
- **Maximum Size:** 5 MB per file
- **Quantity:** One copy of each form allowed (except forms that can have multiples)
- **Storage:** Active case and active provider documents retained

#### **Document Management:**
- Upload documents with form type designation
- View documents from Provider Attachments Screen
- Archive documents (marked during nightly batch cycle)
- Restore archived documents (same day only, before nightly batch)

---

## KEY BUSINESS RULES OVERVIEW {#business-rules-overview}

**Total Business Rules:** 190+

**Rule Categories:**
1. **Provider Hours Management (BR 1-10):** Automatic building, SSN verification, pay rate changes
2. **Provider Eligibility (BR 11-30):** Suspended providers, reinstatement, re-enrollment, DOJ county management
3. **CORI Management (BR 31-40):** Tier 1/2 convictions, General Exceptions, Recipient Waivers
4. **Provider Notifications (BR 40-50):** SOC 2271 generation and cleanup
5. **Labor Market Adjustments (BR 48-54):** Applying LMA changes to providers with assigned hours
6. **Inter-County Transfers (BR 50-88):** Sending county permissions, provider hour management
7. **Provider Workweek Agreements (BR 58-76):** Creation, modification, end-dating rules
8. **Travel Time Management (BR 59-65):** Display rules, termination, inactivation
9. **Enrollment Management (BR 67-70):** SOC 846 requirements, eligible field changes
10. **Overtime Violations (BR 90-190+):** Violation processing, review outcomes, exemptions, consequences

---

## CRITICAL BUSINESS RULES (1-50) {#business-rules-1-50}

### BR PVM 01 - Automatically Build Provider Hours
**When:** New IHSS Provider hours segment added AND there are no Assigned Hours indicated  
**Then:** Starting with Provider Hours Begin Date, copy any change to Recipient authorized hours to Provider hours including begin/end dates. If Recipient in active status, leave most recent Provider hours segment open-ended (no End Date).  
**Status:** IMPLEMENTED

---

### BR PVM 02 - Automatically Build Provider Hours Based on Pay Rate Changes
**When:** New IHSS Provider hours segment added AND there is subsequent change in rate of pay indicated on County Pay Rate table  
**Then:** Starting with Provider Begin Date, add any new Provider hours segment if there is subsequent pay rate change indicated on County Pay Rate table.  
**Status:** IMPLEMENTED

---

### BR PVM 03 - Set Provider SSN Verification to Not Verified
**When:** "Save" link selected on "Create Provider" screen, OR "Save" on "Modify Person" screen when person type is "Provider" and Last/First/Middle Name or DOB changed, OR "Save" on "Modify Alternate ID" screen when type is SSN  
**Then:** Set Provider SSN Verification to '0' (displays as "Not Yet Verified")  
**Status:** IMPLEMENTED

---

### BR PVM 04 - SSN Verification Send
**When:** Creating SSN Verification Send file, find all Person Type "Provider" records where SSN Verification Status is "Not Yet Verified" AND associated Alternative ID Social Security Number Duplicates field is blank  
**Then:** Send Providers to Social Security Administration (SSA) for verification  
**Status:** IMPLEMENTED

---

### BR PVM 05 - SSN Verification Receive
**When:** Upon receipt of SSN Verification Receive file  
**Then:** Update Provider SSN verification status. If received verification is "deceased," generate Task to Case Owner.  
**Status:** IMPLEMENTED

---

### BR PVM 06 - Case Provider List Screen Sort Order
**When:** Case Providers list screen is displayed  
**Then:** Default sort order will be Provider Status  
**Status:** IMPLEMENTED

---

### BR PVM 07 - Create Initial Timesheets with or without Travel Claim
**When:** Save on Assign Case Provider screen AND Pay Period is equal to or later than Travel Claim Start Date AND Print Option is 'Print/Mail from Centralized Print Center' or 'Electronic' AND no other errors  
**Then:** 
- If CaseProvider.isETS = False AND pay period start date not equal to/after CaseProvider.EVVEffectiveDate AND recipient "Timesheet Accommodation" is NOT 'Large Font Timesheet' AND "Provider has Workweek Agreement with Travel" = "Yes": Create timesheet data with Travel Claim Indicator '2'
- If "Provider has Workweek Agreement with Travel" = "No": Create timesheet data with Travel Claim Indicator '0'
- If CaseProvider.isETS = True OR pay period start date equal to/after CaseProvider.EVVEffectiveDate: Generate Electronic timesheet. If "Provider has Workweek Agreement with Travel" = "Yes": Create travel claim data with Indicator '1'
- Timesheets generated starting from Begin Date on Assign Case Provider screen
- Arrears: Up to 24 pay periods (12 months)
- Advance Pay: Up to 6 pay periods (3 months)

**Status:** IMPLEMENTED

---

### BR PVM 09 - Suspended/Ineligible Provider
**When:** Upon receipt of weekly Medi-Cal Suspended and Ineligible interface  
**Then:** Match based on two of three identifiers (Name, SSN, DOB). If provider suspended:
- Update Provider status to Terminated, Termination Reason "Medi-Cal Suspended or Ineligible"
- Set Provider End Date to Suspended/Ineligible Begin Date
- Provider Eligible status set to 'No' with Ineligible Reason 'Suspended or Ineligible'
- Suspended/Ineligible status set to 'Yes' with Begin Date from interface
- Send notifications to Case Worker, Supervisor, Provider Management, and WPCS work queues
- Produce Provider Terminated letter to each Recipient provider actively assigned to
- If provider suspension ended: Update indicator to 'No' and update End Date

**Status:** IMPLEMENTED

---

### BR PVM 10 - SSN Prefix Update
**When:** On a scheduled day  
**Then:** CGI Back Office will update SSN Prefix Table from SSN website  
**Status:** IMPLEMENTED

---

### BR PVM 13 - Update Recipient Funding Source Based on Provider Relationship
**When:** Update, addition, termination or change of Provider who is indicated as parent of minor child or spouse in Relationship to Recipient  
**Then:** 
- Re-determine funding source based on Case Maintenance rules
- If Funding Source changed from PCSP to IPO3/IPO4 AND Reassessment Due Date > 1 year from Home Visit Date: Set Reassessment Due Date to 1 year from Home Visit Date
- Create Case Note with details of funding source change

**Status:** IMPLEMENTED

---

### BR PVM 14 - PA Provider Identification
**When:** Upon receipt of PA Provider Identification Interface  
**Then:** Match with CMIPS Providers on Alternative ID Type Social Security Number (Duplicates field "Blank"), else match on Alternative ID Type Conversion Duplicate AND (Last Name OR DOB). Update indicators for PA Registered, Training, Fingerprinting, and Background Check when match found.  
**Status:** IMPLEMENTED

---

### BR PVM 15 - Modify Enrollment – "Eligible" Field Changes from "Yes" to "No"
**When:** Save link selected on Modify Enrollment screen AND Eligible status updated from "Yes" to "No"  
**Then:** 
- Save Modify Enrollment screen
- If Effective Date updated: Save that date; else set Effective Date to current date
- If Provider Status associated to any case is Active or Leave:
  - Set Provider Status to Terminated
  - Set Leave/Termination Effective Date equal to Modify Enrollment Effective Date
  - Set Termination Reason to "Provider Not Eligible to Work" if Ineligible Reason is: Suspended or Ineligible, Duplicate SSN, Suspect SSN
  - If General Exception exists with blank End Date: Set End Date to Modify Enrollment Effective Date
  - If Recipient Waiver exists with blank End Date: Set End Date to Modify Enrollment Effective Date minus 1 day
  - Set Termination Reason to "Terminated Provider"
  - Generate notification to Case Owner for terminated cases
  - Generate task to WPCS queue for terminated WPCS cases
  - End Date Provider Workweek Agreement
  - End Date Recipient Workweek Agreement
  - Recalculate Monthly Overtime Maximum
  - Recalculate Provider and Recipient Weekly Maximum
  - Update Number of Active Cases
  - Update Number of Active Providers

**Status:** IMPLEMENTED

---

### BR PVM 16 - Update Provider Eligible Status to 'No' Upon Confirmed Death
**When:** Save on Modify Person screen AND death has been confirmed  
**Then:** Update Eligible status to 'No' and set Ineligible Reason to 'Death'  
**Status:** IMPLEMENTED

---

### BR PVM 17 - Blank Out Ineligible Reason When Provider Updated to Eligible 'Yes' from 'Pending Reinstatement'
**When:** Selecting 'Yes' on Approve Provider Enrollment screen  
**Then:** Set Ineligible Reason to blank. If provSLAccrualEligibilityEndDate is other than 12/31/9999, reset date to 12/31/9999  
**Status:** IMPLEMENTED

---

### BR PVM 18 - Terminated/On-Leave Provider Hours Segments When Adding New Provider Hours Segment
**When:** Creating new Provider hours segment on Create Provider Hours screen AND new Begin Date is prior to or equal to Effective Date of Terminated/On Leave segment  
**Then:** Override the Terminated/On-Leave segment with newly created segment  
**Status:** IMPLEMENTED

---

### BR PVM 19 - For Provider Without Assigned Hours, Do Not Update Provider Assigned Hours When Recipient Authorization Updated During Terminated/On Leave Period
**When:** Recipient Submit for Approval (Final Determination)  
**Then:** Do not update Provider Assigned Hours for any Provider who is Terminated or On Leave during newly authorized period for Recipient  
**Status:** IMPLEMENTED

---

### BR PVM 20 - Create Provider - Display Name in All Uppercase Letters
**When:** Save link selected on Create Provider screen AND lowercase letters used in Last Name, First Name, or Middle Name fields  
**Then:** Display First Name, Last Name, and Middle Name entries in all uppercase letters on Person Home screen  
**Status:** IMPLEMENTED

---

### BR PVM 21 - Modify Enrollment – Re-enroll Provider – Default DOJ County
**When:** Save link selected on Modify Enrollment screen AND previous action was Re-enroll action  
**Then:** Default DOJ County to County associated with user (if user security group is other than "CDSSProgramMgmt")  
**Status:** IMPLEMENTED

---

### BR PVM 22 - Inactive Provider Batch Process – Inactive/No Payroll for 1 Year
**When:** Provider has no payroll activity processed and paid for IHSS or WPCS on any case for minimum 12 months AND Provider enrollment Effective Date > 12 months prior to batch date AND Provider "Eligible" field is 'Yes'  
**Then:** 
- Set Provider Eligible status to 'No'
- Set Ineligible Reason to "Inactive/No Payroll for 1 Year"
- Terminate Provider if Active or On Leave as of current date + 20 calendar days
- Set Termination Reason to "Provider Enrollment Ineligible"
- Trigger notification to Case Owner for terminated cases
- Generate task to WPCS queue for terminated WPCS cases
- End Date Provider Workweek Agreement
- End Date Recipient Workweek Agreement
- Recalculate Monthly Overtime Maximum
- Recalculate Provider and Recipient Weekly Maximum
- Update Number of Active Cases/Providers
- Set ProviderSLAccrualEligibility: provSLAccrualEligibilityEndDate to Provider Leave/Terminate Effective Date

**Status:** IMPLEMENTED

---

### BR PVM 23 - Reinstate Provider Will Restore Prior Enrollment Information
**When:** User selects Reinstate from Provider Details screen  
**Then:** Previous Provider enrollment information displayed in Modify Enrollment screen  
**Status:** IMPLEMENTED

---

### BR PVM 24 - Re-enroll Provider Clears Current Enrollment Information
**When:** User selects Re-enroll from Provider Details screen  
**Then:** Modify Enrollment screen displayed with "Pending" Eligibility status and no values in enrollment information  
**Status:** IMPLEMENTED

---

### BR PVM 25 - Provider Reinstatement Must Be Within 30 Calendar Days
**When:** Display of Provider Details screen  
**Then:** Display "Reinstate" link when: Eligible field changed from "Yes" to "No" in prior 30 calendar days AND Ineligible Reason is other than "Third Overtime Violation" or "Fourth Overtime Violation" AND user associated with DOJ County of provider  
**Status:** IMPLEMENTED

---

### BR PVM 26 - Display Link for Provider Re-enrollment
**When:** Display of Provider Details Screen  
**Then:** Only display link to Re-enroll Provider if current Eligible status is 'No'  
**Status:** IMPLEMENTED

---

### BR PVM 27 - Creating Provider When Person is Applicant or Recipient
**When:** Save on Create Provider screen  
**Then:** Set DOJ County for Provider based on DOJ County on Create Provider screen. Do not modify County on Person Home Screen.  
**Status:** IMPLEMENTED

---

### BR PVM 28 - Creating Applicant or Referral When Person is Provider
**When:** Save on Create Applicant or Create Referral screens  
**Then:** Set County on Person Home screen based on User creating Applicant or Referral. Do not modify DOJ County for Provider.  
**Status:** IMPLEMENTED

---

### BR PVM 29 - Creating or Updating DOJ County for Provider
**When:** Save on Create Provider or Modify Enrollment screens  
**Then:** Set DOJ County for Provider based on DOJ County on screen. Update County on Person Home screen based on DOJ County.  
**Status:** IMPLEMENTED

---

### BR PVM 30
**Status:** CANCELLED (by ASR Sprint 42 Team 1&2)

---

### BR PVM 31 - Create Provider CORI
**When:** Save link selected on Create Provider CORI AND conviction date different than any other CORI record  
**Then:** 
- Set Provider Details Eligible status to 'No'
- Set Provider Details Effective Date to 20 calendar days from current date
- If first CORI record:
  - If Tier 01: Set Ineligible Reason to "Tier 1 Conviction"
  - If Tier 02: Set Ineligible Reason to "Tier 2 Conviction"
- If more than one CORI exists:
  - If Tier 01: Set Ineligible Reason to "Subsequent Tier 1 Conviction"
  - If Tier 02: Set Ineligible Reason to "Subsequent Tier 2 Conviction"
- Terminate Provider Hours:
  - Set Termination Reason to "Provider Enrollment Ineligible"
  - If most recent Provider hours segment has End Date < 20 calendar days from current: Set Termination Effective Date to current End Date + 1
  - Else: Set Termination Effective Date to 20 calendar days from current date
- If General Exception exists with no End Date: Set End Date to 20 calendar days from current
- If Recipient Waiver exists with blank End Date: Set End Date to 20 calendar days from current - 1 day
- Set Termination Reason to "Terminated Provider"
- End Date Provider Workweek Agreement
- End Date Recipient Workweek Agreement
- Recalculate Monthly Overtime Maximum
- Recalculate Provider and Recipient Weekly Maximum
- Update Number of Active Cases/Providers

**Status:** IMPLEMENTED

---

### BR PVM 33 - Modify Provider CORI – Adding General Exception
**When:** Save on Modify Provider CORI screen AND General Exception Begin Date populated AND no General Exception End Date AND Provider meets all other enrollment criteria  
**Then:** Save screen and set Provider Details Eligible field to "Yes"  
**Status:** IMPLEMENTED

---

### BR PVM 34 - Modify Provider CORI – Adding General Exception Waiver End Date
**When:** Save link selected on Modify Provider CORI screen AND General Exception End Date populated AND Provider Eligible status 'Yes' AND General Exception End Date prior to CORI End Date  
**Then:** 
- Set Provider Eligible status to 'No'
- Set provider eligibility effective date to General Exception Waiver End Date + 1 day
- Set Ineligibility Reason to "Tier 2 Conviction"
- Terminate Provider Hours (same logic as BR 31)
- End Date Provider Workweek Agreement
- End Date Recipient Workweek Agreement
- Recalculate Monthly Overtime Maximum
- Recalculate Provider and Recipient Weekly Maximum
- Update Number of Active Cases/Providers

**Status:** IMPLEMENTED

---

### BR PVM 35 - Terminate Provider if Recipient Waiver is Terminated
**When:** Save on Modify Recipient Waiver screen with Recipient Waiver End Date  
**Then:** 
- Set Case Provider Termination date to Recipient Waiver End Date + 1 day
- Set Case Provider Termination Reason to 'Provider Enrollment Ineligible'
- End Date Provider Workweek Agreement
- End Date Recipient Workweek Agreement
- Recalculate Monthly Overtime Maximum
- Recalculate Provider and Recipient Weekly Maximum
- Update Number of Active Cases/Providers

**Status:** IMPLEMENTED

---

### BR PVM 36 - Updating CORI Tier from 2 to 1
**When:** Save on Modify Provider CORI screen AND Tier updated from Tier 2 to Tier 1  
**Then:** 
- If Provider Eligible 'Yes': Set to 'No', Ineligible Reason "Tier 1 Conviction", Effective Date to current
- If Provider Eligible 'No': Update Ineligible Reason to "Tier 1 Conviction"
- Terminate any Recipient Waivers effective current date + 20 days
- Set Recipient Waiver Termination Reason to "Terminated Provider"
- See BR 15 for further actions
- End Date Provider Workweek Agreement
- End Date Recipient Workweek Agreement
- Recalculate Monthly Overtime Maximum
- Recalculate Provider and Recipient Weekly Maximum
- Update Number of Active Cases/Providers

**Status:** IMPLEMENTED

---

### BR PVM 37 - End Date Recipient Waivers When CORI End Date Saved
**When:** Save on Modify Provider CORI screen AND CORI End Date populated AND Recipient Waiver active (open end date) for associated CORI  
**Then:** Set Recipient Waiver End Date to CORI End Date, Termination Reason "Waiver No Longer Required". Provider remains Eligible 'No'. See BR 35 for further actions.  
**Status:** IMPLEMENTED

---

### BR PVM 38 - End Date General Exception Waiver When CORI End Date Saved
**When:** Save on Modify Provider CORI screen AND CORI End Date populated AND General Exception active (open end date) for associated CORI  
**Then:** Set General Exception End Date to CORI End Date. Provider remains Eligible 'Yes'. Do not terminate Provider if currently serving on case.  
**Status:** IMPLEMENTED

---

### BR PVM 39 - Updating CORI Tier from 1 to 2
**When:** Save on Modify Provider CORI screen AND Tier updated from Tier 1 to Tier 2  
**Then:** If first CORI record: Set Ineligible Reason to 'Tier 2 Conviction'; Otherwise: Set to 'Subsequent Tier 2 Conviction'  
**Status:** IMPLEMENTED

---

### BR PVM 40 - Provider Notification (SOC 2271) - Initial Assignment
**When:** Provider successfully assigned IHSS hours on recipient case (Assign Case Provider) AND Current case status is Eligible or Presumptive Eligible  
**Then:** Generate SOC 2271 with:
- Recipient case name populated to "You are receiving this notice because you are a provider of IHSS for [case recipient name]"
- Provider Number (9-digit CMIPS Provider Number)
- Various scenarios based on Provider Begin Date relative to Authorization Segments:
  - **If Begin Date corresponds to Authorization Segment with End Date ≥ current date:** Set Notification Date to later of Provider Begin Date or current date
  - **If Begin Date corresponds to Authorization Segment with End Date < current date AND subsequent segment exists:** Set Notification Date to current date, use latest Authorization Segment
  - **If Begin Date corresponds to Authorization Segment with End Date < current date AND no subsequent segment:** Set Notification Date to current date
  - **If Begin Date does not correspond to Authorization Segment (overdue assessment):** Set Notification Date to later of Provider Begin Date or current date, use most recent Authorization Segment
- "Your recipient's monthly authorized hours are" [Auth to Purchase after Assigned Hours]
- Weekly Authorized Hours populated
- Service Types indicated with "X"
- For each subsequent Authorization Segment: Generate additional SOC 2271 if Service Types change
- Save as Correspondence in Pending status to be printed in next batch cycle

**Status:** IMPLEMENTED

---

### BR PVM 41-43 - Provider Notification (SOC 2271) - First Time, Previously On Leave, Previously Terminated
**When:** Provider successfully assigned IHSS hours on Recipient case for first time (no existing IHSS Provider Hours records) AND Current case status is Eligible; OR Provider previously On Leave/Terminated returning to case AND case status is Eligible or Presumptive Eligible  
**Then:** See Action column for BR 40 above  
**Status:** IMPLEMENTED

---

### BR PVM 44-47 - Provider Notification (SOC 2271) Clean-up - Leave/Terminate Provider
**When:** Save on Leave/Terminate Case Provider screen  
**Then:** 
- **If Provider Terminated AND Effective Date ≤ current date:** Inactivate all Pending Provider Notifications
- **If Provider On Leave AND Effective Date ≤ current date:** Inactivate all Pending Provider Notifications
- **If Provider Terminated AND Effective Date > current date:** Inactivate Pending Notifications with Effective Date ≥ Begin Date of Termination
- **If Provider On Leave AND Effective Date > current date:** Inactivate Pending Notifications with Effective Date ≥ Begin Date of Leave

**Status:** IMPLEMENTED

---

### BR PVM 48-49 - Apply LMA Change to Providers with Assigned Hours
**When:** Recipient case has Authorization Segment with Start Date ≥ current LMA Begin Date AND Provider Assigned Hours Form indicated AND current Provider Hours End Date after LMA Start Date  
**Then:** 
- **Case has one Active Provider:** 
  - End Date current Provider Hours segment with one day prior to LMA Start Date
  - Create New Provider Hours segment with Begin Date = LMA Start Date
  - Check Assigned Hours checkbox
  - Populate Assigned Hours with most recent Mode of Service IP Hours
- **Case has multiple Active Providers:**
  - End Date current Provider Hours segment with one day prior to LMA Start Date
  - Create New Provider Hours segment with Begin Date = LMA Start Date
  - Check Assigned Hours checkbox
  - If aggregated Assigned Hours = most recent Mode of Service IP Hours:
    - For all except last Provider: Assign Hours = (previous assigned hours + 7.0%)
    - For last Provider: Assign Hours = (Mode of Service IP Hours - sum of all processed Provider assigned hours)
  - If aggregated Assigned Hours ≠ most recent Mode of Service IP Hours:
    - For each Provider: Assign Hours = (previous assigned hours + 7.0%)

**Status:** IMPLEMENTED

---

### BR PVM 50 - Allow Sending County to Assign Provider When Inter-County Transfer Completed
**When:** User with Security Role ProviderManagement or Blended Role containing ProvMgmt associated with Sending County on most recent Inter-County Transfer in Completed Status selects Assign Provider link  
**Then:** Display Assign Case Provider screen and default IHSS Hours Pay Rate to Sending County Default Pay Rate  
**Status:** IMPLEMENTED

---

## BUSINESS RULES SUMMARY (51-190+) {#business-rules-summary}

Due to the extensive nature of the remaining business rules (140+), here's a categorized summary:

### Provider Hours Management (BR 51-88)
- Inter-County Transfer provider hour permissions for Sending County
- WPCS worker provider hour management
- Provider enrollment history creation
- Provider hours creation for periods prior to oldest segment

### Travel Time Management (BR 59-65)
- Travel time display rules by provider type and status
- Travel from recipients selection
- Automatic travel time termination when provider/recipient terminated
- Travel time inactivation when provider becomes ineligible

### Enrollment and SOC 846 Requirements (BR 67-70)
- Modify Enrollment - Eligible field "Pending" to "No" validation
- Create Provider - SOC 846 checkbox requirements after FLSA
- Modify Enrollment - SOC 846 checkbox requirements after FLSA
- Both Provider Agreement and Overtime Agreement required after FLSA effective date

### Number of Active Cases and Workweek Agreements (BR 71-76)
- Nightly batch determination of Number of Active Cases
- Provider Workweek Agreement end-dating when Provider Leave/Terminate
- Recipient Workweek Agreement end-dating when Provider Leave/Terminate
- Provider Workweek Agreement end-dating when Provider Eligible changes to "No"
- Provider Workweek Agreement end-dating when new segment created

### Inter-County Transfer Provider Management (BR 81-88)
- Sending County permissions to place Provider on Leave prior to transfer
- Sending County permissions to Terminate Provider prior to transfer
- WPCS Worker permissions for Leave/Terminate prior to transfer
- Provider enrollment history creation

### Overtime Violations Processing (BR 90-190+)
These rules cover the complex overtime violation system including:
- Violation triggering and detection
- County Review process and outcomes
- Supervisor Review process and outcomes
- County Dispute process and timelines
- CDSS State Administrative Review (for Violations 3 & 4)
- Violation consequence implementation (Warnings, Training, 90-day suspension, 365-day suspension)
- Overtime Violation Exemption processing
- Next Possible Violation Date calculations
- Provider termination for violations
- Provider reinstatement after violations
- Violation history and tracking
- Work queue task generation
- Letter generation (SOC 2257 series)
- Provider Weekly/Monthly Maximum calculations
- Exemption cutback processing
- Training completion tracking for Violation #2

---

## KEY FORMS

### Provider Enrollment Forms:
- **SOC 426** - Provider Enrollment Form
- **SOC 846** - Provider Enrollment Agreement (includes Provider Agreement and Overtime Agreement after FLSA)

### Provider Notification Forms:
- **SOC 2271** - Provider Notification of Recipient Authorized Hours and Services

### Overtime Violation Forms:
- **SOC 2257 Series** - Various overtime violation notice letters
- **SOC 2305** - Request for Exemption from Workweek Limits (Exemption 2)
- **SOC 2308** - Exemption Approved Provider Agreement
- **SOC 2313** - Exemption State Administrative Review Request Form

### CORI and Waiver Forms:
- General Exception Waiver documentation
- Recipient Waiver documentation

---

## KEY INTERFACES

### Internal Interfaces:
- PROO901A - Create Provider
- PROO922A - Update Person
- PROO923A - Update SSN
- PROO926A - Update Designee

### External Interfaces:
- **SSA (Social Security Administration)** - SSN Verification (Weekly)
- **Medi-Cal Suspended and Ineligible Provider Interface** (Weekly)
- **PA (Public Authority) Provider Identification Interface**
- **DOJ (Department of Justice)** - Criminal background checks

### Batch Jobs:
- **600QINDN** - Provider reinstatement after 90-day suspension (Violation #3)
- **600FINDN** - Determine Number of Active Cases (Nightly)
- **CMDS107Q** - Timesheet data collection (standard timesheets)
- **CMDS107R** - Timesheet data collection (timesheets with travel, indicator '2')
- **CMDS107S** - Travel claim data collection (indicator '1')
- **SSN Verification Batch** (Weekly with SSA)
- **Inactive Provider Batch** - Ineligible/No Payroll for 1 Year (Periodic)
- **Sick Leave Processing Batch** (Various)

---

## KEY BUSINESS LOGIC

### Provider Eligibility Chain:
1. Provider enrolls → SOC 426, Orientation, SOC 846, DOJ Background Check
2. SSN verified with SSA
3. No Tier 1 CORI or has appropriate waivers for Tier 2
4. Not on Medi-Cal Suspended/Ineligible list
5. SOC 846 completed (both Provider Agreement and Overtime Agreement after FLSA)
6. Has payroll activity within 12 months
7. Not terminated for Overtime Violations 3 or 4

### Provider Hour Segment Lifecycle:
- **Active:** Provider currently serving recipient
- **On Leave:** Temporarily not serving, can return
- **Terminated:** No longer serving, reason coded
- System automatically creates new segments when:
  - Recipient authorization changes (if no Assigned Hours)
  - County pay rate changes
  - Provider returns from Leave/Termination

### Overtime Violation Progressive Discipline:
1. **Violation #1:** Active on record
2. **Violation #2:** Optional training (14-21 days) to remove violation (one-time only)
3. **Violation #3:** 90-day suspension, automatic reinstatement
4. **Violation #4:** 365-day suspension, must re-enroll

### Funding Source Determination Rules:
- Spouse or Parent of minor child → IPO (IHSS Plus Option) eligible
- System automatically recalculates when provider relationship changes
- Affects recipient case funding, not provider payment

### Sick Leave Eligibility Progression:
1. Provider starts as IHSS/WPCS provider (Effective Date = Sick Leave Eligibility Period start)
2. After 100 paid hours → Sick Leave Accrued (fiscal year allocation granted)
3. After additional 200 paid hours OR 60 calendar days → Eligible to claim
4. Remains eligible each subsequent fiscal year unless "Inactive/No Payroll for 1 Year"

---

## WORK QUEUES

### County Overtime Violation Work Queue:
- Review Overtime Violation tasks
- County Dispute Overtime Violation tasks
- County Dispute Resolution due in 2/4 business days reminders

### Supervisor Overtime Violation Work Queue:
- Supervisor Review for Provider Overtime Violation tasks
- Dispute Supervisor Review tasks
- Supervisor Dispute Outcome due in 2 business days reminders

### WPCS Work Queue:
- WPCS Provider termination notifications
- WPCS Provider overtime violation tasks

### Provider Management Work Queue:
- Provider enrollment changes
- Provider termination notifications

### Case Owner Work Queue:
- Provider assignment notifications
- Provider termination notifications
- Provider SSN verification deceased alerts

---

## IMPORTANT TIMEFRAMES

### Provider Enrollment:
- **Reinstatement Window:** 30 calendar days after Eligible changed to "No"
- **Provider Ineligibility Warning:** 20 calendar days before effective date

### Overtime Violations:
- **County Review:** 3 business days
- **Supervisor Review:** 2 business days (additional)
- **County Dispute Window:** 10 calendar days from letter date
- **County Dispute Processing:** 10 business days total
- **Training Completion (Violation #2):** 14 calendar days (extended to 21 with CDSS approval)
- **County Training Entry:** Additional 5 calendar days
- **Termination Effective (Violations 3 & 4):** 20 calendar days after upheld action
- **Violation #3 Suspension:** 90 calendar days
- **Violation #4 Suspension:** 365 calendar days
- **Task Triggers:** Day 6 (4 days remaining), Day 8 (2 days remaining) for County Dispute

### Sick Leave:
- **Claim Deadline:** End of month following service dates
- **Fiscal Year End Claims:** By 7/31 for previous fiscal year (6/30) or hours lost

### Provider Inactivity:
- **No Payroll Activity:** 12 months → Provider Eligible set to "No"

---

## ABBREVIATIONS & ACRONYMS

- **CBCB** - Caregiver Background Check Bureau
- **CDSS** - California Department of Social Services
- **CORI** - Criminal Offender Record Information
- **DOJ** - Department of Justice
- **ESP** - Electronic Service Portal
- **EVV** - Electronic Visit Verification
- **FLSA** - Fair Labor Standards Act
- **HTG** - Hourly Task Guidelines
- **IHSS** - In-Home Supportive Services
- **IP** - Individual Provider
- **IPO** - IHSS Plus Option
- **LMA** - Labor Market Adjustment
- **NOA** - Notice of Action
- **PA** - Public Authority
- **PCSP** - Personal Care Services Program
- **RA** - Remittance Advice
- **SAWS** - Statewide Automated Welfare System
- **SOC** - State of California (form prefix)
- **SSA** - Social Security Administration
- **SSN** - Social Security Number
- **WPCS** - Waiver Personal Care Services

---

**Document Generated:** December 10, 2025  
**Source:** _Release_2025_03_01__DSD_Section_23.pdf  
**Note:** This document provides comprehensive overview of key provider management user stories and business rules. Full details of all 190+ business rules available in source PDF.
