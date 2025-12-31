# API Gateway Fixes Summary

## Issues Fixed

### 1. JWT Token Extraction and Validation ✅
- **Fixed**: Improved `JwtValidationFilter` to properly extract user information from JWT tokens
- **Changes**:
  - Now uses `jwt.getSubject()` for user ID (more reliable than `jwt.getId()`)
  - Added proper handling for actuator endpoints (skip validation)
  - Forward original Authorization token to backend services
  - Improved error handling for missing claims
  - Added filter order to ensure it runs after authentication

### 2. Route Configuration ✅
- **Fixed**: Improved route ordering to prevent conflicts
- **Changes**:
  - Auth routes (`/api/auth/**`) → Keycloak
  - CMIPS routes (`/api/cmips/**`) → cmips-backend:8081
  - Spring-app routes (`/api/**`) → spring-app:8080
  - Routes are processed in the correct order

### 3. AuthProxyFilter Improvements ✅
- **Fixed**: Replaced manual JSON parsing with proper ObjectMapper
- **Changes**:
  - Uses Jackson ObjectMapper for reliable JSON parsing
  - Proper URL encoding for username, password, and refresh tokens
  - Better error handling and logging
  - Client secret verified: `trial-app-secret-key-2024` ✅

### 4. Configuration Verification ✅
- **Keycloak URL**: Uses `keycloak:8080` (container has alias)
- **Container Connectivity**: Verified gateway can reach:
  - ✅ Keycloak: `http://keycloak:8080`
  - ✅ Spring-app: `http://spring-app:8080`
  - ✅ CMIPS-backend: `http://cmips-backend:8081`

## Configuration Details

### Keycloak Configuration
- **Issuer URI**: `http://keycloak:8080/realms/cmips`
- **JWK Set URI**: `http://keycloak:8080/realms/cmips/protocol/openid-connect/certs`
- **Realm**: `cmips`
- **Client ID**: `trial-app`
- **Client Secret**: `trial-app-secret-key-2024`

### Route Configuration
```
/api/auth/**          → http://keycloak:8080 (no JWT validation)
/api/cmips/**        → http://cmips-backend:8081 (JWT validated)
/api/**              → http://spring-app:8080 (JWT validated)
```

### Headers Added to Backend Requests
- `X-User-Id`: User subject ID from JWT
- `X-User-Name`: Preferred username from JWT
- `X-User-Email`: Email from JWT
- `X-User-Roles`: Comma-separated list of realm roles
- `Authorization`: Original Bearer token (forwarded)

## Testing

### To Test Gateway:

1. **Test Authentication Endpoint**:
   ```bash
   curl -X POST http://localhost:8090/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"caseworker1","password":"password123"}'
   ```

2. **Test Protected Endpoint** (with token):
   ```bash
   curl -X GET http://localhost:8090/api/some-endpoint \
     -H "Authorization: Bearer <token>"
   ```

3. **Check Gateway Health**:
   ```bash
   curl http://localhost:8090/actuator/health
   ```

## Next Steps

1. Rebuild and restart the gateway:
   ```bash
   cd /Users/mythreya/Desktop/api-gateway
   mvn clean package
   docker-compose restart api-gateway
   ```

2. Monitor gateway logs:
   ```bash
   docker logs -f api-gateway
   ```

3. Test end-to-end authentication flow from frontend

## Notes

- Gateway is accessible on port **8090** (external) → **8080** (internal)
- All services are on the `cmips-shared-network` Docker network
- CORS is configured for frontend origins: `localhost:3000`, `localhost:3001`
- Security config allows `/api/auth/**` and `/actuator/**` without authentication
- All other endpoints require valid JWT token

