# CMIPS API Gateway Sequence Diagram

## Request Flow Sequence

This diagram shows the detailed sequence of events when a client makes a request through the API Gateway.

```mermaid
sequenceDiagram
    participant Client as Web Client
    participant LB as Load Balancer
    participant Gateway as API Gateway
    participant CORS as CORS Filter
    participant Auth as Auth Filter
    participant JWT as JWT Service
    participant Policy as Policy Filter
    participant Service as Business Service
    participant DB as Database
    participant Cache as Redis Cache

    %% Authentication Flow
    Note over Client, Cache: Authentication Flow
    Client->>+LB: POST /api/auth/login
    LB->>+Gateway: Forward Request
    Gateway->>+CORS: Process CORS
    CORS->>+Auth: Check Authentication
    Auth->>+JWT: Validate Credentials
    JWT->>+Service: Authenticate User
    Service->>+DB: Query User Data
    DB-->>-Service: User Details
    Service-->>-JWT: Authentication Result
    JWT-->>-Auth: JWT Token Generated
    Auth-->>-CORS: Auth Success
    CORS-->>-Gateway: Request Processed
    Gateway-->>-LB: JWT Token Response
    LB-->>-Client: Login Success + JWT

    %% Authorized Request Flow
    Note over Client, Cache: Authorized Request Flow
    Client->>+LB: GET /api/timesheets (with JWT)
    LB->>+Gateway: Forward Request
    Gateway->>+CORS: Process CORS
    CORS->>+Auth: Validate JWT Token
    Auth->>+JWT: Verify Token
    JWT->>+Cache: Check Token Cache
    Cache-->>-JWT: Token Valid
    JWT-->>-Auth: Token Verification Success
    Auth->>+Policy: Check Authorization
    Policy->>+Service: Evaluate Policy Rules
    Service->>+DB: Query Policy Data
    DB-->>-Service: Policy Rules
    Service-->>-Policy: Policy Evaluation Result
    Policy-->>-Auth: Authorization Decision
    Auth-->>-CORS: Request Authorized
    CORS->>+Service: Forward to Business Service
    Service->>+DB: Execute Business Logic
    DB-->>-Service: Data Response
    Service-->>-CORS: Business Response
    CORS-->>-Gateway: Processed Response
    Gateway-->>-LB: Final Response
    LB-->>-Client: Timesheet Data

    %% Error Flow
    Note over Client, Cache: Error Flow (Unauthorized)
    Client->>+LB: GET /api/admin/users (with JWT)
    LB->>+Gateway: Forward Request
    Gateway->>+CORS: Process CORS
    CORS->>+Auth: Validate JWT Token
    Auth->>+JWT: Verify Token
    JWT-->>-Auth: Token Valid
    Auth->>+Policy: Check Authorization
    Policy->>+Service: Evaluate Policy Rules
    Service->>+DB: Query Policy Data
    DB-->>-Service: Policy Rules
    Service-->>-Policy: Access Denied
    Policy-->>-Auth: Authorization Failed
    Auth-->>-CORS: 403 Forbidden
    CORS-->>-Gateway: Error Response
    Gateway-->>-LB: 403 Forbidden
    LB-->>-Client: Access Denied Error
```

## Policy Evaluation Flow

```mermaid
sequenceDiagram
    participant Policy as Policy Filter
    participant Engine as Policy Engine
    participant DB as Policy Database
    participant Cache as Redis Cache
    participant Service as Business Service

    Note over Policy, Service: Policy Evaluation Process
    Policy->>+Engine: Evaluate Access Request
    Engine->>+Cache: Check Policy Cache
    alt Policy in Cache
        Cache-->>-Engine: Cached Policy Result
    else Policy not in Cache
        Engine->>+DB: Query Policy Rules
        DB-->>-Engine: Policy Rules
        Engine->>+Cache: Cache Policy Result
        Cache-->>-Engine: Policy Cached
    end
    
    Engine->>Engine: Evaluate Rules
    Note over Engine: Check Role, Resource, Action
    
    alt Access Allowed
        Engine-->>-Policy: Access Granted
        Policy->>+Service: Forward Request
        Service-->>-Policy: Business Response
        Policy-->>-Engine: Request Completed
    else Access Denied
        Engine-->>-Policy: Access Denied
        Policy-->>-Engine: 403 Forbidden Response
    end
```

## JWT Token Lifecycle

```mermaid
sequenceDiagram
    participant Client as Client
    participant Auth as Auth Service
    participant JWT as JWT Service
    participant Cache as Redis Cache
    participant DB as Database

    Note over Client, DB: JWT Token Lifecycle
    Client->>+Auth: Login Request
    Auth->>+DB: Validate Credentials
    DB-->>-Auth: User Data
    Auth->>+JWT: Generate Token
    JWT->>+Cache: Store Token Metadata
    Cache-->>-JWT: Token Stored
    JWT-->>-Auth: JWT Token
    Auth-->>-Client: Login Success + JWT

    Note over Client, DB: Token Usage
    Client->>+Auth: API Request with JWT
    Auth->>+JWT: Validate Token
    JWT->>+Cache: Check Token Status
    Cache-->>-JWT: Token Valid
    JWT-->>-Auth: Token Validation Success
    Auth-->>-Client: Request Processed

    Note over Client, DB: Token Expiration
    Client->>+Auth: API Request with Expired JWT
    Auth->>+JWT: Validate Token
    JWT-->>-Auth: Token Expired
    Auth-->>-Client: 401 Unauthorized
    Client->>+Auth: Refresh Token Request
    Auth->>+JWT: Generate New Token
    JWT-->>-Auth: New JWT Token
    Auth-->>-Client: New Token + Refresh
```

## Error Handling Flow

```mermaid
sequenceDiagram
    participant Client as Client
    participant Gateway as API Gateway
    participant Auth as Auth Filter
    participant Policy as Policy Filter
    participant Service as Business Service

    Note over Client, Service: Error Handling Scenarios

    %% Authentication Error
    Client->>+Gateway: Request without JWT
    Gateway->>+Auth: Check Authentication
    Auth-->>-Gateway: 401 Unauthorized
    Gateway-->>-Client: Authentication Required

    %% Authorization Error
    Client->>+Gateway: Request with JWT
    Gateway->>+Auth: Validate JWT
    Auth-->>-Gateway: Token Valid
    Gateway->>+Policy: Check Authorization
    Policy-->>-Gateway: 403 Forbidden
    Gateway-->>-Client: Access Denied

    %% Service Error
    Client->>+Gateway: Valid Request
    Gateway->>+Auth: Validate JWT
    Auth-->>-Gateway: Token Valid
    Gateway->>+Policy: Check Authorization
    Policy-->>-Gateway: Access Granted
    Gateway->>+Service: Forward Request
    Service-->>-Gateway: 500 Internal Error
    Gateway-->>-Client: Service Error

    %% Rate Limiting Error
    Client->>+Gateway: High Frequency Request
    Gateway->>+Gateway: Check Rate Limit
    Gateway-->>-Client: 429 Too Many Requests
```

## Key Features Demonstrated

1. **Authentication Flow**: JWT token generation and validation
2. **Authorization Flow**: Policy-based access control
3. **Caching**: Redis-based token and policy caching
4. **Error Handling**: Comprehensive error responses
5. **Security**: Multi-layer security validation
6. **Performance**: Caching and optimization strategies
7. **Scalability**: Load balancer and stateless design

This sequence diagram shows the complete flow of requests through the API Gateway system, demonstrating how authentication, authorization, and business logic work together to provide secure and efficient API access.




