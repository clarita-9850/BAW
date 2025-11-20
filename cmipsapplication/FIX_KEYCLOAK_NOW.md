# ⚠️ CRITICAL FIX NEEDED: Make cmips-frontend Confidential

## Current Error
```
401 Unauthorized: Public client not allowed to retrieve service account
```

## Root Cause
`cmips-frontend` client in Keycloak is set to **public**, but it needs to be **confidential** to use client credentials grant.

## Fix Steps (Do this NOW in Keycloak)

### Step 1: Open Keycloak Admin Console
1. Go to: **http://localhost:8080**
2. Login: `admin` / `admin123`

### Step 2: Make cmips-frontend Confidential
1. Click **Clients** in left menu
2. Click **cmips-frontend** from the list
3. Find **Access Type** dropdown (near top of page)
4. Change from **public** → **confidential**
5. Click **Save** button at bottom

### Step 3: Enable Service Account
1. Still on `cmips-frontend` client page
2. Find **Service accounts roles** tab (or **Service Account Enabled** toggle)
3. Make sure **Service accounts enabled** is **ON** ✅
4. Click **Save**

### Step 4: Get New Client Secret
1. Click **Credentials** tab
2. Copy the **Client secret** value (it will be different now!)

### Step 5: Update Configuration
Update `docker-compose.yml` line 88:
```yaml
KEYCLOAK_CLIENT_SECRET: 'paste-new-secret-here'
```

### Step 6: Restart Backend
```bash
docker-compose restart cmips-backend
```

## Verify It Works
```bash
docker-compose logs cmips-backend | grep "admin token"
```
Should see: `Successfully obtained admin token` ✅

## Why This Is Required
- **Public clients** = Cannot use `client_credentials` grant ❌
- **Confidential clients** = Can use `client_credentials` grant ✅
- Backend needs `client_credentials` to authenticate as service account

