#!/bin/bash

# Integration Hub Framework Demo - Playwright Test Runner

echo "========================================"
echo "Integration Hub Framework Demo - E2E Tests"
echo "========================================"
echo ""

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if application is running
echo "Checking if application is running..."
if ! curl -s http://localhost:8081/api/files/health > /dev/null 2>&1; then
    echo "❌ Error: Application is not running on http://localhost:8081"
    echo "Please start the application first:"
    echo "  cd .. && mvn spring-boot:run"
    exit 1
fi

echo "✅ Application is running!"
echo ""

# Parse command line arguments
MODE="${1:-default}"

case "$MODE" in
    "ui"|"--ui")
        echo "Starting Playwright UI Mode (Interactive)..."
        echo ""
        npx playwright test --ui
        ;;
    "headed"|"--headed")
        echo "Running tests in headed mode (visible browser)..."
        echo ""
        npx playwright test --project=chromium --headed --workers=1
        ;;
    "debug"|"--debug")
        echo "Starting Playwright Debug Mode (step-by-step)..."
        echo ""
        npx playwright test --project=chromium --debug
        ;;
    "all"|"--all")
        echo "Running all tests (headless)..."
        echo ""
        npx playwright test
        ;;
    *)
        echo "Running tests (headless, chromium only)..."
        echo ""
        npx playwright test --project=chromium
        ;;
esac

echo ""
echo "Test run complete!"
echo ""
echo "To view the HTML report, run: npm run report"
echo ""
echo "Available modes:"
echo "  ./run-tests.sh ui       - Interactive UI mode"
echo "  ./run-tests.sh headed   - Visible browser"
echo "  ./run-tests.sh debug    - Step-by-step debugging"
echo "  ./run-tests.sh all      - Run all browsers"
echo "  ./run-tests.sh          - Quick run (default)"



