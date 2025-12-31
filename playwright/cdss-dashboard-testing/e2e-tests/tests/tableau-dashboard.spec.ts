import { test, expect, Page } from '@playwright/test';

const TABLEAU_URL = 'https://public.tableau.com/app/profile/vinay.srinivasan6771/viz/CDSSAdhocDetailReport/CDSSAdHocReportingTable';

test.describe('Tableau CDSS Dashboard - Live Tests', () => {
  test.setTimeout(180000); // 3 minute timeout for Tableau loading

  test('TAB-001: Dashboard loads successfully', async ({ page }) => {
    // Navigate without waiting for network idle (Tableau keeps connections open)
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });

    // Wait for page to stabilize
    await page.waitForTimeout(15000);

    // Check page loaded
    const title = await page.title();
    console.log(`Page title: ${title}`);

    // Take a screenshot
    await page.screenshot({ path: 'test-results/tableau-loaded.png', fullPage: true });

    expect(title.length).toBeGreaterThan(0);
  });

  test('TAB-002: Verify page title contains CDSS or Tableau', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(10000);

    const title = await page.title();
    console.log(`Page title: ${title}`);

    // Title should contain relevant keywords
    const hasRelevantKeyword = title.toLowerCase().includes('cdss') ||
                               title.toLowerCase().includes('tableau') ||
                               title.toLowerCase().includes('adhoc') ||
                               title.toLowerCase().includes('report');
    expect(hasRelevantKeyword).toBe(true);
  });

  test('TAB-003: Check for visualization container', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(15000);

    // Look for various Tableau container elements
    const selectors = [
      'iframe',
      '[class*="tableau"]',
      '[class*="Tableau"]',
      '[data-test-id]',
      'canvas',
      'svg'
    ];

    let foundElement = false;
    for (const selector of selectors) {
      const count = await page.locator(selector).count();
      if (count > 0) {
        console.log(`Found ${count} elements matching: ${selector}`);
        foundElement = true;
      }
    }

    await page.screenshot({ path: 'test-results/tableau-containers.png', fullPage: true });
    expect(foundElement).toBe(true);
  });

  test('TAB-004: Click on the visualization area', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(20000);

    // Try to find clickable elements
    const clickTargets = ['canvas', 'svg', '[role="img"]', '[class*="viz"]'];

    for (const target of clickTargets) {
      const element = page.locator(target).first();
      if (await element.isVisible().catch(() => false)) {
        console.log(`Clicking on: ${target}`);
        await element.click({ force: true }).catch(e => console.log(`Click failed: ${e.message}`));
        await page.waitForTimeout(2000);
        break;
      }
    }

    await page.screenshot({ path: 'test-results/tableau-after-click.png', fullPage: true });
  });

  test('TAB-005: Check page structure', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(15000);

    // Get all major elements
    const bodyContent = await page.locator('body').innerHTML();
    const hasContent = bodyContent.length > 1000;

    console.log(`Page body content length: ${bodyContent.length}`);

    // Check for iframes (Tableau embeds in iframe)
    const iframeCount = await page.locator('iframe').count();
    console.log(`Number of iframes: ${iframeCount}`);

    await page.screenshot({ path: 'test-results/tableau-structure.png', fullPage: true });

    expect(hasContent).toBe(true);
  });

  test('TAB-006: Take screenshots at different states', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });

    // Screenshot at 5 seconds
    await page.waitForTimeout(5000);
    await page.screenshot({ path: 'test-results/tableau-5s.png', fullPage: true });

    // Screenshot at 15 seconds
    await page.waitForTimeout(10000);
    await page.screenshot({ path: 'test-results/tableau-15s.png', fullPage: true });

    // Screenshot at 30 seconds
    await page.waitForTimeout(15000);
    await page.screenshot({ path: 'test-results/tableau-30s.png', fullPage: true });
  });

  test('TAB-007: Check for interactive elements', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(20000);

    // Count various interactive elements
    const buttons = await page.locator('button').count();
    const inputs = await page.locator('input').count();
    const selects = await page.locator('select').count();
    const links = await page.locator('a').count();

    console.log(`Buttons: ${buttons}, Inputs: ${inputs}, Selects: ${selects}, Links: ${links}`);

    const totalInteractive = buttons + inputs + selects + links;
    expect(totalInteractive).toBeGreaterThan(0);
  });

  test('TAB-008: Test viewport responsiveness', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(15000);

    // Desktop
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'test-results/tableau-desktop.png', fullPage: true });

    // Tablet
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'test-results/tableau-tablet.png', fullPage: true });
  });

  test('TAB-009: Measure load time', async ({ page }) => {
    const startTime = Date.now();

    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    const domTime = Date.now() - startTime;

    await page.waitForTimeout(10000);
    const totalTime = Date.now() - startTime;

    console.log(`DOM Content Loaded: ${domTime}ms`);
    console.log(`Total wait time: ${totalTime}ms`);

    // Just verify it loaded within reasonable time
    expect(domTime).toBeLessThan(60000);
  });

  test('TAB-010: Hover interactions', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(20000);

    // Try hovering over different areas
    const body = page.locator('body');
    await body.hover();

    // Move mouse around
    await page.mouse.move(400, 300);
    await page.waitForTimeout(1000);
    await page.mouse.move(600, 400);
    await page.waitForTimeout(1000);
    await page.mouse.move(800, 500);
    await page.waitForTimeout(1000);

    await page.screenshot({ path: 'test-results/tableau-hover.png', fullPage: true });
  });
});

test.describe('Tableau Dashboard - Iframe Interaction', () => {
  test.setTimeout(180000);

  test('TAB-IFRAME-001: Access Tableau iframe content', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(20000);

    const iframes = page.locator('iframe');
    const count = await iframes.count();
    console.log(`Found ${count} iframes`);

    if (count > 0) {
      // Try to access first iframe
      const frame = page.frameLocator('iframe').first();

      // Look for elements inside
      const innerDivs = frame.locator('div');
      const innerCount = await innerDivs.count().catch(() => 0);
      console.log(`Found ${innerCount} divs inside iframe`);

      // Look for canvas (Tableau renders to canvas)
      const canvases = frame.locator('canvas');
      const canvasCount = await canvases.count().catch(() => 0);
      console.log(`Found ${canvasCount} canvas elements inside iframe`);
    }

    await page.screenshot({ path: 'test-results/tableau-iframe.png', fullPage: true });
  });

  test('TAB-IFRAME-002: Try clicking inside iframe', async ({ page }) => {
    await page.goto(TABLEAU_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForTimeout(25000);

    try {
      const frame = page.frameLocator('iframe').first();

      // Try to click on visualization
      const viz = frame.locator('canvas, svg, [class*="tab"]').first();
      if (await viz.isVisible().catch(() => false)) {
        await viz.click({ force: true });
        console.log('Clicked inside iframe');
        await page.waitForTimeout(3000);
      }
    } catch (e) {
      console.log(`Iframe interaction error: ${e}`);
    }

    await page.screenshot({ path: 'test-results/tableau-iframe-click.png', fullPage: true });
  });
});
