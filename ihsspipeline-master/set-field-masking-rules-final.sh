#!/bin/bash

# Script to set field masking rules in Keycloak for existing roles
# Uses Python to properly format JSON for Keycloak Admin API

CONTAINER_NAME="cmips-keycloak"
REALM_NAME="cmips"

echo "🔐 Setting Field Masking Rules in Keycloak"
echo "=========================================="
echo ""

# Authenticate
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

# Function to update role attributes using Python script inside container
update_role_attributes() {
    local role_name=$1
    shift
    local rules=("$@")
    
    echo "📝 Updating field masking rules for role: $role_name"
    
    # Check if role exists
    ROLE_CHECK=$(docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh get "roles/$role_name" -r $REALM_NAME 2>&1)
    if echo "$ROLE_CHECK" | grep -q "Could not find role"; then
        echo "  ⚠️  Role $role_name does not exist as realm role"
        echo "  ℹ️  Note: Backend fetches from realm roles. If this role exists only as client role,"
        echo "     you may need to create a corresponding realm role or update backend to use client roles."
        return 1
    fi
    
    # Build Python script to update role
    docker exec $CONTAINER_NAME python3 <<PYTHON_SCRIPT
import subprocess
import json
import sys

role_name = "$role_name"
realm_name = "$REALM_NAME"
rules = [$(printf '"%s",' "${rules[@]}" | sed 's/,$//')]

# Get current role
result = subprocess.run(
    ['/opt/keycloak/bin/kcadm.sh', 'get', f'roles/{role_name}', '-r', realm_name],
    capture_output=True, text=True
)

if result.returncode != 0:
    print(f"Error: Could not get role {role_name}", file=sys.stderr)
    sys.exit(1)

# Parse role data (kcadm outputs in a specific format, we'll use a simpler approach)
# Get admin token for REST API
token_result = subprocess.run(
    ['curl', '-s', '-X', 'POST', 'http://localhost:8080/realms/master/protocol/openid-connect/token',
     '-H', 'Content-Type: application/x-www-form-urlencoded',
     '-d', 'grant_type=password&client_id=admin-cli&username=admin&password=admin123'],
    capture_output=True, text=True
)

if token_result.returncode != 0:
    print("Error: Could not get admin token", file=sys.stderr)
    sys.exit(1)

token_data = json.loads(token_result.stdout)
access_token = token_data.get('access_token')

if not access_token:
    print("Error: No access token in response", file=sys.stderr)
    sys.exit(1)

# Get current role via REST API
import urllib.request
import urllib.parse

get_url = f'http://localhost:8080/admin/realms/{realm_name}/roles/{role_name}'
req = urllib.request.Request(get_url)
req.add_header('Authorization', f'Bearer {access_token}')
req.add_header('Content-Type', 'application/json')

try:
    with urllib.request.urlopen(req) as response:
        current_role = json.loads(response.read().decode())
except urllib.error.HTTPError as e:
    print(f"Error getting role: {e.code}", file=sys.stderr)
    sys.exit(1)

# Update attributes
if 'attributes' not in current_role:
    current_role['attributes'] = {}

current_role['attributes']['field_masking_rules'] = rules

# Update role via REST API
update_url = f'http://localhost:8080/admin/realms/{realm_name}/roles/{role_name}'
update_data = json.dumps(current_role).encode('utf-8')

update_req = urllib.request.Request(update_url, data=update_data, method='PUT')
update_req.add_header('Authorization', f'Bearer {access_token}')
update_req.add_header('Content-Type', 'application/json')

try:
    with urllib.request.urlopen(update_req) as response:
        if response.status in [200, 204]:
            print(f"Success: Updated role {role_name}")
            sys.exit(0)
        else:
            print(f"Error: HTTP {response.status}", file=sys.stderr)
            sys.exit(1)
except urllib.error.HTTPError as e:
    error_body = e.read().decode() if e.fp else "No error details"
    print(f"Error updating role: {e.code} - {error_body}", file=sys.stderr)
    sys.exit(1)
PYTHON_SCRIPT

    if [ $? -eq 0 ]; then
        echo "  ✅ Successfully updated field masking rules for $role_name"
        return 0
    else
        echo "  ❌ Failed to update field masking rules for $role_name"
        return 1
    fi
}

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
update_role_attributes "CASE_WORKER" "${CASE_WORKER_RULES[@]}"

echo ""
echo "=========================================="
echo "Setting Rules for SUPERVISOR"
echo "=========================================="
update_role_attributes "SUPERVISOR" "${SUPERVISOR_RULES[@]}"

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

