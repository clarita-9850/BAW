#!/bin/bash

# Script to set field masking rules in Keycloak using Admin API
# Format: "fieldName:maskingType:accessLevel:enabled"

KEYCLOAK_URL="http://localhost:8080"
REALM_NAME="cmips"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin123"

echo "🔐 Setting Field Masking Rules in Keycloak via Admin API"
echo "=========================================="
echo ""

# Get admin access token
echo "🔐 Getting admin access token..."
TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; data = json.load(sys.stdin); print(data.get('access_token', ''))" 2>/dev/null)

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "None" ]; then
    echo "❌ Failed to get admin access token"
    exit 1
fi

echo "✅ Got admin access token"
echo ""

# Function to update role attributes
update_role_attributes() {
    local role_name=$1
    local rules_array=$2
    
    echo "📝 Updating field masking rules for role: $role_name"
    
    # First, get current role to preserve existing data
    CURRENT_ROLE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/$role_name" \
      -H "Authorization: Bearer $ACCESS_TOKEN")
    
    if echo "$CURRENT_ROLE" | grep -q "Could not find role"; then
        echo "  ⚠️  Role $role_name does not exist, skipping..."
        return 1
    fi
    
    # Extract current attributes (if any) and preserve other role properties
    # Build update payload
    UPDATE_PAYLOAD=$(echo "$CURRENT_ROLE" | python3 -c "
import sys, json
role = json.load(sys.stdin)
rules = $rules_array

# Preserve existing attributes if they exist
attributes = {}
if 'attributes' in role and role['attributes']:
    attributes = role['attributes'].copy()

# Update field_masking_rules
attributes['field_masking_rules'] = rules

# Preserve all other role properties
role['attributes'] = attributes

print(json.dumps(role))
" 2>/dev/null)
    
    if [ -z "$UPDATE_PAYLOAD" ]; then
        echo "  ❌ Failed to build update payload"
        return 1
    fi
    
    # Update role
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/$role_name" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$UPDATE_PAYLOAD")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    
    if [ "$HTTP_CODE" = "204" ] || [ "$HTTP_CODE" = "200" ]; then
        echo "  ✅ Successfully updated field masking rules for $role_name"
        return 0
    else
        echo "  ❌ Failed to update field masking rules for $role_name (HTTP $HTTP_CODE)"
        echo "  Response: $(echo "$RESPONSE" | head -n-1)"
        return 1
    fi
}

# CASE_WORKER rules:
# - employeeid: PARTIAL_MASK, MASKED_ACCESS
# - employeename: PARTIAL_MASK, MASKED_ACCESS
# - location: NONE, FULL_ACCESS (no masking)
# - department: PARTIAL_MASK, MASKED_ACCESS
# - status: PARTIAL_MASK, MASKED_ACCESS

CASE_WORKER_RULES='["employeeid:PARTIAL_MASK:MASKED_ACCESS:true","employeename:PARTIAL_MASK:MASKED_ACCESS:true","location:NONE:FULL_ACCESS:true","department:PARTIAL_MASK:MASKED_ACCESS:true","status:PARTIAL_MASK:MASKED_ACCESS:true"]'

# SUPERVISOR rules:
# - employeeid: NONE, FULL_ACCESS (no masking)
# - employeename: NONE, FULL_ACCESS (no masking)
# - location: NONE, FULL_ACCESS (no masking)
# - department: NONE, FULL_ACCESS (no masking)
# - status: NONE, FULL_ACCESS (no masking)

SUPERVISOR_RULES='["employeeid:NONE:FULL_ACCESS:true","employeename:NONE:FULL_ACCESS:true","location:NONE:FULL_ACCESS:true","department:NONE:FULL_ACCESS:true","status:NONE:FULL_ACCESS:true"]'

echo "=========================================="
echo "Setting Rules for CASE_WORKER"
echo "=========================================="
update_role_attributes "CASE_WORKER" "$CASE_WORKER_RULES"

echo ""
echo "=========================================="
echo "Setting Rules for SUPERVISOR"
echo "=========================================="
update_role_attributes "SUPERVISOR" "$SUPERVISOR_RULES"

echo ""
echo "=========================================="
echo "✅ Field Masking Rules Setup Complete"
echo "=========================================="
echo ""
echo "Summary:"
echo "  CASE_WORKER: employeeid, employeename, department, status = PARTIAL_MASK"
echo "  CASE_WORKER: location = NONE (full visibility)"
echo "  SUPERVISOR: All fields = NONE (full visibility)"
echo ""
echo "Rules will appear in JWT tokens on next login."

