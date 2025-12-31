# Field Masking Rules Setup Guide

## ⚠️ CRITICAL INCONSISTENCY FOUND

### Issue 1: Wrong Client UUID in Config
**Location**: `src/main/resources/application.yml` line 281

**Current (WRONG)**:
```yaml
keycloak:
  client-uuid: a7c600a7-1cad-4723-a28f-b8f5aeec9439
```

**Should be**:
```yaml
keycloak:
  client-uuid: da47fe23-9bb5-475a-82b6-4a875816b108
```

**Impact**: Code is looking for field masking rules in the wrong client!

---

## How Field Masking Rules Work

### 1. Storage Location
- **CLIENT ROLES** (not realm roles) - The code fetches from `/admin/realms/{realm}/clients/{clientUuid}/roles/{roleName}`
- **Client**: `trial-app`
- **Client UUID**: `da47fe23-9bb5-475a-82b6-4a875816b108` (actual)
- **Config UUID**: `a7c600a7-1cad-4723-a28f-b8f5aeec9439` (WRONG - needs fixing)

### 2. Attribute Format
- **Attribute name**: `field_masking_rules`
- **Format**: Array of strings
- **String format**: `"fieldName:maskingType:accessLevel:enabled"`
- **Example**: `["employeeid:NONE:FULL_ACCESS:true", "employeename:PARTIAL_MASK:MASKED_ACCESS:true"]`

### 3. Code Flow

**Reading Rules** (KeycloakFieldMaskingService.java:507):
1. Extracts user role from JWT (checks CLIENT roles first, then realm roles)
2. Fetches role attributes from: `/admin/realms/cmips/clients/{clientUuid}/roles/{roleName}`
3. Looks for `attributes.field_masking_rules`
4. Converts to `FieldMaskingRule` objects

**Writing Rules** (KeycloakFieldMaskingService.java:676):
1. Gets admin token
2. Fetches current CLIENT role from: `/admin/realms/cmips/clients/{clientUuid}/roles/{roleName}`
3. Preserves existing attributes
4. Updates `field_masking_rules` attribute
5. PUTs updated role back to Keycloak

---

## What You Need to Set in Keycloak Admin Console

### Step 1: Navigate to Client Roles
1. Go to: **Clients** → **trial-app** → **Roles** tab
2. Find the role (e.g., **SUPERVISOR**)

### Step 2: Set Attributes
1. Click on the role (e.g., **SUPERVISOR**)
2. Go to **Attributes** tab (or section)
3. Add attribute:
   - **Key**: `field_masking_rules`
   - **Value**: Array of strings in format `"fieldName:maskingType:accessLevel:enabled"`

### Step 3: Example Values

**For SUPERVISOR** (full access - no masking):
```
employeeid:NONE:FULL_ACCESS:true
employeename:NONE:FULL_ACCESS:true
location:NONE:FULL_ACCESS:true
department:NONE:FULL_ACCESS:true
status:NONE:FULL_ACCESS:true
```

**For CASE_WORKER** (partial masking):
```
employeeid:PARTIAL_MASK:MASKED_ACCESS:true
employeename:PARTIAL_MASK:MASKED_ACCESS:true
location:NONE:FULL_ACCESS:true
department:PARTIAL_MASK:MASKED_ACCESS:true
status:PARTIAL_MASK:MASKED_ACCESS:true
```

---

## ⚠️ IMPORTANT NOTES

1. **CLIENT ROLES ONLY**: Rules must be set on CLIENT roles in `trial-app`, NOT realm roles
2. **Client UUID Mismatch**: The config has the wrong UUID - fix it or the code won't find the rules
3. **Attribute Format**: Keycloak stores attributes as `Map<String, List<String>>`, so `field_masking_rules` should be a list
4. **After Setting**: Users need to get a NEW token for rules to appear in JWT

---

## Code Locations

- **Reading rules**: `KeycloakFieldMaskingService.fetchRoleAttributesFromKeycloak()` - line 507
- **Writing rules**: `KeycloakFieldMaskingService.updateRoleAttributesInKeycloak()` - line 676
- **Config**: `application.yml` line 281 - **NEEDS FIXING**

---

## Quick Fix

Update `application.yml`:
```yaml
keycloak:
  client-uuid: da47fe23-9bb5-475a-82b6-4a875816b108  # Changed from a7c600a7-1cad-4723-a28f-b8f5aeec9439
```

Then restart the spring-app container.

