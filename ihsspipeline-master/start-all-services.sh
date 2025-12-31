#!/bin/bash

# Script to start all three services (Spring App, Keycloak, Frontend) on shared network
# This ensures all containers can communicate with each other

set -e

echo "🚀 Starting all services on cmips-shared-network..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if cmips-shared-network exists, create if not
if ! docker network ls | grep -q "cmips-shared-network"; then
    echo -e "${YELLOW}⚠️  cmips-shared-network not found. Creating it...${NC}"
    docker network create cmips-shared-network
    echo -e "${GREEN}✅ Created cmips-shared-network${NC}"
else
    echo -e "${GREEN}✅ cmips-shared-network already exists${NC}"
fi

# Get the directory paths
TRIAL_DIR="/Users/mythreya/Desktop/trial"
KEYCLOAK_DIR="/Users/mythreya/Desktop/sajeevs-codebase-main/cmipsapplication"
FRONTEND_DIR="/Users/mythreya/Desktop/timesheet-frontend"

# Function to check if a container is running
check_container() {
    local container_name=$1
    if docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
        return 0
    else
        return 1
    fi
}

# Step 1: Start Keycloak (from sajeevs-codebase-main)
echo -e "\n${BLUE}📦 Step 1: Starting Keycloak service...${NC}"
cd "$KEYCLOAK_DIR"
if check_container "cmips-keycloak"; then
    echo -e "${YELLOW}⚠️  Keycloak container (cmips-keycloak) is already running${NC}"
else
    echo "Starting Keycloak and its dependencies..."
    docker-compose up -d keycloak postgres
    echo -e "${GREEN}✅ Keycloak service started${NC}"
    echo "   Container: cmips-keycloak"
    echo "   Port: http://localhost:8085"
    echo "   Network: cmips-shared-network"
fi

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to be ready..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s -f http://localhost:8085/health/ready > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Keycloak is ready!${NC}"
        break
    fi
    attempt=$((attempt + 1))
    echo "   Attempt $attempt/$max_attempts - waiting for Keycloak..."
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${YELLOW}⚠️  Keycloak may not be fully ready, but continuing...${NC}"
fi

# Step 2: Start Spring App (from trial)
echo -e "\n${BLUE}📦 Step 2: Starting Spring App service...${NC}"
cd "$TRIAL_DIR"
if check_container "spring-app"; then
    echo -e "${YELLOW}⚠️  Spring App container (spring-app) is already running${NC}"
else
    echo "Starting Spring App and its dependencies..."
    docker-compose up -d spring-app postgres external-validation-api
    echo -e "${GREEN}✅ Spring App service started${NC}"
    echo "   Container: spring-app"
    echo "   Port: http://localhost:8080"
    echo "   Network: cmips-shared-network"
fi

# Wait for Spring App to be ready
echo "Waiting for Spring App to be ready..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Spring App is ready!${NC}"
        break
    fi
    attempt=$((attempt + 1))
    echo "   Attempt $attempt/$max_attempts - waiting for Spring App..."
    sleep 3
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${YELLOW}⚠️  Spring App may not be fully ready, but continuing...${NC}"
fi

# Step 3: Start Frontend (from timesheet-frontend)
echo -e "\n${BLUE}📦 Step 3: Starting Frontend service...${NC}"
cd "$FRONTEND_DIR"
if check_container "timesheet-frontend"; then
    echo -e "${YELLOW}⚠️  Frontend container (timesheet-frontend) is already running${NC}"
else
    echo "Starting Frontend..."
    docker-compose -f docker-compose.frontend.yml up -d
    echo -e "${GREEN}✅ Frontend service started${NC}"
    echo "   Container: timesheet-frontend"
    echo "   Port: http://localhost:3000"
    echo "   Network: cmips-shared-network"
fi

# Summary
echo -e "\n${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ All services started successfully!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Services are running on cmips-shared-network:"
echo "  🔐 Keycloak:      http://localhost:8085"
echo "  🚀 Spring App:    http://localhost:8080"
echo "  🎨 Frontend:       http://localhost:3000"
echo ""
echo "Network: cmips-shared-network"
echo ""
echo "To view logs:"
echo "  docker logs -f cmips-keycloak"
echo "  docker logs -f spring-app"
echo "  docker logs -f timesheet-frontend"
echo ""
echo "To stop all services:"
echo "  ./stop-all-services.sh"
echo ""
