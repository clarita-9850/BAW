#!/usr/bin/env bash
# Create Keycloak Technical Users for Cron Batch Jobs
# This script creates technical users for each role that scheduled batch jobs will use
# Each user will have the correct realm role and county/district attributes

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8085}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
REALM_NAME="${REALM_NAME:-cmips}"

echo "=========================================="
echo "Creating Keycloak Technical Users for Cron Jobs"
echo "=========================================="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Realm: $REALM_NAME"
echo ""

# Check if Keycloak is ready (skip wait if already ready)
echo "⏳ Checking Keycloak status..."
if curl -s -f "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1 || curl -s -f "$KEYCLOAK_URL/health" > /dev/null 2>&1 || curl -s -f "$KEYCLOAK_URL" > /dev/null 2>&1; then
    echo "✅ Keycloak is ready!"
else
    echo "⏳ Keycloak not responding, waiting up to 2 minutes..."
    for i in {1..12}; do
        if curl -s -f "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1 || curl -s -f "$KEYCLOAK_URL/health" > /dev/null 2>&1; then
            echo "✅ Keycloak is ready!"
            break
        fi
        if [ $i -eq 12 ]; then
            echo "⚠️ Keycloak health check failed, but continuing anyway..."
            break
        fi
        sleep 10
    done
fi

# Check for jq
if ! command -v jq &> /dev/null; then
    echo "❌ jq is required but not installed. Please install jq first."
    exit 1
fi

# Get admin token from master realm (using HTTP for local dev)
echo ""
echo "🔐 Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  --http1.1 | jq -r '.access_token // empty')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Failed to get admin token. Check credentials."
    exit 1
fi
echo "✅ Admin token obtained"

# Function to create or update technical user for cron jobs
create_cron_user() {
    local username=$1
    local email=$2
    local role=$3
    local countyId=$4
    local districtId=$5
    local password=${6:-cron_secure_password_123!}
    
    echo ""
    echo "👤 Processing technical user: $username (role: $role)"
    
    # Check if user exists
    USER_RESPONSE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=$username" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    USER_ID=$(echo "$USER_RESPONSE" | jq -r '.[0].id // empty')
    
    if [ -n "$USER_ID" ] && [ "$USER_ID" != "null" ]; then
        echo "  ℹ️  User $username already exists (ID: $USER_ID), updating..."
        
        # Update user attributes
        curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "{
            \"username\": \"$username\",
            \"email\": \"$email\",
            \"firstName\": \"Cron\",
            \"lastName\": \"$role\",
            \"enabled\": true,
            \"emailVerified\": true,
            \"attributes\": {
              \"countyId\": [\"$countyId\"],
              \"districtId\": [\"$districtId\"]
            }
          }" > /dev/null
        
        echo "  ✅ User $username updated"
    else
        # Create new user
        CREATE_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "{
            \"username\": \"$username\",
            \"email\": \"$email\",
            \"firstName\": \"Cron\",
            \"lastName\": \"$role\",
            \"enabled\": true,
            \"emailVerified\": true,
            \"attributes\": {
              \"countyId\": [\"$countyId\"],
              \"districtId\": [\"$districtId\"]
            }
          }")
        
        # Get user ID
        USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=$username" \
          -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')
        
        if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
            echo "  ❌ Failed to create user $username"
            return
        fi
        
        echo "  ✅ User $username created (ID: $USER_ID)"
    fi
    
    # Set password (always update to ensure it's set)
    curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID/reset-password" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"type\": \"password\",
        \"value\": \"$password\",
        \"temporary\": false
      }" > /dev/null
    
    echo "  ✅ Password set for $username"
    
    # Get role ID
    ROLE_RESPONSE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/$role" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    ROLE_ID=$(echo "$ROLE_RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$ROLE_ID" ] || [ "$ROLE_ID" = "null" ]; then
        echo "  ⚠️  Role $role does not exist, skipping role assignment"
        echo "     Please create the role first using setup-keycloak-automated.sh"
    else
        # Remove existing role mappings first (to avoid duplicates)
        curl -s -X DELETE "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID/role-mappings/realm" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "[{\"id\": \"$ROLE_ID\", \"name\": \"$role\"}]" > /dev/null 2>&1 || true
        
        # Assign role
        curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID/role-mappings/realm" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "[{\"id\": \"$ROLE_ID\", \"name\": \"$role\"}]" > /dev/null
        
        echo "  ✅ Role $role assigned to $username"
    fi
    
    # Return user ID for configuration output
    echo "$USER_ID"
}

# Store subject IDs for configuration (using regular variables for bash 3.2 compatibility)
echo ""
echo "=========================================="
echo "Creating Technical Users for Each Role"
echo "=========================================="

# 1. ADMIN
echo ""
echo "📋 Creating technical user for ADMIN..."
SUBJECT_ID_ADMIN=$(create_cron_user \
  "cron_admin" \
  "cron_admin@system.com" \
  "ADMIN" \
  "" \
  "" \
  "cron_admin_pass_123!")

# 2. SUPERVISOR
echo ""
echo "📋 Creating technical user for SUPERVISOR..."
SUBJECT_ID_SUPERVISOR=$(create_cron_user \
  "cron_supervisor" \
  "cron_supervisor@system.com" \
  "SUPERVISOR" \
  "" \
  "" \
  "cron_supervisor_pass_123!")

# 3. CASE_WORKER_CT1 (Orange)
echo ""
echo "📋 Creating technical user for CASE_WORKER - CT1 (Orange)..."
SUBJECT_ID_CASE_WORKER_CT1=$(create_cron_user \
  "caseworker_CT1" \
  "caseworker_CT1@system.com" \
  "CASE_WORKER" \
  "Orange" \
  "district-central" \
  "caseworker_CT1_pass_123!")

# 6. CASE_WORKER_CT2 (Sacramento)
echo ""
echo "📋 Creating technical user for CASE_WORKER - CT2 (Sacramento)..."
SUBJECT_ID_CASE_WORKER_CT2=$(create_cron_user \
  "caseworker_CT2" \
  "caseworker_CT2@system.com" \
  "CASE_WORKER" \
  "Sacramento" \
  "district-north" \
  "caseworker_CT2_pass_123!")

# 7. CASE_WORKER_CT3 (Riverside)
echo ""
echo "📋 Creating technical user for CASE_WORKER - CT3 (Riverside)..."
SUBJECT_ID_CASE_WORKER_CT3=$(create_cron_user \
  "caseworker_CT3" \
  "caseworker_CT3@system.com" \
  "CASE_WORKER" \
  "Riverside" \
  "district-south" \
  "caseworker_CT3_pass_123!")

# 8. CASE_WORKER_CT4 (Los Angeles)
echo ""
echo "📋 Creating technical user for CASE_WORKER - CT4 (Los Angeles)..."
SUBJECT_ID_CASE_WORKER_CT4=$(create_cron_user \
  "caseworker_CT4" \
  "caseworker_CT4@system.com" \
  "CASE_WORKER" \
  "Los Angeles" \
  "district-central" \
  "caseworker_CT4_pass_123!")

# 9. CASE_WORKER_CT5 (Alameda)
echo ""
echo "📋 Creating technical user for CASE_WORKER - CT5 (Alameda)..."
SUBJECT_ID_CASE_WORKER_CT5=$(create_cron_user \
  "caseworker_CT5" \
  "caseworker_CT5@system.com" \
  "CASE_WORKER" \
  "Alameda" \
  "district-north" \
  "caseworker_CT5_pass_123!")

# 10. SYSTEM_SCHEDULER
echo ""
echo "📋 Creating technical user for SYSTEM_SCHEDULER..."
SUBJECT_ID_SYSTEM_SCHEDULER=$(create_cron_user \
  "system_scheduler" \
  "system_scheduler@system.com" \
  "SYSTEM_SCHEDULER" \
  "" \
  "" \
  "system_scheduler_pass_123!")

# 11-15. SUPERVISOR users for each county
echo ""
echo "📋 Creating supervisor users for each county..."

echo ""
echo "📋 Creating supervisor user for CT1 (Orange)..."
SUBJECT_ID_SUPERVISOR_CT1=$(create_cron_user \
  "supervisor_CT1" \
  "supervisor_CT1@system.com" \
  "SUPERVISOR" \
  "Orange" \
  "district-central" \
  "supervisor_CT1_pass_123!")

echo ""
echo "📋 Creating supervisor user for CT2 (Sacramento)..."
SUBJECT_ID_SUPERVISOR_CT2=$(create_cron_user \
  "supervisor_CT2" \
  "supervisor_CT2@system.com" \
  "SUPERVISOR" \
  "Sacramento" \
  "district-north" \
  "supervisor_CT2_pass_123!")

echo ""
echo "📋 Creating supervisor user for CT3 (Riverside)..."
SUBJECT_ID_SUPERVISOR_CT3=$(create_cron_user \
  "supervisor_CT3" \
  "supervisor_CT3@system.com" \
  "SUPERVISOR" \
  "Riverside" \
  "district-south" \
  "supervisor_CT3_pass_123!")

echo ""
echo "📋 Creating supervisor user for CT4 (Los Angeles)..."
SUBJECT_ID_SUPERVISOR_CT4=$(create_cron_user \
  "supervisor_CT4" \
  "supervisor_CT4@system.com" \
  "SUPERVISOR" \
  "Los Angeles" \
  "district-central" \
  "supervisor_CT4_pass_123!")

echo ""
echo "📋 Creating supervisor user for CT5 (Alameda)..."
SUBJECT_ID_SUPERVISOR_CT5=$(create_cron_user \
  "supervisor_CT5" \
  "supervisor_CT5@system.com" \
  "SUPERVISOR" \
  "Alameda" \
  "district-north" \
  "supervisor_CT5_pass_123!")

echo ""
echo "=========================================="
echo "✅ Technical Users Created Successfully!"
echo "=========================================="
echo ""
echo "📋 Configuration for application.yml:"
echo ""
echo "keycloak:"
echo "  role-token:"
echo "    exchange-enabled: true"
echo "    admin-subject-id: ${SUBJECT_ID_ADMIN}"
echo "    supervisor-subject-id: ${SUBJECT_ID_SUPERVISOR}"
echo "    # County code-based technical users (for county-specific scheduled jobs)"
echo "    case-worker-ct1-subject-id: ${SUBJECT_ID_CASE_WORKER_CT1}"
echo "    case-worker-ct2-subject-id: ${SUBJECT_ID_CASE_WORKER_CT2}"
echo "    case-worker-ct3-subject-id: ${SUBJECT_ID_CASE_WORKER_CT3}"
echo "    case-worker-ct4-subject-id: ${SUBJECT_ID_CASE_WORKER_CT4}"
echo "    case-worker-ct5-subject-id: ${SUBJECT_ID_CASE_WORKER_CT5}"
echo ""
echo "📝 Technical Users Created:"
echo "  - cron_admin (ADMIN)"
echo "  - cron_supervisor (SUPERVISOR)"
echo "  - system_scheduler (SYSTEM_SCHEDULER)"
echo "  - caseworker_CT1 (CASE_WORKER, county: Orange)"
echo "  - caseworker_CT2 (CASE_WORKER, county: Sacramento)"
echo "  - caseworker_CT3 (CASE_WORKER, county: Riverside)"
echo "  - caseworker_CT4 (CASE_WORKER, county: Los Angeles)"
echo "  - caseworker_CT5 (CASE_WORKER, county: Alameda)"
echo "  - supervisor_CT1 (SUPERVISOR, county: Orange)"
echo "  - supervisor_CT2 (SUPERVISOR, county: Sacramento)"
echo "  - supervisor_CT3 (SUPERVISOR, county: Riverside)"
echo "  - supervisor_CT4 (SUPERVISOR, county: Los Angeles)"
echo "  - supervisor_CT5 (SUPERVISOR, county: Alameda)"
echo ""
echo "⚠️  IMPORTANT: Copy the subject IDs above into your application.yml file"
echo "   under keycloak.role-token.*-subject-id properties"
echo ""
echo "✅ Script completed successfully!"

