# CMIPS API Gateway Architecture Diagram

## System Overview

This document provides a comprehensive architecture diagram of the CMIPS API Gateway system, showing the complete flow from client requests through authentication, authorization, and service responses.

## Architecture Diagram

```mermaid
graph TB
    %% Client Layer
    subgraph "Client Layer"
        WEB[Web Browser<br/>React Frontend]
        MOBILE[Mobile App<br/>Future Implementation]
        API_CLIENT[API Client<br/>External Systems]
    end

    %% Load Balancer / Reverse Proxy
    LB[Load Balancer<br/>Nginx/AWS ALB]

    %% API Gateway Layer
    subgraph "API Gateway Layer"
        GATEWAY[Spring Boot API Gateway<br/>Port 8080]
        
        subgraph "Gateway Components"
            CORS[CORS Filter<br/>Cross-Origin Requests]
            AUTH_FILTER[Authentication Filter<br/>JWT Validation]
            POLICY_FILTER[Policy Authorization Filter<br/>RBAC + Policy Engine]
            RATE_LIMIT[Rate Limiting<br/>Request Throttling]
            LOGGING[Request/Response Logging<br/>Audit Trail]
        end
    end

    %% Authentication Services
    subgraph "Authentication Services"
        MOCK_LDAP[Mock LDAP Service<br/>User Authentication]
        JWT_SERVICE[JWT Service<br/>Token Generation/Validation]
        AUTH_SERVICE[Auth Service<br/>Login/Token Exchange]
    end

    %% Policy Engine
    subgraph "Policy Engine"
        POLICY_SERVICE[Policy Engine Service<br/>Runtime Policy Evaluation]
        POLICY_DB[(Policy Database<br/>PostgreSQL)]
    end

    %% Business Services
    subgraph "Business Services Layer"
        TIMESHEET_SVC[Timesheet Service<br/>CRUD Operations]
        USER_SVC[User Management Service<br/>User CRUD]
        ADMIN_SVC[Admin Service<br/>System Administration]
        NOTIFICATION_SVC[Notification Service<br/>Email/SMS]
    end

    %% Data Layer
    subgraph "Data Layer"
        MAIN_DB[(Main Database<br/>PostgreSQL)]
        CACHE[(Redis Cache<br/>Session/Token Cache)]
        FILE_STORE[(File Storage<br/>Document Storage)]
    end

    %% External Services
    subgraph "External Services"
        EXTERNAL_API[External Validation API<br/>Third-party Integration]
        EMAIL_SERVICE[Email Service<br/>SMTP/SES]
        SMS_SERVICE[SMS Service<br/>Twilio/AWS SNS]
    end

    %% Security Components
    subgraph "Security Components"
        ENCRYPTION[Data Encryption<br/>AES-256]
        AUDIT_LOG[Audit Logging<br/>Security Events]
        MONITORING[Security Monitoring<br/>Intrusion Detection]
    end

    %% Flow Connections
    WEB --> LB
    MOBILE --> LB
    API_CLIENT --> LB
    
    LB --> GATEWAY
    
    GATEWAY --> CORS
    CORS --> AUTH_FILTER
    AUTH_FILTER --> POLICY_FILTER
    POLICY_FILTER --> RATE_LIMIT
    RATE_LIMIT --> LOGGING
    
    %% Authentication Flow
    AUTH_FILTER --> JWT_SERVICE
    JWT_SERVICE --> AUTH_SERVICE
    AUTH_SERVICE --> MOCK_LDAP
    
    %% Policy Evaluation Flow
    POLICY_FILTER --> POLICY_SERVICE
    POLICY_SERVICE --> POLICY_DB
    
    %% Service Routing
    LOGGING --> TIMESHEET_SVC
    LOGGING --> USER_SVC
    LOGGING --> ADMIN_SVC
    LOGGING --> NOTIFICATION_SVC
    
    %% Data Access
    TIMESHEET_SVC --> MAIN_DB
    USER_SVC --> MAIN_DB
    ADMIN_SVC --> MAIN_DB
    NOTIFICATION_SVC --> MAIN_DB
    
    TIMESHEET_SVC --> CACHE
    USER_SVC --> CACHE
    AUTH_SERVICE --> CACHE
    
    %% External Integrations
    TIMESHEET_SVC --> EXTERNAL_API
    NOTIFICATION_SVC --> EMAIL_SERVICE
    NOTIFICATION_SVC --> SMS_SERVICE
    
    %% Security Integration
    MAIN_DB --> ENCRYPTION
    GATEWAY --> AUDIT_LOG
    AUDIT_LOG --> MONITORING

    %% Styling
    classDef clientClass fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef gatewayClass fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef serviceClass fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef dataClass fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef securityClass fill:#ffebee,stroke:#c62828,stroke-width:2px
    classDef externalClass fill:#f1f8e9,stroke:#33691e,stroke-width:2px

    class WEB,MOBILE,API_CLIENT clientClass
    class GATEWAY,CORS,AUTH_FILTER,POLICY_FILTER,RATE_LIMIT,LOGGING gatewayClass
    class TIMESHEET_SVC,USER_SVC,ADMIN_SVC,NOTIFICATION_SVC,MOCK_LDAP,JWT_SERVICE,AUTH_SERVICE,POLICY_SERVICE serviceClass
    class MAIN_DB,POLICY_DB,CACHE,FILE_STORE dataClass
    class ENCRYPTION,AUDIT_LOG,MONITORING securityClass
    class EXTERNAL_API,EMAIL_SERVICE,SMS_SERVICE externalClass
```

## Detailed Component Description

### 1. Client Layer
- **Web Browser (React Frontend)**: Primary user interface
- **Mobile App**: Future mobile application
- **API Client**: External system integrations

### 2. API Gateway Layer
- **Spring Boot API Gateway**: Central entry point for all requests
- **CORS Filter**: Handles cross-origin requests
- **Authentication Filter**: Validates JWT tokens
- **Policy Authorization Filter**: Enforces role-based and policy-driven access control
- **Rate Limiting**: Prevents abuse and ensures fair usage
- **Request/Response Logging**: Comprehensive audit trail

### 3. Authentication Services
- **Mock LDAP Service**: Simulates enterprise LDAP authentication
- **JWT Service**: Generates and validates JSON Web Tokens
- **Auth Service**: Orchestrates login and token exchange processes

### 4. Policy Engine
- **Policy Engine Service**: Runtime evaluation of access policies
- **Policy Database**: Stores configurable access control rules

### 5. Business Services
- **Timesheet Service**: Manages timesheet CRUD operations
- **User Management Service**: Handles user administration
- **Admin Service**: System administration functions
- **Notification Service**: Email and SMS notifications

### 6. Data Layer
- **Main Database (PostgreSQL)**: Primary data storage
- **Redis Cache**: Session and token caching
- **File Storage**: Document and attachment storage

### 7. External Services
- **External Validation API**: Third-party integrations
- **Email Service**: SMTP or cloud email services
- **SMS Service**: Text message delivery

### 8. Security Components
- **Data Encryption**: AES-256 encryption for sensitive data
- **Audit Logging**: Comprehensive security event logging
- **Security Monitoring**: Intrusion detection and alerting

## Request Flow

### 1. Authentication Flow
```
Client → Load Balancer → API Gateway → CORS Filter → Auth Filter → JWT Service → Auth Service → Mock LDAP
```

### 2. Authorized Request Flow
```
Client → Load Balancer → API Gateway → CORS Filter → Auth Filter → Policy Filter → Rate Limiting → Business Service → Database
```

### 3. Policy Evaluation Flow
```
Policy Filter → Policy Service → Policy Database → Decision (Allow/Deny)
```

## Security Features

1. **JWT-based Authentication**: Stateless token-based authentication
2. **Policy-driven Authorization**: Runtime configurable access control
3. **Role-based Access Control (RBAC)**: User role management
4. **Rate Limiting**: Request throttling and abuse prevention
5. **CORS Protection**: Cross-origin request security
6. **Audit Logging**: Comprehensive request/response logging
7. **Data Encryption**: Sensitive data protection
8. **Security Monitoring**: Real-time threat detection

## Scalability Features

1. **Load Balancer**: Horizontal scaling support
2. **Redis Caching**: Performance optimization
3. **Database Connection Pooling**: Efficient database access
4. **Stateless Design**: Easy horizontal scaling
5. **Microservice Architecture**: Independent service scaling

## Monitoring and Observability

1. **Request/Response Logging**: Full audit trail
2. **Performance Metrics**: Response time monitoring
3. **Error Tracking**: Exception and error logging
4. **Security Events**: Authentication and authorization logs
5. **Health Checks**: Service availability monitoring

This architecture provides a robust, scalable, and secure API Gateway solution for the CMIPS application with comprehensive authentication, authorization, and monitoring capabilities.




