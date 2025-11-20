# CMIPS POC - Policy-Driven Authentication & Authorization

A comprehensive Proof of Concept (POC) application demonstrating policy-driven authentication and authorization for CMIPS-like use cases. Built with Spring Boot (Java 17) backend and React (TypeScript) frontend.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   React Frontend│    │  Spring Boot     │    │   PostgreSQL    │
│   (Port 5173)   │◄──►│  Backend         │◄──►│   Database      │
│                 │    │  (Port 8080)     │    │   (Port 5432)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │   Mock LDAP      │
                       │   Service        │
                       └──────────────────┘
```

## 🚀 Key Features

### Authentication & Authorization
- **Mock LDAP Service** - Simulates enterprise LDAP authentication
- **SSO Token Exchange** - Converts SSO tokens to JWT tokens
- **Policy-Driven Access Control** - Runtime-configurable permissions
- **API Gateway Filter** - Centralized authentication and authorization
- **Role-Based Access** - Granular permissions per role

### Timesheet Management
- **CRUD Operations** - Create, read, update, delete timesheets
- **Status Tracking** - Submitted, approved, rejected, revision requested
- **Role-Based Access** - Different permissions per user role
- **Real-time Updates** - Immediate policy enforcement

### Admin Dashboard
- **User Management** - Create, edit, activate/deactivate users
- **Policy Management** - Configure access policies in real-time
- **System Statistics** - Monitor user activity and system health
- **Policy Testing** - Test access policies before applying

## 🛠️ Tech Stack

### Backend
- **Java 17** - Modern Java features
- **Spring Boot 3.2.0** - Rapid application development
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Relational database
- **JWT** - JSON Web Tokens for authentication
- **Maven** - Dependency management

### Frontend
- **React 18** - Modern React with hooks
- **TypeScript** - Type-safe JavaScript
- **Vite** - Fast build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client for API calls
- **React Router** - Client-side routing

## 📋 Prerequisites

- **Java 17+** - For backend development
- **Node.js 18+** - For frontend development
- **PostgreSQL 13+** - Database server
- **Maven 3.6+** - Build tool
- **npm** - Package manager

## 🚀 Quick Start

### 1. Database Setup

```bash
# Create PostgreSQL database
createdb cmips_poc

# Create user (optional)
psql -c "CREATE USER cmips_user WITH PASSWORD 'cmips_password';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE cmips_poc TO cmips_user;"
```

### 2. Backend Setup

```bash
cd CMIPS/backend

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd CMIPS/frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on `http://localhost:5173`

## 🔐 Demo Accounts

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `admin123` | Admin | Full access to all features |
| `caseworker` | `case123` | Case Worker | Can manage timesheets |
| `auditor` | `audit123` | Auditor | Can view timesheets only |
| `manager` | `manager123` | Manager | Can manage users and timesheets |
| `supervisor` | `super123` | Supervisor | Can manage timesheets |

## 📚 API Endpoints

### Authentication
- `POST /api/auth/login` - Login with username/password
- `POST /api/auth/sso/exchange` - Exchange SSO token for JWT
- `GET /api/auth/health` - Health check

### Timesheets
- `GET /api/timesheets` - List timesheets
- `POST /api/timesheets` - Create timesheet
- `GET /api/timesheets/{id}` - Get timesheet by ID
- `PUT /api/timesheets/{id}` - Update timesheet
- `DELETE /api/timesheets/{id}` - Delete timesheet
- `GET /api/timesheets/status/{status}` - Get timesheets by status
- `GET /api/timesheets/pending` - Get pending timesheets

### Policies
- `GET /api/policies` - List all policies
- `POST /api/policies` - Create policy
- `PUT /api/policies/{id}` - Update policy
- `DELETE /api/policies/{id}` - Delete policy
- `GET /api/policies/role/{role}` - Get policies by role
- `GET /api/policies/resource/{resource}` - Get policies by resource
- `GET /api/policies/test` - Test policy access

### Admin
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}/role` - Update user role
- `PUT /api/admin/users/{id}/activate` - Activate user
- `PUT /api/admin/users/{id}/deactivate` - Deactivate user
- `GET /api/admin/stats` - Get system statistics

## 🔧 Configuration

### Backend Configuration (`application.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cmips_poc
    username: cmips_user
    password: cmips_password
  
jwt:
  secret: cmips-poc-secret-key-for-jwt-token-generation-and-validation
  expiration: 86400000 # 24 hours

policy:
  cache:
    enabled: true
    ttl: 300 # 5 minutes
```

### Frontend Configuration (`src/config/api.ts`)

```typescript
export const API_BASE_URL = 'http://localhost:8080/api';
```

## 🎯 Demo Scenarios

### Scenario 1: Basic Timesheet Management
1. Login as `caseworker` / `case123`
2. Create a new timesheet
3. View your timesheets
4. Edit an existing timesheet

### Scenario 2: Policy-Driven Access Control
1. Login as `admin` / `admin123`
2. Go to Policy Management
3. Create a new policy denying `CASE_WORKER` access to `DELETE` on `timesheets`
4. Login as `caseworker` / `case123`
5. Try to delete a timesheet (should be denied)

### Scenario 3: Role-Based Access
1. Login as `auditor` / `audit123`
2. Try to create a timesheet (should be denied)
3. View timesheets (should work)
4. Login as `admin` / `admin123`
5. Go to User Management
6. Change `auditor` role to `CASE_WORKER`
7. Login as `auditor` / `audit123`
8. Now you can create timesheets

### Scenario 4: Real-time Policy Updates
1. Login as `admin` / `admin123`
2. Go to Policy Management
3. Update a policy to deny access
4. Login as affected user
5. Access is immediately denied (no restart required)

## 🏛️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    department VARCHAR(255),
    location VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Policies Table
```sql
CREATE TABLE policies (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(20) NOT NULL,
    allowed BOOLEAN NOT NULL,
    description TEXT,
    priority INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Timesheets Table
```sql
CREATE TABLE timesheets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    hours DECIMAL(5,2) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'SUBMITTED',
    comments TEXT,
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    revision_count INTEGER DEFAULT 0,
    last_revision_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔒 Security Features

### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. Mock LDAP validates credentials
3. System generates SSO token
4. SSO token exchanged for JWT token
5. JWT token used for all subsequent requests

### Authorization Flow
1. API Gateway filter extracts JWT token
2. Token validated and user role extracted
3. Policy engine checks role against resource/action
4. Access granted or denied based on policies
5. Request proceeds or returns 403 Forbidden

### Policy Engine
- **Priority-based** - Higher priority policies override lower ones
- **Wildcard support** - `*` for role, resource, or action
- **Real-time updates** - Policies can be changed without restart
- **Caching** - Policies cached for performance
- **Default deny** - No policy means no access

## 🚀 Deployment

### Backend Deployment
```bash
# Build JAR file
mvn clean package

# Run JAR file
java -jar target/cmips-poc-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment
```bash
# Build for production
npm run build

# Serve static files
npm run preview
```

## 🧪 Testing

### Backend Tests
```bash
cd CMIPS/backend
mvn test
```

### Frontend Tests
```bash
cd CMIPS/frontend
npm test
```

## 📝 Development Notes

### Adding New Roles
1. Update `User` entity with new role
2. Add role to `DataInitializer`
3. Create policies for new role
4. Update frontend role handling

### Adding New Resources
1. Create new controller with `@RequestMapping`
2. Add resource to policy management
3. Update API Gateway filter if needed
4. Test with different roles

### Policy Configuration
- Policies are evaluated in priority order
- Higher priority numbers take precedence
- Wildcard policies (`*`) are fallbacks
- Default deny if no policy matches

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For questions or issues:
1. Check the documentation
2. Review the demo scenarios
3. Check the API endpoints
4. Create an issue in the repository

---

**Built with ❤️ for CMIPS Policy-Driven Authentication & Authorization Demo**




