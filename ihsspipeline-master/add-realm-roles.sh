#!/bin/bash

# Script to add realm roles back to Keycloak and assign them to users
# This allows JWT tokens to include realm roles as a fallback when client roles are missing

set -e

KEYCLOAK_URL="${KEYCLOAK_AUTH_SERVER_URL:-http://localhost:8085}"
REALM_NAME="${KEYCLOAK_REALM:-cmips}"
ADMIN_USER="${KEYCLOAK_ADMIN_USER:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin123}"

echo "=========================================="
echo "Adding Realm Roles to Keycloak"
echo "=========================================="
echo ""
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Realm: $REALM_NAME"
echo ""

# Get admin token
echo "🔐 Authenticating as admin..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "❌ Failed to get admin token"
    exit 1
fi

echo "✅ Admin token obtained"
echo ""

# Function to create realm role if it doesn't exist
create_realm_role() {
    local role_name=$1
    echo "📋 Checking realm role: $role_name"
    
    # Check if role exists
    ROLE_CHECK=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/$role_name" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -w "%{http_code}" -o /dev/null)
    
    if [ "$ROLE_CHECK" = "200" ]; then
        echo "  ✅ Realm role $role_name already exists"
    else
        echo "  ➕ Creating realm role: $role_name"
        CREATE_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "{\"name\": \"$role_name\"}")
        
        if [ $? -eq 0 ]; then
            echo "  ✅ Realm role $role_name created"
        else
            echo "  ❌ Failed to create realm role $role_name"
        fi
    fi
}

# Create realm roles
echo "Creating realm roles..."
create_realm_role "CASE_WORKER"
create_realm_role "SUPERVISOR"
create_realm_role "ADMIN"
echo ""

# Function to assign realm role to user
assign_realm_role_to_user() {
    local username=$1
    local role_name=$2
    
    echo "📋 Assigning realm role $role_name to user: $username"
    
    # Get user ID
    USER_RESPONSE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=$username" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    USER_ID=$(echo "$USER_RESPONSE" | jq -r '.[0].id // empty')
    
    if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
        echo "  ⚠️  User $username not found, skipping"
        return
    fi
    
    # Get role ID
    ROLE_RESPONSE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/$role_name" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    ROLE_ID=$(echo "$ROLE_RESPONSE" | jq -r '.id // empty')
    
    if [ -z "$ROLE_ID" ] || [ "$ROLE_ID" = "null" ]; then
        echo "  ⚠️  Realm role $role_name not found, skipping"
        return
    fi
    
    # Check if role is already assigned
    EXISTING_ROLES=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID/role-mappings/realm" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    ROLE_ALREADY_ASSIGNED=$(echo "$EXISTING_ROLES" | jq -r ".[] | select(.name == \"$role_name\") | .name")
    
    if [ -n "$ROLE_ALREADY_ASSIGNED" ]; then
        echo "  ✅ Realm role $role_name already assigned to $username"
    else
        # Assign role
        ASSIGN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID/role-mappings/realm" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "[{\"id\": \"$ROLE_ID\", \"name\": \"$role_name\"}]")
        
        if [ $? -eq 0 ]; then
            echo "  ✅ Realm role $role_name assigned to $username"
        else
            echo "  ❌ Failed to assign realm role $role_name to $username"
        fi
    fi
}

# Assign realm roles to users
echo "=========================================="
echo "Assigning Realm Roles to Users"
echo "=========================================="
echo ""

# Admin user
assign_realm_role_to_user "admin_user" "ADMIN"
echo ""

# Case worker users
assign_realm_role_to_user "caseworker_ct1" "CASE_WORKER"
assign_realm_role_to_user "caseworker_ct2" "CASE_WORKER"
echo ""

# Supervisor users (if they exist)
assign_realm_role_to_user "supervisor_la" "SUPERVISOR"
assign_realm_role_to_user "supervisor_sf" "SUPERVISOR"
echo ""

echo "=========================================="
echo "✅ Realm roles setup complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Users need to log out and log back in to get a fresh JWT token"
echo "2. The new JWT token should include realm roles in realm_access.roles"
echo "3. The backend code will extract roles from realm_access.roles as fallback"
echo ""

