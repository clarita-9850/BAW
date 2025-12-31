# Field Masking Issues and Fixes Report

## Issue 1: CASE_WORKER Role Update Returns 500 Error

### Problem
- SUPERVISOR role updates work successfully
- CASE_WORKER role updates fail with 500 Internal Server Error
- Error occurs in `KeycloakFieldMaskingService.updateRoleAttributesInKeycloak()`

### Root Cause Analysis

**Location**: `KeycloakFieldMaskingService.java` lines 824-836

**The Bug**:
When Keycloak returns a 500 error when fetching the role (line 773-778), we set:
```java
currentRoleData = new HashMap<>();  // Empty map, NOT null
```

Then at line 825, we check:
```java
if (currentRoleData != null) {  // This is TRUE even for empty map!
    // Try to preserve properties from empty map - finds nothing
    for (Map.Entry<String, Object> entry : currentRoleData.entrySet()) {
        // Empty map = no entries = name/description never set
    }
} else {
    // This block is SKIPPED because currentRoleData is not null (it's empty HashMap)
    updatePayload.put("name", normalizedRole);
    updatePayload.put("description", "Client role for " + normalizedRole);
}
```

**Result**: When `currentRoleData` is an empty HashMap (not null), we skip setting `name` and `description` in the update payload. Keycloak's PUT endpoint likely requires these fields, causing a 500 error.

### Fix
Change the condition to check if the map is empty, not just null:
```java
if (currentRoleData != null && !currentRoleData.isEmpty()) {
    // Preserve existing properties
} else {
    // Set name and description for new/empty roles
    updatePayload.put("name", normalizedRole);
    updatePayload.put("description", "Client role for " + normalizedRole);
}
```

---

## Issue 2: Test Scheduler Flooding Logs

### Problem
- Test scheduler runs every 2 minutes, creating batch jobs
- These jobs fail and flood logs with errors
- Makes it impossible to debug actual issues

### Fix Applied
- Disabled test scheduler in `application.yml`:
  ```yaml
  batch:
    test-scheduler:
      enabled: false
  ```

---

## Issue 3: Frontend Not Updating accessLevel When Toggle Changes

### Problem
- When user toggles "visible" switch off, only `selectedFields` is updated
- `accessLevel` in rules remains as `FULL_ACCESS` instead of `HIDDEN_ACCESS`
- Backend receives wrong access level

### Fix Applied
- Updated `toggleFieldAccess()` to also update `accessLevel`:
  - Hidden → `HIDDEN_ACCESS`
  - Visible → Based on `maskingType` (FULL_ACCESS or MASKED_ACCESS)
- Updated `handleRuleChange()` to update `accessLevel` when `maskingType` changes

---

## Issue 4: Fields Not Being Filtered in Reports

### Problem
- User sets only 6 fields to visible, but all fields appear in reports
- Hidden fields (not in `selectedFields`) are not being set to `HIDDEN_ACCESS`

### Fix Applied
- Updated `updateMaskingRulesInternal()` to:
  1. Check if field is in `selectedFields`
  2. If NOT in `selectedFields`, set `accessLevel` to `HIDDEN_ACCESS`
  3. Ensure ALL available fields have rules (visible or hidden)

---

## Issue 5: Keycloak 500 Error Handling

### Problem
- When Keycloak returns 500 when fetching role, code throws exception
- Should proceed with update anyway since role exists

### Fix Applied
- Added handling for `HttpServerErrorException` (5xx errors)
- When 500 occurs, set `currentRoleData = new HashMap<>()` and proceed
- **BUT**: This introduced Issue 1 (empty map vs null check)

---

## Summary of Required Fixes

1. **CRITICAL**: Fix empty HashMap check in `updateRoleAttributesInKeycloak()` - ensure name/description are set when `currentRoleData` is empty
2. **DONE**: Disable test scheduler
3. **DONE**: Fix frontend accessLevel updates
4. **DONE**: Fix field filtering logic
5. **DONE**: Handle Keycloak 500 errors (but needs the empty map fix)

---

## Next Steps

1. Apply the fix for Issue 1 (empty HashMap check)
2. Rebuild and test CASE_WORKER role update
3. Verify logs are clean (no scheduler noise)
4. Test that only visible fields appear in reports

