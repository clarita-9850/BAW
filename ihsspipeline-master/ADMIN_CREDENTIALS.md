# Admin Credentials for Field Masking Access

## Keycloak Admin Console
- **URL**: http://localhost:8085 (or http://localhost:8080 if using default port)
- **Realm**: `master` (for Keycloak admin console)
- **Username**: `admin`
- **Password**: `admin123`

## Application Admin User (for Field Masking)

### Current Status
The system currently has a `cron_admin` user with `CENTRAL_WORKER` role, but since we've moved to ADMIN-only access, you need an ADMIN role user.

### Option 1: Create New Admin User

**Username**: `admin_user` (or your preferred username)
**Password**: `admin_pass_123!` (or your preferred password)
**Role**: `ADMIN`
**Email**: `admin@cmips.com`

### Option 2: Update Existing cron_admin User

The `cron_admin` user exists but has `CENTRAL_WORKER` role. You can:
1. Update the role to `ADMIN` in Keycloak
2. Or create a new user with ADMIN role

## Steps to Create/Update Admin User

### Via Keycloak Admin Console:
1. Go to http://localhost:8085
2. Login with `admin` / `admin123`
3. Select `cmips` realm
4. Go to **Users** → **Add user** (or edit existing user)
5. Set username, email, enable user
6. Go to **Credentials** tab → Set password
7. Go to **Role Mappings** → Assign `ADMIN` role

### Via Command Line:
```bash
# Authenticate
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user admin \
    --password admin123

# Create admin user
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create users -r cmips \
    -s username=admin_user \
    -s email=admin@cmips.com \
    -s firstName=Admin \
    -s lastName=User \
    -s enabled=true \
    -s emailVerified=true

# Get user ID
USER_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get users -r cmips -q username=admin_user --fields id --format csv --noquotes | head -1)

# Set password
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh update "users/$USER_ID" -r cmips \
    -s "credentials=[{\"type\":\"password\",\"value\":\"admin_pass_123!\",\"temporary\":false}]"

# Assign ADMIN role
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh add-roles -r cmips \
    --uid "$USER_ID" \
    --rolename "ADMIN"
```

## Quick Reference

### Keycloak Master Realm (Admin Console)
- **Username**: `admin`
- **Password**: `admin123`
- **URL**: http://localhost:8085

### Application Realm (cmips) - For Login
- **Realm**: `cmips`
- **Client**: `trial-app`
- **Admin User** (to be created):
  - **Username**: `admin_user` (or your choice)
  - **Password**: `admin_pass_123!` (or your choice)
  - **Role**: `ADMIN`

## Current Users in System

From `recreate-keycloak-users.sh`:
- `cron_admin` / `cron_admin_pass_123!` - **CENTRAL_WORKER** role (needs to be updated to ADMIN)
- `cron_supervisor` / `cron_supervisor_pass_123!` - SUPERVISOR role
- `caseworker_CT1` through `caseworker_CT5` - CASE_WORKER role
- `supervisor_CT1` through `supervisor_CT5` - SUPERVISOR role

## Important Notes

⚠️ **Since we removed CENTRAL_WORKER and now use ADMIN only:**
- The `cron_admin` user currently has `CENTRAL_WORKER` role
- You need to either:
  1. Update `cron_admin` to have `ADMIN` role, OR
  2. Create a new user with `ADMIN` role

## Testing Admin Access

After creating/updating the admin user:
1. Go to http://localhost:3000/login
2. Login with admin credentials
3. You should see "Field Masking" link in navigation
4. Access http://localhost:3000/admin/field-masking
5. You should be able to configure field masking rules

