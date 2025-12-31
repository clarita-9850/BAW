# JWT Token Configuration Summary

## Current Status

### ✅ What's Fixed
1. **Removed all username pattern extraction** - No more fallback to extracting county from username
2. **Removed all default/fallback logic** - Application will fail explicitly if countyId is not in token
3. **County extraction only from JWT token** - Only checks:
   - Direct `countyId` claim in JWT
   - `attributes.countyId` in JWT (handles both List and String)

### ⚠️ What Needs to Be Done

## 1. Configure Keycloak Protocol Mapper

**Script**: `configure-keycloak-county-mapper.sh`

**Purpose**: Configures Keycloak to include `countyId` user attribute in JWT tokens

**How to Run**:
```bash
cd /Users/mythreya/Desktop/trial
./configure-keycloak-county-mapper.sh
```

**What it does**:
- Creates/updates a protocol mapper for the `trial-app` client
- Maps user attribute `countyId` to JWT claim `countyId`
- Includes in: ID token, Access token, UserInfo token

**Important**: This must be run AFTER users are created with countyId attributes.

## 2. User Attributes in Keycloak

**Script**: `create-keycloak-cron-users.sh`

**Current Status**: Users are created with `countyId` in attributes:
```json
"attributes": {
  "countyId": ["Orange"],  // Array format
  "districtId": ["district-central"]
}
```

**Note**: Keycloak stores attributes as arrays, but the mapper will extract the first value.

## 3. Token Structure After Configuration

**Before Mapper** (Current - Missing countyId):
```json
{
  "preferred_username": "supervisor_ct2",
  "resource_access": {
    "trial-app": {
      "roles": ["SUPERVISOR"]
    }
  },
  "realm_access": {
    "roles": ["SUPERVISOR", "offline_access"]
  }
  // NO countyId here
}
```

**After Mapper** (Expected):
```json
{
  "preferred_username": "supervisor_ct2",
  "resource_access": {
    "trial-app": {
      "roles": ["SUPERVISOR"]
    }
  },
  "realm_access": {
    "roles": ["SUPERVISOR", "offline_access"]
  },
  "countyId": "Sacramento",  // ✅ Added by mapper
  "attributes": {
    "countyId": ["Sacramento"]  // ✅ Original attribute (may or may not be included)
  }
}
```

## 4. Application Extraction Logic

**Location**: `DataPipelineController.extractUserInfoFromJwtObject()`

**Flow**:
1. Check `jwt.getClaimAsString("countyId")` - Direct claim (from mapper)
2. Check `jwt.getClaimAsMap("attributes").get("countyId")` - Attributes map
   - If List: Take first element
   - If String: Use directly
3. **NO FALLBACK** - If not found, logs error and returns null
4. Validation fails if countyId is null for roles requiring it

## 5. Roles Requiring CountyId

These roles **MUST** have countyId in JWT token:
- `SUPERVISOR`
- `CASE_WORKER`
- `PROVIDER`
- `RECIPIENT`

If countyId is missing, the application will return:
```json
{
  "status": "ERROR",
  "message": "County is required for role <ROLE> and must come from the authenticated user context."
}
```

## 6. Testing Steps

1. **Run Keycloak mapper configuration**:
   ```bash
   ./configure-keycloak-county-mapper.sh
   ```

2. **Verify users have countyId attribute**:
   - Check Keycloak Admin Console
   - Users → Select user → Attributes tab
   - Should see `countyId: ["Orange"]` or similar

3. **Get a new token** (old tokens won't have countyId):
   ```bash
   curl -X POST http://localhost:8085/realms/cmips/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=trial-app&username=supervisor_CT2&password=supervisor_CT2_pass_123!&grant_type=password"
   ```

4. **Decode token and verify countyId is present**:
   - Use jwt.io or decode base64 payload
   - Should see `countyId: "Sacramento"` in token

5. **Test API endpoint**:
   ```bash
   curl -X POST http://localhost:8080/api/pipeline/generate-report \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <NEW_TOKEN>" \
     -d '{"reportType":"TIMESHEET_REPORT","startDate":"2025-11-03","endDate":"2025-12-03","page":0,"pageSize":25}'
   ```

6. **Check logs** for county extraction:
   ```bash
   docker-compose logs spring-app | grep -E "countyId|County|Extracted"
   ```

## 7. Files Modified

### Removed Username Pattern Extraction:
- ✅ `DataPipelineController.extractUserInfoFromJwtObject()`
- ✅ `DataPipelineController.extractUserInfoFromJWT()`
- ✅ `BusinessIntelligenceController.extractUserInfoFromJwtObject()`
- ✅ `BusinessIntelligenceController.extractUserInfoFromJWT()`
- ✅ `CountyAccessController.extractCountyFromJWT()`
- ✅ `ScheduledReportService.extractCountyFromJWT()`
- ✅ `JobQueueService.extractCountyFromJob()`

### Removed Fallback Logic:
- ✅ `ScheduledReportService.generateAndEmailReport()` - No fallback to `getCountyForRole()`
- ✅ `ScheduledReportService.createScheduledJob()` - No fallback to `getCountyForRole()`

### Created:
- ✅ `configure-keycloak-county-mapper.sh` - Script to configure Keycloak mapper

## 8. Next Steps

1. **Run the mapper configuration script** to add countyId to JWT tokens
2. **Rebuild the application** with the updated code
3. **Test with a fresh token** (old tokens won't work)
4. **Verify county filtering** works correctly for each role

## 9. Important Notes

- **Old tokens won't work**: Tokens issued before the mapper is configured won't have countyId
- **Users must re-login**: After mapper is configured, users need to get new tokens
- **No defaults**: If countyId is missing, the application will fail explicitly (no silent fallbacks)
- **Array handling**: Keycloak stores attributes as arrays `["Orange"]`, but mapper extracts first value as string `"Orange"`

