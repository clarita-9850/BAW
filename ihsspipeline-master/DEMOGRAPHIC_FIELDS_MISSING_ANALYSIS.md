# Demographic Fields Missing Analysis Report

**Date**: November 29, 2025  
**System**: Timesheet Management System

## Executive Summary

The frontend analytics and visualization pages are attempting to use demographic fields (gender, ethnicity, age) that **do not exist** in the current database schema. This report identifies all missing fields and provides recommendations for implementation.

---

## 1. Missing Fields Identified

### 1.1 Provider Demographic Fields

| Field Name | Frontend Usage | Backend Endpoint | Database Status | Priority |
|------------|---------------|------------------|-----------------|----------|
| `provider_gender` / `providerGender` | ✅ Used in visualization | ✅ `/demographics/gender` | ❌ **MISSING** | High |
| `provider_ethnicity` / `providerEthnicity` | ✅ Used in visualization | ✅ `/demographics/ethnicity` | ❌ **MISSING** | High |
| `provider_age_group` / `providerAgeGroup` | ✅ Used in analytics | ✅ `/demographics/age` | ❌ **MISSING** | High |
| `provider_date_of_birth` | ❌ Not used | ❌ Not available | ❌ **MISSING** | Medium |

### 1.2 Recipient Demographic Fields

| Field Name | Frontend Usage | Backend Endpoint | Database Status | Priority |
|------------|---------------|------------------|-----------------|----------|
| `recipient_gender` / `recipientGender` | ✅ Used in visualization | ✅ `/demographics/gender` | ❌ **MISSING** | High |
| `recipient_ethnicity` / `recipientEthnicity` | ✅ Used in visualization | ✅ `/demographics/ethnicity` | ❌ **MISSING** | High |
| `recipient_age_group` / `recipientAgeGroup` | ✅ Used in analytics | ✅ `/demographics/age` | ❌ **MISSING** | High |
| `recipient_date_of_birth` | ❌ Not used | ❌ Not available | ❌ **MISSING** | Medium |

---

## 2. Frontend Usage Analysis

### 2.1 Visualization Page (`app/visualization/page.tsx`)

**Location**: Lines 350-375

**Code Snippet**:
```typescript
const demographicBreakdowns = useMemo(() => {
  const genderCounts: Record<string, number> = {};
  const ethnicityCounts: Record<string, number> = {};

  dataset.rows.forEach((row: any) => {
    const providerGender = row.provider_gender || row.providerGender;
    const recipientGender = row.recipient_gender || row.recipientGender;
    const providerEthnicity = row.provider_ethnicity || row.providerEthnicity;
    const recipientEthnicity = row.recipient_ethnicity || row.recipientEthnicity;

    if (providerGender) genderCounts[providerGender] = (genderCounts[providerGender] || 0) + 1;
    if (recipientGender) genderCounts[recipientGender] = (genderCounts[recipientGender] || 0) + 1;
    if (providerEthnicity) ethnicityCounts[providerEthnicity] = (ethnicityCounts[providerEthnicity] || 0) + 1;
    if (recipientEthnicity) ethnicityCounts[recipientEthnicity] = (ethnicityCounts[recipientEthnicity] || 0) + 1;
  });

  return { gender: genderCounts, ethnicity: ethnicityCounts };
}, [dataset.rows]);
```

**Fields Attempted**:
- ✅ `provider_gender` / `providerGender`
- ✅ `recipient_gender` / `recipientGender`
- ✅ `provider_ethnicity` / `providerEthnicity`
- ✅ `recipient_ethnicity` / `recipientEthnicity`

**UI Display**: 
- Gender Distribution table (lines 871-901)
- Ethnicity Distribution table (lines 903-933)

**Current Behavior**: 
- Tables are rendered but show empty data (no demographic fields in dataset)
- Code gracefully handles missing fields (no errors)

### 2.2 Analytics Service (`lib/services/analytics.service.ts`)

**Interfaces Defined**:
```typescript
export interface DemographicData {
  gender: Array<{ gender: string; count: number }>;
  ethnicity: Array<{ ethnicity: string; count: number }>;
  ageGroup: Array<{ ageGroup: string; count: number }>;
}

export interface AdhocFiltersResponse {
  genders: string[];
  ethnicities: string[];
  ageGroups: string[];
  // ... other fields
}

export interface AdhocBreakdownsResponse {
  gender?: Record<string, number>;
  ethnicity?: Record<string, number>;
  ageGroup?: Record<string, number>;
}

export interface AdhocCrosstabResponse {
  genderEthnicity?: Array<{ gender: string; ethnicity: string; count: number }>;
  genderAge?: Array<{ gender: string; ageGroup: string; count: number }>;
  ethnicityAge?: Array<{ ethnicity: string; ageGroup: string; count: number }>;
}
```

**Methods**:
- `getDemographics()` - Calls `/analytics/demographics`
- `getAdhocFilters()` - Expects `genders`, `ethnicities`, `ageGroups`
- `getAdhocBreakdowns()` - Expects gender, ethnicity, ageGroup breakdowns
- `getAdhocCrosstabs()` - Expects cross-tabulations

---

## 3. Backend Endpoint Analysis

### 3.1 `/api/analytics/demographics/gender`

**Status**: ⚠️ **IMPLEMENTED BUT RETURNS EMPTY DATA**

**Code** (lines 310-354):
```java
// Query gender distribution - NOT AVAILABLE in new schema (no demographic fields)
// Returning empty data as demographic fields were removed in CMIPS schema migration
List<Object[]> providerGender = new ArrayList<>();
List<Object[]> recipientGender = new ArrayList<>();
```

**Returns**: Empty maps for provider and recipient gender distributions

### 3.2 `/api/analytics/demographics/ethnicity`

**Status**: ⚠️ **IMPLEMENTED BUT RETURNS EMPTY DATA**

**Code** (lines 359-401):
```java
// Ethnicity distribution - NOT AVAILABLE in new schema (no demographic fields)
List<Object[]> providerEthnicity = new ArrayList<>();
List<Object[]> recipientEthnicity = new ArrayList<>();
```

**Returns**: Empty maps for provider and recipient ethnicity distributions

### 3.3 `/api/analytics/demographics/age`

**Status**: ⚠️ **IMPLEMENTED BUT RETURNS EMPTY DATA**

**Code** (lines 406-448):
```java
// Age group distribution - NOT AVAILABLE in new schema (no demographic fields)
List<Object[]> providerAge = new ArrayList<>();
List<Object[]> recipientAge = new ArrayList<>();
```

**Returns**: Empty maps for provider and recipient age group distributions

### 3.4 `/api/analytics/adhoc-data`

**Status**: ⚠️ **ACCEPTS PARAMETERS BUT DOESN'T USE THEM**

**Parameters Accepted** (lines 456-461):
- `providerGender` - ❌ Not used in query
- `providerAgeGroup` - ❌ Not used in query
- `providerEthnicity` - ❌ Not used in query
- `recipientGender` - ❌ Not used in query
- `recipientAgeGroup` - ❌ Not used in query
- `recipientEthnicity` - ❌ Not used in query

**Fields Returned**: Does NOT include demographic fields in response

### 3.5 `/api/analytics/adhoc-filters`

**Status**: ⚠️ **RETURNS EMPTY ARRAYS**

**Code** (lines 641-650):
```java
// Genders and ethnicities - NOT AVAILABLE in new schema (no demographic fields)
List<String> genders = new ArrayList<>();
List<String> ethnicities = new ArrayList<>();

// Age groups - NOT AVAILABLE in new schema
List<String> ageGroups = new ArrayList<>();

response.put("genders", genders);
response.put("ethnicities", ethnicities);
response.put("ageGroups", ageGroups);
```

**Returns**: Empty arrays for genders, ethnicities, and ageGroups

### 3.6 `/api/analytics/adhoc-breakdowns`

**Status**: ⚠️ **RETURNS EMPTY MAPS**

**Code** (lines 681-704):
```java
// Get breakdowns - NOT AVAILABLE in new schema (no demographic fields)
List<Object[]> genderBreakdown = new ArrayList<>();
List<Object[]> ethnicityBreakdown = new ArrayList<>();
List<Object[]> ageGroupBreakdown = new ArrayList<>();
```

**Returns**: Empty maps for gender, ethnicity, and ageGroup

### 3.7 `/api/analytics/adhoc-crosstab`

**Status**: ⚠️ **RETURNS EMPTY ARRAYS**

**Code**: Similar pattern - returns empty cross-tabulation data

---

## 4. Database Schema Analysis

### 4.1 Current TimesheetEntity Fields

**Total Fields**: 23  
**Demographic Fields**: **0** ❌

**Available Fields**:
- Employee identification: `employeeId`, `employeeName`, `userId`
- Location: `location`, `department`
- Time: `payPeriodStart`, `payPeriodEnd`, `createdAt`, `updatedAt`, `submittedAt`, `approvedAt`
- Hours: `regularHours`, `overtimeHours`, `sickHours`, `vacationHours`, `holidayHours`, `totalHours`
- Status: `status`
- Comments: `comments`, `supervisorComments`
- Users: `submittedBy`, `approvedBy`

**Missing Demographic Fields**: All 8 fields listed in Section 1

---

## 5. Required Database Fields

### 5.1 Provider Demographic Fields (4 fields)

```sql
-- Add to timesheets table
ALTER TABLE timesheets ADD COLUMN provider_gender VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN provider_ethnicity VARCHAR(100);
ALTER TABLE timesheets ADD COLUMN provider_age_group VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN provider_date_of_birth DATE;
```

**Field Specifications**:

| Field | Type | Nullable | Values | Notes |
|-------|------|----------|--------|-------|
| `provider_gender` | VARCHAR(50) | Yes | Male, Female, Non-Binary, Prefer Not to Say, Other | Standard gender options |
| `provider_ethnicity` | VARCHAR(100) | Yes | Hispanic/Latino, White, Black/African American, Asian, Native American, Pacific Islander, Other | Standard ethnicity categories |
| `provider_age_group` | VARCHAR(50) | Yes | 18-24, 25-34, 35-44, 45-54, 55-64, 65+ | Age group categories |
| `provider_date_of_birth` | DATE | Yes | Valid date | For calculating age groups |

### 5.2 Recipient Demographic Fields (4 fields)

```sql
-- Add to timesheets table
ALTER TABLE timesheets ADD COLUMN recipient_gender VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN recipient_ethnicity VARCHAR(100);
ALTER TABLE timesheets ADD COLUMN recipient_age_group VARCHAR(50);
ALTER TABLE timesheets ADD COLUMN recipient_date_of_birth DATE;
```

**Field Specifications**:

| Field | Type | Nullable | Values | Notes |
|-------|------|----------|--------|-------|
| `recipient_gender` | VARCHAR(50) | Yes | Male, Female, Non-Binary, Prefer Not to Say, Other | Standard gender options |
| `recipient_ethnicity` | VARCHAR(100) | Yes | Hispanic/Latino, White, Black/African American, Asian, Native American, Pacific Islander, Other | Standard ethnicity categories |
| `recipient_age_group` | VARCHAR(50) | Yes | 18-24, 25-34, 35-44, 45-54, 55-64, 65+ | Age group categories |
| `recipient_date_of_birth` | DATE | Yes | Valid date | For calculating age groups |

**Total Fields to Add**: 8 fields

---

## 6. Implementation Requirements

### 6.1 Database Changes

**Priority**: High

1. **Add 8 demographic columns** to `timesheets` table
2. **Create database migration script** for production deployment
3. **Add indexes** on demographic fields for faster queries:
   ```sql
   CREATE INDEX idx_provider_gender ON timesheets(provider_gender);
   CREATE INDEX idx_provider_ethnicity ON timesheets(provider_ethnicity);
   CREATE INDEX idx_provider_age_group ON timesheets(provider_age_group);
   CREATE INDEX idx_recipient_gender ON timesheets(recipient_gender);
   CREATE INDEX idx_recipient_ethnicity ON timesheets(recipient_ethnicity);
   CREATE INDEX idx_recipient_age_group ON timesheets(recipient_age_group);
   ```

### 6.2 Entity Changes

**Priority**: High

**File**: `TimesheetEntity.java`

**Add Fields**:
```java
@Column(name = "provider_gender", length = 50)
private String providerGender;

@Column(name = "provider_ethnicity", length = 100)
private String providerEthnicity;

@Column(name = "provider_age_group", length = 50)
private String providerAgeGroup;

@Column(name = "provider_date_of_birth")
private LocalDate providerDateOfBirth;

@Column(name = "recipient_gender", length = 50)
private String recipientGender;

@Column(name = "recipient_ethnicity", length = 100)
private String recipientEthnicity;

@Column(name = "recipient_age_group", length = 50)
private String recipientAgeGroup;

@Column(name = "recipient_date_of_birth")
private LocalDate recipientDateOfBirth;
```

### 6.3 Backend Controller Changes

**Priority**: High

**File**: `AnalyticsController.java`

1. **Update `/api/analytics/adhoc-data`**:
   - Include demographic fields in response columns
   - Add demographic fields to returned data
   - Implement filtering by demographic parameters

2. **Update `/api/analytics/demographics/gender`**:
   - Query actual `provider_gender` and `recipient_gender` fields
   - Return real distribution data

3. **Update `/api/analytics/demographics/ethnicity`**:
   - Query actual `provider_ethnicity` and `recipient_ethnicity` fields
   - Return real distribution data

4. **Update `/api/analytics/demographics/age`**:
   - Query actual `provider_age_group` and `recipient_age_group` fields
   - Return real distribution data

5. **Update `/api/analytics/adhoc-filters`**:
   - Query distinct values for `provider_gender`, `recipient_gender`
   - Query distinct values for `provider_ethnicity`, `recipient_ethnicity`
   - Query distinct values for `provider_age_group`, `recipient_age_group`
   - Return populated arrays

6. **Update `/api/analytics/adhoc-breakdowns`**:
   - Query actual demographic breakdowns from database
   - Return real breakdown data

7. **Update `/api/analytics/adhoc-crosstab`**:
   - Query actual cross-tabulation data
   - Return real cross-tabulation results

### 6.4 Repository Changes

**Priority**: High

**File**: `TimesheetRepository.java`

**Add Methods**:
```java
@Query(value = "SELECT DISTINCT provider_gender FROM timesheets WHERE provider_gender IS NOT NULL ORDER BY provider_gender", nativeQuery = true)
List<String> findDistinctProviderGenders();

@Query(value = "SELECT DISTINCT recipient_gender FROM timesheets WHERE recipient_gender IS NOT NULL ORDER BY recipient_gender", nativeQuery = true)
List<String> findDistinctRecipientGenders();

@Query(value = "SELECT DISTINCT provider_ethnicity FROM timesheets WHERE provider_ethnicity IS NOT NULL ORDER BY provider_ethnicity", nativeQuery = true)
List<String> findDistinctProviderEthnicities();

@Query(value = "SELECT DISTINCT recipient_ethnicity FROM timesheets WHERE recipient_ethnicity IS NOT NULL ORDER BY recipient_ethnicity", nativeQuery = true)
List<String> findDistinctRecipientEthnicities();

@Query(value = "SELECT DISTINCT provider_age_group FROM timesheets WHERE provider_age_group IS NOT NULL ORDER BY provider_age_group", nativeQuery = true)
List<String> findDistinctProviderAgeGroups();

@Query(value = "SELECT DISTINCT recipient_age_group FROM timesheets WHERE recipient_age_group IS NOT NULL ORDER BY recipient_age_group", nativeQuery = true)
List<String> findDistinctRecipientAgeGroups();
```

### 6.5 Frontend Changes

**Priority**: Low (Frontend is already prepared)

**Status**: ✅ **NO CHANGES NEEDED**

The frontend code already handles demographic fields correctly:
- Gracefully handles missing fields (no errors)
- Displays demographic breakdowns when data is available
- Uses fallback field name variations (`provider_gender` vs `providerGender`)

**Optional Enhancements**:
- Add demographic filters to analytics page filters section
- Add age group dimension option
- Add demographic breakdown charts

---

## 7. Impact Assessment

### 7.1 Current Impact

**User Experience**:
- ⚠️ Demographic breakdown tables show empty data
- ⚠️ No demographic filtering available
- ⚠️ No demographic-based analytics

**System Functionality**:
- ✅ No errors (graceful handling)
- ✅ Other analytics work correctly
- ⚠️ Demographic endpoints return empty data

### 7.2 After Implementation

**User Experience**:
- ✅ Demographic breakdown tables show real data
- ✅ Demographic filtering available
- ✅ Demographic-based analytics functional

**System Functionality**:
- ✅ All demographic endpoints return real data
- ✅ Complete analytics coverage
- ✅ Full feature parity with frontend expectations

---

## 8. Implementation Checklist

### Phase 1: Database Schema (High Priority)

- [ ] Create database migration script
- [ ] Add 8 demographic columns to `timesheets` table
- [ ] Add indexes on demographic fields
- [ ] Test migration on development database
- [ ] Document field values and constraints

### Phase 2: Entity & Repository (High Priority)

- [ ] Add 8 demographic fields to `TimesheetEntity.java`
- [ ] Add repository methods for distinct demographic values
- [ ] Add repository methods for demographic queries
- [ ] Test entity mapping

### Phase 3: Backend Controller (High Priority)

- [ ] Update `/api/analytics/adhoc-data` to include demographic fields
- [ ] Update `/api/analytics/demographics/gender` to query real data
- [ ] Update `/api/analytics/demographics/ethnicity` to query real data
- [ ] Update `/api/analytics/demographics/age` to query real data
- [ ] Update `/api/analytics/adhoc-filters` to return real demographic options
- [ ] Update `/api/analytics/adhoc-breakdowns` to return real breakdowns
- [ ] Update `/api/analytics/adhoc-crosstab` to return real cross-tabulations
- [ ] Test all endpoints with sample data

### Phase 4: Data Migration (Medium Priority)

- [ ] Create data migration script for existing records
- [ ] Populate demographic fields from external source (if available)
- [ ] Or mark as nullable and populate going forward

### Phase 5: Frontend Enhancements (Low Priority)

- [ ] Add demographic filters to analytics page
- [ ] Add age group dimension option
- [ ] Enhance demographic visualization charts
- [ ] Test end-to-end flow

---

## 9. Summary

### Missing Fields Count

- **Provider Demographic Fields**: 4 fields
- **Recipient Demographic Fields**: 4 fields
- **Total Missing Fields**: **8 fields**

### Current Status

- ✅ Frontend code is ready (handles missing fields gracefully)
- ✅ Backend endpoints exist (but return empty data)
- ❌ Database schema missing all demographic fields
- ❌ Entity missing all demographic fields
- ❌ Repository missing demographic query methods

### Priority

**HIGH** - Demographic analytics are a core feature that users expect to work. The frontend is already built to display this data, and the backend endpoints exist but return empty data.

### Estimated Implementation Time

- **Database Migration**: 1-2 hours
- **Entity & Repository**: 2-3 hours
- **Backend Controller Updates**: 4-6 hours
- **Testing**: 2-3 hours
- **Total**: **9-14 hours**

---

**Report Generated**: November 29, 2025  
**Next Steps**: Implement database schema changes and update backend controllers

