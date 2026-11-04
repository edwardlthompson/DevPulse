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

CLI_TRACKED=(
  examples/rust/src/lib.rs
  examples/rust/src/main.rs
  examples/rust/src/about.rs
  examples/rust/src/log.rs
  examples/go/main.go
  examples/go/log.go
  examples/go/about.go
  examples/go/about_test.go
  examples/node/src/app.ts
  examples/node/src/about.ts
  examples/node/src/about.test.ts
  examples/node/src/app.test.ts
  examples/node/src/openapi.test.ts
  examples/python/src/hello/cli.py
  examples/python/src/hello/about.py
  examples/python/tests/test_about.py
  examples/python/tests/test_cli.py
  examples/python/tests/test_openapi.py
)

ABOUT_TRACKED=(
  examples/web/src/about
  examples/web/src/main.ts
  examples/web/src/appBootstrap.ts
  examples/web/src/appBootstrap.test.ts
  examples/web/src/AppShell.ts
  examples/web/src/AppShell.test.ts
  examples/web/src/components/AboutPanel.ts
  examples/web/src/settings/preferences.ts
  examples/web/e2e/app.spec.ts
  examples/web/vitest.config.ts
)

restore() {
  # Restore must return 0: with set -e a failed trap cp would fail a passing gate.
  copy_retry() {
    "$PY" - "$1" "$2" <<'PY'
import shutil, sys, time
src, dest = sys.argv[1], sys.argv[2]
last = None
for attempt in range(8):
    try:
        shutil.copy2(src, dest)
        raise SystemExit(0)
    except OSError as exc:
        last = exc
        time.sleep(0.25 * (attempt + 1))
print(f"WARN: copy failed {src} -> {dest}: {last}", file=sys.stderr)
raise SystemExit(1)
PY
  }
  if [ -d "$BACKUP/about" ]; then
    rm -rf "$WEB_SRC/about"
    cp -a "$BACKUP/about" "$WEB_SRC/about" || git checkout HEAD -- examples/web/src/about || true
    for rel in main.ts appBootstrap.ts appBootstrap.test.ts AppShell.ts AppShell.test.ts; do
      if [ -f "$BACKUP/$rel" ]; then
        copy_retry "$BACKUP/$rel" "$WEB_SRC/$rel" || git checkout HEAD -- "examples/web/src/$rel" || true
      fi
    done
    if [ -f "$BACKUP/components/AboutPanel.ts" ]; then
      copy_retry "$BACKUP/components/AboutPanel.ts" "$WEB_SRC/components/AboutPanel.ts" \
        || git checkout HEAD -- examples/web/src/components/AboutPanel.ts || true
    fi
    if [ -f "$BACKUP/settings/preferences.ts" ]; then
      copy_retry "$BACKUP/settings/preferences.ts" "$WEB_SRC/settings/preferences.ts" \
        || git checkout HEAD -- examples/web/src/settings/preferences.ts || true
    fi
    if [ -f "$BACKUP/app.spec.ts" ]; then
      copy_retry "$BACKUP/app.spec.ts" "$WEB_E2E/app.spec.ts" \
        || git checkout HEAD -- examples/web/e2e/app.spec.ts || true
    fi
    if [ -f "$BACKUP/vitest.config.ts" ]; then
      copy_retry "$BACKUP/vitest.config.ts" "$ROOT/examples/web/vitest.config.ts" \
        || git checkout HEAD -- examples/web/vitest.config.ts || true
    fi
  else
    echo "WARN: About backup missing; restoring tracked slice from HEAD"
    git checkout HEAD -- "${ABOUT_TRACKED[@]}" || true
  fi
  if [ -d "$BACKUP/cli" ]; then
    "$PY" "$ROOT/scripts/lib/about_lego_cli.py" restore "$BACKUP/cli" \
      || git checkout HEAD -- "${CLI_TRACKED[@]}" || true
  fi
  rm -rf "$BACKUP"
  return 0
}
trap restore EXIT

echo "=== About feature gate verification ==="

echo "1/4 Gate with About feature present..."
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
if [ -f "$WEB_SRC/AppShell.test.ts" ]; then
  cp -a "$WEB_SRC/AppShell.test.ts" "$BACKUP/AppShell.test.ts"
fi
cp -a "$WEB_SRC/components/AboutPanel.ts" "$BACKUP/components/AboutPanel.ts"
cp -a "$WEB_SRC/settings/preferences.ts" "$BACKUP/settings/preferences.ts"
cp -a "$WEB_E2E/app.spec.ts" "$BACKUP/app.spec.ts"
cp -a "$ROOT/examples/web/vitest.config.ts" "$BACKUP/vitest.config.ts"

$PY << 'PY'
from pathlib import Path
import os
import re
import shutil
import time

web = Path("examples/web/src")
e2e = Path("examples/web/e2e")

def write_lf(path: Path, text: str) -> None:
    # Biome format:check fails on CRLF stubs on Windows — always write LF.
    # Write via sibling tmp + replace; retry if the target is briefly locked.
    data = text.replace("\r\n", "\n").encode("utf-8")
    tmp = path.with_name(path.name + ".about-stub.tmp")
    last: OSError | None = None
    for attempt in range(8):
        try:
            tmp.write_bytes(data)
            os.replace(tmp, path)
            return
        except OSError as exc:
            last = exc
            time.sleep(0.25 * (attempt + 1))
    raise last if last else OSError("write_lf failed")


write_lf(
    web.joinpath("main.ts"),
    """import "./style.css";
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
}

initTheme();
render();
window.addEventListener("online", render);
window.addEventListener("offline", render);
""",
)

# Settings is theme-only and does not import About — leave preferences.ts in place.

for path in (
    web / "about",
    web / "appBootstrap.ts",
    web / "appBootstrap.test.ts",
    web / "AppShell.ts",
    web / "AppShell.test.ts",
    web / "components" / "AboutPanel.ts",
):
    if path.is_dir():
        shutil.rmtree(path, ignore_errors=True)
    elif path.exists():
        path.unlink()

vitest = Path("examples/web/vitest.config.ts")
if vitest.is_file():
    text = vitest.read_text(encoding="utf-8").replace("\r\n", "\n")
    patched, n = re.subn(
        r"include:\s*\[.*?\]",
        'include: [\n        "src/settings/preferences.ts",\n        "src/greet.ts",\n      ]',
        text,
        count=1,
        flags=re.S,
    )
    if n != 1:
        raise SystemExit("could not rewrite vitest coverage include for about-without")
    write_lf(vitest, patched)

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

echo "2/4 Gate after web About removal (in-place, restored on exit)..."
set +e
ABOUT_WITHOUT_JSON="$(bash scripts/feature-gate.sh --stack web --step about-without --json)"
ABOUT_WITHOUT_EXIT=$?
set -e
if [ "$ABOUT_WITHOUT_EXIT" -ne 0 ]; then
  echo "$ABOUT_WITHOUT_JSON"
  echo "FAIL: about-without feature-gate (exit $ABOUT_WITHOUT_EXIT)"
  exit "$ABOUT_WITHOUT_EXIT"
fi

cli_gate() {
  local stack="$1"
  local step="$2"
  case "$stack" in
    rust) command -v cargo >/dev/null 2>&1 || { echo "SKIP rust About $step (cargo not found)"; return 0; } ;;
    go) command -v go >/dev/null 2>&1 || { echo "SKIP go About $step (go not found)"; return 0; } ;;
    node) command -v npm >/dev/null 2>&1 || { echo "SKIP node About $step (npm not found)"; return 0; } ;;
    python) command -v uv >/dev/null 2>&1 || { echo "SKIP python About $step (uv not found)"; return 0; } ;;
  esac
  bash scripts/feature-gate.sh --stack "$stack" --skip-preamble --step "$step"
}

echo "3/4 CLI stacks with About present..."
"$PY" "$ROOT/scripts/lib/about_lego_cli.py" backup "$BACKUP/cli"
for stack in rust go node python; do
  cli_gate "$stack" "about-with-$stack"
done

echo "4/4 CLI stacks after About removal..."
"$PY" "$ROOT/scripts/lib/about_lego_cli.py" strip
if command -v npm >/dev/null 2>&1 && [ -f examples/node/package.json ]; then
  (cd examples/node && npm run format >/dev/null 2>&1) || true
fi
if command -v uv >/dev/null 2>&1 && [ -f examples/python/pyproject.toml ]; then
  (cd examples/python && uv run ruff format src tests >/dev/null 2>&1) || true
fi
if command -v cargo >/dev/null 2>&1 && [ -f examples/rust/Cargo.toml ]; then
  (cd examples/rust && cargo fmt >/dev/null 2>&1) || true
fi
if command -v gofmt >/dev/null 2>&1; then
  gofmt -w examples/go/main.go examples/go/about_test.go >/dev/null 2>&1 || true
fi
for stack in rust go node python; do
  cli_gate "$stack" "about-without-$stack"
done
"$PY" "$ROOT/scripts/lib/about_lego_cli.py" restore "$BACKUP/cli"

echo "About add/remove verification passed (web + rust/go/node/python)"
