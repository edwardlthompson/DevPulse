# File Size Guide

> Enforced by `scripts/check-file-limits.sh` (pre-commit, `feature-gate.sh`, CI). See [`docs/FEATURE_MODULES.md`](FEATURE_MODULES.md) for the feature container layout.

## Template limits

| Tier | Limit | Enforced paths | Purpose |
|------|-------|----------------|---------|
| **Static data** | 300 lines | UI markup/adapters (TSX/JSX/Vue, web `components/*.ts`, Android `ui/*.kt`) + i18n (`locales/*.json`, `res/values*/strings.xml`) | Declarative content can run longer than algorithmic code |
| **Pure logic** | 150 lines | Feature logic under `examples/` (`.ts`, `.py`, `.kt` excluding tests, UI, and wiring entrypoints) | Keep domain rules small, testable, and agent-readable |
| **Wiring** | 10 lines/feature | Composition roots (`appBootstrap.ts`, `GoldenPathApp.kt`, etc.) | Thin composition — no feature logic in the root |

Line limits are a **maintainability and agent-context proxy**. They are not a direct measure of runtime performance.

## Why this template uses line limits

1. **Agent context efficiency** — files that fit in a single read window reduce token waste and missed edge cases during autonomous edits.
2. **Reviewability** — smaller files map cleanly to BUILD_PLAN vertical slices and PR scope.
3. **Vertical-slice decomposition** — hitting a limit signals it is time to split by feature namespace, not pad a monolith.

## Industry parallels

- **Single Responsibility (Clean Code, SOLID):** no universal file-length cap, but cognitive load rises when one file mixes layout, state, and business rules. Split on responsibility, not a magic number.
- **React / Vue community practice:** extract subcomponents when a file handles multiple concerns or props drilling becomes painful — typically before ~200–400 lines of mixed markup and handlers.
- **Android Compose:** narrow recomposition scope; split Composables when unrelated UI state causes broad invalidation — line count is a hint, not the rule.
- **Google / Airbnb style guides:** emphasize short functions and single purpose over file length; function-level limits (~20–50 lines) matter more than file totals for logic.

Use the template limits as **budgets**, not goals. A 80-line logic file is better than a 149-line one with mixed concerns.

## What actually keeps apps responsive

Line count does not guarantee fast loads or smooth UI. Prefer these patterns:

### Web (PWA)

- **Route-level code splitting** — lazy-import feature panels; keep initial bundle small.
- **Locale namespaces** — split `locales/en.json` by feature prefix when approaching ~200 keys or 300 lines (`settings.*`, `about.*`, etc.).
- **CI perf gates** — Lighthouse budgets and bundle-size checks in `modules/web/MODULE.md` and `.github/workflows/ci.yml` enforce real user-facing metrics.

### Android (Compose)

- **Narrow recomposition** — pass stable state; avoid heavy work inside `@Composable` bodies.
- **Feature-scoped strings** — use `{feature}_*` prefixes in `strings.xml`; split `values/strings.xml` by feature when files grow large.
- **Lazy lists** — use `LazyColumn` / `LazyRow` for long scrollable content instead of eager layout in one Composable file.

### All stacks

- **Keep hot-path pure logic under 150 lines** — fast unit tests, predictable behavior, easy agent fixes.
- **Split before the gate fails** — if a file is within ~20 lines of its limit, extract the next sub-feature proactively.
- **Do not confuse file budgets with bundle size** — minification, tree-shaking, and lazy loading determine download size; line limits keep code organized so those techniques can work.

## When a file exceeds a limit

1. Identify the tier: static data vs pure logic (see table above).
2. Split by **feature namespace** or **sub-responsibility** — not arbitrary line chunks.
3. Re-run `bash scripts/check-file-limits.sh` before committing.
4. For logic splits, add or adjust unit tests so coverage stays on the pure functions.

No autofix exists for file limits — splitting requires semantic judgment.
