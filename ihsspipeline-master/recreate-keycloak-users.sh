#!/usr/bin/env bash
# Recreate Keycloak users using kcadm.sh inside the container
# This script recreates all users that were lost due to realm import

set -e

CONTAINER_NAME="cmips-keycloak"
REALM_NAME="cmips"

echo "=========================================="
echo "Recreating Keycloak Users"
echo "=========================================="
echo "Container: $CONTAINER_NAME"
echo "Realm: $REALM_NAME"
echo ""

# Function to create or update user
create_user() {
    local username=$1
    local email=$2
    local role=$3
    local countyId=$4
    local districtId=$5
    local password=$6
    local firstName=$7
    local lastName=$8
    
    echo "👤 Processing user: $username (role: $role)"
    
    # Check if user exists
    USER_ID=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get users -r $REALM_NAME -q username=$username --fields id --format csv --noquotes 2>/dev/null | grep -v "^$" | head -1 || echo "")
    
    if [ -n "$USER_ID" ] && [ "$USER_ID" != "null" ]; then
        echo "  ℹ️  User $username already exists (ID: $USER_ID), updating..."
        docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update "users/$USER_ID" -r $REALM_NAME \
            -s "email=$email" \
            -s "firstName=$firstName" \
            -s "lastName=$lastName" \
            -s "enabled=true" \
            -s "emailVerified=true" \
            > /dev/null 2>&1
    else
        # Create new user
        USER_ID=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh create users -r $REALM_NAME \
            -s "username=$username" \
            -s "email=$email" \
            -s "firstName=$firstName" \
            -s "lastName=$lastName" \
            -s "enabled=true" \
            -s "emailVerified=true" \
            --id 2>/dev/null || echo "")
        
        if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
            echo "  ❌ Failed to create user $username"
            return
        fi
        echo "  ✅ User $username created (ID: $USER_ID)"
    fi
    
    # Set password using user ID
    docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update "users/$USER_ID" -r $REALM_NAME \
        -s "credentials=[{\"type\":\"password\",\"value\":\"$password\",\"temporary\":false}]" \
        > /dev/null 2>&1
    
    echo "  ✅ Password set for $username"
    
    # Assign role using user ID
    docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh add-roles -r $REALM_NAME \
        --uid "$USER_ID" \
        --rolename "$role" \
        > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "  ✅ Role $role assigned to $username"
    else
        # Check if role is already assigned
        HAS_ROLE=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "users/$USER_ID/role-mappings/realm" -r $REALM_NAME --fields name --format csv --noquotes 2>/dev/null | grep "^$role$" || echo "")
        if [ -n "$HAS_ROLE" ]; then
            echo "  ℹ️  Role $role already assigned to $username"
        else
            echo "  ⚠️  Failed to assign role $role to $username"
        fi
    fi
    
    # Set user attributes (countyId and districtId)
    if [ -n "$countyId" ] && [ "$countyId" != "" ]; then
        docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update "users/$USER_ID" -r $REALM_NAME \
            -s "attributes.countyId=[\"$countyId\"]" \
            > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo "  ✅ CountyId attribute set to $countyId for $username"
        else
            echo "  ⚠️  Failed to set countyId attribute for $username"
        fi
    fi
    
    if [ -n "$districtId" ] && [ "$districtId" != "" ]; then
        docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update "users/$USER_ID" -r $REALM_NAME \
            -s "attributes.districtId=[\"$districtId\"]" \
            > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo "  ✅ DistrictId attribute set to $districtId for $username"
        else
            echo "  ⚠️  Failed to set districtId attribute for $username"
        fi
    fi
}

# Authenticate
echo "🔐 Authenticating with Keycloak..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user admin \
    --password admin123 \
    > /dev/null 2>&1

echo "✅ Authenticated"
echo ""

# Create all users
echo "=========================================="
echo "Creating Technical Users"
echo "=========================================="

create_user "cron_admin" "cron_admin@system.com" "CENTRAL_WORKER" "" "" "cron_admin_pass_123!" "Cron" "ADMIN"
create_user "cron_supervisor" "cron_supervisor@system.com" "SUPERVISOR" "" "" "cron_supervisor_pass_123!" "Cron" "SUPERVISOR"
create_user "caseworker_CT1" "caseworker_CT1@system.com" "CASE_WORKER" "Orange" "district-central" "caseworker_CT1_pass_123!" "Case" "Worker CT1"
create_user "caseworker_CT2" "caseworker_CT2@system.com" "CASE_WORKER" "Sacramento" "district-north" "caseworker_CT2_pass_123!" "Case" "Worker CT2"
create_user "caseworker_CT3" "caseworker_CT3@system.com" "CASE_WORKER" "Riverside" "district-south" "caseworker_CT3_pass_123!" "Case" "Worker CT3"
create_user "caseworker_CT4" "caseworker_CT4@system.com" "CASE_WORKER" "Los Angeles" "district-central" "caseworker_CT4_pass_123!" "Case" "Worker CT4"
create_user "caseworker_CT5" "caseworker_CT5@system.com" "CASE_WORKER" "Alameda" "district-north" "caseworker_CT5_pass_123!" "Case" "Worker CT5"
create_user "supervisor_CT1" "supervisor_CT1@system.com" "SUPERVISOR" "Orange" "district-central" "supervisor_CT1_pass_123!" "Supervisor" "CT1"
create_user "supervisor_CT2" "supervisor_CT2@system.com" "SUPERVISOR" "Sacramento" "district-north" "supervisor_CT2_pass_123!" "Supervisor" "CT2"
create_user "supervisor_CT3" "supervisor_CT3@system.com" "SUPERVISOR" "Riverside" "district-south" "supervisor_CT3_pass_123!" "Supervisor" "CT3"
create_user "supervisor_CT4" "supervisor_CT4@system.com" "SUPERVISOR" "Los Angeles" "district-central" "supervisor_CT4_pass_123!" "Supervisor" "CT4"
create_user "supervisor_CT5" "supervisor_CT5@system.com" "SUPERVISOR" "Alameda" "district-north" "supervisor_CT5_pass_123!" "Supervisor" "CT5"

echo ""
echo "=========================================="
echo "✅ All Users Recreated Successfully!"
echo "=========================================="

