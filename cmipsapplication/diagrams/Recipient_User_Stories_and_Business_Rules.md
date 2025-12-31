# IHSS CMIPS - Recipient User Stories and Business Rules
## Release 2025.03.01 - DSD Section 20

---

## TABLE OF CONTENTS
1. [User Stories / Functional Requirements](#user-stories)
2. [Business Rules (1-70)](#business-rules)

---

## USER STORIES / FUNCTIONAL REQUIREMENTS {#user-stories}

The following functional requirements serve as the user stories for the IHSS CMIPS Recipient Case Management and Online Search system:

### 1. Person, Case and State Hearing Search

**As a** CMIPS user  
**I want to** search for persons, cases, and state hearings in the system  
**So that** I can determine if a person exists and access their information

**Acceptance Criteria:**
- Search can be performed by:
  - Last name (full or partial, with Soundex support)
  - Social Security Number (SSN)
  - Client Index Number (CIN)
  - Address (Street Number, Street Name, Unit Type, Unit Number, City)
  - Phone number
  - Email address
  - Provider Number

- Search results display:
  - Last Name, First Name, MI
  - SSN
  - Type (Alternative ID SSN Type)
  - CIN (for Applicant or Recipient only)
  - Date of Birth
  - Gender
  - Person Type (Recipient or Provider)
  - Address
  - City
  - County

- A person can exist only once in CMIPS but may have different roles
- Cases have a unique CMIPS case number that follows the recipient if they move counties
- State Hearing search displays cases by State Hearing status

### 2. Initial Contact and Intake Application

**As a** County staff member  
**I want to** create referrals and applications for potential IHSS recipients  
**So that** individuals can be registered in the system and begin the application process

**Acceptance Criteria:**
- If person not found in search, can create:
  - **Referral**: When caller is not the individual being referred
  - **Application**: When caller is the applicant themselves

- Person Types supported:
  - Open-Referral
  - Closed-Referral
  - Applicant
  - Recipient
  - Provider

- Address verification and standardization occurs for both residence and mailing addresses
- Changes to person records are date-bound and create history records
- An individual may be both a Provider and another Person Type

### 3. Create Referral (Recipient)

**As a** County staff member receiving a referral  
**I want to** create a minimal referral record for someone referred for IHSS  
**So that** the county can follow up with the individual

**Acceptance Criteria:**
- Required data for referral:
  - First and Last names
  - Referral source
  - Either person's address OR telephone number

- Person Type is set to "Open-Referral" when saved
- County staff will contact referred individual to determine interest
- Open-Referral can be:
  - Converted to Applicant if interested
  - Closed (becomes "Closed-Referral")
  - Re-opened if previously closed

### 4. Create Case

**As a** County staff member  
**I want to** create an IHSS case for an applicant  
**So that** the application process can begin and services can be tracked

**Acceptance Criteria:**
- System searches Statewide Client Index (SCI) for CIN
- If no CIN exists:
  - IHSS Referral for Medi-Cal Eligibility sent to County SAWS
  - County SAWS returns CIN to CMIPS
  - CIN becomes key identifier across SAWS, MEDS, and CMIPS

- If CIN exists:
  - User selects CIN
  - System retrieves MEDS data for case processing

- Case is assigned to a case owner (actual owner or supervisor)
- System generates task notification for case assignment
- Person Type changes from "Open-Referral" to "Applicant"
- Case Status is "Pending" until services are granted or denied
- Person Type changes to "Recipient" when services are granted

### 5. Person Notes Management

**As a** CMIPS user  
**I want to** create, view, edit, and manage notes on person records  
**So that** important information about individuals is documented and accessible

**Acceptance Criteria:**
- Notes are listed in chronological order (most recent first)
- Notes can be:
  - Created
  - Viewed
  - Edited (by appending additional notes, not modifying original)
  - Cancelled (changes status but note remains viewable)

- Both active and cancelled notes are viewable
- Note history is preserved

---

## BUSINESS RULES {#business-rules}

### Business Rules 1-10

#### BR OS 01 - CIN Selection
**When:** CIN is selected from SCI during Case creation process  
**Then:** CMIPS will save MEDS information associated with the OM Transaction (Medi-Cal Eligibility transaction)  
**Status:** IMPLEMENTED

#### BR OS 02 - Person Search by SSN
**When:** The Search button is selected AND SSN is entered (regardless of other fields)  
**Then:** The search will be executed. Only the SSN is used as the search criteria and other entered search criteria will be ignored  
**Status:** IMPLEMENTED

#### BR OS 03 - Person Search by CIN
**When:** SSN is not entered AND CIN is entered (regardless of other fields)  
**Then:** The search will perform. Only CIN is used as the search criteria and other entered search criteria will be ignored  
**Status:** IMPLEMENTED

#### BR OS 04 - Person Search by Address
**When:** 
- SSN is not entered
- CIN is not entered
- Provider Number is not entered
- Full or partial last name is not entered
- Complete or incomplete address is entered

**Then:** The search will perform. All search criteria (address and other search criteria) will be used together to do the search  
**Status:** IMPLEMENTED

#### BR OS 05 - Person Search by All Criteria
**When:** 
- SSN is not entered
- CIN is not entered
- Provider Number is not entered
- Full or partial last name is or is not entered
- Complete address is or is not entered

**Then:** The search will perform. All search criteria will be used together to do the search  
**Status:** IMPLEMENTED

#### BR OS 06 - Create Alternative ID - Reset Blank SSN Reason
**When:** Save link is selected on Create Alternative ID screen AND Type is Social Security Number AND Alternative ID field is not blank AND associated Person Record has "Blank SSN Reason" of Applied for SSN  
**Then:** 
- Allow the save action
- Reset the Person Home "Blank SSN Reason" field to "blank"
- The value that matches the SSN entered will display

**Status:** IMPLEMENTED

#### BR OS 07 - Create Phone Number - Primary Phone
**When:** Save link is selected on "Create Phone Number" screen AND "Primary" is indicated AND another "Phone Number" indicates "Primary"  
**Then:** 
- Indicate the current record as "Primary"
- Deselect "Primary" on the previous "Phone Number" record

**Status:** IMPLEMENTED

#### BR OS 08 - Modify Phone Number - Primary Phone
**When:** Save link is selected on "Modify Phone Number" screen AND "Primary" is indicated AND an existing "Phone Number" screen indicates "Primary"  
**Then:** 
- Indicate the current record as "Primary"
- Deselect "Primary" on the previous "Phone Number" record

**Status:** IMPLEMENTED

#### BR OS 09 - SAWS Send - No or Inactive Medi-Cal
**When:** Save link is selected on "Create Case" screen for a Recipient with either no Medi-Cal or without active Medi-Cal  
**Then:** Send SAWS S1 (CMSD4XXB) "IHSS Referral for Medi-Cal determination"

**Definitions:**
- No Medi-Cal: SCI OI response return code = 100 (No match found)
- Without active Medi-Cal: The Medi-Cal eligibility status in the SCI OM response contains a 9 in both the first and third digits

**Status:** IMPLEMENTED

#### BR OS 10 - SAWS Receive - CIN Update
**When:** S2 transaction is received from SAWS in response to S1 transaction AND CIN received does not match the CIN on the Person record AND CIN received does not match a CIN on another Person record in CMIPS  
**Then:** 
- Update the CIN on the Person record
- Perform CIN re-clearance and Medi-Cal Eligibility selection
- Send MEDS IH18 (CMDS103C) – Pending Application

**Status:** IMPLEMENTED

---

### Business Rules 11-20

#### BR OS 11 - Update to Applicant's Demographic Data
**When:** There is an update to the IHSS applicant's demographic data (via Save on Modify Person, Change on Case Home, Modify Phone Number, or Maintain Address screens)  
**Then:** Send MEDS IH12 (CMDS103C) – Update Client Information  
**Status:** IMPLEMENTED

#### BR OS 12 - IHSS Application Denied
**When:** Case status changes from Pending to Denied  
**Then:** Send MEDS IH34 (CMDS103C) – Update Application Data  
**Status:** IMPLEMENTED

#### BR OS 13 - IHSS Application Created with Active Medi-Cal
**When:** Save link is selected on "Create Case" screen AND CIN selected has active Medi-Cal  
**Then:** Send MEDS IH18 (CMDS103C) – Pending Application

**Definition:** Active Medi-Cal = Any Medi-Cal eligibility status in the SCI OM response contains other than a 9 in both the first and third digits  
**Status:** IMPLEMENTED

#### BR OS 14 - Modify Person - Send to SCI
**When:** Save link is selected on Modify Person screen AND any of the following fields have been modified: Last Name, First Name, Middle Initial, Date of Birth, Gender  
**Then:** Send OU transaction to SCI  
**Status:** IMPLEMENTED

#### BR OS 15
**Status:** CANCELLED (Cancelled by ASR Sprint 43 Team 1&2)

#### BR OS 16 - SAWS Send - Active Medi-Cal
**When:** Save link is selected on "Create Case" screen for a Recipient with active Medi-Cal  
**Then:** Send S8 (SMDS4XXB) Notification of IHSS "Pending" status

**Definition:** Active Medi-Cal = Medi-Cal eligibility status in the SCI OM response contains other than a nine (9) in both the first and third digits AND Medi-Cal Aid Codes is other than 10, 20 or 60  
**Status:** IMPLEMENTED

#### BR OS 17 - Create Referral with Provider Person Type
**When:** Save link is selected on Create Referral screen AND current record has a Person Type of Provider  
**Then:** Add and display second Person Type of Open Referral  
**Status:** IMPLEMENTED

#### BR OS 18 - Create Case with Provider Person Type
**When:** Save link is selected on Create Case screen AND current record has a Person Type of Provider  
**Then:** Add and display the second Person Type of Applicant  
**Status:** IMPLEMENTED

#### BR OS 19 - Create Case with Open Referral Person Type
**When:** Save link is selected on Create Case screen AND current record has a Person Type of Open Referral  
**Then:** Update the Open Referral Person Type to Applicant  
**Status:** IMPLEMENTED

#### BR OS 20 - Person Search by Provider Number
**When:** 
- SSN is not entered
- CIN is not entered
- Provider Number is entered
- Full or partial last name is or is not entered (minimum 2 characters)
- Complete or incomplete address is not entered

**Then:** The search will perform. Only the Provider Number is used as the search criteria and other entered search criteria will be ignored  
**Status:** IMPLEMENTED

---

### Business Rules 21-30

#### BR OS 21 - Create Alternative ID - Duplicate SSN
**When:** Save link is selected on Create Alternative ID screen AND Type is Social Security Number AND Duplicate field is indicated as Duplicate SSN  
**Then:** Set the Person record Duplicate field to "Duplicate SSN"  
**Status:** IMPLEMENTED

#### BR OS 22 - Create Alternative ID - Suspect SSN
**When:** Save link is selected on Create Alternative ID screen AND Type is Social Security Number AND Duplicate field is indicated as Suspect SSN  
**Then:** 
- Set the Person record Duplicate field to Suspect SSN
- The indicated SSN shall be "released" for use in the CMIPS application (Released SSN allows Alternative ID - Type SSN to be used as SSN on another Person record. If associated Person record is Type Provider, all Provider/Recipient relationships must be terminated. If Type Applicant or Recipient, case must be "Withdrawn, Denied or Terminated")

**Status:** IMPLEMENTED

#### BR OS 23-27
**Status:** CANCELLED (Cancelled by ASR Sprint 43 Team 1&2)

#### BR OS 28 - Create Referral - Display Name in Uppercase
**When:** Save link is selected on Create Referral screen AND lowercase letters were used when entering Last Name, First Name, or Middle Name  
**Then:** Display the First Name, Last Name and Middle Name entries in all uppercase letters on the Person Home screen  
**Status:** IMPLEMENTED

#### BR OS 29 - Create Application - Display Name in Uppercase
**When:** Save link is selected on Create Applicant screen AND lowercase letters were used when entering Last Name, First Name, or Middle Name  
**Then:** Display the First Name, Last Name and Middle Name entries in all uppercase letters on the Person Home screen  
**Status:** IMPLEMENTED

#### BR OS 30 - Modify Person - Display Name in Uppercase
**When:** Save link is selected on Modify Person screen AND any of Last Name, First Name, or Middle Name were modified using lowercase letters  
**Then:** Display the First Name, Last Name and Middle Name entries in all uppercase letters on the Person Home screen  
**Status:** IMPLEMENTED

---

### Business Rules 31-40

#### BR OS 31 - Create Alternate ID - Set From Date
**When:** Create Alternate ID screen displays  
**Then:** Set the "From" field to the current date  
**Status:** IMPLEMENTED

#### BR OS 32 - SCI CIN Search with CIN on Case
**When:** CIN Clearance is processed AND Alternate ID Type Client Index Number is indicated  
**Then:** 
- If Medi-Cal Pseudo field is NOT checked: Send Client Index Number, First Name, Last Name, Date of Birth, Gender and Social Security Number (if present) as SCI search criteria
- If Medi-Cal Pseudo field IS checked: Send Client Index Number, First Name, Last Name, Date of Birth, and Gender (if present) as SCI search criteria

**Notes:**
- If CIN is more than 9 characters, send only first 9 characters
- CIN may be alphanumeric value

**Status:** IMPLEMENTED

#### BR OS 33 - SCI CIN Search - No CIN or SSN on Case
**When:** CIN Clearance is processed AND neither Alternate ID Type Client Index Number nor Alternate ID Type Social Security Number is indicated  
**Then:** Send Person First Name, Last Name, Date of Birth, and Gender as SCI search criteria  
**Status:** IMPLEMENTED

#### BR OS 34 - Merge Duplicate SSN - Verify Link
**When:** Verify link is selected on Merge Duplicate SSN screen AND no errors are encountered  
**Then:** Change the Verify link to "Save"  
**Status:** NOT EXPLICITLY STATED

#### BR OS 35 - Merge Duplicate SSN - Make Master (Recipient Case)
**When:** Verify link is selected on Merge Duplicate SSN screen AND all errors resolved AND:
- Master Record has Alternative ID Type of Conversion Duplicate
- Person Type is Applicant or Recipient with case status: Pending, Eligible, Presumptive Eligible or Leave
- Any Duplicate record has Alternative ID Type of Social Security Number with blank Duplicates field
- Person Type is Applicant or Recipient with case status: Application Withdrawn, Terminated, or Denied

**Then:** 
- Change Verify link to "Save"
- Allow user to select "Make Master" checkbox
- Display message: "[Case Number] is currently a Master record. If you are sure you want the currently indicated case [Master Record Case] to be indicated as the Master Record, select the 'Make Master' checkbox."

**Status:** NOT EXPLICITLY STATED

#### BR OS 36 - Merge Duplicate SSN - Make Master (Provider)
**When:** Verify link is selected on Merge Duplicate SSN screen AND all errors resolved AND:
- Master Record has Alternative ID Type of Conversion Duplicate
- Person Type is Provider with current Provider Status Active
- Any Duplicate record has Alternative ID Type of Social Security Number with blank Duplicates field
- Person Type is Provider with View Provider Details Provider Status "Terminated"

**Then:** 
- Change Verify link to "Save"
- Allow user to select "Make Master" checkbox
- Display message: "[Provider Number] is currently a Master record. If you are sure you want the currently indicated Provider record [Master Record Provider Number] to be indicated as the Master Record, select the 'Make Master' checkbox."

**Status:** NOT EXPLICITLY STATED

#### BR OS 37 - Merge Duplicate SSN - Save Action
**When:** Save link is selected on Merge Duplicate SSN screen  
**Then:** Update Alternative ID Type Social Security Number:
- If current Alternative ID Type of Social Security Number does not exist for Master Record:
  - Add Alternative ID Type of Social Security Number with:
    - Alternative ID = SSN indicated on Merge Duplicate SSN screen
    - Type = Social Security Number
    - Duplicates field = blank
    - If Blank SSN Reason indicated, clear the field
    
- For all records indicated as Duplicate SSN, create Alternative ID:
  - Alternative ID = SSN indicated on Merge Duplicate SSN screen
  - Type = Social Security Number
  - Duplicates field = Duplicate SSN
  - If Blank SSN Reason indicated, clear the field
  - Comment field: "[User County] County indicated this record as Duplicate SSN and is no longer usable. See [Master Record] for usable record."
  
- If record being merged is a CMIPS case:
  - Clear current County Use field
  - Populate County Use field with: "[Current Date] [User County] County indicated this record as Duplicate SSN and is no longer usable. See [Master Record] for usable case."

**Status:** NOT EXPLICITLY STATED

#### BR OS 38-40 - Merge Duplicate SSN (Additional Rules)
Similar to BR OS 34-37 but for different scenarios  
**Status:** IMPLEMENTED

---

### Business Rules 41-50

#### BR OS 41 - Merge Duplicate SSN - Save Action (Alternate)
Same as BR OS 37 with different implementation details  
**Status:** IMPLEMENTED

#### BR OS 42 - Re-open Closed Referral
**When:** Save link is selected on Re-open Referral screen AND current record has Person Type of Closed Referral  
**Then:** 
- Update Person Type to Open Referral
- Update Person Home County to user's County
- Update Referral Date to current date
- Update Referral Source to indicated Referral Source

**Status:** IMPLEMENTED

#### BR OS 43
**Status:** Business Rule Removed with CR 1183

#### BR OS 44 - Create Application - Update Person Contact
**When:** Create Case link is selected on Create Application screen AND any of the following have been updated: Residence Address, Mailing Address, Phone Number, Email Address  
**Then:** Allow save action and create new record in appropriate Person Contact with From Date set to current date  
**Status:** IMPLEMENTED

#### BR OS 45 - Create Case - Update Person Contact From Date
**When:** Save link is selected on Create Case screen AND IHSS Referral Date has been modified AND any Person Contact records exist (Residence Address, Mailing Address, Phone Number, Email Address)  
**Then:** Update the From Date on all existing Person Contacts to the IHSS Referral Date  
**Status:** IMPLEMENTED

#### BR OS 46 - Create Email Address
**When:** Save link is selected on Create Email Address Screen  
**Then:** 
- Save new email address record with:
  - Begin Date = Current Date
  - Status = Active
  - End Date = blank
  
- If active email address currently exists:
  - If Begin Date on that record is NOT current date: Set End Date to current date - 1, Set status to "Inactive"
  - If Begin Date on that record IS current date: Set End Date to Current Date, Set status to "Inactive"
  
- If person is registered to IHSS Website: Send AMQP modifyEmail to CMIPS Message Broker

- If Recipient with E-Timesheet Option "IHSS Website" OR Provider with E-Timesheet Status "Enrolled" or "Request Pending":
  - Generate Notification of Email Address Change (ETSE21) to both old and new email addresses with date of change

**Status:** IMPLEMENTED

#### BR OS 47
**Status:** CANCELLED (Cancelled by ASR 1287 – Sprint 33)

#### BR OS 48 - Inactivate Email Address
**When:** Yes link is selected on Inactivate Email Address screen  
**Then:** 
- Save record with current date as End Date
- Set status of email address record to Inactive

**Status:** IMPLEMENTED

#### BR OS 49 - Modify Person - Written Language (English)
**When:** Recipient's Written Language is updated to English AND Blind or Visually Impaired Notice of Action Option is Large Font, Audio CD, or Data CD  
**Then:** Update Blind or Visually Impaired Notice of Action Option Language to English  
**Status:** IMPLEMENTED

#### BR OS 50 - Modify Person - Written Language (Spanish)
**When:** Recipient's Written Language is updated to Spanish AND Blind or Visually Impaired Notice of Action Option is Large Font, Audio CD, or Data CD  
**Then:** Update Blind or Visually Impaired Notice of Action Option Language to Spanish  
**Status:** IMPLEMENTED

---

### Business Rules 51-60

#### BR OS 51 - Modify Person - Written Language (Chinese)
**When:** Recipient's Written Language is updated to Cantonese or Mandarin AND Blind or Visually Impaired Notice of Action Option is Large Font or Data CD  
**Then:** Update Blind or Visually Impaired Notice of Action Option Language to Chinese  
**Status:** IMPLEMENTED

#### BR OS 52 - Modify Person - Written Language (Armenian)
**When:** Recipient's Written Language is updated to Armenian AND Blind or Visually Impaired Notice of Action Option is Large Font or Data CD  
**Then:** Update Blind or Visually Impaired Notice of Action Option Language to Armenian  
**Status:** IMPLEMENTED

#### BR OS 53 - Modify Person - Written Language (Non-threshold)
**When:** Recipient's Written Language is updated to a non-threshold language AND Blind or Visually Impaired Notice of Action Option is Large Font, Audio CD, or Data CD  
**Then:** Blind or Visually Impaired Notice of Action Option Language will be updated to or remain English  
**Status:** IMPLEMENTED

#### BR OS 54 - Modify Person - Written Language (Other Languages)
**When:** Recipient's Written Language is updated to anything other than English, Spanish, Cantonese, Mandarin, or Armenian AND Blind or Visually Impaired Notice of Action Option is Large Font or Data CD  
**Then:** Update Blind or Visually Impaired Notice of Action Option Language to English  
**Status:** IMPLEMENTED

#### BR OS 55 - Modify Person - Written Language for IHSS Forms (English)
**When:** Recipient's Written Language is updated to English AND Blind or Visually Impaired IHSS Required Forms Option is Large Font, Audio CD, or Data CD  
**Then:** Update Blind or Visually Impaired IHSS Required Forms Option Language to English  
**Status:** IMPLEMENTED

#### BR OS 56 - Modify Person - Written Language for IHSS Forms (Spanish)
**When:** Recipient's Written Language is updated to Spanish AND Blind or Visually Impaired IHSS Required Forms Option is Large Font, Audio CD, or Data CD  
**Then:** Update Blind or Visually Impaired IHSS Required Forms Option Language to Spanish  
**Status:** IMPLEMENTED

#### BR OS 57 - Modify Person - Written Language for IHSS Forms (Chinese)
**When:** Recipient's Written Language is updated to Cantonese or Mandarin AND Blind or Visually Impaired IHSS Required Forms Option is Large Font or Data CD  
**Then:** Update Blind or Visually Impaired Notice of Action Option Language to Chinese  
**Status:** IMPLEMENTED

#### BR OS 58 - Modify Person - Written Language for IHSS Forms (Armenian)
**When:** Recipient's Written Language is updated to Armenian AND Blind or Visually Impaired IHSS Required Forms Option is Large Font or Data CD  
**Then:** Update Blind or Visually Impaired IHSS Required Forms Option Language to Armenian  
**Status:** IMPLEMENTED

#### BR OS 59 - Modify Person - Written Language for IHSS Forms (Non-threshold)
**When:** Recipient's Written Language is updated to a non-threshold language AND Blind or Visually Impaired IHSS Required Forms Option is Large Font, Audio CD, or Data CD  
**Then:** Blind or Visually Impaired IHSS Required Forms Option will be updated to or remain English  
**Status:** IMPLEMENTED

#### BR OS 60 - Modify Person - Written Language for IHSS Forms (Other)
**When:** Recipient's Written Language is updated to anything other than English, Spanish, Cantonese, Mandarin, or Armenian AND Blind or Visually Impaired IHSS Required Forms Option is Large Font or Data CD  
**Then:** Update Blind or Visually Impaired IHSS Required Forms Option Language to English  
**Status:** IMPLEMENTED

---

### Business Rules 61-70

#### BR OS 61 - Update IHSS ESP Written Language
**When:** Written Language for a Recipient or Provider is successfully updated AND person is registered to IHSS Website  
**Then:** Send AMQP modifyWrittenLanguage (IWOM885C) web service to CMIPS Message Broker to update ESP written language  
**Status:** IMPLEMENTED

#### BR OS 62-64
**Status:** CANCELLED (Cancelled by CR170)

#### BR OS 65 - Create Alternative ID - Add Taxpayer ID for Recipient
**When:** Save link is selected on Create Alternative ID pop-up AND:
- Person Type is Applicant OR Recipient OR Provider with Eligible = Yes
- Case Status is Pending, Eligible, Presumptive Eligible or On-Leave
- Alternative ID field is populated
- Type = Taxpayer ID
- Blank SSN Reason field is blank
- No errors encountered

**Then:** 
- If existing Alternative ID record with Type = Taxpayer ID exists, set 'To' date to day prior to current date
- Create new Alternative ID record:
  - Alternative ID = entered value
  - Type = Taxpayer ID
  - From date = current system date (To date = 12/31/9999)
  - Blank SSN Reason = blank
  - Capture username for Changed By
  - Save Comments
- Close Create Alternative ID pop-up, return to Alternative IDs list screen
- Note: Web service to Advantage is not invoked

**Status:** IMPLEMENTED

#### BR OS 66 - Create Alternative ID - Remove SSN
**When:** Save link is selected on Create Alternative ID pop-up AND:
- Alternative ID field is blank
- Type = Social Security Number
- Blank SSN Reason = Applied For SSN
- No errors encountered

**Then:** 
**CM:**
- For existing Alternative ID record with Type = Social Security Number:
  - Set 'To' date to day prior to current date
- Create new Alternative ID record:
  - Alternative ID = blank (NULL)
  - Type = Social Security Number
  - From date = current system date
  - Blank SSN Reason = 'Applied For SSN'
  - Capture username for Changed By
  - Save Comments
- Close Create Alternative ID pop-up, return to Alternative IDs list screen
- Webservice (PROO923A) to Advantage with blank SSN and Blank SSN Reason

**ADV:**
- Process and apply SSN update to remove SSN and indicate Blank SSN Reason as Applied For SSN

**Status:** IMPLEMENTED

#### BR OS 67 - Addresses - Update Address
**When:** Save link is selected on Create Address pop-up AND Save is successful AND No errors encountered  
**Then:** 
- New address "From" date is set to current date
- New address "To" date is set to blank
- Old address "To" date is set to date prior to current date
- If address is Recipient's Residential address, it is marked as pending in GeocodeAddressTrigger table

**Status:** IMPLEMENTED

#### BR OS 68 - Print Person Notes Form - Generate via Nightly Batch
**When:** User selects Save button on Print Person Notes Form screen AND Print type = Print in Nightly Batch AND person type is Provider  
**Then:** Case Management creates new record on Provider Forms screen displaying:
- Action: Displays View and Edit links
- Name: Displays Person Notes
- Language: Displays English
- Status: Displays Pending
- Date Time: Display date and time record created in system

Note: When person is both provider and recipient, Recipient ID on PDF file takes precedent; Provider ID is blank

**Status:** IMPLEMENTED

#### BR OS 69 - Print Person Notes Form - Generate on Local Printer
**When:** User selects Save button on Print Person Notes Form screen AND Print type = Print Now on CMIPS II Printer or Generate Now for Local Print  
**Then:** Case Management creates new record on Provider Forms screen displaying:
- Action: Displays View, Edit, and Print links
- Name: Displays Person Notes
- Language: Displays English
- Status: Displays Printed
- Date Time: Display date and time record created in system
- System launches PDF form document when user clicks Print link
- Form document printed to requestor's local county printer when request is from Print type = Print Now on CMIPS II Printer

Note: When person is both provider and recipient, Recipient ID on PDF file takes precedent; Provider ID is blank

**Status:** IMPLEMENTED

#### BR OS 70 - Modify/Update Mailing/Residential Address Validations
**When:** CMIPS user enters special character in address field via copy/paste AND user selects Verify button on any impacted Address screens  
**Then:** Case Management will:
- Refine data entered to remove special characters
- Convert special characters into keyboard recognized characters
- Ensure UTF-8 encoding is used to process and store addresses

**Special characters NOT allowed (will be removed):**
- Bullet Point (•), Bullet Circle (○), Bullet Square (■), Bullet Diamond (◆), Bullet Arrow (➔)
- Check Mark (✓), Ellipsis (…), em-dash (–), Tab
- Exclamation (!), Quotation Mark ("), Ampersand (&), Open/Left Parenthesis (()
- Asterisk (*), Plus (+), Colon (:), Semi Colon (;)
- Greater Than (>), Question Mark (?), At (@)
- Closed/Right Square Bracket (]), Underscore (_), Grave Accent (`)
- Two or more continuous spaces or special characters
- Single quote/Apostrophe (')

**Special characters ALLOWED:**
- Space, Slash (/), Hyphen (-), Pound sign (#), Comma (,), Period (.)

**Field-specific limitations:**
- Establishment Name, Number and Street Name: Space, Slash, Hyphen, Pound sign, Comma, Period
- Unit Number: Space, Slash, Hyphen, Pound sign, Period
- City: Space, Hyphen
- Zip Code: Hyphen

**Status:** IMPLEMENTED

---

## SUMMARY

**Total User Stories / Functional Requirements:** 5 main functional areas
**Total Business Rules:** 70 (with some cancelled)
**Implementation Status:** Majority IMPLEMENTED
**Release:** 2025.03.01
**Document Section:** DSD Section 20
**System:** IHSS CMIPS - Recipient Case Management & Online Search

---

## KEY INTERFACES

### Internal Interfaces:
- PROO901A - Create Recipient in Payroll
- PROO922A - Update Person Request
- PROO923A - Update Person SSN
- PROO924A - Update Person Address
- PR00925A - Update Recipient Worker Number

### External Interfaces:
- CMOO106A - SCI Real Time
- IWOM885C - IHSS ESP CM Written Language AMQP Update Msg
- CMSD4XXB - SAWS Interface
- CMDS103C - MEDS Interface

---

**Document Generated:** December 10, 2025  
**Source:** _Release_2025_03_01__DSD_Section_20.pdf
