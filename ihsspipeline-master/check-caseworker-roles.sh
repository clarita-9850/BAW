#!/bin/bash

echo "=== Checking Caseworker User and Roles in trial-app Client ==="
echo ""

# Get admin token
echo "Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST 'http://localhost:8085/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=admin' \
  -d 'password=admin123' \
  -d 'grant_type=password' \
  -d 'client_id=admin-cli' | python3 -c "import sys, json; print(json.load(sys.stdin).get('access_token', 'ERROR'))" 2>/dev/null)

if [ "$ADMIN_TOKEN" = "ERROR" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "ERROR: Failed to get admin token"
    echo "Trying with cmips realm..."
    ADMIN_TOKEN=$(curl -s -X POST 'http://localhost:8085/realms/cmips/protocol/openid-connect/token' \
      -H 'Content-Type: application/x-www-form-urlencoded' \
      -d 'username=admin' \
      -d 'password=admin123' \
      -d 'grant_type=password' \
      -d 'client_id=admin-cli' | python3 -c "import sys, json; print(json.load(sys.stdin).get('access_token', 'ERROR'))" 2>/dev/null)
fi

if [ "$ADMIN_TOKEN" = "ERROR" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "ERROR: Still failed to get admin token. Please check Keycloak admin credentials."
    exit 1
fi

echo "Admin token obtained"
echo ""

# Get caseworker user (try caseworker_CT1)
echo "=== Getting caseworker_CT1 User Info ==="
USER_RESPONSE=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/users?username=caseworker_CT1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

USER_ID=$(echo "$USER_RESPONSE" | python3 -c "import sys, json; users = json.load(sys.stdin); print(users[0]['id'] if users and len(users) > 0 else 'NOT_FOUND')" 2>/dev/null)

if [ "$USER_ID" = "NOT_FOUND" ] || [ -z "$USER_ID" ]; then
    echo "User caseworker_CT1 not found, trying to list all users..."
    curl -s -X GET "http://localhost:8085/admin/realms/cmips/users?max=100" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; users = json.load(sys.stdin); caseworkers = [u for u in users if 'caseworker' in u.get('username', '').lower()]; print('\n'.join([f\"{u['username']}: {u['id']}\" for u in caseworkers[:5]]))" 2>/dev/null
    exit 1
fi

echo "User ID: $USER_ID"
echo ""

# Get trial-app client ID
echo "=== Getting trial-app Client ID ==="
CLIENT_RESPONSE=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/clients?clientId=trial-app" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

CLIENT_ID=$(echo "$CLIENT_RESPONSE" | python3 -c "import sys, json; clients = json.load(sys.stdin); print(clients[0]['id'] if clients and len(clients) > 0 else 'NOT_FOUND')" 2>/dev/null)

if [ "$CLIENT_ID" = "NOT_FOUND" ] || [ -z "$CLIENT_ID" ]; then
    echo "ERROR: trial-app client not found!"
    exit 1
fi

echo "Client ID: $CLIENT_ID"
echo ""

# Get client roles in trial-app
echo "=== CLIENT ROLES IN trial-app ==="
curl -s -X GET "http://localhost:8085/admin/realms/cmips/clients/$CLIENT_ID/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool

echo ""
echo "=== CLIENT ROLE MAPPINGS FOR caseworker_CT1 ==="
curl -s -X GET "http://localhost:8085/admin/realms/cmips/users/$USER_ID/role-mappings/clients/$CLIENT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool

echo ""
echo "=== REALM ROLE MAPPINGS FOR caseworker_CT1 ==="
curl -s -X GET "http://localhost:8085/admin/realms/cmips/users/$USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool

echo ""
echo "=== User Attributes ==="
curl -s -X GET "http://localhost:8085/admin/realms/cmips/users/$USER_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys, json; user = json.load(sys.stdin); print(json.dumps(user.get('attributes', {}), indent=2))"

