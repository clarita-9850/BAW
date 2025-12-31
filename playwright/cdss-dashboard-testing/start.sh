#!/bin/bash

# CDSS Dashboard Testing Application Startup Script

echo "========================================"
echo "CDSS Dashboard Testing Application"
echo "========================================"
echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "Checking prerequisites..."

if ! command_exists java; then
    echo "Error: Java is not installed. Please install JDK 17 or higher."
    exit 1
fi

if ! command_exists node; then
    echo "Error: Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi

if ! command_exists mvn; then
    echo "Error: Maven is not installed. Please install Maven."
    exit 1
fi

echo "All prerequisites are met!"
echo ""

# Navigate to project directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Start Backend
echo "Starting Backend Server..."
cd backend
mvn clean spring-boot:run &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 15

# Check if backend is running
if ! curl -s http://localhost:8082/api/dashboard/filters > /dev/null 2>&1; then
    echo "Warning: Backend might not be fully started yet. Continuing..."
fi

# Install frontend dependencies and start
echo "Starting Frontend Server..."
cd frontend
npm install
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================"
echo "Application Started!"
echo "========================================"
echo ""
echo "Backend API: http://localhost:8082"
echo "Frontend:    http://localhost:3000"
echo "H2 Console:  http://localhost:8082/h2-console"
echo ""
echo "Press Ctrl+C to stop all services"
echo ""

# Wait for interrupt
trap "echo 'Stopping services...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT TERM
wait
