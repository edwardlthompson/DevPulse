# Feature Modules (Vertical Slices)

> Slow lego assembly: one feature container at a time, smoke-tested before the next. Read when implementing BUILD_PLAN Sprint 2+.

**Cursor modes:** Plan new features (BUILD_PLAN row + resolved `### Critique` Issue→Resolution); Agent Mode for approved scaffold/tests/wire steps; Debug Mode when gates fail after autofix. See [`docs/CURSOR_MODES.md`](CURSOR_MODES.md).

## Industry alignment

| Practice | How this template applies it |
|----------|------------------------------|
| Vertical slices | One folder = logic + view + tests + i18n |
| Ports & adapters | Pure logic; composition root wires adapters only |
| Test pyramid | Unit (many) → smoke (one) → e2e (milestone) |
| Trunk-based batches | One feature per BUILD_PLAN row / PR |
| Definition of Done | Per-feature checklist in BUILD_PLAN plus `PROJECT_CHECKLIST.md` after init |
| Spec-driven | Product intent in `docs/spec.md`; milestone stub in `docs/plan.md` |
| Test-first | Every feature ships tests, or a written fallback validation command |
| Fast feedback | `scripts/feature-gate.sh` after every AGENT step |
## Feature container contract

| Layer | Web | Android | Python | Node |
|-------|-----|---------|--------|------|
| Public API | `src/{feature}/index.ts` (optional barrel) | `{feature}/` package surface | `src/{feature}/` | `src/{feature}/` |
| Pure logic | `src/{feature}/*.ts` (≤150 lines/file) | `{feature}/*.kt` | `src/{feature}/` | `src/{feature}/*.ts` |
| Static data | `src/components/{Feature}Panel.ts`, `locales/*.json` (≤300 lines/file) | `ui/{feature}/` Composable, `strings.xml` | CLI/GUI adapter | route handler / Hono router |
| Tests | `src/{feature}/*.test.ts` | `src/test/.../{feature}/` | `tests/{feature}/` | `src/{feature}/*.test.ts` |
| i18n | `locales/en.json` `{feature}.*` | `strings.xml` `{feature}_*` | help strings module | API error messages / OpenAPI |
| Wiring only | `appBootstrap.ts` / `main.ts` ≤10 lines/feature | `GoldenPathApp.kt` / `MainActivity` nav hook | `main` imports | `src/index.ts` imports |
See [`docs/FILE_SIZE_GUIDE.md`](FILE_SIZE_GUIDE.md) for limits rationale and responsiveness guidance.

**Lego rule:** Remove a feature by deleting its folder, removing wiring lines and i18n keys, then running `bash scripts/feature-gate.sh`. Golden Path must still pass.

## Parallel split (after Sequential step 2)

After the feature container public API is locked, `/build` auto-runs `/scope`. Default Parallel rows:

| Agent | Scope |
|-------|-------|
| Logic + unit tests | `src/{feature}/` or stack equivalent |
| View + i18n | `components/` or `ui/{feature}/`, locales |
| Feature spec | `docs/features/{feature}.md` |
| E2e / instrumented | `e2e/` or `androidTest/` |
See BUILD_PLAN decomposition checklist for multi-stack and docs/CI splits.

**Reference exemplars:** About (Sprint 1) — `examples/web/src/about/`, `examples/android/.../about/`. Settings (Sprint 2) — `examples/web/src/settings/`, `examples/android/.../settings/`.

## Per-feature Definition of Done

Status markers: 🔲 open · ✅ done · ❌ blocked (see `BUILD_PLAN.md` legend).

- 🔲 `[HUMAN]` Acceptance criteria + one smoke scenario documented
- 🔲 `[AGENT]` Feature container scaffolded (no unrelated edits)
- 🔲 `[AGENT]` Unit tests for pure logic (or written fallback command in the feature spec)
- 🔲 `[AGENT]` View wired; composition root (`appBootstrap.ts` / `GoldenPathApp.kt`) diff ≤10 lines
- 🔲 `[AUTO]` `bash scripts/watch-agent-gates.sh --once --autofix`
- 🔲 `[HUMAN]` Manual smoke happy path; approve before next feature

## Autonomous agent protocol

Agents may **auto-fix** lint, format, type, and test failures within feature scope without human approval until **3-strike** on the same step. `git push` still requires human approval.

```bash
# After each AGENT BUILD_PLAN step
bash scripts/watch-agent-gates.sh --once --autofix

# Extended session loop
bash scripts/watch-agent-gates.sh --interval 60 --max-attempts 10 --autofix

# Read progress
bash scripts/agent-progress.sh status --json

# Set active feature (scopes autofix paths)
bash scripts/agent-progress.sh set-feature --name settings

```

**Loop:** gate → `feature-autofix.sh` (mechanical) → re-gate → agent semantic fix from JSON → repeat.

Progress file: `.cursor/agent-progress.json` (gitignored). See `.cursor-session-state.example.json` for chat-restore fields.

## Commands

| Script | Purpose |
|--------|---------|
| `scripts/feature-gate.sh` | Hygiene + encoding + stack lint/test/build |
| `scripts/feature-autofix.sh` | Mechanical multi-stack format/lint (ruff, Biome, cargo fmt, gofmt, whitespace) |
| `scripts/apply-suggested-gate-fixes.sh` | Allowlisted `failed_stage` → safe fixer commands |
| `scripts/watch-agent-gates.sh` | Gate loop with autofix + progress tracking |
| `scripts/agent-progress.sh` | Read/write agent progress JSON |
| `scripts/smoke-stack.sh` | Alias for `feature-gate.sh` |
**CI-only gates (not in local `feature-gate.sh`):** Playwright e2e, Lighthouse budgets, bundle-size, license compliance — see `.github/workflows/ci.yml`. Use `watch-agent-gates.sh --wait-ci 300` after push.

## Anti-patterns

| Do not | Why |
|--------|-----|
| Batch multiple features in one PR | Breaks lego isolation |
| Put business logic in `main.ts` or `GoldenPathApp.kt` | Prevents removal/testing |
| Skip gate after AGENT step | Regressions compound |
| Refactor unrelated code during feature work | Scope creep; breaks parallel safety |
| `git push` without human approval | `destructive-ops.mdc` |
## Related

- [`docs/FOR_AGENTS.md`](FOR_AGENTS.md) — autonomous loop
- [`.cursor/rules/feature-modules.mdc`](../.cursor/rules/feature-modules.mdc)
- [`BUILD_PLAN.md`](../BUILD_PLAN.md) — Sprint 2+ template
