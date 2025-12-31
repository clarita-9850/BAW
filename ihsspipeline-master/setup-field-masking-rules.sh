#!/bin/bash

# Script to create roles (if needed) and set field masking rules in Keycloak
# Format: "fieldName:maskingType:accessLevel:enabled"

CONTAINER_NAME="cmips-keycloak"
REALM_NAME="cmips"

echo "🔐 Setting Up Field Masking Rules in Keycloak"
echo "=========================================="
echo ""

# Authenticate with Keycloak
echo "🔐 Authenticating with Keycloak..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user admin \
    --password admin123 \
    > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Authenticated"
else
    echo "❌ Failed to authenticate"
    exit 1
fi

echo ""

# Function to create role if it doesn't exist
create_role_if_needed() {
    local role_name=$1
    
    echo "🔍 Checking if role $role_name exists..."
    ROLE_CHECK=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "roles/$role_name" -r $REALM_NAME 2>&1)
    
    if echo "$ROLE_CHECK" | grep -q "Could not find role"; then
        echo "  📝 Creating role: $role_name"
        docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh create roles -r $REALM_NAME \
            -s name="$role_name" \
            > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            echo "  ✅ Created role: $role_name"
        else
            echo "  ❌ Failed to create role: $role_name"
            return 1
        fi
    else
        echo "  ✅ Role $role_name already exists"
    fi
}

# Function to set role attributes
set_role_attributes() {
    local role_name=$1
    shift
    local rules=("$@")
    
    echo "📝 Setting field masking rules for role: $role_name"
    
    # Build JSON array of rules
    RULES_JSON="["
    for i in "${!rules[@]}"; do
        if [ $i -gt 0 ]; then
            RULES_JSON+=","
        fi
        RULES_JSON+="\"${rules[$i]}\""
    done
    RULES_JSON+="]"
    
    # Use Python to properly format the update
    UPDATE_CMD="docker exec $CONTAINER_NAME python3 -c \"
import json
import subprocess
import sys

rules = $RULES_JSON
attr_value = json.dumps(rules)

# Get current role
result = subprocess.run(['/opt/keycloak/bin/kcadm.sh', 'get', 'roles/$role_name', '-r', '$REALM_NAME'], 
                       capture_output=True, text=True)
if result.returncode != 0:
    print('Error getting role', file=sys.stderr)
    sys.exit(1)

# Parse current role (simplified - just update attributes)
# Use kcadm update with proper escaping
subprocess.run(['/opt/keycloak/bin/kcadm.sh', 'update', 'roles/$role_name', '-r', '$REALM_NAME',
                '-s', f'attributes.field_masking_rules={attr_value}'],
               check=True)
\""
    
    # Simpler approach: use a temp file
    TEMP_FILE="/tmp/keycloak_update_${role_name}.json"
    docker exec $CONTAINER_NAME sh -c "echo '$RULES_JSON' > $TEMP_FILE"
    
    # Try using curl from inside container to update
    # First get admin token
    ADMIN_TOKEN=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin123 2>&1 > /dev/null && \
        docker exec $CONTAINER_NAME sh -c "curl -s -X POST 'http://localhost:8080/realms/master/protocol/openid-connect/token' -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password&client_id=admin-cli&username=admin&password=admin123' | grep -o '\"access_token\":\"[^\"]*' | cut -d'\"' -f4")
    
    if [ -n "$ADMIN_TOKEN" ]; then
        # Get current role
        CURRENT_ROLE=$(docker exec $CONTAINER_NAME sh -c "curl -s -X GET 'http://localhost:8080/admin/realms/$REALM_NAME/roles/$role_name' -H 'Authorization: Bearer $ADMIN_TOKEN'")
        
        # Update with new attributes
        UPDATE_PAYLOAD=$(echo "$CURRENT_ROLE" | docker exec -i $CONTAINER_NAME python3 -c "
import sys, json
role = json.load(sys.stdin)
rules = $RULES_JSON
if 'attributes' not in role:
    role['attributes'] = {}
role['attributes']['field_masking_rules'] = rules
print(json.dumps(role))
")
        
        RESPONSE=$(docker exec $CONTAINER_NAME sh -c "curl -s -w '%{http_code}' -X PUT 'http://localhost:8080/admin/realms/$REALM_NAME/roles/$role_name' -H 'Authorization: Bearer $ADMIN_TOKEN' -H 'Content-Type: application/json' -d '$UPDATE_PAYLOAD'")
        HTTP_CODE=$(echo "$RESPONSE" | tail -c 4)
        
        if [ "$HTTP_CODE" = "204" ] || [ "$HTTP_CODE" = "200" ]; then
            echo "  ✅ Successfully set field masking rules for $role_name"
            return 0
        else
            echo "  ❌ Failed (HTTP $HTTP_CODE)"
            echo "  Response: $(echo "$RESPONSE" | head -c 200)"
        fi
    fi
    
    # Fallback: try kcadm with escaped JSON
    ATTRIBUTE_VALUE=$(echo "$RULES_JSON" | sed 's/"/\\"/g')
    docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update "roles/$role_name" -r $REALM_NAME \
        -s "attributes.field_masking_rules=[$ATTRIBUTE_VALUE]" \
        > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "  ✅ Successfully set field masking rules for $role_name"
        return 0
    else
        echo "  ❌ Failed to set field masking rules for $role_name"
        return 1
    fi
}

# Create roles if needed
echo "=========================================="
echo "Creating Roles (if needed)"
echo "=========================================="
create_role_if_needed "CASE_WORKER"
create_role_if_needed "SUPERVISOR"
echo ""

# CASE_WORKER rules
CASE_WORKER_RULES=(
    "employeeid:PARTIAL_MASK:MASKED_ACCESS:true"
    "employeename:PARTIAL_MASK:MASKED_ACCESS:true"
    "location:NONE:FULL_ACCESS:true"
    "department:PARTIAL_MASK:MASKED_ACCESS:true"
    "status:PARTIAL_MASK:MASKED_ACCESS:true"
)

# SUPERVISOR rules
SUPERVISOR_RULES=(
    "employeeid:NONE:FULL_ACCESS:true"
    "employeename:NONE:FULL_ACCESS:true"
    "location:NONE:FULL_ACCESS:true"
    "department:NONE:FULL_ACCESS:true"
    "status:NONE:FULL_ACCESS:true"
)

echo "=========================================="
echo "Setting Rules for CASE_WORKER"
echo "=========================================="
set_role_attributes "CASE_WORKER" "${CASE_WORKER_RULES[@]}"

echo ""
echo "=========================================="
echo "Setting Rules for SUPERVISOR"
echo "=========================================="
set_role_attributes "SUPERVISOR" "${SUPERVISOR_RULES[@]}"

echo ""
echo "=========================================="
echo "✅ Field Masking Rules Setup Complete"
echo "=========================================="

