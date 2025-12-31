#!/bin/bash

# Run tests slowly so you can see all actions

echo "========================================"
echo "Running Playwright Tests - Slow Mode"
echo "========================================"
echo ""
echo "This will run tests with a 1 second delay between actions"
echo "so you can see everything happening clearly."
echo ""
echo "Make sure the application is running on http://localhost:8081"
echo "Press Ctrl+C to stop"
echo ""
sleep 2

cd "$(dirname "$0")"

# Check if application is running
if ! curl -s http://localhost:8081/api/files/health > /dev/null 2>&1; then
    echo "❌ Error: Application is not running on http://localhost:8081"
    echo "Please start it first: cd .. && mvn spring-boot:run"
    exit 1
fi

SLOW_MO=1000 npx playwright test --project=chromium --headed --workers=1



