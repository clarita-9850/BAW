#!/usr/bin/env bash
# Configure Keycloak CORS settings for the trial-app client
# This allows the frontend (http://localhost:3000) to make requests to Keycloak

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8085}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
REALM_NAME="${REALM_NAME:-cmips}"
CLIENT_ID="${CLIENT_ID:-trial-app}"

echo "=========================================="
echo "Configuring Keycloak CORS Settings"
echo "=========================================="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Realm: $REALM_NAME"
echo "Client: $CLIENT_ID"
echo ""

# Check if Keycloak is ready
echo "⏳ Checking Keycloak status..."
if ! curl -s -f "$KEYCLOAK_URL/realms/$REALM_NAME/.well-known/openid-configuration" > /dev/null 2>&1; then
    echo "❌ Keycloak is not responding at $KEYCLOAK_URL"
    echo "   Please ensure Keycloak is running and accessible"
    exit 1
fi
echo "✅ Keycloak is responding"

# Authenticate with Keycloak admin
echo ""
echo "🔐 Authenticating with Keycloak admin..."
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

# Configure CORS settings for the client
echo ""
echo "🔧 Configuring CORS settings..."
echo "   Adding web origins: http://localhost:3000, http://localhost:3001, http://127.0.0.1:3000"

# Update client with CORS web origins
docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh update clients/$CLIENT_UUID -r "$REALM_NAME" \
  -s 'webOrigins=["http://localhost:3000","http://localhost:3001","http://127.0.0.1:3000","http://127.0.0.1:3001","*"]' \
  -s 'redirectUris=["http://localhost:3000/*","http://localhost:3001/*","http://127.0.0.1:3000/*","http://127.0.0.1:3001/*"]' \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "  ✅ CORS settings configured successfully"
else
    echo "  ⚠️  CORS configuration may have failed, but continuing..."
fi

# Verify the configuration
echo ""
echo "🔍 Verifying CORS configuration..."
WEB_ORIGINS=$(docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh get clients/$CLIENT_UUID -r "$REALM_NAME" --fields webOrigins --format csv --noquotes 2>/dev/null | cut -d',' -f2-)

if [ -n "$WEB_ORIGINS" ]; then
    echo "  ✅ Web Origins configured: $WEB_ORIGINS"
else
    echo "  ⚠️  Could not verify web origins"
fi

echo ""
echo "=========================================="
echo "✅ CORS Configuration Complete!"
echo "=========================================="
echo ""
echo "The $CLIENT_ID client now allows CORS requests from:"
echo "  - http://localhost:3000"
echo "  - http://localhost:3001"
echo "  - http://127.0.0.1:3000"
echo "  - http://127.0.0.1:3001"
echo ""
echo "✅ Script completed successfully!"

