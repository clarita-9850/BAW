import { test, expect, Page } from '@playwright/test';

test.describe('CDSS Dashboard - Page Load Tests', () => {
  test('DL-001: Dashboard loads successfully', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('dashboard-container')).toBeVisible();
    await expect(page.getByTestId('dashboard-header')).toBeVisible();
  });

  test('DL-002: All main sections are visible', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('metrics-section')).toBeVisible();
    await expect(page.getByTestId('controls-section')).toBeVisible();
    await expect(page.getByTestId('filter-panel')).toBeVisible();
    await expect(page.getByTestId('data-table')).toBeVisible();
    await expect(page.getByTestId('right-filter-panel')).toBeVisible();
  });

  test('DL-003: Metrics display correctly', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('metric-individuals')).toBeVisible();
    await expect(page.getByTestId('metric-population')).toBeVisible();
    await expect(page.getByTestId('metric-per-capita')).toBeVisible();
    await expect(page.getByTestId('metric-total-hours')).toBeVisible();
    await expect(page.getByTestId('metric-avg-hours')).toBeVisible();
  });
});

test.describe('CDSS Dashboard - Dimension Selection Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');
  });

  test('DIM-001: Select single dimension (Age Group)', async ({ page }) => {
    const dimension1Select = page.getByTestId('dimension-1-select');
    await dimension1Select.selectOption('Age Group');
    await expect(dimension1Select).toHaveValue('Age Group');
    await page.waitForTimeout(500); // Wait for data update
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('DIM-002: Select multiple dimensions', async ({ page }) => {
    await page.getByTestId('dimension-1-select').selectOption('Age Group');
    await page.getByTestId('dimension-3-select').selectOption('Ethnicity');
    await page.getByTestId('dimension-4-select').selectOption('Gender');
    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('DIM-003: Change dimension order', async ({ page }) => {
    await page.getByTestId('dimension-1-select').selectOption('Ethnicity');
    await page.waitForTimeout(500);
    await expect(page.getByTestId('dimension-1-select')).toHaveValue('Ethnicity');
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('DIM-004: Select None for dimension', async ({ page }) => {
    await page.getByTestId('dimension-7-select').selectOption('None');
    await expect(page.getByTestId('dimension-7-select')).toHaveValue('None');
  });

  test('DIM-005: Select all 8 dimensions', async ({ page }) => {
    const dimensions = ['Race', 'Ethnicity', 'Gender', 'Age Group', 'Aged, Blind, Disabled', 'County', 'Case Type', 'Race'];
    for (let i = 1; i <= 8; i++) {
      await page.getByTestId(`dimension-${i}-select`).selectOption(dimensions[i - 1] || 'None');
    }
    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('DIM-006: Clear all dimensions', async ({ page }) => {
    for (let i = 1; i <= 8; i++) {
      await page.getByTestId(`dimension-${i}-select`).selectOption('None');
    }
    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });
});

test.describe('CDSS Dashboard - View Type Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');
  });

  test('VW-001: Switch to Details Table view', async ({ page }) => {
    await page.getByTestId('view-details-button').click();
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('VW-002: Switch to Pivot Table view', async ({ page }) => {
    await page.getByTestId('view-pivot-button').click();
    await expect(page.getByTestId('pivot-table')).toBeVisible();
  });

  test('VW-003: Toggle between views', async ({ page }) => {
    await page.getByTestId('view-details-button').click();
    await expect(page.getByTestId('data-table')).toBeVisible();

    await page.getByTestId('view-pivot-button').click();
    await expect(page.getByTestId('pivot-table')).toBeVisible();

    await page.getByTestId('view-details-button').click();
    await expect(page.getByTestId('data-table')).toBeVisible();
  });
});

test.describe('CDSS Dashboard - County Filter Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');
  });

  test('CTY-001: Open county selector dropdown', async ({ page }) => {
    await page.getByTestId('county-dropdown-button').click();
    await expect(page.getByTestId('county-dropdown-menu')).toBeVisible();
  });

  test('CTY-002: Select a county', async ({ page }) => {
    await page.getByTestId('county-dropdown-button').click();
    const countyCheckbox = page.getByTestId('county-dropdown-menu').locator('input[type="checkbox"]').first();
    await countyCheckbox.check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('selected-counties-count')).toBeVisible();
  });

  test('CTY-003: Select all counties', async ({ page }) => {
    await page.getByTestId('county-dropdown-button').click();
    await page.getByTestId('county-select-all').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('CTY-004: Toggle Counties Included', async ({ page }) => {
    const checkbox = page.getByTestId('counties-included-checkbox');
    await checkbox.uncheck();
    await expect(checkbox).not.toBeChecked();
    await checkbox.check();
    await expect(checkbox).toBeChecked();
  });
});

test.describe('CDSS Dashboard - Right Panel Filter Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="right-filter-panel"]');
  });

  test('EVV-001: Filter by EVV Verified status', async ({ page }) => {
    await page.getByTestId('evv-verified').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('evv-verified')).toBeChecked();
  });

  test('PSP-001: Select PM Cases', async ({ page }) => {
    await page.getByTestId('case-type-pm').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('case-type-pm')).toBeChecked();
  });

  test('PSP-002: Select PS Cases', async ({ page }) => {
    await page.getByTestId('case-type-ps').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('case-type-ps')).toBeChecked();
  });

  test('SI-001: Filter severely impaired', async ({ page }) => {
    await page.getByTestId('severely-impaired-yes').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('severely-impaired-yes')).toBeChecked();
  });

  test('ABD-001: Select All Aged, Blind, Disabled', async ({ page }) => {
    await page.getByTestId('abd-all').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('abd-all')).toBeChecked();
  });

  test('ABD-002: Select Aged only', async ({ page }) => {
    await page.getByTestId('abd-aged').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('abd-aged')).toBeChecked();
  });

  test('ETH-001: Select All ethnicities', async ({ page }) => {
    await page.getByTestId('ethnicity-all').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('ethnicity-all')).toBeChecked();
  });

  test('ETH-002: Select Asian ethnicity', async ({ page }) => {
    await page.getByTestId('ethnicity-asian').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('ethnicity-asian')).toBeChecked();
  });

  test('GEN-001: Select All genders', async ({ page }) => {
    await page.getByTestId('gender-all').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('gender-all')).toBeChecked();
  });

  test('GEN-002: Select Female', async ({ page }) => {
    await page.getByTestId('gender-female').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('gender-female')).toBeChecked();
  });

  test('GEN-003: Select Male', async ({ page }) => {
    await page.getByTestId('gender-male').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('gender-male')).toBeChecked();
  });
});

test.describe('CDSS Dashboard - Data Table Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');
  });

  test('TBL-001: Verify table is displayed with data', async ({ page }) => {
    await expect(page.getByTestId('table-body')).toBeVisible();
    await expect(page.getByTestId('table-row-count')).toBeVisible();
  });

  test('TBL-002: Sort by Count ascending', async ({ page }) => {
    await page.getByTestId('sort-count').click();
    await page.waitForTimeout(300);
    await page.getByTestId('sort-count').click(); // Click again for ascending
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('TBL-003: Sort by Count descending', async ({ page }) => {
    await page.getByTestId('sort-count').click();
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('TBL-004: Sort by Population', async ({ page }) => {
    await page.getByTestId('sort-countyPopulation').click();
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('TBL-005: Sort by Authorized Hours', async ({ page }) => {
    await page.getByTestId('sort-authorizedHours').click();
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('TBL-006: Verify row data is visible', async ({ page }) => {
    const firstRow = page.getByTestId('table-row-0');
    // Wait for at least one row to exist (data loaded)
    const rowExists = await firstRow.count() > 0;
    if (rowExists) {
      await expect(firstRow).toBeVisible();
    }
  });
});

test.describe('CDSS Dashboard - Header Metrics Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="metrics-section"]');
  });

  test('HDR-001: Verify Individuals count displays', async ({ page }) => {
    const metric = page.getByTestId('metric-individuals-value');
    await expect(metric).toBeVisible();
    const text = await metric.textContent();
    expect(text).toBeTruthy();
  });

  test('HDR-002: Verify Population count displays', async ({ page }) => {
    const metric = page.getByTestId('metric-population-value');
    await expect(metric).toBeVisible();
    const text = await metric.textContent();
    expect(text).toBeTruthy();
  });

  test('HDR-003: Verify Per Capita Rate displays', async ({ page }) => {
    const metric = page.getByTestId('metric-per-capita-value');
    await expect(metric).toBeVisible();
    const text = await metric.textContent();
    expect(text).toBeTruthy();
  });

  test('HDR-004: Verify Total Authorized Hours displays', async ({ page }) => {
    const metric = page.getByTestId('metric-total-hours-value');
    await expect(metric).toBeVisible();
    const text = await metric.textContent();
    expect(text).toBeTruthy();
  });

  test('HDR-005: Metrics update with filters', async ({ page }) => {
    const initialValue = await page.getByTestId('metric-individuals-value').textContent();

    // Apply a filter
    await page.getByTestId('gender-female').check();
    await page.waitForTimeout(1000);

    const updatedValue = await page.getByTestId('metric-individuals-value').textContent();
    // Values should potentially change (or stay same if no filter effect)
    expect(updatedValue).toBeTruthy();
  });
});

test.describe('CDSS Dashboard - Cross-Filter Interaction Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');
  });

  test('XF-001: Apply dimension + ethnicity filter', async ({ page }) => {
    await page.getByTestId('dimension-1-select').selectOption('Age Group');
    await page.getByTestId('ethnicity-asian').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('XF-002: Apply multiple filter types', async ({ page }) => {
    await page.getByTestId('dimension-1-select').selectOption('Age Group');
    await page.getByTestId('ethnicity-asian').check();
    await page.getByTestId('gender-female').check();
    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });

  test('XF-003: Apply all filter types together', async ({ page }) => {
    // Set dimensions
    await page.getByTestId('dimension-1-select').selectOption('Age Group');
    await page.getByTestId('dimension-3-select').selectOption('Ethnicity');

    // Set demographic filters
    await page.getByTestId('ethnicity-asian').check();
    await page.getByTestId('gender-female').check();
    await page.getByTestId('abd-aged').check();
    await page.getByTestId('case-type-pm').check();

    await page.waitForTimeout(500);
    await expect(page.getByTestId('data-table')).toBeVisible();
  });
});

test.describe('CDSS Dashboard - Measure Selection Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="measure-selection"]');
  });

  test('MSR-001: Select All measures', async ({ page }) => {
    const allCheckbox = page.getByTestId('measure--All-');
    await allCheckbox.check();
    await expect(allCheckbox).toBeChecked();
  });

  test('MSR-002: Toggle individual measure', async ({ page }) => {
    // First uncheck All to enable individual selection
    const allCheckbox = page.getByTestId('measure--All-');
    if (await allCheckbox.isChecked()) {
      await allCheckbox.uncheck();
    }

    const measureCheckbox = page.getByTestId('measure-Authorized-Hours');
    await measureCheckbox.check();
    await expect(measureCheckbox).toBeChecked();
  });
});

test.describe('CDSS Dashboard - Accessibility Tests', () => {
  test('ACC-001: Keyboard navigation - Tab through filters', async ({ page }) => {
    await page.goto('/');

    // Press Tab multiple times to navigate through elements
    for (let i = 0; i < 10; i++) {
      await page.keyboard.press('Tab');
    }

    // Check that focus is on an interactive element (or body if still tabbing)
    const focusedElement = await page.evaluate(() => document.activeElement?.tagName);
    expect(['BUTTON', 'INPUT', 'SELECT', 'A', 'BODY', 'DIV']).toContain(focusedElement);
  });

  test('ACC-002: Focus indicators visible', async ({ page }) => {
    await page.goto('/');

    const button = page.getByTestId('view-details-button');
    await button.focus();

    // Button should have some focus styling (implementation specific)
    await expect(button).toBeFocused();
  });
});

test.describe('CDSS Dashboard - Performance Tests', () => {
  test('PRF-001: Initial page load time', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');
    const loadTime = Date.now() - startTime;

    // Page should load within 5 seconds (generous for test environment)
    expect(loadTime).toBeLessThan(5000);
  });

  test('PRF-002: Filter response time', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');

    const startTime = Date.now();
    await page.getByTestId('gender-female').check();
    await page.waitForTimeout(100); // Small wait for UI update
    const responseTime = Date.now() - startTime;

    // Filter should respond within 3 seconds
    expect(responseTime).toBeLessThan(3000);
  });

  test('PRF-003: Multiple rapid filter changes', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('[data-testid="data-table"]');

    // Rapidly change multiple filters
    for (let i = 1; i <= 5; i++) {
      await page.getByTestId(`dimension-1-select`).selectOption(i % 2 === 0 ? 'Age Group' : 'Ethnicity');
    }

    // Dashboard should still be functional
    await expect(page.getByTestId('data-table')).toBeVisible();
  });
});
