# IHSS CMIPS - Service Eligibility User Stories and Business Rules
## Release 2025.03.01 - DSD Section 21

---

## TABLE OF CONTENTS
1. [User Stories / Functional Requirements](#user-stories)
2. [Business Rules (1-50)](#business-rules)

---

## USER STORIES / FUNCTIONAL REQUIREMENTS {#user-stories}

The following functional requirements serve as the user stories for the IHSS CMIPS Service Eligibility system:

### 1. Service Eligibility Management

**As a** County case worker  
**I want to** enter and modify service eligibility evidence for applicants and recipients  
**So that** I can determine IHSS program eligibility and authorized service hours

**Acceptance Criteria:**
The Service Eligibility process includes the following evidence types:

#### **Household Evidence**
- Information about applicant/recipient's home environment
- Household members information
- Residence details

#### **Service Evidence**
- Functional ability assessment within home
- Service needs evaluation
- Task-based assessments
- Functional rankings
- Hourly Task Guidelines (HTG) tracking

#### **Program Evidence**
- IHSS Service Assessment period information
- Authorization dates
- Reassessment due dates
- Home visit dates
- Waiver program information
- Advance pay rates
- County pay rates

#### **Share of Cost (SOC) Evidence**
- Net income information
- Countable income calculations
- IHSS Share of Cost determination
- Income evidence tracking

#### **Disaster Preparedness**
- Emergency contact designation
- Contact information for emergencies

**System Capabilities:**
- Calculate total assessed need for service types
- Calculate adjustments for service types with proration
- Track functional ranks against Hourly Task Guidelines (HTG)
- Support multiple assessment types:
  - Initial
  - Change
  - Reassessment
  - Inter-County Transfer
  - Telehealth
- Maintain history of all evidence changes
- Support companion case relationships

---

### 2. Health Care Certification

**As a** County case worker  
**I want to** manage Health Care Certification requirements for IHSS applicants  
**So that** IHSS services can be authorized in compliance with WIC Section 12309.1

**Background:**
Senate Bill (SB) 72, implemented in 2011, added Welfare and Institutions Code (WIC) Section 12309.1 requiring IHSS applicants to submit a completed SOC 873 – "Health Care Certification" or alternative documentation before IHSS services are authorized.

**Acceptance Criteria:**

#### **Two Methods for Health Care Certification:**

**1. User Entered (3rd Party Provided)**
- When organization other than county provides SOC 873 to applicant
- User creates certification record manually
- Required fields:
  - "SOC 873 Given to Applicant by Other Organization" checkbox
  - Health Care Certification Type
  - Health Care Certification Received Date
  - Comments (recommended but not required)
- Editable until end of business day
- Becomes non-editable after nightly batch job
- Can be inactivated before case authorization

**2. Form Generated (County Provided)**
- County provides certification documents to applicant
- Automatically created when user selects form from Create Form screen:
  - "SOC 873 & 874 – IHSS Health Care Certification and Requirements"
  - "SOC 873 – IHSS Health Care Certification – English Only"
  - "SOC 873L & 874L – IHSS Health Care Certification – Large Font"
  - "SOC 873L – IHSS Health Care Certification – English Only - Large Print"
- Forms print in threshold languages (Armenian, Chinese, English, Spanish) when applicable
- Can send electronic copy to Recipient's ESP Message Center Inbox
- SOC 873 & 874 Printed Date and Due Date populated automatically

#### **Due Date Management:**
- Initial due date: 45 calendar days from form request/generation date
- Good Cause Extension: Additional 45 days (total 90 days) with county approval
- Extension must be requested before initial 45-day due date passes
- Due date recalculation when "SOC 873 & 874 Mailed/Given To Recipient" date entered (within 10 business days of print date)
- 10 business day grace period after due date for data entry (internal use only)

#### **Task Triggers:**
1. **First Task**: 10 business days before Due Date or Good Cause Extension Due Date
   - Notifies case owner of upcoming due date
   - Allows follow-up with applicant/recipient

2. **Second Task**: 1 business day before Due Date or Good Cause Extension Due Date
   - Case owner should deny or terminate case if documentation not received
   - Can only be closed when case terminated/denied OR when Health Care Certification Type and Documentation Received Date entered

#### **Exceptions Permitting Authorization Without Certification:**
1. **Hospital/Health Care Facility Discharge**: Services needed to enable individual to return safely home or into community
2. **Risk of Out-of-Home Placement**: Services authorized temporarily pending receipt of certification

#### **Inactivation:**
- Health Care Certification record can be inactivated and new record generated anytime prior to case authorization (approval or denial)
- Cannot be inactivated after authorization processed

#### **Forms and Languages:**
- SOC 873 & 874: Standard and Large Font versions
- Available in threshold languages: Armenian, Chinese (Cantonese/Mandarin), English, Spanish
- English-only versions available
- Electronic delivery to ESP supported

---

### 3. Contact Management for Service Eligibility

**As a** County case worker  
**I want to** create and manage contacts associated with cases  
**So that** important relationships and contact information are tracked

**Acceptance Criteria:**
- Create contacts with automatic start/end date assignment
- Contacts become active upon creation
- Contacts can be inactivated (end dated)
- Designee contacts (Guardian/Conservator) sync with Payroll system
- Batch processing removes Guardian contacts when minor recipient turns 18
- Contact names stored in uppercase
- Contact history maintained

---

### 4. Companion Case Management

**As a** County case worker  
**I want to** identify and link companion cases  
**So that** household relationships are properly tracked

**Acceptance Criteria:**
- Search for companion cases by matching residence address
- Address matching criteria:
  - Street Address: Street Number, Street Name, Unit Type, Unit Number, City
  - Other Address: Street Number, Street Name, Unit Type, Unit Number, City
  - Rural Route: Rural Route, City
  - Highway Address: Highway Address, City
- Display matching cases for selection
- Support companion case linking during household member creation/modification

---

### 5. Evidence History Search

**As a** County case worker or supervisor  
**I want to** search and view historical evidence records  
**So that** I can review past assessments and changes

**Acceptance Criteria:**
- Search by various criteria
- Results sorted by most recent Authorization Start Date
- When multiple records have same Auth Start Date, sort by timestamp
- Oldest records displayed last
- View complete history of evidence changes

---

### 6. IHSS Notification to SAWS

**As a** County case worker  
**I want to** notify SAWS of income/resource changes  
**So that** Medi-Cal eligibility determinations are accurate

**Acceptance Criteria:**
- Send S5 transaction to SAWS with notification text
- Only sends when Recipient Medi-Cal Aid Code is other than 10, 20, or 60
- Validates Aid Code for current Medi-Cal Eligibility Month
- User enters notification text on IHSS Notification to SAWS screen

---

## BUSINESS RULES {#business-rules}

### Business Rules 1-10

#### BR SE 01 - Calculate Total Assessed Need for a Service Type
**When:** Tasks with or without Proration are saved on either "Create Task Details" or "Modify Task Details" screen  
**Then:** Calculate Total Assessed Need (Modify Service Type Details : Total Assessed Need (HH:MM)):

**For Weekly Service Type:**
- If Frequency is Daily: Total Assessed Need = Addition of each task's ((Quantity * Duration) * 7)
- If Frequency is Weekly: Total Assessed Need = Addition of each task's (Quantity * Duration)

**For Monthly Service Type:**
- If Frequency is Daily: Total Assessed Need = Addition of each task's (((Quantity * Duration) * 7) * 4.33)
- If Frequency is Weekly: Total Assessed Need = Addition of each task's ((Quantity * Duration) * 4.33)
- If Frequency is Monthly: Total Assessed Need = Addition of each task's (Quantity * Duration)

**Status:** IMPLEMENTED

---

#### BR SE 02 - Calculate Adjustments for a Service Type
**When:** Tasks with Proration are saved on either "Create Task Details" or "Modify Task Details" screen  
**Then:** Calculate Adjustments (Modify Service Type Details : Adjustments (HH:MM)):

**For Weekly Service Type:**
- If Frequency is Daily: Adjustments = Addition of each task's [((Quantity * Duration) * 7) / Proration]
- If Frequency is Weekly: Adjustments = Addition of each task's [(Quantity * Duration) / Proration]

**For Monthly Service Type:**
- If Frequency is Daily: Adjustments = Addition of each task's [(((Quantity * Duration) * 7) * 4.33) / Proration]
- If Frequency is Weekly: Adjustments = Addition of each task's [((Quantity * Duration) * 4.33) / Proration]
- If Frequency is Monthly: Adjustments = Addition of each task's [(Quantity * Duration) / Proration]

**Status:** IMPLEMENTED

---

#### BR SE 03 - Set Date Fields on Modify Program Evidence Screen (Change Assessment)
**When:** "Change" assessment type is selected on Assessment Type screen  
**Then:** 
- Set Authorization End Date to same date as last Active Evidence
- Set Reassessment Due Date to same date as last Active Evidence
- Set Home Visit Date to same date as last Active Evidence
- Allow update of Authorization End Date and Home Visit Date

**Status:** IMPLEMENTED

---

#### BR SE 04 - Set Date Fields on Modify Program Evidence Screen (Initial Assessment)
**When:** "Initial" assessment type is selected on Assessment Type screen  
**Then:** 
- All Date fields are blank
- If not user entered, set Reassessment Due Date to one (1) year from Home Visit Date

**Status:** IMPLEMENTED

---

#### BR SE 05 - Set Date Fields on Modify Program Evidence Screen (Reassessment)
**When:** "Reassessment" assessment type is selected on Assessment Type screen  
**Then:** 
- All Date fields are blank
- If not user entered, set Reassessment Due Date to one (1) year from Home Visit Date

**Status:** IMPLEMENTED

---

#### BR SE 06 - Inter-County Transfer Assessment Type
**When:** "Inter-County Transfer" assessment type is selected on Assessment Type screen  
**Then:** 
- Set all fields to blank
- If not user entered, set Reassessment Due Date to one (1) year from Home Visit Date

**Status:** IMPLEMENTED

---

#### BR SE 07 - Hourly Task Guideline Indication (Exceeds HTG)
**When:** Service Type has an associated Hourly Task Guideline (HTG) AND indicated associated "Individual Assessed Need" exceeds the HTG for the indicated Functional Rank  
**Then:** A plus (+) sign displays in the HTG (Service Evidence Home : HTG) column for the Service Type  
**Status:** IMPLEMENTED

---

#### BR SE 08 - Hourly Task Guideline Indication (Below HTG)
**When:** Service Type has an associated Hourly Task Guideline (HTG) AND indicated associated "Individual Assessed Need" is below the HTG for the indicated Functional Rank  
**Then:** A minus (-) sign displays in the HTG (Service Evidence Home : HTG) column for the Service Type  
**Status:** IMPLEMENTED

---

#### BR SE 09 - Set Advance Pay Rate on Modify Program Evidence Screen
**When:** Save Modify Program Evidence screen AND Advance Pay is indicated AND Advance Pay Rate is blank  
**Then:** Set the Advance Pay Rate to the highest County IP Rate  
**Status:** IMPLEMENTED

---

#### BR SE 10 - Display County Pay Rates List
**When:** On the County Pay Rate screen when a valid From Date is entered AND search link is selected  
**Then:** The County Pay Rate List screen will display all the pay rates of the county  
**Status:** IMPLEMENTED

---

### Business Rules 11-20

#### BR SE 11 - Notification of Income/Resource Change
**When:** "Submit" link is selected on "IHSS Notification to SAWS" screen AND Recipient Medi-Cal Aid Code is other than 10, 20 or 60 for current Medi-Cal Eligibility Month  
**Then:** Send S5 to SAWS with text entered on the screen  
**Status:** IMPLEMENTED

---

#### BR SE 12 - Create Contact - Display Name in All Uppercase Letters
**When:** Save link is selected on Create Contact screen AND lowercase letters were used when entering the Name field  
**Then:** Display the Name field in all uppercase letters on the Contacts list screen  
**Status:** IMPLEMENTED

---

#### BR SE 13 - Share of Cost Evidence (Delete Income Evidence)
**When:** An Income Evidence row is deleted  
**Then:** Reset the Countable Income and IHSS Share of Cost fields to blank  
**Status:** IMPLEMENTED

---

#### BR SE 14 - Modify Income Evidence
**When:** Save link is selected on Modify Income Evidence screen  
**Then:** Reset the Countable Income and IHSS Share of Cost fields on the Share of Cost Evidence screen to blank  
**Status:** IMPLEMENTED

---

#### BR SE 15 - Create Income Evidence
**When:** Save link is selected on Create Income Evidence screen AND other Income Evidence exists  
**Then:** Reset the Countable Income and IHSS Share of Cost fields on the Share of Cost Evidence screen to blank  
**Status:** IMPLEMENTED

---

#### BR SE 16 - Retain Health Care Certification when Batch Action Adds Assessment
**When:** Any Assessment Type is added by the system by a batch action AND Health Care Certification data exists  
**Then:** Retain all the Health Care Certification data field indications as previously indicated  
**Status:** IMPLEMENTED (Note: BR 17 was removed with CR 651)

---

#### BR SE 18 - Reduced Hours – Initial Assessment
**When:** Assessment Type "Initial" is selected on Assessment Type screen  
**Then:** 
- Set Program Evidence Waiver Program field to blank
- Set the following Reduce Hours fields:
  - Set Reinstated Hours to blank
  - Set Social Worker Certification to blank
  - Set Verified by Case Owner or Supervisor to unchecked

**Status:** IMPLEMENTED

---

#### BR SE 19
**Status:** Removed with CR 1251

---

#### BR SE 20 - Reduced Hours – User Selected Assessment Other Than Initial
**When:** Assessment Type other than Initial is selected by a user  
**Then:** Set Program Evidence Waiver Program field to previously indicated Waiver Program  
**Status:** IMPLEMENTED

---

### Business Rules 21-30

#### BR SE 21 - Reduced Hours – Batch Processed Assessment Type
**When:** Batch processed Assessment Type is added  
**Then:** 
- Set Program Evidence Waiver Program field to previously indicated Waiver Program
- Set the following fields associated with Reduce Hours:
  - Set Reinstated Hours to previously indicated Reinstated Hours
  - Set Social Worker Certification to previously indicated Social Worker Certification
  - Set Verified by Case Owner or Supervisor to checked

**Status:** IMPLEMENTED

---

#### BR SE 22 - Modify Program Evidence – Reset "Verified by Case Owner or Supervisor"
**When:** Save link is selected on Modify Program Evidence screen AND Waiver Program field has changed AND "Verified by Case Owner or Supervisor" is checked on Modify Reduced Hours screen  
**Then:** Reset the "Verified by Case Owner or Supervisor" to unchecked  
**Status:** IMPLEMENTED

---

#### BR SE 23 - Net Adjusted Need Change – Reset "Verified by Case Owner or Supervisor"
**When:** Net Adjusted Need changes for any service AND "Verified by Case Owner or Supervisor" is checked on Modify Reduced Hours screen  
**Then:** Reset the "Verified by Case Owner or Supervisor" to unchecked  
**Status:** IMPLEMENTED

---

#### BR SE 24
**Status:** CANCELLED (during R2024.07.01 - Modify reduced hours screen is obsolete)

---

#### BR SE 25 - Evidence History Search
**When:** Search link is selected on Evidence History Search screen AND records are found matching entered criteria  
**Then:** Display results with default sort order of record with most recent Auth Start Date at top. "Most Recent" record determined by date timestamp when multiple records have same Auth Start Date. All others listed afterward to oldest record.  
**Status:** IMPLEMENTED

---

#### BR SE 26 - Create Household Member – Companion Case Search
**When:** Companion Case Number search icon is selected on Create Household Member screen  
**Then:** CMIPS II shall display cases which have the same residence address as the case to which companion case is being added AND residence address type for both persons is:

**Street Address:**
- Street Number, Street Name, Unit Type, Unit Number, City

**Other Address:**
- Street Number, Street Name, Unit Type, Unit Number, City

**Rural Route:**
- Rural Route, City

**Highway Address:**
- Highway Address, City

**Status:** IMPLEMENTED

---

#### BR SE 27 - Modify Household Member – Companion Case Search
**When:** Companion Case Number search icon is selected on Modify Household Member screen  
**Then:** Same criteria as BR SE 26  
**Status:** IMPLEMENTED

---

#### BR SE 28 - Create Form (Case) Health Care Certification - SOC 873 & 874 - Calculate Due Date
**When:** Save link is selected on Case Create Form screen AND either of following checkboxes selected:
- SOC 873 and 874 IHSS Health Care Certification and Requirements
- SOC 873 – IHSS Health Care Certification – English Only

**Then:** 
- If "Print in Nightly Batch": Populate "SOC 873 & 874 Printed Date" to one business day following action and queue to print
- Else If "Print Now on CMIPS II Printer": Populate "SOC 873 & 874 Printed Date" with current date and print form
- Else "Generate Now for Local Print": Populate "SOC 873 & 874 Printed Date" with current date, generate form, but do not queue to print
- Calculate and populate "Health Care Certification Due Date" as "Print Date" plus 45 calendar days

**Status:** IMPLEMENTED

---

#### BR SE 29 - Modify Health Care Certification – Form Generated - SOC 873 & 874 Mailed/Given To Recipient - Update Due Date
**When:** Save link is selected on Modify Health Care Certification screen AND "SOC 873 & 874 Mailed/Given To Recipient" was previously blank and is now indicated  
**Then:** 
- Save the data
- Recalculate the "Due Date" as "SOC 873 & 874 Mailed/Given To Recipient" plus 45 calendar days

**Status:** IMPLEMENTED

---

#### BR SE 30 - Modify Health Care Certification – Form Generated - Exception Granted Date
**When:** Save link is selected on Modify Health Care Certification screen AND Exception Granted Date is indicated  
**Then:** 
- Save the data
- Set Exception Granted Date Entered Date to current date
- Calculate and populate "Health Care Certification Due Date" as "Exception Granted Date" plus 45 calendar days

**Status:** IMPLEMENTED

---

### Business Rules 31-40

#### BR SE 31 - Modify Health Care Certification – Form Generated - Good Cause Extension
**When:** Save link is selected on Modify Health Care Certification screen AND "Good Cause Extension Date" was previously blank and is now indicated  
**Then:** 
- Save the data
- Populate Good Cause Extension Entered Date as current date
- Calculate "Good Cause Extension Due Date" as "Due Date" + 45 days

**Status:** IMPLEMENTED

---

#### BR SE 32 - Modify Health Care Certification – Form Generated - Documentation Received
**When:** Save link is selected on Modify Health Care Certification screen AND "Documentation Received Date" is indicated AND "Health Care Certification Type" is indicated  
**Then:** 
- Save the data
- Set "Documentation Received Entered Date" to current date

**Status:** IMPLEMENTED (Note: This is a duplicate of BR SE 31 in the original numbering)

---

#### BR SE 33 - Modify Health Care Certification – Form Generated - Clear Documentation Received
**When:** Save link is selected on Modify Health Care Certification screen AND current date matches Documentation Received Entered Date AND "Documentation Received Date" was previously indicated and is now blank AND "Health Care Certification Type" was previously indicated and is now blank

**Note:** This BR allows user to change Documentation Received Date and Health Care Certification Document to blank from previous indication on same business day it was originally entered.

**Then:** 
- Save the data
- Clear the Documentation Received Entered Date field

**Status:** IMPLEMENTED

---

#### BR SE 34 - Inactivate Health Care Certification
**When:** Yes link is selected on Inactivate Health Care Certification Confirmation screen  
**Then:** 
- Inactivate the Health Care Certification record
- Return user to Health Care Certification screen with no record displayed
- Mark any previously printed SOC 873 & 874 forms as Inactive with current date

**Status:** IMPLEMENTED

---

#### BR SE 35 - Health Care Certification - Display Form Generated or No Form Printed
**When:** Health Care Certification page navigation is selected from Evidence & Authorization content tab  
**Then:** 
- If SOC 873 & 874 OR SOC 873L & 874L forms have been triggered to print OR Sent to ESP: Display Health Care Certification screen with Form Generated content
- Else: Display Health Care Certification screen with Form Not Printed content

**Status:** IMPLEMENTED

---

#### BR SE 36 - Build Health Care Certification History
**When:** Save action is taken on Modify Health Care Certification screen  
**Then:** 
- Save the current data
- Push updated record to Health Care Certification History

**Status:** IMPLEMENTED

---

#### BR SE 37 - Create Form (Case) Health Care Certification – SOC 873 & 874 (Duplicate Copy)
**When:** Save link is selected on Create Form screen (Case) AND one of following forms generated:
- SOC 873 & 874 Health Care Certification form
- SOC 873 – IHSS Program Health Care Certification – English Only
- SOC 873L & 874L Health Care Certification - Large Font
- SOC 873L – IHSS Program Health Care Certification – English Only - Large Print

AND Health Care Certification record already exists on case

**Then:** 
- Allow the action
- Generate form as indicated in Print field
- Do NOT update SOC 873 & 874 Print Date on Health Care Certification screen
- Do NOT update Due Date on Health Care Certification screen

**Status:** IMPLEMENTED

---

#### BR SE 38 - Display Health Care Certification Screen (Forms Generated)
**When:** Health Care Certification page navigation is selected on Evidence and Authorization content tab AND one of following forms have been generated:
- SOC 873 & 874 Health Care Certification form
- SOC 873 – IHSS Program Health Care Certification – English Only
- SOC 873L & 874L Health Care Certification - Large Font
- SOC 873L – IHSS Program Health Care Certification – English Only - Large Print

**Then:** Display the Health Care Certification screen with "Forms Generated" fields  
**Status:** IMPLEMENTED

---

#### BR SE 39 - Display Health Care Certification Screen (No Forms Printed)
**When:** Health Care Certification page navigation is selected on Evidence and Authorization content tab AND NONE of following forms have been generated:
- SOC 873 & 874 Health Care Certification form
- SOC 873 – IHSS Program Health Care Certification – English Only
- SOC 873L & 874L Health Care Certification - Large Font
- SOC 873L – IHSS Program Health Care Certification – English Only - Large Print

**Then:** Display the Health Care Certification screen with "No Forms Printed" fields  
**Status:** IMPLEMENTED

---

#### BR SE 40 - Modify Health Care Certification – Form Printed
**When:** Edit Health Care Certification button is selected on Health Care Certification Screen  
**Then:** Display Health Care Certification – Form Printed pop up with all fields editable except:
- SOC 873 & 874 Print Date
- Due Date
- Good Cause Extension Due Date

**Status:** IMPLEMENTED

---

### Business Rules 41-50

#### BR SE 41 - Modify Health Care Certification – No Form Printed
**When:** Edit Health Care Certification button is selected on Health Care Certification Screen  
**Then:** Display Health Care Certification – No Form Printed pop up with all fields editable except:
- Documentation Received Entered Date

**Status:** IMPLEMENTED

---

#### BR SE 42 - Recipient Declines CFCO Indicator Checked
**When:** 
- Recipient Declines CFCO indicator is selected on Modify Program Evidence Screen
- Recipient Declines CFCO was not previously selected
- Modify Program Evidence Screen is saved
- No errors encountered
- Change in Program Evidence is approved

**Then:** 
- Recipient's funding source is redetermined (excluding CFCO) based on existing business rules (Determine Funding Program Aid Code) in DSD Section 22
- Current date is recorded in cfcoOptOutSnapshot database table

**Status:** IMPLEMENTED

---

#### BR SE 43 - Recipient Declines CFCO Indicator Unchecked
**When:** 
- Recipient Declines CFCO indicator is not selected on Modify Program Evidence Screen
- Recipient Declines CFCO was previously selected
- Modify Program Evidence Screen is saved
- No errors encountered
- Change in Program Evidence is approved

**Then:** 
- Recipient's funding source is redetermined based on existing business rules (Determine Funding Program Aid Code) in DSD Section 22
- Current date is recorded in cfcoOptOutSnapshot database table

**Status:** IMPLEMENTED

---

#### BR SE 44 - Create Contact - Set Start and End Date
**When:** Save link is selected on Create Contact screen  
**Then:** 
- Contact Start Date is set to current date
- Contact End Date is set to 12/31/9999
- Contact status is set to active (RST1) and contact is visible on Contacts screen

**Status:** IMPLEMENTED

---

#### BR SE 45 - Inactivate Contact - Set End Date
**When:** Inactivate link is selected on View Contact screen  
**Then:** 
- Contact End Date is set to current date
- Contact status is set to inactive (RST2) and contact is not visible on Contacts screen

**Status:** IMPLEMENTED

---

#### BR SE 46 - Health Care Certification History Details
**When:** View link is clicked on Health Care Certification History list pop-up  
**Then:** Display the Health Care Certification History Details pop-up  
**Status:** IMPLEMENTED

---

#### BR SE 47 - Create Form (Case) - Send Health Care Certification - SOC 873 & 874/SOC 873L & 874L Electronically - Calculate Due Date
**When:** Save button on Create Form Pop-Up is selected AND ("SOC 873 & 874 IHSS Program Health Care Certification and Requirements" OR "SOC 873L & 874L IHSS Program Health Care Certification and Requirements- Large Font" is checked) AND "Send Electronic Copy to ESP" print option is selected

**Then:** 
- Calculate and populate "Health Care Certification Due Date" as "Generation Date" plus 45 calendar days (Current Date + 45 calendar days)
- Set the Electronic Form Due Date to Health Care Certification Due Date
- Create Health Care Certification record
- If selected language is available as PDF template:
  - Generate two copies of "SOC 873 & 874" or "SOC 873L & 874L": First in English, second in selected language
  - Populate Due Date field on both SOC 874 or SOC 874L (45 days from created on date)
- Else if selected language is not available as PDF template:
  - Create one copy of "SOC 873 & SOC 874" or "SOC 873L & 874L" in English
  - Populate Due Date field on SOC 874 or SOC 874L (45 days from created on date)

**Status:** IMPLEMENTED

---

#### BR SE 48 - Inactivate Health Care Certification (3rd Party Form)
**When:** County worker selects Yes button on Inactivate Health Care Certification Pop-Up AND One of SOC 873 or SOC 873/874 were sent as 3rd party form  
**Then:** 
- Inactivate Health Care Certification record
- Return user to Health Care Certification screen with no record displayed
- Mark any previously printed or 3rd party SOC 873 & 874 forms as Inactive with current date
- Replace '3rd party form message' in users ESP Inbox with '3rd party form Inactivated' message

**Status:** IMPLEMENTED

---

#### BR SE 49 - Inactivate Health Care Certification for Pending Recipient
**When:** County worker selects Yes button on Inactivate Health Care Certification Pop-Up AND One of SOC 873L or SOC 873L/874L is selected AND Recipient case status is "Pending"  
**Then:** 
- Health Care Certification Status is set to Inactivated
- SOC 873L - IHSS Program Health Care Certification-English Only - Large Font Status = Inactivated in Forms & Correspondence
  OR
- SOC 873L & 874L IHSS Program Health Care Certification and Requirements - Large Font Status = Inactivated in Forms & Correspondence

**Status:** IMPLEMENTED

---

#### BR SE 50 - Set Date Fields on Modify Program Evidence Screen (Telehealth Assessment)
**When:** "Telehealth" assessment type is selected on Assessment Type screen  
**Then:** 
- All Date fields are blank
- If not user entered, set Reassessment Due Date to one (1) year from Home Visit Date

**Status:** IMPLEMENTED

---

## SUMMARY

**Total User Stories / Functional Requirements:** 6 main functional areas
**Total Business Rules:** 50 (with some cancelled/removed)
**Implementation Status:** Majority IMPLEMENTED
**Release:** 2025.03.01
**Document Section:** DSD Section 21
**System:** IHSS CMIPS - Service Eligibility

---

## KEY INTERFACES

### Internal Interfaces:
- PROO926A - Update Designee

### External Interfaces:
- CMDS4XXB - Statewide Automated Welfare System (SAWS) Daily Send (see DSD Section 22)

---

## KEY FORMS

### Health Care Certification Forms:
- SOC 873 - IHSS Program Health Care Certification
- SOC 874 - IHSS Program Health Care Certification Requirements
- SOC 873L - IHSS Program Health Care Certification - Large Font
- SOC 874L - IHSS Program Health Care Certification Requirements - Large Font

**Available Languages:** Armenian, Chinese, English, Spanish

---

## KEY ASSESSMENT TYPES

1. **Initial** - First assessment for new applicant
2. **Change** - Assessment due to change in circumstances
3. **Reassessment** - Periodic reassessment (typically annual)
4. **Inter-County Transfer** - Assessment when recipient transfers between counties
5. **Telehealth** - Assessment conducted via telehealth

---

## IMPORTANT TIMEFRAMES

### Health Care Certification:
- **Initial Due Date:** 45 calendar days from form request/generation
- **Good Cause Extension:** Additional 45 days (total 90 days)
- **Grace Period:** 10 business days after due date for data entry (internal)
- **Task Trigger 1:** 10 business days before due date
- **Task Trigger 2:** 1 business day before due date
- **SOC 873 Mailed/Given Edit Window:** 10 business days from print date

### Reassessment:
- **Standard Reassessment Due:** 1 year from Home Visit Date

---

**Document Generated:** December 10, 2025  
**Source:** _Release_2025_03_01__DSD_Section_21.pdf
