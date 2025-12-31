# Integration Hub Framework Demo - E2E Tests

This directory contains Playwright end-to-end tests for the Integration Hub Framework Demo web interface.

## Prerequisites

1. **Node.js** (v18 or higher) and npm
2. **Application Running**: The Spring Boot application should be running on `http://localhost:8081`

## Setup

1. Install dependencies:
```bash
npm install
```

2. Install Playwright browsers:
```bash
npx playwright install
```

## Running Tests

### Run tests with UI mode (RECOMMENDED - Best for watching actions)
This opens an interactive window where you can:
- Watch tests run step-by-step
- See exactly what's happening in the browser
- Pause, replay, and step through tests
- View the timeline of all actions

```bash
npm run test:ui
```

**This is the BEST way to see all actions being tested!**

### Run tests in headed mode (See browser)
This runs tests with a visible browser window:
```bash
npm run test:headed
```
Or using the script:
```bash
./run-tests.sh headed
```

### Run tests in debug mode (Step-by-step)
Pauses at each step so you can inspect the page:
```bash
npm run test:debug
```
Or using the script:
```bash
./run-tests.sh debug
```

### Run all tests (headless - fastest)
```bash
npm test
```

### Run all browsers
```bash
npm run test:all
```

### View test report
```bash
npm run report
```

## Test Coverage

The test suite covers:

1. **UI Navigation Tests**
   - Page load and header verification
   - Tab switching functionality

2. **Schema View Tests**
   - View schema for Employee record type
   - View schema for SimpleRecord record type

3. **Merge Operation Tests**
   - Merge two files
   - Merge with deduplication
   - Merge with sorting

4. **Split Operation Tests**
   - Split file by field
   - Split file by count

5. **Convert Operation Tests**
   - Convert CSV to JSON
   - Convert CSV to XML

6. **Error Handling Tests**
   - Invalid file paths
   - Required field validation

7. **UI Interaction Tests**
   - Show/hide merge options
   - Show/hide split field options based on split type

## Test Data

Tests use sample files from `../data/input/`:
- `employees.csv` - Employee data for testing
- Other test files as needed

## Configuration

Test configuration is in `playwright.config.ts`:
- Base URL: `http://localhost:8081`
- Browsers: Chromium, Firefox, WebKit
- Screenshots: On failure
- Videos: Retained on failure
- Traces: On first retry

## Troubleshooting

1. **Application not running**: Make sure the Spring Boot application is running on port 8081
   ```bash
   cd ..
   mvn spring-boot:run
   ```

2. **Tests timing out**: Increase timeout in `playwright.config.ts` or check if the application is responding

3. **Browser installation issues**: Run `npx playwright install --force`

