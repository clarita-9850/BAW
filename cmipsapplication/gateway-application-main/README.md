# API Gateway

Spring Cloud Gateway service that acts as a single entry point for all frontend requests.

## Features

- **Authentication Proxy**: Handles login/refresh/logout requests by proxying to Keycloak
- **JWT Validation**: Validates JWT tokens for all API requests
- **Request Routing**: Routes requests to appropriate backend services
- **CORS Handling**: Centralized CORS configuration

## Architecture

```
Frontend → API Gateway (port 8090)
    ├─→ /api/auth/** → Keycloak (authentication)
    ├─→ /api/** → Spring-app backend (port 8080)
    └─→ /api/cmips/** → CMIPS backend (port 8081)
```

## Routes

- `/api/auth/login` - Login endpoint (proxies to Keycloak)
- `/api/auth/refresh` - Refresh token endpoint (proxies to Keycloak)
- `/api/auth/logout` - Logout endpoint (proxies to Keycloak)
- `/api/**` - Routes to spring-app backend (requires JWT)
- `/api/cmips/**` - Routes to CMIPS backend (requires JWT)

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/api-gateway-1.0.0.jar
```

Or via Docker:

```bash
docker build -t api-gateway .
docker run -p 8090:8080 api-gateway
```

## Configuration

Key configuration in `application.yml`:
- Port: 8080 (exposed as 8090 in Docker)
- Keycloak issuer: `http://cmips-keycloak:8080/realms/cmips`
- CORS origins: `http://localhost:3000`, `http://localhost:3001`

## Environment Variables

- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` - Keycloak issuer URI

