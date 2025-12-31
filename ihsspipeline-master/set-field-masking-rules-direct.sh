#!/bin/bash

# Script to set field masking rules in Keycloak using kcadm.sh
# Format: "fieldName:maskingType:accessLevel:enabled"

CONTAINER_NAME="cmips-keycloak"
REALM_NAME="cmips"

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

# Function to set role attributes using kcadm.sh
set_role_attributes() {
    local role_name=$1
    shift
    local rules=("$@")
    
    echo "📝 Setting field masking rules for role: $role_name"
    
    # Check if role exists
    ROLE_CHECK=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "roles/$role_name" -r $REALM_NAME 2>&1)
    if echo "$ROLE_CHECK" | grep -q "Could not find role"; then
        echo "  ⚠️  Role $role_name does not exist, skipping..."
        return 1
    fi
    
    # Build the attributes string for kcadm.sh
    # Format: attributes.field_masking_rules=["rule1","rule2",...]
    RULES_JSON="["
    for i in "${!rules[@]}"; do
        if [ $i -gt 0 ]; then
            RULES_JSON+=","
        fi
        RULES_JSON+="\"${rules[$i]}\""
    done
    RULES_JSON+="]"
    
    # Update role with attributes
    # Note: kcadm.sh requires escaping quotes properly
    ATTRIBUTE_STRING="attributes.field_masking_rules=$RULES_JSON"
    
    docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh update "roles/$role_name" -r $REALM_NAME \
        -s "$ATTRIBUTE_STRING" \
        > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "  ✅ Successfully set field masking rules for $role_name"
        echo "  Rules: ${rules[*]}"
        return 0
    else
        echo "  ❌ Failed to set field masking rules for $role_name"
        return 1
    fi
}

# CASE_WORKER rules:
# - employeeid: PARTIAL_MASK, MASKED_ACCESS
# - employeename: PARTIAL_MASK, MASKED_ACCESS
# - location: NONE, FULL_ACCESS (no masking)
# - department: PARTIAL_MASK, MASKED_ACCESS
# - status: PARTIAL_MASK, MASKED_ACCESS

CASE_WORKER_RULES=(
    "employeeid:PARTIAL_MASK:MASKED_ACCESS:true"
    "employeename:PARTIAL_MASK:MASKED_ACCESS:true"
    "location:NONE:FULL_ACCESS:true"
    "department:PARTIAL_MASK:MASKED_ACCESS:true"
    "status:PARTIAL_MASK:MASKED_ACCESS:true"
)

# SUPERVISOR rules:
# - employeeid: NONE, FULL_ACCESS (no masking)
# - employeename: NONE, FULL_ACCESS (no masking)
# - location: NONE, FULL_ACCESS (no masking)
# - department: NONE, FULL_ACCESS (no masking)
# - status: NONE, FULL_ACCESS (no masking)

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
echo "Verifying Rules"
echo "=========================================="

# Verify CASE_WORKER
echo "Checking CASE_WORKER rules..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "roles/CASE_WORKER" -r $REALM_NAME --fields attributes 2>/dev/null | grep -A 5 "field_masking_rules" || echo "  ⚠️  Could not verify CASE_WORKER rules"

# Verify SUPERVISOR
echo "Checking SUPERVISOR rules..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "roles/SUPERVISOR" -r $REALM_NAME --fields attributes 2>/dev/null | grep -A 5 "field_masking_rules" || echo "  ⚠️  Could not verify SUPERVISOR rules"

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

