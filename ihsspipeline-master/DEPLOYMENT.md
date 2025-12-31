# Deployment Guide

## Prerequisites

- Docker and Docker Compose
- Java 17+ (for local development)
- Maven 3.6+ (for local development)
- Node.js 18+ (for frontend)
- PostgreSQL 13+ (or use Docker container)
- Keycloak (or use Docker container)

## Local Development Setup

### 1. Start Infrastructure Services

```bash
cd /Users/mythreya/Desktop/trial

# Start PostgreSQL and Keycloak
docker-compose up -d postgres

# Start Keycloak (from sajeevs-codebase-main)
cd /Users/mythreya/Desktop/sajeevs-codebase-main/cmipsapplication
docker-compose up -d keycloak postgres
```

### 2. Configure Keycloak

```bash
cd /Users/mythreya/Desktop/trial

# Configure county mapper
./configure-keycloak-county-mapper.sh

# Create users with county attributes
./create-keycloak-cron-users.sh
```

### 3. Start Backend Application

```bash
cd /Users/mythreya/Desktop/trial

# Build and start with Docker Compose
docker-compose up -d spring-app

# Or run locally with Maven
mvn clean install
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### 4. Start Frontend Application

```bash
cd /Users/mythreya/Desktop/timesheet-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will be available at `http://localhost:3000`

## Docker Compose Configuration

### Services

- **postgres** - PostgreSQL database (Port 5432)
- **spring-app** - Spring Boot application (Port 8080)
- **mailhog** - Email testing server (Port 1025)
- **mock-sftp-server** - SFTP server for testing (Port 22)
- **external-validation-api** - External validation service (Port 8082)
- **notification-service** - Notification service (Port 8081)

### Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database
DB_HOST=trial-postgres
DB_PORT=5432
DB_NAME=ihsscmips
SPRING_DATASOURCE_USERNAME=cmips_app
SPRING_DATASOURCE_PASSWORD=cmips_app_password

# Keycloak
KEYCLOAK_ISSUER_URI=http://cmips-keycloak:8080/realms/cmips

# Email
MAIL_HOST=mailhog
MAIL_PORT=1025

# SFTP
SFTP_HOST=mock-sftp-server
SFTP_PORT=22
SFTP_USERNAME=reports
SFTP_PASSWORD=password
```

## Production Deployment

### AWS Deployment

#### 1. Database (RDS PostgreSQL)

- Create RDS PostgreSQL instance
- Configure security groups
- Set up automated backups
- Update connection string in application configuration

#### 2. Keycloak

- Deploy Keycloak on EC2 or ECS
- Configure PostgreSQL as Keycloak database
- Set up SSL/TLS certificates
- Configure realm and client settings

#### 3. Spring Boot Application

- Build Docker image:
  ```bash
  docker build -t timesheet-backend .
  ```

- Deploy to EC2:
  ```bash
  # Copy docker-compose.ec2.app.yml
  docker-compose -f docker-compose.ec2.app.yml up -d
  ```

- Or deploy to ECS:
  - Create ECS task definition
  - Create ECS service
  - Configure load balancer

#### 4. Frontend

- Build Next.js application:
  ```bash
  cd /Users/mythreya/Desktop/timesheet-frontend
  npm run build
  ```

- Deploy to EC2:
  ```bash
  docker build -t timesheet-frontend .
  docker run -d -p 3000:3000 timesheet-frontend
  ```

- Or deploy to S3 + CloudFront:
  ```bash
  npm run build
  aws s3 sync out/ s3://your-bucket-name
  ```

### Configuration for Production

#### Application Configuration

Update `application.yml` or use environment variables:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
```

#### Keycloak Configuration

1. Configure realm: `cmips`
2. Configure client: `trial-app`
3. Set up protocol mapper for `countyId` claim
4. Create users with `countyId` attribute

#### Database Initialization

Run database migrations:

```bash
# Connect to database
psql -h ${DB_HOST} -U ${DB_USER} -d ${DB_NAME}

# Run initialization scripts
\i init-postgres.sql
\i migrate-to-cmips-schema.sql
```

## Health Checks

### Application Health

```bash
curl http://localhost:8080/actuator/health
```

### Database Health

```bash
curl http://localhost:8080/actuator/health/db
```

### Keycloak Health

```bash
curl http://localhost:8080/realms/cmips/.well-known/openid-configuration
```

## Monitoring

### Application Metrics

Access metrics at:
```
http://localhost:8080/actuator/metrics
```

### Logs

View application logs:
```bash
docker-compose logs -f spring-app
```

### Database Monitoring

Monitor database performance:
```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d ihsscmips

# Check active connections
SELECT count(*) FROM pg_stat_activity;
```

## Troubleshooting

### Common Issues

1. **Keycloak Connection Failed**
   - Verify Keycloak is running: `docker ps | grep keycloak`
   - Check issuer URI in application.yml
   - Verify network connectivity

2. **Database Connection Failed**
   - Verify PostgreSQL is running: `docker ps | grep postgres`
   - Check connection string
   - Verify credentials

3. **County Filtering Not Working**
   - Verify JWT token contains `countyId` claim
   - Run `configure-keycloak-county-mapper.sh`
   - Check user attributes in Keycloak

4. **Reports Not Generating**
   - Check job queue: `GET /api/batch-jobs`
   - Verify email/SFTP configuration
   - Check application logs

## Backup and Recovery

### Database Backup

```bash
# Create backup
pg_dump -h ${DB_HOST} -U ${DB_USER} -d ${DB_NAME} > backup.sql

# Restore backup
psql -h ${DB_HOST} -U ${DB_USER} -d ${DB_NAME} < backup.sql
```

### Application Backup

- Backup configuration files
- Backup encryption keys
- Backup report templates

## Security Considerations

1. **Use HTTPS in production**
2. **Rotate JWT secrets regularly**
3. **Use strong database passwords**
4. **Enable database encryption at rest**
5. **Configure firewall rules**
6. **Regular security updates**
7. **Monitor access logs**

## Scaling

### Horizontal Scaling

- Deploy multiple Spring Boot instances behind load balancer
- Use session affinity for stateful operations
- Configure database connection pooling

### Vertical Scaling

- Increase container memory/CPU
- Optimize database queries
- Enable caching where appropriate

