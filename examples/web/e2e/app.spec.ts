import AxeBuilder from "@axe-core/playwright";
import { expect, test } from "@playwright/test";

test("renders golden path heading", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();
  await expect(page.getByTestId("status")).toContainText("Golden Path PWA");
});

test("passes accessibility audit", async ({ page }) => {
  await page.goto("/");
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("passes accessibility audit with settings panel open", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  await expect(page.getByTestId("settings-panel")).toBeVisible();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("passes accessibility audit with about panel open", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "About" }).click();
  await expect(page.getByTestId("about-panel")).toBeVisible();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("homepage visual snapshot", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("main")).toBeVisible();
  await expect(page).toHaveScreenshot("homepage.png", { maxDiffPixelRatio: 0.02 });
});

test("opens settings panel and toggles theme", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  await expect(page.getByRole("heading", { name: "Settings" })).toBeVisible();
  await page.locator("[data-settings-theme]").selectOption("dark");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("persists dark theme after reload", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  await page.locator("[data-settings-theme]").selectOption("dark");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await page.reload();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("toggles update check in settings", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  const toggle = page.locator("[data-settings-update]");
  await expect(toggle).not.toBeChecked();
  await toggle.check();
  await expect(toggle).toBeChecked();
});

test("opens about panel with version", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "About" }).click();
  await expect(page.getByRole("heading", { name: "About" })).toBeVisible();
  await expect(page.getByTestId("about-status")).toBeVisible();
});

test.describe("update status", () => {
  test.use({ serviceWorkers: "block" });

  test("shows update status in about after enabling update check", async ({ page }) => {
    await page.route("**/*", async (route) => {
      const url = route.request().url();
      if (url.includes("/app-update.json")) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            release_repo: "test-owner/test-repo",
            installed_artifact_format: "pwa",
          }),
        });
        return;
      }
      if (url.includes("api.github.com/repos/test-owner/test-repo/releases/latest")) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({ tag_name: "v99.0.0" }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/");
    await page.locator("[data-about-open]").click();
    await expect(page.getByTestId("about-status")).toContainText("latest version");

    await page.getByRole("button", { name: "Close about" }).click();
    await page.getByRole("button", { name: "Settings" }).click();
    await page.locator("[data-settings-update]").check();
    await page.waitForResponse(/releases\/latest/);
    await page.locator("[data-about-open]").click();
    const status = page.getByTestId("about-status");
    await expect(status).toBeVisible();
    await expect(status).toContainText("Update available");
    await expect(status).toContainText("99.0.0");
    await expect(status).not.toContainText("latest version");
  });
});

test.describe("PWA apply update", () => {
  test.use({ serviceWorkers: "block" });

  test("shows apply button when update is available", async ({ page }) => {
    await page.route("**/*", async (route) => {
      const url = route.request().url();
      if (url.includes("/app-update.json")) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            release_repo: "test-owner/test-repo",
            installed_artifact_format: "pwa",
          }),
        });
        return;
      }
      if (url.includes("api.github.com/repos/test-owner/test-repo/releases/latest")) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({ tag_name: "v99.0.0" }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/");
    await page.getByRole("button", { name: "Settings" }).click();
    await page.locator("[data-settings-update]").check();
    await page.waitForResponse(/releases\/latest/);
    await page.getByRole("button", { name: "About" }).click();
    await expect(page.getByTestId("about-apply")).toBeVisible();
    await expect(page.getByTestId("about-apply")).toBeEnabled();
  });

  test("clears restart guard on load", async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem("gp-update-restart-pending", "true");
    });
    await page.goto("/");
    const pending = await page.evaluate(() => localStorage.getItem("gp-update-restart-pending"));
    expect(pending).toBeNull();
  });
});

test.describe("home update banner", () => {
  test.use({ serviceWorkers: "block" });

  test("shows update status on home when check finds newer version", async ({ page }) => {
    await page.route("**/*", async (route) => {
      const url = route.request().url();
      if (url.includes("/app-update.json")) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            release_repo: "test-owner/test-repo",
            installed_artifact_format: "pwa",
          }),
        });
        return;
      }
      if (url.includes("api.github.com/repos/test-owner/test-repo/releases/latest")) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({ tag_name: "v99.0.0" }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/");
    await page.getByRole("button", { name: "Settings" }).click();
    await page.locator("[data-settings-update]").check();
    await page.waitForResponse(/releases\/latest/);
    await expect(page.getByTestId("home-update-status")).toContainText("Update available");
    await expect(page.getByTestId("home-update-status")).toContainText("99.0.0");
  });
});

test("serves cached shell offline via service worker", async ({ page, context }) => {
  await page.goto("/");
  await page.waitForLoadState("networkidle");
  await page.waitForFunction(() => navigator.serviceWorker?.controller != null, null, {
    timeout: 15_000,
  });
  await page.reload();
  await page.waitForLoadState("networkidle");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();

  await context.setOffline(true);
  await page.reload();
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();
  await expect(page.getByTestId("status")).toBeVisible();
});
