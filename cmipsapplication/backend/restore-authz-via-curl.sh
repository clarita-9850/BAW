#!/bin/bash

KEYCLOAK_URL="http://localhost:8080"
REALM="cmips"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin123"

echo "🔐 Getting admin token..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "❌ Failed to get token"
  exit 1
fi

echo "✅ Token obtained"

# Get client UUID
echo "📋 Getting cmips-backend client UUID..."
CLIENT_RESPONSE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/clients?clientId=cmips-backend" \
  -H "Authorization: Bearer $TOKEN")
CLIENT_UUID=$(echo $CLIENT_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)[0]['id'])" 2>/dev/null)

if [ -z "$CLIENT_UUID" ]; then
  echo "❌ Client not found"
  exit 1
fi

echo "✅ Client UUID: $CLIENT_UUID"

# Get role IDs
echo "📋 Getting role IDs..."
PROVIDER_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/roles/PROVIDER" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
RECIPIENT_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/roles/RECIPIENT" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
CASE_WORKER_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/roles/CASE_WORKER" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null)

echo "✅ PROVIDER Role ID: $PROVIDER_ROLE_ID"
echo "✅ RECIPIENT Role ID: $RECIPIENT_ROLE_ID"
echo "✅ CASE_WORKER Role ID: $CASE_WORKER_ROLE_ID"

# Create Timesheet Resource
echo ""
echo "📝 Creating Timesheet Resource..."
TIMESHEET_RESOURCE=$(cat <<JSON
{
  "name": "Timesheet Resource",
  "displayName": "Timesheet Resource",
  "type": "Timesheet",
  "ownerManagedAccess": false,
  "uris": [
    "/api/timesheets/*",
    "/api/timesheets",
    "/api/timesheets/*/submit",
    "/api/timesheets/*/approve",
    "/api/timesheets/*/reject"
  ],
  "scopes": [
    {"name": "create", "displayName": "Create Timesheet"},
    {"name": "read", "displayName": "Read Timesheet"},
    {"name": "update", "displayName": "Update Timesheet"},
    {"name": "delete", "displayName": "Delete Timesheet"},
    {"name": "submit", "displayName": "Submit Timesheet"},
    {"name": "approve", "displayName": "Approve Timesheet"},
    {"name": "reject", "displayName": "Reject Timesheet"},
    {"name": "timesheet:employee_id", "displayName": "View Employee ID"},
    {"name": "timesheet:employee_name", "displayName": "View Employee Name"},
    {"name": "timesheet:department", "displayName": "View Department"},
    {"name": "timesheet:location", "displayName": "View Location"},
    {"name": "timesheet:regular_hours", "displayName": "View Regular Hours"},
    {"name": "timesheet:overtime_hours", "displayName": "View Overtime Hours"},
    {"name": "timesheet:holiday_hours", "displayName": "View Holiday Hours"},
    {"name": "timesheet:sick_hours", "displayName": "View Sick Hours"},
    {"name": "timesheet:vacation_hours", "displayName": "View Vacation Hours"},
    {"name": "timesheet:comments", "displayName": "View Comments"},
    {"name": "timesheet:supervisor_comments", "displayName": "View Supervisor Comments"},
    {"name": "timesheet:approval_info", "displayName": "View Approval Information"}
  ]
}
JSON
)

TIMESHEET_RESULT=$(curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/clients/$CLIENT_UUID/authz/resource-server/resource" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$TIMESHEET_RESOURCE")

if echo "$TIMESHEET_RESULT" | grep -q "_id"; then
  echo "✅ Timesheet Resource created"
  TIMESHEET_RESOURCE_ID=$(echo "$TIMESHEET_RESULT" | python3 -c "import sys, json; print(json.load(sys.stdin)['_id'])" 2>/dev/null)
else
  echo "⚠️ Timesheet Resource may already exist or failed"
  TIMESHEET_RESOURCE_ID=""
fi

# Create EVV Resource  
echo "📝 Creating EVV Resource..."
EVV_RESOURCE=$(cat <<JSON
{
  "name": "EVV Resource",
  "displayName": "EVV Resource",
  "type": "EVV",
  "ownerManagedAccess": false,
  "uris": ["/api/evv/*", "/api/evv"],
  "scopes": [
    {"name": "create", "displayName": "Create EVV Record"},
    {"name": "read", "displayName": "Read EVV Record"},
    {"name": "update", "displayName": "Update EVV Record"},
    {"name": "delete", "displayName": "Delete EVV Record"}
  ]
}
JSON
)

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/clients/$CLIENT_UUID/authz/resource-server/resource" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$EVV_RESOURCE" > /dev/null && echo "✅ EVV Resource created" || echo "⚠️ EVV Resource may already exist"

# Create Provider-Recipient Resource
echo "📝 Creating Provider-Recipient Resource..."
PROVIDER_RECIPIENT_RESOURCE=$(cat <<JSON
{
  "name": "Provider-Recipient Resource",
  "displayName": "Provider-Recipient Resource",
  "type": "ProviderRecipient",
  "ownerManagedAccess": false,
  "uris": ["/api/provider-recipient/*", "/api/provider-recipient"],
  "scopes": [
    {"name": "create", "displayName": "Create Provider-Recipient Relationship"},
    {"name": "read", "displayName": "Read Provider-Recipient Relationship"},
    {"name": "update", "displayName": "Update Provider-Recipient Relationship"},
    {"name": "delete", "displayName": "Delete Provider-Recipient Relationship"}
  ]
}
JSON
)

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/clients/$CLIENT_UUID/authz/resource-server/resource" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$PROVIDER_RECIPIENT_RESOURCE" > /dev/null && echo "✅ Provider-Recipient Resource created" || echo "⚠️ Provider-Recipient Resource may already exist"

echo ""
echo "✅ Resources created successfully!"
echo ""
echo "📝 Next: Create Policies and Permissions via Keycloak Admin UI:"
echo "   http://localhost:8080/admin → Clients → cmips-backend → Authorization"


