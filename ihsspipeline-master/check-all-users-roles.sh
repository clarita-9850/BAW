#!/bin/bash

ADMIN_TOKEN=$(curl -s -X POST 'http://localhost:8085/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=admin' \
  -d 'password=admin123' \
  -d 'grant_type=password' \
  -d 'client_id=admin-cli' | python3 -c "import sys, json; print(json.load(sys.stdin).get('access_token', ''))" 2>/dev/null)

CLIENT_ID="da47fe23-9bb5-475a-82b6-4a875816b108"

echo "=== ALL USERS AND THEIR CLIENT ROLES IN trial-app ==="
echo ""

# Get all users
USERS_JSON=$(curl -s -X GET "http://localhost:8085/admin/realms/cmips/users?max=1000" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

# Process each user
python3 << EOF
import sys
import json
import urllib.request

users = json.loads('''$USERS_JSON''')
admin_token = '$ADMIN_TOKEN'
client_id = '$CLIENT_ID'

print(f'Total users: {len(users)}')
print('=' * 80)
print()

for user in users:
    username = user.get('username', 'N/A')
    user_id = user.get('id', '')
    
    if not user_id:
        continue
    
    # Get client roles
    url = f'http://localhost:8085/admin/realms/cmips/users/{user_id}/role-mappings/clients/{client_id}'
    req = urllib.request.Request(url)
    req.add_header('Authorization', f'Bearer {admin_token}')
    
    try:
        with urllib.request.urlopen(req) as response:
            client_roles = json.loads(response.read())
            role_names = [r.get('name', 'N/A') for r in client_roles if r.get('name')]
    except Exception as e:
        role_names = []
    
    # Get realm roles
    url = f'http://localhost:8085/admin/realms/cmips/users/{user_id}/role-mappings/realm'
    req = urllib.request.Request(url)
    req.add_header('Authorization', f'Bearer {admin_token}')
    
    try:
        with urllib.request.urlopen(req) as response:
            realm_roles = json.loads(response.read())
            realm_role_names = [r.get('name', 'N/A') for r in realm_roles if r.get('name')]
    except Exception as e:
        realm_role_names = []
    
    print(f'Username: {username}')
    client_roles_str = ', '.join(role_names) if role_names else 'None'
    print(f'  Client Roles (trial-app): {client_roles_str}')
    realm_roles_str = ', '.join(realm_role_names[:5]) if realm_role_names else 'None'
    if len(realm_role_names) > 5:
        realm_roles_str += f' (+{len(realm_role_names)-5} more)'
    print(f'  Realm Roles: {realm_roles_str}')
    print()
EOF

