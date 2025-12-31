#!/bin/bash

# Script to check Keycloak role mappings for supervisor_CT4

echo "=== Checking Keycloak Role Mappings ==="
echo ""

# Configure admin CLI
docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin123 --client admin-cli" 2>&1 | grep -v "Using.*config"

echo ""
echo "=== Getting supervisor_CT4 User ID ==="
USER_ID=$(docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh get users --realm cmips -q username=supervisor_CT4 --fields id --format csv --noquotes 2>&1" | grep -v "Using.*config" | tail -1)
echo "User ID: $USER_ID"

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
    echo "ERROR: User not found!"
    exit 1
fi

echo ""
echo "=== REALM ROLE MAPPINGS ==="
docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh get users/$USER_ID/role-mappings/realm --realm cmips 2>&1" | grep -v "Using.*config" | python3 -m json.tool

echo ""
echo "=== Getting trial-app Client ID ==="
CLIENT_ID=$(docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh get clients --realm cmips -q clientId=trial-app --fields id --format csv --noquotes 2>&1" | grep -v "Using.*config" | tail -1)
echo "Client ID: $CLIENT_ID"

if [ -z "$CLIENT_ID" ] || [ "$CLIENT_ID" = "null" ]; then
    echo "ERROR: trial-app client not found!"
    exit 1
fi

echo ""
echo "=== CLIENT ROLES IN trial-app ==="
docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh get clients/$CLIENT_ID/roles --realm cmips 2>&1" | grep -v "Using.*config" | python3 -m json.tool

echo ""
echo "=== CLIENT ROLE MAPPINGS FOR supervisor_CT4 ==="
docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh get users/$USER_ID/role-mappings/clients/$CLIENT_ID --realm cmips 2>&1" | grep -v "Using.*config" | python3 -m json.tool

echo ""
echo "=== User Attributes ==="
docker exec cmips-keycloak bash -c "cd /opt/keycloak && ./bin/kcadm.sh get users/$USER_ID --realm cmips --fields attributes 2>&1" | grep -v "Using.*config" | python3 -m json.tool

