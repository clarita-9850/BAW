#!/bin/bash

# Script to set field masking rules in Keycloak for CASE_WORKER and SUPERVISOR roles
# Format: "fieldName:maskingType:accessLevel:enabled"

CONTAINER_NAME="cmips-keycloak"
REALM_NAME="cmips"
KEYCLOAK_URL="http://localhost:8080"

echo "🔐 Setting Field Masking Rules in Keycloak"
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

# Function to update role attributes
update_role_attributes() {
    local role_name=$1
    local rules_json=$2
    
    echo "📝 Updating field masking rules for role: $role_name"
    
    # Get current role data
    CURRENT_ROLE=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "roles/$role_name" -r $REALM_NAME 2>/dev/null)
    
    if [ -z "$CURRENT_ROLE" ]; then
        echo "  ⚠️  Role $role_name does not exist, skipping..."
        return 1
    fi
    
    # Extract current attributes (if any)
    CURRENT_ATTRS=$(echo "$CURRENT_ROLE" | grep -o '"attributes":{[^}]*}' || echo "")
    
    # Build the update command with field_masking_rules
    # Keycloak expects attributes as JSON with arrays
    UPDATE_CMD="docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update \"roles/$role_name\" -r $REALM_NAME -b '{\"attributes\":{\"field_masking_rules\":$rules_json}}'"
    
    # Execute update
    eval $UPDATE_CMD > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "  ✅ Successfully updated field masking rules for $role_name"
        return 0
    else
        echo "  ❌ Failed to update field masking rules for $role_name"
        return 1
    fi
}

# CASE_WORKER rules:
# - employeeid: PARTIAL_MASK, MASKED_ACCESS
# - employeename: PARTIAL_MASK, MASKED_ACCESS
# - location: NONE, FULL_ACCESS (no masking)
# - department: PARTIAL_MASK, MASKED_ACCESS
# - status: PARTIAL_MASK, MASKED_ACCESS

CASE_WORKER_RULES='[
  "employeeid:PARTIAL_MASK:MASKED_ACCESS:true",
  "employeename:PARTIAL_MASK:MASKED_ACCESS:true",
  "location:NONE:FULL_ACCESS:true",
  "department:PARTIAL_MASK:MASKED_ACCESS:true",
  "status:PARTIAL_MASK:MASKED_ACCESS:true"
]'

# SUPERVISOR rules:
# - employeeid: NONE, FULL_ACCESS (no masking)
# - employeename: NONE, FULL_ACCESS (no masking)
# - location: NONE, FULL_ACCESS (no masking)
# - department: NONE, FULL_ACCESS (no masking)
# - status: NONE, FULL_ACCESS (no masking)

SUPERVISOR_RULES='[
  "employeeid:NONE:FULL_ACCESS:true",
  "employeename:NONE:FULL_ACCESS:true",
  "location:NONE:FULL_ACCESS:true",
  "department:NONE:FULL_ACCESS:true",
  "status:NONE:FULL_ACCESS:true"
]'

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

