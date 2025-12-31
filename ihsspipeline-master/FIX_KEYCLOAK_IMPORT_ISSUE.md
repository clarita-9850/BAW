# Fix for Keycloak Users Being Lost on Restart

## Problem
Keycloak is configured with `--import-realm` which imports the realm configuration on every startup, overwriting any users created via the API that aren't in the import file.

## Root Cause
In `/Users/mythreya/Desktop/sajeevs-codebase-main/cmipsapplication/docker-compose.yml`:
- Line 81: `command: start-dev --import-realm`
- This imports `keycloak-realm-with-cors.json` on every startup
- The import file only contains 5 users (caseworker1, provider1, recipient1, system_scheduler, supervisor_CT2)
- Users created by your script (cron_admin, cron_supervisor, caseworker_CT1-5, etc.) are NOT in the import file
- When Keycloak restarts, it overwrites the realm, removing all users not in the import file

## Solution Options

### Option 1: Remove --import-realm (Recommended for Development)
Remove the `--import-realm` flag and let Keycloak use the database directly. The ensure-cors.sh script will handle CORS configuration.

**Pros:**
- Users persist across restarts
- No data loss

**Cons:**
- Need to manually import realm once initially
- CORS needs to be configured via script

### Option 2: Update Import File to Include All Users
Add all the users created by your script to the `keycloak-realm-with-cors.json` file.

**Pros:**
- All configuration in one place
- Version controlled

**Cons:**
- Need to update file every time you add users
- Passwords in plain text in file

### Option 3: Conditional Import Script
Create a script that only imports if the realm doesn't exist.

**Pros:**
- Best of both worlds
- Users persist after initial import

**Cons:**
- More complex setup

## Recommended Fix (Option 1)

Modify `/Users/mythreya/Desktop/sajeevs-codebase-main/cmipsapplication/docker-compose.yml`:

Change line 81 from:
```yaml
command: start-dev --import-realm
```

To:
```yaml
command: start-dev
```

Then run the ensure-cors script after Keycloak starts (it's already mounted and can be executed).

## Immediate Action
1. Users have been recreated using `recreate-keycloak-users.sh`
2. To prevent this from happening again, apply Option 1 above
3. After modifying docker-compose.yml, restart Keycloak: `docker-compose restart keycloak`

