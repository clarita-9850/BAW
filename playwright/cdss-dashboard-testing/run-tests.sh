#!/bin/bash

# CDSS Dashboard Playwright Test Runner Script

echo "========================================"
echo "CDSS Dashboard E2E Tests"
echo "========================================"
echo ""

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if services are running
echo "Checking if services are running..."

if ! curl -s http://localhost:8082/api/dashboard/filters > /dev/null 2>&1; then
    echo "Error: Backend is not running. Please start the application first with ./start.sh"
    exit 1
fi

if ! curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "Error: Frontend is not running. Please start the application first with ./start.sh"
    exit 1
fi

echo "Services are running!"
echo ""

# Navigate to e2e-tests directory
cd e2e-tests

# Install dependencies
echo "Installing test dependencies..."
npm install

# Install Playwright browsers
echo "Installing Playwright browsers..."
npx playwright install

echo ""
echo "Running tests..."
echo ""

# Run tests based on argument
if [ "$1" == "--ui" ]; then
    npx playwright test --ui
elif [ "$1" == "--headed" ]; then
    npx playwright test --headed
elif [ "$1" == "--debug" ]; then
    npx playwright test --debug
else
    npx playwright test
fi

echo ""
echo "Test run complete!"
echo ""
echo "To view the HTML report, run: cd e2e-tests && npx playwright show-report"
