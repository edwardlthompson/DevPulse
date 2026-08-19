#!/usr/bin/env bash
# Verify lego removal: feature-gate passes with About present and after simulated removal.
# Usage: scripts/verify-about-feature-gate.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

WEB_SRC="$ROOT/examples/web/src"
WEB_E2E="$ROOT/examples/web/e2e"
BACKUP="$(mktemp -d)"

ABOUT_TRACKED=(
  examples/web/src/about
  examples/web/src/main.ts
  examples/web/src/appBootstrap.ts
  examples/web/src/appBootstrap.test.ts
  examples/web/src/AppShell.ts
  examples/web/src/components/AboutPanel.ts
  examples/web/src/settings/preferences.ts
  examples/web/e2e/app.spec.ts
)

restore() {
  if [ -d "$BACKUP/about" ]; then
    rm -rf "$WEB_SRC/about"
    cp -a "$BACKUP/about" "$WEB_SRC/about"
    for rel in main.ts appBootstrap.ts appBootstrap.test.ts AppShell.ts; do
      if [ -f "$BACKUP/$rel" ]; then
        cp -a "$BACKUP/$rel" "$WEB_SRC/$rel"
      fi
    done
    if [ -f "$BACKUP/components/AboutPanel.ts" ]; then
      cp -a "$BACKUP/components/AboutPanel.ts" "$WEB_SRC/components/AboutPanel.ts"
    fi
    if [ -f "$BACKUP/settings/preferences.ts" ]; then
      cp -a "$BACKUP/settings/preferences.ts" "$WEB_SRC/settings/preferences.ts"
    fi
    if [ -f "$BACKUP/app.spec.ts" ]; then
      cp -a "$BACKUP/app.spec.ts" "$WEB_E2E/app.spec.ts"
    fi
  else
    echo "WARN: About backup missing; restoring tracked slice from HEAD"
    git checkout HEAD -- "${ABOUT_TRACKED[@]}" || true
  fi
  rm -rf "$BACKUP"
}
trap restore EXIT

echo "=== About feature gate verification ==="

echo "1/2 Gate with About feature present..."
bash scripts/feature-gate.sh --stack web --step about-with

if [ ! -d "$WEB_SRC/about" ]; then
  echo "WARN: About slice missing before backup; restoring from HEAD"
  git checkout HEAD -- "${ABOUT_TRACKED[@]}"
fi
mkdir -p "$BACKUP/components" "$BACKUP/settings"
cp -a "$WEB_SRC/about" "$BACKUP/about"
cp -a "$WEB_SRC/main.ts" "$BACKUP/main.ts"
cp -a "$WEB_SRC/appBootstrap.ts" "$BACKUP/appBootstrap.ts"
cp -a "$WEB_SRC/appBootstrap.test.ts" "$BACKUP/appBootstrap.test.ts"
cp -a "$WEB_SRC/AppShell.ts" "$BACKUP/AppShell.ts"
cp -a "$WEB_SRC/components/AboutPanel.ts" "$BACKUP/components/AboutPanel.ts"
cp -a "$WEB_SRC/settings/preferences.ts" "$BACKUP/settings/preferences.ts"
cp -a "$WEB_E2E/app.spec.ts" "$BACKUP/app.spec.ts"

$PY << 'PY'
from pathlib import Path
import shutil

web = Path("examples/web/src")
e2e = Path("examples/web/e2e")

def write_lf(path: Path, text: str) -> None:
    # Biome format:check fails on CRLF stubs on Windows — always write LF.
    path.write_text(text, encoding="utf-8", newline="\n")


write_lf(
    web.joinpath("main.ts"),
    """import "./style.css";
import { createThemeToggle } from "./components/ThemeToggle";
import { isOnline } from "./greet";
import { t } from "./i18n";
import { initTheme } from "./theme";

const app = document.querySelector<HTMLDivElement>("#app");
if (!app) throw new Error("App root element not found");
const root = app;

function render(): void {
  const online = isOnline();
  const statusKey = online ? "app.status.online" : "app.status.offline";
  root.innerHTML = `
    <main>
      <div class="gp-header">
        <h1 class="gp-title">${t("app.title")}</h1>
        <div class="gp-header-actions"></div>
      </div>
      <p class="gp-headline">${t("app.greeting")}</p>
      <p class="gp-body" data-testid="status">${t(statusKey)}</p>
    </main>
  `;
  const actions = root.querySelector<HTMLDivElement>(".gp-header-actions");
  if (actions) actions.insertBefore(createThemeToggle(), actions.firstChild);
}

initTheme();
render();
window.addEventListener("online", render);
window.addEventListener("offline", render);
""",
)

write_lf(
    web.joinpath("settings/preferences.ts"),
    """import { getThemeMode, setThemeMode, type ThemeMode } from "../theme";

const INTERVAL_KEY = "gp-app-update-interval";

export function isUpdateCheckEnabled(): boolean {
  return localStorage.getItem(INTERVAL_KEY) !== "off";
}

export function setUpdateCheckEnabled(enabled: boolean): void {
  localStorage.setItem(INTERVAL_KEY, enabled ? "weekly" : "off");
}

export function getSettingsThemeMode(): ThemeMode {
  return getThemeMode();
}

export function applySettingsThemeMode(mode: ThemeMode): void {
  setThemeMode(mode);
}
""",
)

for path in (
    web / "about",
    web / "appBootstrap.ts",
    web / "appBootstrap.test.ts",
    web / "AppShell.ts",
    web / "components" / "AboutPanel.ts",
):
    if path.is_dir():
        shutil.rmtree(path, ignore_errors=True)
    elif path.exists():
        path.unlink()

write_lf(
    e2e.joinpath("app.spec.ts"),
    """import { expect, test } from "@playwright/test";

test("renders golden path heading without About slice", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByTestId("status")).toBeVisible();
});
""",
)
PY

# Normalize stub formatting for Biome format:check (import order, etc.)
if command -v npm >/dev/null 2>&1 && [ -f examples/web/package.json ]; then
  (cd examples/web && npm run format >/dev/null 2>&1) || true
fi

echo "2/2 Gate after About removal (in-place, restored on exit)..."
set +e
ABOUT_WITHOUT_JSON="$(bash scripts/feature-gate.sh --stack web --step about-without --json 2>/dev/null)"
ABOUT_WITHOUT_EXIT=$?
set -e
if [ "$ABOUT_WITHOUT_EXIT" -ne 0 ]; then
  echo "$ABOUT_WITHOUT_JSON"
  echo "FAIL: about-without feature-gate (exit $ABOUT_WITHOUT_EXIT)"
  exit "$ABOUT_WITHOUT_EXIT"
fi

echo "About add/remove verification passed"
