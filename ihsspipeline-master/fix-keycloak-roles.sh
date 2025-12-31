#!/bin/bash

ADMIN_TOKEN=$(curl -s -X POST 'http://localhost:8085/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=admin' \
  -d 'password=admin123' \
  -d 'grant_type=password' \
  -d 'client_id=admin-cli' | python3 -c "import sys, json; print(json.load(sys.stdin).get('access_token', ''))" 2>/dev/null)

CLIENT_ID="da47fe23-9bb5-475a-82b6-4a875816b108"

echo "=== Fixing Keycloak Role Issues ==="
echo ""

# Step 1: Clean up null/empty role names
echo "Step 1: Finding and removing roles with null/empty names..."
NULL_ROLES=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "
import sys, json
roles = json.load(sys.stdin)
null_roles = [r for r in roles if not r.get('name') or r.get('name') == '']
for role in null_roles:
    print(role.get('id', ''))
" 2>/dev/null)

if [ -n "$NULL_ROLES" ]; then
    echo "Found roles with null names. Attempting to delete..."
    for ROLE_ID in $NULL_ROLES; do
        if [ -n "$ROLE_ID" ]; then
            echo "  Deleting role with ID: $ROLE_ID"
            curl -s -X DELETE "http://localhost:8085/admin/realms/cmips/roles-by-id/$ROLE_ID" \
              -H "Authorization: Bearer $ADMIN_TOKEN" 2>&1 | head -1
        fi
    done
else
    echo "  No null roles found"
fi
echo ""

# Step 2: Assign SUPERVISOR realm roles to all supervisor users
echo "Step 2: Assigning SUPERVISOR realm roles to supervisor users..."
SUPERVISOR_ROLE_ID=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/roles/SUPERVISOR" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

if [ -n "$SUPERVISOR_ROLE_ID" ]; then
    for USERNAME in supervisor_ct1 supervisor_ct2 supervisor_ct3 supervisor_ct4 supervisor_ct5; do
        USER_ID=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/users?username=$USERNAME" \
          -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; users = json.load(sys.stdin); print(users[0]['id'] if users and len(users) > 0 else '')" 2>/dev/null)
        
        if [ -n "$USER_ID" ]; then
            # Check if already has the role
            HAS_ROLE=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/users/$USER_ID/role-mappings/realm" \
              -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; roles = json.load(sys.stdin); print('yes' if any(r.get('name') == 'SUPERVISOR' for r in roles) else 'no')" 2>/dev/null)
            
            if [ "$HAS_ROLE" = "no" ]; then
                echo "  Assigning SUPERVISOR realm role to $USERNAME..."
                curl -s -X POST "http://localhost:8085/admin/realms/cmips/users/$USER_ID/role-mappings/realm" \
                  -H "Authorization: Bearer $ADMIN_TOKEN" \
                  -H "Content-Type: application/json" \
                  -d "[{\"id\": \"$SUPERVISOR_ROLE_ID\", \"name\": \"SUPERVISOR\"}]" > /dev/null
                echo "    ✅ Done"
            else
                echo "  $USERNAME already has SUPERVISOR realm role"
            fi
        fi
    done
else
    echo "  ERROR: SUPERVISOR realm role not found!"
fi
echo ""

# Step 3: Assign client roles to all users matching their realm roles
echo "Step 3: Assigning client roles to users (matching their realm roles)..."
echo ""

# Function to assign client role to user
assign_client_role() {
    local USERNAME=$1
    local CLIENT_ROLE_NAME=$2
    
    USER_ID=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/users?username=$USERNAME" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; users = json.load(sys.stdin); print(users[0]['id'] if users and len(users) > 0 else '')" 2>/dev/null)
    
    if [ -z "$USER_ID" ]; then
        return
    fi
    
    # Get client role ID
    CLIENT_ROLE_ID=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/clients/$CLIENT_ID/roles/$CLIENT_ROLE_NAME" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
    
    if [ -z "$CLIENT_ROLE_ID" ]; then
        return
    fi
    
    # Check if already has the role
    HAS_ROLE=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/users/$USER_ID/role-mappings/clients/$CLIENT_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; roles = json.load(sys.stdin); print('yes' if any(r.get('name') == '$CLIENT_ROLE_NAME' for r in roles) else 'no')" 2>/dev/null)
    
    if [ "$HAS_ROLE" = "no" ]; then
        curl -s -X POST "http://localhost:8085/admin/realms/cmips/users/$USER_ID/role-mappings/clients/$CLIENT_ID" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -H "Content-Type: application/json" \
          -d "[{\"id\": \"$CLIENT_ROLE_ID\", \"name\": \"$CLIENT_ROLE_NAME\"}]" > /dev/null
        echo "    ✅ Assigned $CLIENT_ROLE_NAME to $USERNAME"
    fi
}

# Assign CASE_WORKER client roles
echo "  Assigning CASE_WORKER client roles..."
for USERNAME in caseworker_ct1 caseworker_ct2 caseworker_ct3 caseworker_ct4 caseworker_ct5; do
    assign_client_role "$USERNAME" "CASE_WORKER"
done

# Assign SUPERVISOR client roles
echo "  Assigning SUPERVISOR client roles..."
for USERNAME in supervisor_ct1 supervisor_ct2 supervisor_ct3 supervisor_ct4 supervisor_ct5; do
    assign_client_role "$USERNAME" "SUPERVISOR"
done

# Assign PROVIDER client role
echo "  Assigning PROVIDER client role..."
assign_client_role "provider1" "PROVIDER"

# Assign RECIPIENT client role
echo "  Assigning RECIPIENT client role..."
assign_client_role "recipient1" "RECIPIENT"

# Assign SYSTEM_SCHEDULER client role (if it exists)
echo "  Checking for SYSTEM_SCHEDULER client role..."
SYSTEM_SCHEDULER_CLIENT_ROLE=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/clients/$CLIENT_ID/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; roles = json.load(sys.stdin); print('yes' if any(r.get('name') == 'SYSTEM_SCHEDULER' for r in roles) else 'no')" 2>/dev/null)

if [ "$SYSTEM_SCHEDULER_CLIENT_ROLE" = "no" ]; then
    echo "  Creating SYSTEM_SCHEDULER client role..."
    curl -s -X POST "http://localhost:8085/admin/realms/cmips/clients/$CLIENT_ID/roles" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"name": "SYSTEM_SCHEDULER", "description": "System Scheduler role"}' > /dev/null
fi
assign_client_role "system_scheduler" "SYSTEM_SCHEDULER"

echo ""
echo "=== Summary ==="
echo "✅ Cleaned up null/empty role names"
echo "✅ Assigned SUPERVISOR realm roles to all supervisor users"
echo "✅ Assigned client roles to all users matching their realm roles"
echo ""
echo "Please get a NEW token for users to see the changes in JWT tokens!"

