# CDSS Adhoc Detail Report Dashboard - Test Plan Document

## 1. Dashboard Overview

The CDSS (California Department of Social Services) Adhoc Reporting Table is a comprehensive demographic dashboard displaying data about individuals, population, per capita rates, authorized hours, and various demographic breakdowns.

### Key Metrics Displayed:
- **Individuals**: 1,501,500
- **Population**: 38,814,347
- **Per Capita Rate**: 77.29
- **Total Authorized Hours**: 346,450
- **Total Authorized Hours (Alt)**: 115.48

### Dashboard Components:
1. **Dimension Selectors** (Left Panel)
   - Select Dimension 1: Age Group
   - Select Dimension 2: (Dropdown)
   - Select Dimension 3: Ethnicity
   - Select Dimension 4: Gender
   - Select Dimension 5: Rspnt_Blind_Disabled
   - Select Dimension 6: (Dropdown)
   - Select Dimension 7: None
   - Select Dimension 8: (Dropdown)

2. **Measure Selection** (Left Panel Checkboxes)
   - (All)
   - Active Error Rate
   - Active Error Rate - C...
   - Active Error Rate - R...
   - All Federal Persons
   - All SOC Apps submit...
   - All Non-Assistance H...
   - All Non-Assistance P...
   - All Public Assistance...
   - Application Denial R...
   - Applications Approv...
   - Applications Approv... (Black or African American)
   - Applications Denied
   - Applications Denied...
   - Applications Received
   - Applications Submit...
   - And more...

3. **View Options**
   - Details Table
   - Pivot Table
   - Select a County (Dropdown)
   - Counties Included toggle

4. **Data Table Columns**
   - Dimension columns (Race, Ethnicity, Gender, Age Group, etc.)
   - # Count
   - County Population
   - Authorized Hours

5. **Filter Panels (Right Side)**
   - Electronic Visit Verification
   - Productive Sup / Paramedical
   - Severely Impaired
   - Aged, Blind, Disabled
   - Ethnicity
   - Gender
   - Age Group

---

## 2. Test Scenarios

### 2.1 Dimension Selection Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| DIM-001 | Select single dimension (Age Group) | 1. Navigate to dashboard<br>2. Select "Age Group" from Dimension 1 dropdown | Data table updates to show Age Group breakdown |
| DIM-002 | Select multiple dimensions | 1. Select "Age Group" in Dimension 1<br>2. Select "Ethnicity" in Dimension 3<br>3. Select "Gender" in Dimension 4 | Data table shows cross-tabulation of all selected dimensions |
| DIM-003 | Change dimension order | 1. Select dimensions<br>2. Change Dimension 1 from Age Group to Ethnicity | Data reorganizes with new primary dimension |
| DIM-004 | Select "None" for dimension | 1. Set Dimension 7 to "None" | That dimension is excluded from the report |
| DIM-005 | Select all 8 dimensions | 1. Select values for all 8 dimension dropdowns | Dashboard handles maximum dimension selection |
| DIM-006 | Clear all dimensions | 1. Set all dimensions to None or default | Dashboard shows aggregate data without breakdown |

### 2.2 Measure Selection Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| MSR-001 | Select "(All)" measures | 1. Check "(All)" checkbox in Measure Selection | All measures are selected and displayed |
| MSR-002 | Select single measure | 1. Uncheck "(All)"<br>2. Check only "Active Error Rate" | Only Active Error Rate data is displayed |
| MSR-003 | Select multiple specific measures | 1. Select "Applications Approved", "Applications Denied", "Applications Received" | Selected measures are displayed in columns |
| MSR-004 | Deselect all measures | 1. Uncheck all measure checkboxes | Dashboard shows message or default view |
| MSR-005 | Toggle measure selection | 1. Check a measure<br>2. Uncheck same measure | Data updates accordingly each time |
| MSR-006 | Select Active Error Rate variants | 1. Select all "Active Error Rate" related measures | All error rate variants display correctly |

### 2.3 View Type Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| VW-001 | Switch to Details Table view | 1. Click "Details Table" button | Data displays in flat table format |
| VW-002 | Switch to Pivot Table view | 1. Click "Pivot Table" button | Data displays in pivot table format |
| VW-003 | Toggle between views | 1. Click Details Table<br>2. Click Pivot Table<br>3. Click Details Table | View switches correctly each time |
| VW-004 | Verify data consistency across views | 1. Note totals in Details Table<br>2. Switch to Pivot Table<br>3. Compare totals | Totals match between views |

### 2.4 County Filter Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| CTY-001 | Select single county | 1. Click "Select a County" dropdown<br>2. Select "Sacramento" | Data filters to show only Sacramento county |
| CTY-002 | Select multiple counties | 1. Select multiple counties from dropdown | Data shows combined data for selected counties |
| CTY-003 | Select all counties | 1. Select all available counties | Data shows statewide totals |
| CTY-004 | Clear county selection | 1. Clear county selection | Dashboard returns to default state |
| CTY-005 | Toggle "Counties Included" | 1. Toggle the "Counties Included" switch | Display changes based on toggle state |

### 2.5 Right Panel Filter Tests

#### 2.5.1 Electronic Visit Verification Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| EVV-001 | Filter by EVV status | 1. Select EVV filter option | Data filters by EVV verification status |
| EVV-002 | Clear EVV filter | 1. Clear EVV selection | Filter is removed |

#### 2.5.2 Productive Sup / Paramedical Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| PSP-001 | Select PM Cases | 1. Check "PM Cases" checkbox | Data filters to PM Cases only |
| PSP-002 | Select PS Cases | 1. Check "PS Cases" checkbox | Data filters to PS Cases only |
| PSP-003 | Select both PM and PS | 1. Check both checkboxes | Data shows combined PM and PS cases |

#### 2.5.3 Severely Impaired Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| SI-001 | Filter severely impaired | 1. Select Severely Impaired filter | Data shows only severely impaired cases |

#### 2.5.4 Aged, Blind, Disabled Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| ABD-001 | Select "(All)" | 1. Select "(All)" option | All categories included |
| ABD-002 | Select "Aged" only | 1. Select "Aged" | Data filters to Aged population |
| ABD-003 | Select "Blind" only | 1. Select "Blind" | Data filters to Blind population |
| ABD-004 | Select "Disabled" only | 1. Select "Disabled" | Data filters to Disabled population |
| ABD-005 | Select multiple ABD categories | 1. Select "Aged" and "Disabled" | Combined data displayed |

#### 2.5.5 Ethnicity Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| ETH-001 | Select "(All)" ethnicities | 1. Select "(All)" | All ethnicities included |
| ETH-002 | Select "Asian" | 1. Select "Asian" | Data filters to Asian ethnicity |
| ETH-003 | Select "Black or African American" | 1. Select option | Data filters accordingly |
| ETH-004 | Select "Hispanic or Latino" | 1. Select option | Data filters accordingly |
| ETH-005 | Select "White" | 1. Select "White" | Data filters to White ethnicity |
| ETH-006 | Select "Other" | 1. Select "Other" | Data filters to Other ethnicity |
| ETH-007 | Select multiple ethnicities | 1. Select multiple options | Combined data displayed |

#### 2.5.6 Gender Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| GEN-001 | Select "(All)" genders | 1. Select "(All)" | All genders included |
| GEN-002 | Select "Female" | 1. Select "Female" | Data filters to Female |
| GEN-003 | Select "Male" | 1. Select "Male" | Data filters to Male |

#### 2.5.7 Age Group Filter
| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| AGE-001 | Select specific age range | 1. Select an age group | Data filters to selected age range |
| AGE-002 | Select multiple age ranges | 1. Select multiple age groups | Combined data displayed |
| AGE-003 | Select all age groups | 1. Select all options | All age groups included |

### 2.6 Data Table Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| TBL-001 | Verify column headers | 1. Check all column headers displayed | Headers match expected: Race, Ethnicity, Gender, Age, # Count, County Population, Authorized Hours |
| TBL-002 | Sort by Count ascending | 1. Click # Count column header | Data sorts ascending by count |
| TBL-003 | Sort by Count descending | 1. Click # Count column header twice | Data sorts descending by count |
| TBL-004 | Sort by Population | 1. Click County Population header | Data sorts by population |
| TBL-005 | Sort by Authorized Hours | 1. Click Authorized Hours header | Data sorts by hours |
| TBL-006 | Verify row data integrity | 1. Select known filters<br>2. Verify row values | Row data matches expected values |
| TBL-007 | Scroll through large dataset | 1. Apply filters generating many rows<br>2. Scroll to bottom | All rows accessible, no data loss |
| TBL-008 | Verify totals calculation | 1. Sum visible Count values<br>2. Compare to header total | Sum matches displayed total |

### 2.7 Header Metrics Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| HDR-001 | Verify Individuals count | 1. Check Individuals metric | Displays 1,501,500 or updates with filters |
| HDR-002 | Verify Population count | 1. Check Population metric | Displays 38,814,347 or updates |
| HDR-003 | Verify Per Capita Rate | 1. Check Per Capita Rate | Displays 77.29 or recalculates |
| HDR-004 | Verify Total Authorized Hours | 1. Check authorized hours metrics | Displays 346,450 and 115.48 |
| HDR-005 | Metrics update with filters | 1. Apply county filter<br>2. Check metrics update | All metrics recalculate for filtered data |

### 2.8 Cross-Filter Interaction Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| XF-001 | Apply dimension + county filter | 1. Select Age Group dimension<br>2. Select Sacramento county | Data shows age breakdown for Sacramento |
| XF-002 | Apply measure + ethnicity filter | 1. Select specific measures<br>2. Filter by Hispanic ethnicity | Measures shown for Hispanic population |
| XF-003 | Apply all filter types | 1. Set dimensions<br>2. Select measures<br>3. Set county<br>4. Set demographic filters | All filters combine correctly |
| XF-004 | Clear all filters | 1. Apply multiple filters<br>2. Clear/reset all | Dashboard returns to default state |
| XF-005 | Filter results in no data | 1. Apply conflicting filters | Dashboard shows "No data" message gracefully |

### 2.9 Performance Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| PRF-001 | Initial page load time | 1. Navigate to dashboard<br>2. Measure load time | Page loads within 3 seconds |
| PRF-002 | Filter response time | 1. Apply filter<br>2. Measure update time | Data updates within 2 seconds |
| PRF-003 | Large data export | 1. Select all data<br>2. Attempt export | Export completes successfully |
| PRF-004 | Multiple rapid filter changes | 1. Quickly change multiple filters | Dashboard handles without crashing |
| PRF-005 | Concurrent user simulation | 1. Simulate multiple users | Dashboard remains responsive |

### 2.10 Accessibility Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| ACC-001 | Keyboard navigation | 1. Navigate using Tab key | All controls accessible via keyboard |
| ACC-002 | Screen reader compatibility | 1. Enable screen reader<br>2. Navigate dashboard | All elements properly announced |
| ACC-003 | Color contrast | 1. Check color contrast ratios | Meets WCAG AA standards |
| ACC-004 | Focus indicators | 1. Tab through elements | Focus visible on all interactive elements |

### 2.11 Responsive Design Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| RES-001 | Desktop view (1920x1080) | 1. View at 1920x1080 | All elements visible and properly aligned |
| RES-002 | Tablet view (1024x768) | 1. Resize to tablet size | Layout adapts appropriately |
| RES-003 | Mobile view (375x667) | 1. Resize to mobile size | Dashboard usable on mobile |

### 2.12 Data Validation Tests

| Test ID | Test Scenario | Test Steps | Expected Result |
|---------|---------------|------------|-----------------|
| VAL-001 | Verify Asian population data | 1. Filter by Asian ethnicity<br>2. Check counts | Data matches source records |
| VAL-002 | Verify age group calculations | 1. Select age breakdown<br>2. Verify totals | Age group totals sum to overall total |
| VAL-003 | Verify authorized hours | 1. Check hours by category<br>2. Sum values | Sum matches total authorized hours |
| VAL-004 | Cross-reference with source | 1. Compare dashboard data to source database | Data matches source system |

---

## 3. Test Data Requirements

### 3.1 Demographic Data
- Race categories: Asian, Black or African American, White, Hispanic/Latino, Other
- Gender: Male, Female
- Age Groups: Various ranges (e.g., 0-17, 18-64, 65+)
- Counties: All 58 California counties
- Disability status: Aged, Blind, Disabled

### 3.2 Metrics Data
- Individual counts per demographic combination
- County population figures
- Authorized hours data
- Per capita rate calculations
- Application statistics (approved, denied, received)
- Error rates

### 3.3 Sample Data Volumes
- Minimum 1000 unique demographic combinations
- At least 10 counties with significant data
- Multiple time periods if applicable

---

## 4. Test Environment Requirements

### 4.1 Technical Stack
- **Backend**: Spring Boot 3.x
- **Frontend**: React/Next.js or similar
- **Testing Framework**: Playwright
- **Database**: H2 (in-memory) or PostgreSQL
- **Build Tool**: Maven/Gradle

### 4.2 Browser Coverage
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

---

## 5. Test Execution Priority

### High Priority
- Dimension selection tests (DIM-001 to DIM-006)
- Measure selection tests (MSR-001 to MSR-006)
- County filter tests (CTY-001 to CTY-005)
- Data table tests (TBL-001 to TBL-008)

### Medium Priority
- Right panel filter tests
- Cross-filter interaction tests
- Header metrics tests
- View type tests

### Low Priority
- Performance tests
- Accessibility tests
- Responsive design tests

---

## 6. Defect Severity Classification

| Severity | Description | Example |
|----------|-------------|---------|
| Critical | Dashboard unusable | Page won't load, data completely wrong |
| High | Major feature broken | Filters don't work, calculations wrong |
| Medium | Feature partially broken | Some filters don't combine correctly |
| Low | Minor issues | UI alignment, minor display issues |

---

## 7. Sign-off Criteria

- All High Priority tests pass
- No Critical or High severity defects open
- 95% of Medium Priority tests pass
- Performance within acceptable thresholds
- Cross-browser compatibility verified

---

**Document Version**: 1.0
**Created**: December 2024
**Author**: Test Automation Team
