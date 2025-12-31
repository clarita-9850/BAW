# Quick Setup Checklist - Infra & App Node Communication

## ✅ Phase 1: Network & Security Setup (15 minutes)

### Step 1.1: Verify VPC Configuration
- [ ] Open AWS Console → EC2 → Instances
- [ ] Select both instances (Infra & App)
- [ ] Verify **VPC ID** is the same for both
- [ ] Note **Security Group IDs** for both instances

### Step 1.2: Configure Security Groups

**Infra Node Security Group (Inbound Rules):**
- [ ] Add rule: Custom TCP, Port 8083, Source: App Node Security Group (Keycloak)
- [ ] Add rule: Custom TCP, Port 1025, Source: App Node Security Group (MailHog SMTP)
- [ ] Add rule: SSH, Port 22, Source: Your IP

**App Node Security Group (Inbound Rules):**
- [ ] Add rule: Custom TCP, Port 8080, Source: 0.0.0.0/0 (Spring App)
- [ ] Add rule: Custom TCP, Port 8081, Source: Your IP (Notification Service)
- [ ] Add rule: Custom TCP, Port 8082, Source: Your IP (External Validation)
- [ ] Add rule: SSH, Port 22, Source: Your IP

**RDS Security Group (Inbound Rules):**
- [ ] Add rule: PostgreSQL, Port 5432, Source: Infra Node Security Group
- [ ] Add rule: PostgreSQL, Port 5432, Source: App Node Security Group

---

## ✅ Phase 2: Infra Node Setup (20 minutes)

### Step 2.1: Install Docker
```bash
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user
newgrp docker
docker --version
```

### Step 2.2: Install Docker Compose
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### Step 2.3: Setup Project
```bash
mkdir -p ~/timesheet-app
cd ~/timesheet-app
# Copy docker-compose.ec2.infra.yml here (use SCP or create manually)
```

### Step 2.4: Create Environment File
```bash
cat > .env << 'EOF'
KC_DB_USERNAME=postgres
KC_DB_PASSWORD=0coqyKpW#R.wuSBEPZlIN1rt_8RG
EOF
chmod 600 .env
```

### Step 2.5: Pull Images
```bash
docker pull quay.io/keycloak/keycloak:22.0.5
docker pull mailhog/mailhog:latest
```

### Step 2.6: Start Services
```bash
sudo docker-compose -f docker-compose.ec2.infra.yml up -d
sudo docker-compose -f docker-compose.ec2.infra.yml ps
```

### Step 2.7: Verify Services
```bash
# Wait 2 minutes for Keycloak
curl http://localhost:8083/health/ready

# Check all
sudo docker stats --no-stream
```

---

## ✅ Phase 3: App Node Setup (20 minutes)

### Step 3.1: Install Docker & Docker Compose
```bash
# Same as Infra Node Step 2.1 & 2.2
```

### Step 3.2: Setup Project
```bash
mkdir -p ~/timesheet-app
cd ~/timesheet-app
# Copy docker-compose.ec2.app.yml here
```

### Step 3.3: Get Infra Node Hostname
```bash
# On Infra Node, get hostname:
curl -s http://169.254.169.254/latest/meta-data/local-hostname
# Copy the output (e.g., ip-172-31-31-85.ec2.internal)
```

### Step 3.4: Create Environment File
```bash
# On App Node:
INFRA_HOSTNAME=ip-172-31-31-85.ec2.internal  # Paste from Step 3.3
cat > .env << EOF
INFRA_HOSTNAME=$INFRA_HOSTNAME
EOF
```

### Step 3.5: Test Connection to Infra Node
```bash
INFRA_IP=172.31.31.85  # Replace with actual
curl --connect-timeout 10 http://$INFRA_IP:8083/health/ready
```

### Step 3.6: Pull Images
```bash
docker pull mythreya9850/timesheet-app:latest
docker pull mythreya9850/notification-service:latest
docker pull mythreya9850/external-validation-api:latest
docker pull mythreya9850/mock-sftp-server:latest
```

### Step 3.7: Start Services
```bash
sudo docker-compose -f docker-compose.ec2.app.yml up -d
sudo docker-compose -f docker-compose.ec2.app.yml ps
```

### Step 3.8: Verify Services
```bash
# Wait 2-3 minutes
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/api/validation/health
```

---

## ✅ Phase 4: Keycloak Configuration (15 minutes)

### Option A: Automated Setup (Recommended)

**On Infra Node:**
```bash
cd ~/timesheet-app

# Copy setup-keycloak-automated.sh to infra node
# Then run:
chmod +x setup-keycloak-automated.sh
KEYCLOAK_URL=http://localhost:8083 ./setup-keycloak-automated.sh
```

### Option B: Manual Setup

1. **Access Keycloak Admin:**
   - URL: `http://<infra-public-ip>:8083/admin`
   - Login: admin / admin123

2. **Create Realm:**
   - Name: `reporting-realm`
   - Click "Create"

3. **Create Roles:**
   - CENTRAL_WORKER
   - DISTRICT_WORKER
   - COUNTY_WORKER
   - PROVIDER
   - RECIPIENT
   - SYSTEM_SCHEDULER

4. **Create Users:**
   - central_worker / password123 → CENTRAL_WORKER
   - district_worker / password123 → DISTRICT_WORKER
   - county_worker / password123 → COUNTY_WORKER
   - provider1 / password123 → PROVIDER
   - recipient1 / password123 → RECIPIENT

5. **Create Client:**
   - Client ID: `field-masking-interface`
   - Public Client: ON
   - Valid Redirect URIs: `http://*/*`

---

## ✅ Phase 5: Database Setup (10 minutes)

### Step 5.1: Connect to RDS
```bash
sudo yum install -y postgresql15

psql -h database-1.cqn2wg0syn5v.us-east-1.rds.amazonaws.com \
     -U postgres \
     -d database1 \
     -p 5432
```

### Step 5.2: Insert Mock Data
```bash
# Copy mock-data.sql to your machine, then:
psql -h database-1.cqn2wg0syn5v.us-east-1.rds.amazonaws.com \
     -U postgres \
     -d database1 \
     -f mock-data.sql
```

**Or manually:**
```sql
-- Connect to psql first, then run:
\c database1;
\i mock-data.sql
```

### Step 5.3: Verify Data
```sql
SELECT COUNT(*) FROM timesheets;
SELECT provider_county, COUNT(*) FROM timesheets GROUP BY provider_county;
```

---

## ✅ Phase 6: Testing (10 minutes)

### Test 1: Authentication
```bash
# Get JWT token
curl -X POST http://<infra-public-ip>:8083/realms/reporting-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=field-masking-interface" \
  -d "username=central_worker" \
  -d "password=password123" \
  -d "grant_type=password"
```

### Test 2: Generate Report
```bash
# Use token from Test 1
TOKEN="paste-token-here"

curl -X POST http://<app-public-ip>:8080/api/pipeline/generate-report \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userRole": "CENTRAL_WORKER",
    "reportType": "TIMESHEET_REPORT",
    "dateRange": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    }
  }'
```

## 🔧 Troubleshooting Quick Fixes

### Connection Issues
```bash
# Test from App Node to Infra Node
INFRA_IP=172.31.31.85
curl http://$INFRA_IP:8083/health/ready

# Check security groups if fails
```

### Keycloak Not Starting
```bash
# On Infra Node
sudo docker logs keycloak | tail -n 50
sudo docker-compose -f docker-compose.ec2.infra.yml restart keycloak
```

### App Services Not Starting
```bash
# On App Node
sudo docker logs spring-app | tail -n 50
sudo docker-compose -f docker-compose.ec2.app.yml restart
```

---

## 📋 Final Verification Checklist

- [ ] All security groups configured correctly
- [ ] Infra node services running (2 containers)
- [ ] App node services running (4 containers)
- [ ] Can access Keycloak admin console
- [ ] Can authenticate with test users
- [ ] Mock data exists in database
- [ ] Can generate reports
- [ ] Field masking working based on roles

---

## 🎯 Success Criteria

Your setup is complete when:
1. ✅ Authentication works (can get JWT tokens)
2. ✅ Reports generate with field masking
3. ✅ All services show "UP" status
4. ✅ No connection errors in logs

---

**Total Setup Time: ~90 minutes**

**Need help?** Refer to `COMPLETE_SETUP_GUIDE.md` for detailed explanations.

