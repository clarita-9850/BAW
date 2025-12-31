# Available Fields for Field Visibility Testing

## Database Schema Fields (from `timesheets` table)

### Core Timesheet Fields
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `id` | id | bigint | Timesheet ID |
| `timesheetid` | id (legacy) | String | Timesheet ID (legacy format) |
| `employeeid` | employee_id | String | Employee ID |
| `employeename` | employee_name | String | Employee Name |
| `userid` | user_id | String | User ID |
| `department` | department | String | Department |
| `location` | location | String | Location |
| `servicelocation` | location (legacy) | String | Service Location (legacy) |

### Date Fields
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `payperiodstart` | pay_period_start | Date | Pay Period Start Date |
| `payperiodend` | pay_period_end | Date | Pay Period End Date |
| `startdate` | pay_period_start (legacy) | Date | Start Date (legacy) |
| `enddate` | pay_period_end (legacy) | Date | End Date (legacy) |

### Hours Fields
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `regularhours` | regular_hours | Numeric | Regular Hours |
| `overtimehours` | overtime_hours | Numeric | Overtime Hours |
| `sickhours` | sick_hours | Numeric | Sick Hours |
| `vacationhours` | vacation_hours | Numeric | Vacation Hours |
| `holidayhours` | holiday_hours | Numeric | Holiday Hours |
| `totalhours` | total_hours | Numeric | Total Hours |

### Status & Comments
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `status` | status | String | Timesheet Status (DRAFT, SUBMITTED, APPROVED, etc.) |
| `comments` | comments | String | Comments |
| `supervisorcomments` | supervisor_comments | String | Supervisor Comments |

### Timestamp Fields
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `submittedat` | submitted_at | Timestamp | Submitted At |
| `submittedby` | submitted_by | String | Submitted By |
| `approvedat` | approved_at | Timestamp | Approved At |
| `approvedby` | approved_by | String | Approved By |
| `createdat` | created_at | Timestamp | Created At |
| `updatedat` | updated_at | Timestamp | Updated At |

### Provider Fields (Legacy Mapping)
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `providerid` | employee_id (mapped) | String | Provider ID (maps to employee_id) |
| `providername` | employee_name (mapped) | String | Provider Name (maps to employee_name) |
| `providergender` | provider_gender | String | Provider Gender |
| `providerethnicity` | provider_ethnicity | String | Provider Ethnicity |
| `provideragegroup` | provider_age_group | String | Provider Age Group |
| `providerdateofbirth` | provider_date_of_birth | Date | Provider Date of Birth |

### Recipient Fields
| Field Name (Keycloak) | Database Column | Type | Description |
|----------------------|----------------|------|-------------|
| `recipientgender` | recipient_gender | String | Recipient Gender |
| `recipientethnicity` | recipient_ethnicity | String | Recipient Ethnicity |
| `recipientagegroup` | recipient_age_group | String | Recipient Age Group |
| `recipientdateofbirth` | recipient_date_of_birth | Date | Recipient Date of Birth |

## Quick Reference: All Field Names (for Keycloak Configuration)

### Recommended Fields for Testing (Most Common)
```
id
employeeid
employeename
department
location
payperiodstart
payperiodend
totalhours
status
comments
submittedat
approvedat
createdat
```

### All Available Fields (Complete List)
```
id
timesheetid
employeeid
employeename
userid
department
location
servicelocation
payperiodstart
payperiodend
startdate
enddate
regularhours
overtimehours
sickhours
vacationhours
holidayhours
totalhours
status
comments
supervisorcomments
submittedat
submittedby
approvedat
approvedby
createdat
updatedat
providerid
providername
providergender
providerethnicity
provideragegroup
providerdateofbirth
recipientgender
recipientethnicity
recipientagegroup
recipientdateofbirth
```

## Field Visibility Configuration Format (for Keycloak)

When configuring in Keycloak, use this format in the role's `field_masking_rules` attribute:

```
fieldName:maskingType:accessLevel:enabled
```

### Example Configurations:

**Full Access (no masking):**
```
employeeid:NONE:FULL_ACCESS:true
employeename:NONE:FULL_ACCESS:true
totalhours:NONE:FULL_ACCESS:true
```

**Masked Access:**
```
employeeid:HASH_MASK:MASKED_ACCESS:true
employeename:ANONYMIZE:MASKED_ACCESS:true
totalhours:AGGREGATE:MASKED_ACCESS:true
```

**Hidden Access:**
```
providerdateofbirth:HIDDEN:HIDDEN_ACCESS:true
recipientdateofbirth:HIDDEN:HIDDEN_ACCESS:true
```

### Masking Types Available:
- `NONE` - No masking (show original value)
- `HIDDEN` - Hide completely
- `PARTIAL_MASK` - Show partial data (e.g., XXX-XX-1234)
- `HASH_MASK` - Show hash value
- `ANONYMIZE` - Replace with generic value
- `AGGREGATE` - Show aggregated data only

### Access Levels Available:
- `FULL_ACCESS` - Show complete data
- `MASKED_ACCESS` - Show masked data
- `HIDDEN_ACCESS` - Hide field completely

## Testing Recommendations

For testing field visibility, select a mix of fields:

1. **Basic Info Fields:**
   - `id`, `employeeid`, `employeename`, `department`, `location`

2. **Time Fields:**
   - `payperiodstart`, `payperiodend`, `totalhours`

3. **Status Fields:**
   - `status`, `comments`, `submittedat`, `approvedat`

4. **Sensitive Fields (for masking tests):**
   - `providerdateofbirth`, `recipientdateofbirth`
   - `providergender`, `recipientgender`
   - `providerethnicity`, `recipientethnicity`

