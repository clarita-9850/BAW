#!/bin/bash

# Script to restore Keycloak Authorization Services configuration
# This sets up resources, scopes, policies, and permissions

set -e

echo "🔐 Restoring Keycloak Authorization Services Configuration..."
echo ""

# Get admin token
echo "1️⃣ Authenticating with Keycloak..."
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin123

# Get client ID
echo ""
echo "2️⃣ Getting cmips-backend client ID..."
CLIENT_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients -r cmips --query clientId=cmips-backend --fields id --format csv --noquotes | tail -1)
echo "Client ID: $CLIENT_ID"

# Get realm role IDs
echo ""
echo "3️⃣ Getting role IDs..."
PROVIDER_ROLE_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get roles/PROVIDER -r cmips --fields id --format csv --noquotes | tail -1)
RECIPIENT_ROLE_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get roles/RECIPIENT -r cmips --fields id --format csv --noquotes | tail -1)
CASE_WORKER_ROLE_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get roles/CASE_WORKER -r cmips --fields id --format csv --noquotes | tail -1)

echo "PROVIDER Role ID: $PROVIDER_ROLE_ID"
echo "RECIPIENT Role ID: $RECIPIENT_ROLE_ID"
echo "CASE_WORKER Role ID: $CASE_WORKER_ROLE_ID"

echo ""
echo "4️⃣ Creating Resources and Scopes..."

# Create Timesheet Resource with all scopes
echo "   Creating Timesheet Resource..."
docker exec cmips-keycloak sh -lc "cat > /tmp/timesheet-resource.json <<'EOF'
{
  \"name\": \"Timesheet Resource\",
  \"displayName\": \"Timesheet Resource\",
  \"type\": \"Timesheet\",
  \"ownerManagedAccess\": false,
  \"uris\": [
    \"/api/timesheets/*\",
    \"/api/timesheets\",
    \"/api/timesheets/*/submit\",
    \"/api/timesheets/*/approve\",
    \"/api/timesheets/*/reject\"
  ],
  \"scopes\": [
    {\"name\": \"create\", \"displayName\": \"Create Timesheet\"},
    {\"name\": \"read\", \"displayName\": \"Read Timesheet\"},
    {\"name\": \"update\", \"displayName\": \"Update Timesheet\"},
    {\"name\": \"delete\", \"displayName\": \"Delete Timesheet\"},
    {\"name\": \"submit\", \"displayName\": \"Submit Timesheet\"},
    {\"name\": \"approve\", \"displayName\": \"Approve Timesheet\"},
    {\"name\": \"reject\", \"displayName\": \"Reject Timesheet\"},
    {\"name\": \"timesheet:employee_id\", \"displayName\": \"View Employee ID\"},
    {\"name\": \"timesheet:employee_name\", \"displayName\": \"View Employee Name\"},
    {\"name\": \"timesheet:department\", \"displayName\": \"View Department\"},
    {\"name\": \"timesheet:location\", \"displayName\": \"View Location\"},
    {\"name\": \"timesheet:regular_hours\", \"displayName\": \"View Regular Hours\"},
    {\"name\": \"timesheet:overtime_hours\", \"displayName\": \"View Overtime Hours\"},
    {\"name\": \"timesheet:holiday_hours\", \"displayName\": \"View Holiday Hours\"},
    {\"name\": \"timesheet:sick_hours\", \"displayName\": \"View Sick Hours\"},
    {\"name\": \"timesheet:vacation_hours\", \"displayName\": \"View Vacation Hours\"},
    {\"name\": \"timesheet:comments\", \"displayName\": \"View Comments\"},
    {\"name\": \"timesheet:supervisor_comments\", \"displayName\": \"View Supervisor Comments\"},
    {\"name\": \"timesheet:approval_info\", \"displayName\": \"View Approval Information\"}
  ]
}
EOF
"

TIMESHEET_RESOURCE_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/resource -r cmips -f /tmp/timesheet-resource.json --id --format csv --noquotes 2>&1 | tail -1 | grep -o '[a-f0-9-]\{36\}' || echo "")
echo "   Timesheet Resource ID: $TIMESHEET_RESOURCE_ID"

# Create EVV Resource
echo "   Creating EVV Resource..."
docker exec cmips-keycloak sh -lc "cat > /tmp/evv-resource.json <<'EOF'
{
  \"name\": \"EVV Resource\",
  \"displayName\": \"EVV Resource\",
  \"type\": \"EVV\",
  \"ownerManagedAccess\": false,
  \"uris\": [
    \"/api/evv/*\",
    \"/api/evv\"
  ],
  \"scopes\": [
    {\"name\": \"create\", \"displayName\": \"Create EVV Record\"},
    {\"name\": \"read\", \"displayName\": \"Read EVV Record\"},
    {\"name\": \"update\", \"displayName\": \"Update EVV Record\"},
    {\"name\": \"delete\", \"displayName\": \"Delete EVV Record\"}
  ]
}
EOF
"

EVV_RESOURCE_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/resource -r cmips -f /tmp/evv-resource.json --id --format csv --noquotes 2>&1 | tail -1 | grep -o '[a-f0-9-]\{36\}' || echo "")
echo "   EVV Resource ID: $EVV_RESOURCE_ID"

# Create Provider-Recipient Resource
echo "   Creating Provider-Recipient Resource..."
docker exec cmips-keycloak sh -lc "cat > /tmp/provider-recipient-resource.json <<'EOF'
{
  \"name\": \"Provider-Recipient Resource\",
  \"displayName\": \"Provider-Recipient Resource\",
  \"type\": \"ProviderRecipient\",
  \"ownerManagedAccess\": false,
  \"uris\": [
    \"/api/provider-recipient/*\",
    \"/api/provider-recipient\"
  ],
  \"scopes\": [
    {\"name\": \"create\", \"displayName\": \"Create Provider-Recipient Relationship\"},
    {\"name\": \"read\", \"displayName\": \"Read Provider-Recipient Relationship\"},
    {\"name\": \"update\", \"displayName\": \"Update Provider-Recipient Relationship\"},
    {\"name\": \"delete\", \"displayName\": \"Delete Provider-Recipient Relationship\"}
  ]
}
EOF
"

PROVIDER_RECIPIENT_RESOURCE_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/resource -r cmips -f /tmp/provider-recipient-resource.json --id --format csv --noquotes 2>&1 | tail -1 | grep -o '[a-f0-9-]\{36\}' || echo "")
echo "   Provider-Recipient Resource ID: $PROVIDER_RECIPIENT_RESOURCE_ID"

echo ""
echo "5️⃣ Creating Policies..."

# Create Role Policies
echo "   Creating Provider Policy..."
docker exec cmips-keycloak sh -lc "cat > /tmp/provider-policy.json <<'EOF'
{
  \"name\": \"Provider Policy\",
  \"type\": \"role\",
  \"logic\": \"POSITIVE\",
  \"decisionStrategy\": \"UNANIMOUS\",
  \"config\": {
    \"roles\": \"[{\\\"id\\\":\\\"$PROVIDER_ROLE_ID\\\",\\\"required\\\":false}]\"
  }
}
EOF
"

docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/policy/role -r cmips -f /tmp/provider-policy.json 2>&1 | grep -q "Created\|already exists" && echo "   ✅ Provider Policy created" || echo "   ⚠️ Provider Policy may already exist"

echo "   Creating Recipient Policy..."
docker exec cmips-keycloak sh -lc "cat > /tmp/recipient-policy.json <<'EOF'
{
  \"name\": \"Recipient Policy\",
  \"type\": \"role\",
  \"logic\": \"POSITIVE\",
  \"decisionStrategy\": \"UNANIMOUS\",
  \"config\": {
    \"roles\": \"[{\\\"id\\\":\\\"$RECIPIENT_ROLE_ID\\\",\\\"required\\\":false}]\"
  }
}
EOF
"

docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/policy/role -r cmips -f /tmp/recipient-policy.json 2>&1 | grep -q "Created\|already exists" && echo "   ✅ Recipient Policy created" || echo "   ⚠️ Recipient Policy may already exist"

echo "   Creating Case Worker Policy..."
docker exec cmips-keycloak sh -lc "cat > /tmp/caseworker-policy.json <<'EOF'
{
  \"name\": \"Case Worker Policy\",
  \"type\": \"role\",
  \"logic\": \"POSITIVE\",
  \"decisionStrategy\": \"UNANIMOUS\",
  \"config\": {
    \"roles\": \"[{\\\"id\\\":\\\"$CASE_WORKER_ROLE_ID\\\",\\\"required\\\":false}]\"
  }
}
EOF
"

docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/policy/role -r cmips -f /tmp/caseworker-policy.json 2>&1 | grep -q "Created\|already exists" && echo "   ✅ Case Worker Policy created" || echo "   ⚠️ Case Worker Policy may already exist"

# Get policy IDs
PROVIDER_POLICY_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients/$CLIENT_ID/authz/resource-server/policy -r cmips --query name="Provider Policy" --fields id --format csv --noquotes | tail -1)
RECIPIENT_POLICY_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients/$CLIENT_ID/authz/resource-server/policy -r cmips --query name="Recipient Policy" --fields id --format csv --noquotes | tail -1)
CASE_WORKER_POLICY_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients/$CLIENT_ID/authz/resource-server/policy -r cmips --query name="Case Worker Policy" --fields id --format csv --noquotes | tail -1)

# Create Aggregate Policies
echo "   Creating Aggregate Policies..."
docker exec cmips-keycloak sh -lc "cat > /tmp/provider-caseworker-policy.json <<'EOF'
{
  \"name\": \"Provider or Case Worker Policy\",
  \"type\": \"aggregate\",
  \"logic\": \"POSITIVE\",
  \"decisionStrategy\": \"AFFIRMATIVE\",
  \"policies\": [\"$PROVIDER_POLICY_ID\", \"$CASE_WORKER_POLICY_ID\"]
}
EOF
"

docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/policy/aggregate -r cmips -f /tmp/provider-caseworker-policy.json 2>&1 | grep -q "Created\|already exists" && echo "   ✅ Provider or Case Worker Policy created" || echo "   ⚠️ May already exist"

docker exec cmips-keycloak sh -lc "cat > /tmp/recipient-caseworker-policy.json <<'EOF'
{
  \"name\": \"Recipient or Case Worker Policy\",
  \"type\": \"aggregate\",
  \"logic\": \"POSITIVE\",
  \"decisionStrategy\": \"AFFIRMATIVE\",
  \"policies\": [\"$RECIPIENT_POLICY_ID\", \"$CASE_WORKER_POLICY_ID\"]
}
EOF
"

docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_ID/authz/resource-server/policy/aggregate -r cmips -f /tmp/recipient-caseworker-policy.json 2>&1 | grep -q "Created\|already exists" && echo "   ✅ Recipient or Case Worker Policy created" || echo "   ⚠️ May already exist"

echo ""
echo "✅ Configuration restored successfully!"
echo ""
echo "📝 Next steps:"
echo "   1. Go to Keycloak Admin Console: http://localhost:8080/admin"
echo "   2. Navigate to: Clients → cmips-backend → Authorization"
echo "   3. Review Resources, Policies, and Permissions"
echo "   4. Create Permissions manually via UI or use the KeycloakAdminController API endpoints"


