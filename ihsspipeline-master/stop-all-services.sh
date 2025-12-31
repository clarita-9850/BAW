#!/bin/bash

# Script to stop all three services

set -e

echo "🛑 Stopping all services..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the directory paths
TRIAL_DIR="/Users/mythreya/Desktop/trial"
KEYCLOAK_DIR="/Users/mythreya/Desktop/sajeevs-codebase-main/cmipsapplication"
FRONTEND_DIR="/Users/mythreya/Desktop/timesheet-frontend"

# Stop Frontend
echo -e "\n${YELLOW}Stopping Frontend...${NC}"
cd "$FRONTEND_DIR"
docker-compose -f docker-compose.frontend.yml down

# Stop Spring App
echo -e "\n${YELLOW}Stopping Spring App...${NC}"
cd "$TRIAL_DIR"
docker-compose stop spring-app external-validation-api

# Stop Keycloak (optional - keep postgres running if needed)
echo -e "\n${YELLOW}Stopping Keycloak...${NC}"
cd "$KEYCLOAK_DIR"
docker-compose stop keycloak

echo -e "\n${GREEN}✅ All services stopped${NC}"
echo ""
echo "Note: Database containers are still running."
echo "To stop everything including databases, run:"
echo "  cd $TRIAL_DIR && docker-compose down"
echo "  cd $KEYCLOAK_DIR && docker-compose down"

