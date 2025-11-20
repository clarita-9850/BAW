#!/usr/bin/env python3
"""
Script to restore Keycloak Authorization Services configuration
Creates resources, scopes, policies, and permissions
"""

import requests
import json
import time

KEYCLOAK_URL = "http://localhost:8080"
REALM = "cmips"
ADMIN_USER = "admin"
ADMIN_PASSWORD = "admin123"

def get_admin_token():
    """Get admin access token"""
    url = f"{KEYCLOAK_URL}/realms/master/protocol/openid-connect/token"
    data = {
        "grant_type": "password",
        "client_id": "admin-cli",
        "username": ADMIN_USER,
        "password": ADMIN_PASSWORD
    }
    response = requests.post(url, data=data)
    response.raise_for_status()
    return response.json()["access_token"]

def get_client_id(token, client_id_name):
    """Get client UUID by client ID"""
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/clients"
    headers = {"Authorization": f"Bearer {token}"}
    params = {"clientId": client_id_name}
    response = requests.get(url, headers=headers, params=params)
    response.raise_for_status()
    clients = response.json()
    if clients:
        return clients[0]["id"]
    return None

def get_role_id(token, role_name):
    """Get role UUID by role name"""
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/roles/{role_name}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()["id"]

def create_resource(token, client_uuid, resource_data):
    """Create a resource"""
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/clients/{client_uuid}/authz/resource-server/resource"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    response = requests.post(url, headers=headers, json=resource_data)
    if response.status_code == 201:
        print(f"   ✅ Created resource: {resource_data['name']}")
        return response.json()["_id"]
    elif response.status_code == 409:
        print(f"   ⚠️ Resource already exists: {resource_data['name']}")
        # Try to get existing resource
        get_url = f"{url}?name={resource_data['name']}"
        get_response = requests.get(get_url, headers=headers)
        if get_response.status_code == 200:
            resources = get_response.json()
            if resources:
                return resources[0]["_id"]
    else:
        print(f"   ❌ Failed to create resource {resource_data['name']}: {response.status_code} - {response.text}")
        response.raise_for_status()
    return None

def create_role_policy(token, client_uuid, policy_name, role_id):
    """Create a role-based policy"""
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/clients/{client_uuid}/authz/resource-server/policy/role"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    policy_data = {
        "name": policy_name,
        "type": "role",
        "logic": "POSITIVE",
        "decisionStrategy": "UNANIMOUS",
        "config": {
            "roles": json.dumps([{"id": role_id, "required": False}])
        }
    }
    response = requests.post(url, headers=headers, json=policy_data)
    if response.status_code == 201:
        print(f"   ✅ Created policy: {policy_name}")
        return response.json()["id"]
    elif response.status_code == 409:
        print(f"   ⚠️ Policy already exists: {policy_name}")
        # Get existing policy
        get_url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/clients/{client_uuid}/authz/resource-server/policy"
        get_response = requests.get(get_url, headers=headers, params={"name": policy_name})
        if get_response.status_code == 200:
            policies = get_response.json()
            if policies:
                return policies[0]["id"]
    else:
        print(f"   ❌ Failed to create policy {policy_name}: {response.status_code} - {response.text}")
        response.raise_for_status()
    return None

def create_aggregate_policy(token, client_uuid, policy_name, policy_ids, decision_strategy="AFFIRMATIVE"):
    """Create an aggregate policy"""
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/clients/{client_uuid}/authz/resource-server/policy/aggregate"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    policy_data = {
        "name": policy_name,
        "type": "aggregate",
        "logic": "POSITIVE",
        "decisionStrategy": decision_strategy,
        "policies": policy_ids
    }
    response = requests.post(url, headers=headers, json=policy_data)
    if response.status_code == 201:
        print(f"   ✅ Created aggregate policy: {policy_name}")
        return response.json()["id"]
    elif response.status_code == 409:
        print(f"   ⚠️ Policy already exists: {policy_name}")
        # Get existing policy
        get_url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/clients/{client_uuid}/authz/resource-server/policy"
        get_response = requests.get(get_url, headers=headers, params={"name": policy_name})
        if get_response.status_code == 200:
            policies = get_response.json()
            if policies:
                return policies[0]["id"]
    else:
        print(f"   ❌ Failed to create aggregate policy {policy_name}: {response.status_code} - {response.text}")
        response.raise_for_status()
    return None

def main():
    print("🔐 Restoring Keycloak Authorization Services Configuration...\n")
    
    # Get admin token
    print("1️⃣ Authenticating...")
    token = get_admin_token()
    print("   ✅ Authenticated\n")
    
    # Get client UUID
    print("2️⃣ Getting client ID...")
    client_uuid = get_client_id(token, "cmips-backend")
    if not client_uuid:
        print("   ❌ Client cmips-backend not found!")
        return
    print(f"   ✅ Client UUID: {client_uuid}\n")
    
    # Get role IDs
    print("3️⃣ Getting role IDs...")
    provider_role_id = get_role_id(token, "PROVIDER")
    recipient_role_id = get_role_id(token, "RECIPIENT")
    case_worker_role_id = get_role_id(token, "CASE_WORKER")
    print(f"   ✅ PROVIDER: {provider_role_id}")
    print(f"   ✅ RECIPIENT: {recipient_role_id}")
    print(f"   ✅ CASE_WORKER: {case_worker_role_id}\n")
    
    # Create Resources
    print("4️⃣ Creating Resources and Scopes...")
    
    timesheet_resource = {
        "name": "Timesheet Resource",
        "displayName": "Timesheet Resource",
        "type": "Timesheet",
        "ownerManagedAccess": False,
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
    timesheet_resource_id = create_resource(token, client_uuid, timesheet_resource)
    
    evv_resource = {
        "name": "EVV Resource",
        "displayName": "EVV Resource",
        "type": "EVV",
        "ownerManagedAccess": False,
        "uris": ["/api/evv/*", "/api/evv"],
        "scopes": [
            {"name": "create", "displayName": "Create EVV Record"},
            {"name": "read", "displayName": "Read EVV Record"},
            {"name": "update", "displayName": "Update EVV Record"},
            {"name": "delete", "displayName": "Delete EVV Record"}
        ]
    }
    evv_resource_id = create_resource(token, client_uuid, evv_resource)
    
    provider_recipient_resource = {
        "name": "Provider-Recipient Resource",
        "displayName": "Provider-Recipient Resource",
        "type": "ProviderRecipient",
        "ownerManagedAccess": False,
        "uris": ["/api/provider-recipient/*", "/api/provider-recipient"],
        "scopes": [
            {"name": "create", "displayName": "Create Provider-Recipient Relationship"},
            {"name": "read", "displayName": "Read Provider-Recipient Relationship"},
            {"name": "update", "displayName": "Update Provider-Recipient Relationship"},
            {"name": "delete", "displayName": "Delete Provider-Recipient Relationship"}
        ]
    }
    provider_recipient_resource_id = create_resource(token, client_uuid, provider_recipient_resource)
    
    print("\n5️⃣ Creating Policies...")
    
    # Create role policies
    provider_policy_id = create_role_policy(token, client_uuid, "Provider Policy", provider_role_id)
    recipient_policy_id = create_role_policy(token, client_uuid, "Recipient Policy", recipient_role_id)
    case_worker_policy_id = create_role_policy(token, client_uuid, "Case Worker Policy", case_worker_role_id)
    
    # Create aggregate policies
    time.sleep(1)  # Small delay to ensure policies are available
    provider_caseworker_policy_id = create_aggregate_policy(
        token, client_uuid, 
        "Provider or Case Worker Policy",
        [provider_policy_id, case_worker_policy_id]
    )
    
    recipient_caseworker_policy_id = create_aggregate_policy(
        token, client_uuid,
        "Recipient or Case Worker Policy",
        [recipient_policy_id, case_worker_policy_id]
    )
    
    print("\n✅ Resources and Policies created successfully!")
    print("\n📝 Next: Create Permissions via Keycloak Admin UI or use the KeycloakAdminController API")
    print("   Go to: http://localhost:8080/admin → Clients → cmips-backend → Authorization → Permissions")

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()


