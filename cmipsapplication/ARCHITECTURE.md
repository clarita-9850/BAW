# CMIPS Application Architecture - Complete Overview

## Executive Summary

The CMIPS (Case Management Information and Payroll System) is a microservices-based application deployed on AWS, designed to manage healthcare provider services, case management, and payroll processing. The architecture follows event-driven principles using Apache Kafka for asynchronous communication and implements role-based access control (RBAC) via Keycloak.

---

## Architecture Diagram

See `architecture-diagram.svg` for a visual representation of the complete system architecture and data flow.

---

## Component Overview

### 1. Frontend (Next.js)

**Technology Stack:**
- Next.js 14 with React
- TypeScript
- Tailwind CSS
- Axios for HTTP requests
- React Context API for state management

**Deployment:**
- **AWS Service:** ECS (Elastic Container Service) or EC2
- **Container:** Docker container running Node.js
- **Port:** 3000
- **Load Balancing:** Application Load Balancer (ALB) for high availability
- **Auto-scaling:** Based on CPU/memory metrics

**Responsibilities:**
- User interface for all roles (Admin, Supervisor, Case Worker, Provider, Recipient)
- Authentication UI (login, password change)
- Dashboard views for each role
- Work queue management (Supervisor)
- Task management (Case Worker)
- Address change forms (Provider)
- Notification display (all roles)

**Communication:**
- REST API calls to Backend (HTTPS)
- JWT token stored in localStorage
- Token included in Authorization header for all API requests

---

### 2. Keycloak (Identity & Access Management)

**Technology Stack:**
- Keycloak Server
- PostgreSQL (for Keycloak's internal database)
- JWT tokens

**Deployment:**
- **AWS Service:** ECS/EC2
- **Port:** 8080
- **Database:** Separate PostgreSQL instance or RDS

**Responsibilities:**
- User authentication (username/password)
- Role-based authorization (RBAC)
- JWT token issuance and validation
- User management (create, update, delete users)
- Role management (ADMIN, SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
- Password policies and required actions (e.g., UPDATE_PASSWORD)

**Integration:**
- Backend validates JWT tokens on each request
- Frontend redirects to Keycloak for authentication
- Token contains user info and roles

---

### 3. Backend (Spring Boot)

**Technology Stack:**
- Spring Boot 3.x
- Spring Security (OAuth2 Resource Server)
- Spring Data JPA
- Spring Kafka (Producer & Consumer)
- PostgreSQL (via HikariCP connection pool)
- REST Controllers

**Deployment:**
- **AWS Service:** ECS or EC2
- **Port:** 8081
- **Load Balancing:** ALB
- **Auto-scaling:** Horizontal scaling based on request volume

**Key Components:**

#### REST Controllers:
- `AuthController` - Login, password change
- `CaseController` - Address change submission
- `TaskController` - Task CRUD operations
- `WorkQueueController` - Work queue management
- `NotificationController` - Notification management
- `KeycloakAdminController` - Admin operations
- `ProviderRecipientController` - Provider-recipient relationships

#### Services:
- `TaskService` - Task business logic
- `WorkQueueSubscriptionService` - Queue subscription management
- `NotificationService` - Notification creation/retrieval
- `KeycloakAdminService` - Keycloak API integration
- `WorkQueueCatalogService` - Predefined queue definitions

#### Kafka Integration:
- **Producer:** Publishes events to Kafka topics
- **Consumer:** `TaskEventConsumer` listens to events and creates tasks/notifications

**Communication:**
- Receives REST requests from Frontend
- Validates JWT tokens with Keycloak
- Queries/updates PostgreSQL database
- Publishes events to Kafka
- Consumes events from Kafka

---

### 4. PostgreSQL Database (RDS)

**Technology Stack:**
- PostgreSQL 14+
- Hibernate/JPA for ORM

**Deployment:**
- **AWS Service:** RDS PostgreSQL
- **Configuration:** Multi-AZ deployment for high availability
- **Backup:** Automated daily backups, point-in-time recovery
- **Port:** 5432

**Key Tables:**

1. **tasks**
   - Stores all tasks (address validation, timesheet exceptions, etc.)
   - Fields: id, title, description, status, priority, workQueue, queueRole, assignedTo, dueDate, etc.

2. **work_queue_subscriptions**
   - Links users to work queues
   - Fields: id, username, work_queue, subscribed_by, created_at, updated_at

3. **notifications**
   - User notifications
   - Fields: id, user_id, message, notification_type, read_status, action_link, created_at

4. **provider_recipient_relationships**
   - Links providers to recipients
   - Fields: id, provider_id, recipient_id, status, authorized_hours, case_number

5. **timesheets**
   - Timesheet records
   - Fields: id, employee_name, pay_period_start, pay_period_end, total_hours, status

**Connection Pooling:**
- HikariCP manages database connections
- Configurable pool size based on load

---

### 5. Apache Kafka (MSK)

**Technology Stack:**
- Apache Kafka
- Managed by AWS MSK (Managed Streaming for Apache Kafka)

**Deployment:**
- **AWS Service:** Amazon MSK
- **Port:** 9092
- **Configuration:** Multi-AZ, auto-scaling
- **Topics:**
  - `cmips-case-events` - Case-related events (address changes, case creation)
  - `cmips-timesheet-events` - Timesheet-related events (exceptions, violations)

**Consumer Groups:**
- `task-consumer-group` - Processes events and creates tasks/notifications

**Event Schema:**
```json
{
  "eventId": "uuid",
  "eventType": "case.address.changed",
  "timestamp": "ISO-8601",
  "userId": "provider1",
  "source": "provider-portal",
  "payload": {
    "caseId": "CASE-001",
    "providerId": "provider1",
    "recipientId": "recipient1",
    "newAddress": {...},
    "isOutsideCA": true
  },
  "metadata": {
    "correlationId": "uuid",
    "version": "1.0"
  }
}
```

**Benefits:**
- Decouples event producers from consumers
- Enables async processing
- Supports event replay
- Scales horizontally

---

### 6. Zookeeper

**Technology Stack:**
- Apache Zookeeper
- Managed by AWS MSK (automatically included)

**Deployment:**
- **AWS Service:** Managed by MSK
- **Port:** 2181
- **Configuration:** 3-node cluster for fault tolerance

**Responsibilities:**
- Coordinates Kafka brokers
- Leader election for partitions
- Maintains cluster metadata
- Configuration management
- Service discovery

**Note:** With MSK, Zookeeper is fully managed and transparent to the application.

---

## End-to-End Flow: Address Change Scenario

### Step-by-Step Data Flow

#### **Step 1: Authentication**
1. Provider logs in via Frontend
2. Frontend sends credentials to Keycloak
3. Keycloak validates credentials and returns JWT token
4. Frontend stores token in localStorage

**Data Flow:**
```
Frontend → Keycloak (POST /realms/cmips/protocol/openid-connect/token)
Keycloak → Frontend (JWT token)
```

---

#### **Step 2: Address Change Submission**
1. Provider fills address change form
2. Frontend sends POST request to Backend
3. Backend validates JWT token with Keycloak
4. Backend extracts user info from token

**Data Flow:**
```
Frontend → Backend (POST /api/cases/address-change, Authorization: Bearer <token>)
Backend → Keycloak (Validate token)
Keycloak → Backend (Token valid, user info)
```

**Request Payload:**
```json
{
  "caseId": "CASE-001",
  "recipientId": "recipient1",
  "recipientName": "recipient1",
  "providerId": "provider1",
  "newAddress": {
    "line1": "123 Main St",
    "city": "Las Vegas",
    "state": "NV",
    "zip": "89101"
  }
}
```

---

#### **Step 3: Notification Creation**
1. Backend creates notification for recipient
2. Notification saved to PostgreSQL

**Data Flow:**
```
Backend → PostgreSQL (INSERT INTO notifications)
```

**SQL:**
```sql
INSERT INTO notifications (user_id, message, notification_type, read_status, action_link, created_at)
VALUES ('recipient1', 'Your care giver provider1 has changed their address to 123 Main St, Las Vegas, NV 89101', 'INFO', false, '/recipient/dashboard', NOW());
```

---

#### **Step 4: Kafka Event Publication**
1. Backend creates Kafka event
2. Event published to `cmips-case-events` topic
3. Kafka broker stores event

**Data Flow:**
```
Backend → Kafka Broker (KafkaTemplate.send("cmips-case-events", event))
```

**Event:**
```json
{
  "eventType": "case.address.changed",
  "payload": {
    "caseId": "CASE-001",
    "providerId": "provider1",
    "recipientId": "recipient1",
    "newAddress": {...},
    "isOutsideCA": true
  }
}
```

---

#### **Step 5: Kafka Consumer Processing**
1. `TaskEventConsumer` receives event via `@KafkaListener`
2. Consumer checks `isOutsideCA` flag
3. If true, creates Task entity

**Data Flow:**
```
Kafka Broker → TaskEventConsumer (@KafkaListener)
TaskEventConsumer → TaskService.createTask()
```

**Logic:**
```java
if (isOutsideCA) {
    Task task = Task.builder()
        .title("Address Validation Required - Provider: provider1")
        .workQueue("PROVIDER_MANAGEMENT")
        .queueRole("CASE_WORKER")
        .assignedTo("PROVIDER_MANAGEMENT")
        .status(OPEN)
        .build();
    taskService.createTask(task);
}
```

---

#### **Step 6: Task Persistence**
1. TaskService saves task to PostgreSQL
2. Task available in PROVIDER_MANAGEMENT queue

**Data Flow:**
```
TaskService → PostgreSQL (INSERT INTO tasks)
```

**SQL:**
```sql
INSERT INTO tasks (title, description, work_queue, queue_role, assigned_to, status, priority, created_at)
VALUES ('Address Validation Required - Provider: provider1', 'Provider provider1 has changed their address to outside California - requires review', 'PROVIDER_MANAGEMENT', 'CASE_WORKER', 'PROVIDER_MANAGEMENT', 'OPEN', 'MEDIUM', NOW());
```

---

#### **Step 7: Supervisor Views Queue**
1. Supervisor navigates to Work Queues
2. Frontend requests tasks from PROVIDER_MANAGEMENT queue
3. Backend queries PostgreSQL
4. Tasks returned to Frontend

**Data Flow:**
```
Frontend → Backend (GET /api/work-queues/PROVIDER_MANAGEMENT/tasks)
Backend → PostgreSQL (SELECT * FROM tasks WHERE work_queue = 'PROVIDER_MANAGEMENT')
PostgreSQL → Backend (Task list)
Backend → Frontend (JSON response)
```

---

#### **Step 8: Supervisor Adds Caseworker to Queue**
1. Supervisor selects caseworker and queue
2. Frontend sends subscription request
3. Backend saves subscription to PostgreSQL

**Data Flow:**
```
Frontend → Backend (POST /api/work-queues/subscribe)
Backend → PostgreSQL (INSERT INTO work_queue_subscriptions)
```

**SQL:**
```sql
INSERT INTO work_queue_subscriptions (username, work_queue, subscribed_by, created_at)
VALUES ('caseworker1', 'PROVIDER_MANAGEMENT', 'supervisor1', NOW());
```

---

#### **Step 9: Caseworker Views Tasks**
1. Caseworker logs in and views dashboard
2. Frontend requests tasks with `includeSubscribedQueues=true`
3. Backend:
   - Gets user's subscribed queues from `work_queue_subscriptions`
   - Queries tasks from those queues
   - Returns combined task list

**Data Flow:**
```
Frontend → Backend (GET /api/tasks?username=caseworker1&includeSubscribedQueues=true)
Backend → PostgreSQL (SELECT work_queue FROM work_queue_subscriptions WHERE username = 'caseworker1')
Backend → PostgreSQL (SELECT * FROM tasks WHERE work_queue IN ('PROVIDER_MANAGEMENT'))
PostgreSQL → Backend (Task list)
Backend → Frontend (JSON response)
```

---

#### **Step 10: Recipient Views Notifications**
1. Recipient logs in
2. Frontend requests notifications
3. Backend queries PostgreSQL
4. Notifications displayed in NotificationCenter

**Data Flow:**
```
Frontend → Backend (GET /api/notifications/user/recipient1)
Backend → PostgreSQL (SELECT * FROM notifications WHERE user_id = 'recipient1' ORDER BY created_at DESC)
PostgreSQL → Backend (Notification list)
Backend → Frontend (JSON response)
```

---

## AWS Infrastructure Details

### VPC (Virtual Private Cloud)
- **CIDR:** 10.0.0.0/16
- **Public Subnet:** 10.0.1.0/24 (Frontend, Keycloak)
- **Private Subnet:** 10.0.2.0/24 (Backend, Database, Kafka)

### Security Groups
- **Frontend SG:** Allows inbound HTTPS (443) from ALB, outbound to Backend
- **Backend SG:** Allows inbound from Frontend SG, outbound to RDS and MSK
- **RDS SG:** Allows inbound from Backend SG only
- **MSK SG:** Allows inbound from Backend SG only

### Load Balancing
- **Application Load Balancer (ALB):**
  - Routes traffic to Frontend containers
  - Health checks on /health endpoint
  - SSL termination with ACM certificate
  - Sticky sessions if needed

### Auto-Scaling
- **ECS Service Auto-Scaling:**
  - Scales based on CPU utilization (target: 70%)
  - Min: 2 tasks, Max: 10 tasks
  - Scale-out: +2 tasks, Scale-in: -1 task

### Monitoring & Logging
- **CloudWatch:**
  - Application logs (ECS log driver)
  - Metrics (CPU, memory, request count, latency)
  - Alarms for error rates, high latency
- **X-Ray:** Distributed tracing (optional)

### Backup & Disaster Recovery
- **RDS:**
  - Automated daily backups (7-day retention)
  - Point-in-time recovery
  - Multi-AZ for high availability
- **MSK:**
  - Automatic backups
  - Multi-AZ deployment

---

## Security Architecture

### Authentication Flow
1. User logs in → Keycloak validates credentials
2. Keycloak issues JWT token (signed with RSA key)
3. Frontend stores token, includes in Authorization header
4. Backend validates token signature with Keycloak's public key
5. Backend extracts user info and roles from token

### Authorization
- **Role-Based Access Control (RBAC):**
  - Roles defined in Keycloak
  - Backend checks roles from JWT token
  - Endpoints protected by `@PreAuthorize` annotations

### Network Security
- **VPC Isolation:** Private subnet components not directly accessible from internet
- **Security Groups:** Least privilege access
- **SSL/TLS:** All API communications encrypted
- **Secrets Management:** AWS Secrets Manager for database passwords, API keys

### Data Encryption
- **At Rest:** RDS encryption enabled, EBS volumes encrypted
- **In Transit:** TLS 1.2+ for all communications

---

## Scalability & Performance

### Horizontal Scaling
- **Frontend:** ECS auto-scaling (2-10 containers)
- **Backend:** ECS auto-scaling (2-10 containers)
- **Kafka:** MSK auto-scales brokers based on throughput

### Caching
- **Redis (Optional):** Cache frequently accessed data (user info, queue lists)
- **Application Cache:** In-memory cache for Keycloak admin tokens

### Database Optimization
- **Connection Pooling:** HikariCP (default: 10 connections)
- **Indexes:** On frequently queried columns (work_queue, user_id, status)
- **Query Optimization:** JPA query hints, native queries for complex operations

---

## Deployment Strategy

### CI/CD Pipeline
1. **Source:** GitHub/GitLab
2. **Build:** AWS CodeBuild (Docker image build)
3. **Test:** Unit tests, integration tests
4. **Deploy:** AWS CodeDeploy to ECS
5. **Rollback:** Blue/Green deployment with ECS

### Environment Management
- **Development:** Single EC2 instance, local Docker Compose
- **Staging:** ECS with 2 tasks, RDS single-AZ
- **Production:** ECS with auto-scaling, RDS Multi-AZ, MSK

---

## Disaster Recovery

### RTO (Recovery Time Objective): < 1 hour
### RPO (Recovery Point Objective): < 15 minutes

**Strategy:**
1. **Database:** RDS automated backups + Multi-AZ
2. **Application:** ECS tasks can be recreated from Docker images
3. **Kafka:** MSK automatic backups, can replay events
4. **Keycloak:** Database backup + configuration export

---

## Cost Optimization

### Reserved Instances
- RDS: 1-year reserved instance for predictable workloads
- EC2: Reserved instances for stable components

### Auto-Scaling
- Scale down during off-peak hours
- Use Spot Instances for non-critical workloads (optional)

### Storage
- RDS: GP3 volumes (cheaper than IO1)
- ECS: Use Fargate Spot for cost savings (optional)

---

## Monitoring & Alerting

### Key Metrics
- **Application:** Request rate, error rate, latency (p50, p95, p99)
- **Database:** Connection count, query latency, CPU utilization
- **Kafka:** Lag per consumer group, throughput, broker CPU
- **Infrastructure:** CPU, memory, network I/O

### Alarms
- Error rate > 5% for 5 minutes
- Latency p95 > 2 seconds
- Database CPU > 80%
- Consumer lag > 1000 messages

---

## Future Enhancements

1. **API Gateway:** AWS API Gateway for rate limiting, API versioning
2. **CDN:** CloudFront for static assets
3. **Service Mesh:** AWS App Mesh for advanced traffic management
4. **GraphQL:** Add GraphQL API layer
5. **Event Sourcing:** Full event sourcing for audit trail
6. **Microservices:** Split Backend into smaller services (Task Service, Notification Service, etc.)

---

## Conclusion

The CMIPS application architecture is designed for:
- **High Availability:** Multi-AZ deployment, auto-scaling
- **Scalability:** Horizontal scaling, event-driven architecture
- **Security:** RBAC, encryption, network isolation
- **Reliability:** Automated backups, disaster recovery
- **Performance:** Caching, connection pooling, optimized queries

The event-driven architecture with Kafka enables loose coupling between components and supports future microservices migration.


