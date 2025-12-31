# CDSS Dashboard Testing Application

This project provides a complete testing environment for the CDSS Adhoc Detail Report Dashboard, including:
- Spring Boot backend with dummy data
- React frontend matching the Tableau dashboard interface
- Playwright E2E tests covering all test scenarios

## Project Structure

```
cdss-dashboard-testing/
├── CDSS_Dashboard_Test_Plan.md    # Comprehensive test plan document
├── backend/                        # Spring Boot backend application
│   ├── src/main/java/com/cdss/dashboard/
│   │   ├── config/                 # Configuration and data initialization
│   │   ├── controller/             # REST API controllers
│   │   ├── dto/                    # Data transfer objects
│   │   ├── entity/                 # JPA entities
│   │   ├── repository/             # Data repositories
│   │   └── service/                # Business logic services
│   └── src/main/resources/
│       └── application.yml         # Application configuration
├── frontend/                       # React frontend application
│   ├── src/
│   │   ├── components/             # React components
│   │   ├── services/               # API services
│   │   └── types/                  # TypeScript types
│   └── package.json
├── e2e-tests/                      # Playwright E2E tests
│   ├── tests/
│   │   └── dashboard.spec.ts       # Test specifications
│   ├── playwright.config.ts        # Playwright configuration
│   └── package.json
├── docker-compose.yml              # Docker Compose configuration
├── start.sh                        # Application startup script
└── run-tests.sh                    # Test runner script
```

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.8 or higher
- npm 8 or higher

## Quick Start

### Option 1: Using Shell Scripts (Recommended)

1. **Start the application:**
   ```bash
   ./start.sh
   ```

2. **In a new terminal, run the tests:**
   ```bash
   ./run-tests.sh
   ```

   Or with options:
   ```bash
   ./run-tests.sh --ui      # Open Playwright UI mode
   ./run-tests.sh --headed  # Run tests with browser visible
   ./run-tests.sh --debug   # Run tests in debug mode
   ```

### Option 2: Manual Setup

1. **Start the Backend:**
   ```bash
   cd backend
   mvn clean spring-boot:run
   ```
   Backend will be available at http://localhost:8082

2. **Start the Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend will be available at http://localhost:3000

3. **Run Playwright Tests:**
   ```bash
   cd e2e-tests
   npm install
   npx playwright install
   npx playwright test
   ```

### Option 3: Using Docker

```bash
docker-compose up --build
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/dashboard/data` | GET/POST | Get dashboard data with optional filters |
| `/api/dashboard/filters` | GET | Get available filter options |

## Test Coverage

The test suite covers the following scenarios (as documented in CDSS_Dashboard_Test_Plan.md):

### Dimension Selection Tests (DIM-001 to DIM-006)
- Single dimension selection
- Multiple dimension selection
- Dimension order changes
- None selection
- All 8 dimensions
- Clear all dimensions

### View Type Tests (VW-001 to VW-004)
- Details Table view
- Pivot Table view
- Toggle between views
- Data consistency across views

### County Filter Tests (CTY-001 to CTY-005)
- Single county selection
- Multiple county selection
- Select all counties
- Clear county selection
- Counties Included toggle

### Right Panel Filter Tests
- Electronic Visit Verification (EVV-001, EVV-002)
- Productive Sup / Paramedical (PSP-001 to PSP-003)
- Severely Impaired (SI-001)
- Aged, Blind, Disabled (ABD-001 to ABD-005)
- Ethnicity (ETH-001 to ETH-007)
- Gender (GEN-001 to GEN-003)
- Age Group (AGE-001 to AGE-003)

### Data Table Tests (TBL-001 to TBL-008)
- Column headers verification
- Sorting (ascending/descending)
- Row data integrity
- Scrolling through large datasets
- Totals calculation

### Header Metrics Tests (HDR-001 to HDR-005)
- Individuals count
- Population count
- Per Capita Rate
- Total Authorized Hours
- Metrics update with filters

### Cross-Filter Interaction Tests (XF-001 to XF-005)
- Dimension + county filter
- Measure + ethnicity filter
- All filter types combined
- Clear all filters
- No data scenarios

### Performance Tests (PRF-001 to PRF-005)
- Initial page load time
- Filter response time
- Large data handling
- Multiple rapid filter changes

### Accessibility Tests (ACC-001 to ACC-004)
- Keyboard navigation
- Screen reader compatibility
- Color contrast
- Focus indicators

## Viewing Test Reports

After running tests, view the HTML report:
```bash
cd e2e-tests
npx playwright show-report
```

## Dummy Data

The application automatically generates realistic dummy data on startup, including:
- 8 major California counties (Los Angeles, San Diego, Sacramento, etc.)
- 5 ethnicities (Asian, Black/African American, Hispanic/Latino, White, Other)
- 2 genders (Male, Female)
- 4 age groups (0-17, 18-64, 65-74, 75+)
- 3 ABD categories (Aged, Blind, Disabled)
- 2 case types (PM, PS)

Total records: ~1,920 demographic combinations

## Configuration

### Backend (application.yml)
- Port: 8082
- Database: H2 in-memory
- H2 Console: http://localhost:8082/h2-console

### Frontend (vite.config.ts)
- Port: 3000
- API Proxy: http://localhost:8082

### Playwright (playwright.config.ts)
- Browsers: Chromium, Firefox, WebKit, Mobile Chrome, Mobile Safari
- Base URL: http://localhost:3000
- Reports: HTML + List

## Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Check if port 8082 is available

### Frontend won't start
- Ensure Node.js 18+ is installed: `node -version`
- Delete `node_modules` and run `npm install` again
- Check if port 3000 is available

### Tests fail to run
- Ensure both backend and frontend are running
- Install Playwright browsers: `npx playwright install`
- Check the base URL in playwright.config.ts

## License

For testing purposes only.
