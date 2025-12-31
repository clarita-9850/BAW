#!/usr/bin/env bash
# Configure Keycloak to include countyId in JWT tokens
# This script creates a User Attribute Mapper for the trial-app client
# to include the countyId user attribute in the JWT token

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8085}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
REALM_NAME="${REALM_NAME:-cmips}"
CLIENT_ID="${CLIENT_ID:-trial-app}"

echo "=========================================="
echo "Configuring Keycloak County Mapper"
echo "=========================================="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Realm: $REALM_NAME"
echo "Client: $CLIENT_ID"
echo ""

# Check if Keycloak is ready by testing the realm endpoint
echo "⏳ Checking Keycloak status..."
if ! curl -s -f "$KEYCLOAK_URL/realms/$REALM_NAME/.well-known/openid-configuration" > /dev/null 2>&1; then
    echo "❌ Keycloak is not responding at $KEYCLOAK_URL"
    echo "   Please ensure Keycloak is running and accessible"
    exit 1
fi
echo "✅ Keycloak is responding"

# Check for jq
if ! command -v jq &> /dev/null; then
    echo "❌ jq is required but not installed. Please install jq first."
    exit 1
fi

# Use kcadm.sh inside container (most reliable method)
echo ""
echo "🔐 Configuring Keycloak using kcadm.sh..."
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin123 > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo "❌ Failed to authenticate with Keycloak admin"
    exit 1
fi

echo "✅ Authenticated with Keycloak admin"

# Get client UUID
echo ""
echo "🔍 Getting client UUID for $CLIENT_ID..."
CLIENT_UUID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients -r "$REALM_NAME" --fields id,clientId --format csv --noquotes | grep ",$CLIENT_ID" | cut -d',' -f1)

if [ -z "$CLIENT_UUID" ]; then
    echo "❌ Client $CLIENT_ID not found in realm $REALM_NAME"
    exit 1
fi
echo "✅ Found client UUID: $CLIENT_UUID"

# Check if mapper already exists
echo ""
echo "🔍 Checking for existing countyId mapper..."
EXISTING_MAPPER_ID=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients/$CLIENT_UUID/protocol-mappers/models -r "$REALM_NAME" --fields id,name --format csv --noquotes 2>/dev/null | grep "countyId-mapper" | cut -d',' -f1 | head -1)

if [ -n "$EXISTING_MAPPER_ID" ]; then
    echo "  ℹ️  Mapper already exists (ID: $EXISTING_MAPPER_ID), deleting to recreate..."
    docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh delete clients/$CLIENT_UUID/protocol-mappers/models/$EXISTING_MAPPER_ID -r "$REALM_NAME" > /dev/null 2>&1
fi

echo "  ℹ️  Creating mapper..."
# Create mapper using kcadm.sh
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_UUID/protocol-mappers/models -r "$REALM_NAME" \
  -s name=countyId-mapper \
  -s protocol=openid-connect \
  -s protocolMapper=oidc-usermodel-attribute-mapper \
  -s 'config."user.attribute"=countyId' \
  -s 'config."claim.name"=countyId' \
  -s 'config."jsonType.label"=String' \
  -s 'config."id.token.claim"=true' \
  -s 'config."access.token.claim"=true' \
  -s 'config."userinfo.token.claim"=true' \
  -s 'config.multivalued=false' \
  -s 'config."aggregate.attrs"=false' > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "  ✅ Mapper created successfully"
else
    echo "  ⚠️  Mapper creation may have failed, but continuing..."
fi

echo ""
echo "=========================================="
echo "✅ County Mapper Configuration Complete!"
echo "=========================================="
echo ""
echo "The countyId user attribute will now be included in JWT tokens"
echo "for the $CLIENT_ID client in the $REALM_NAME realm."
echo ""
echo "⚠️  IMPORTANT: Users must have the countyId attribute set in Keycloak"
echo "   for this to work. Run create-keycloak-cron-users.sh to set up users."
echo ""
echo "✅ Script completed successfully!"

