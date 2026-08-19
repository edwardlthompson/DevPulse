# Knowledge Base

> Repository of stack-specific edge cases, resolved complex bugs, anti-patterns, and reusable project solutions.
> **Do not populate with generic framework definitions.**

## How to use

1. Add entries only after resolving a non-obvious issue specific to this project.
2. Include: symptom, root cause, fix, and prevention.
3. Link to relevant ADRs or PRs when available.

## Entries

### KB-001 — UTF-16 file corruption on Windows

| Field | Detail |
|-------|--------|
| **Symptom** | `check-json` / `npm` / `json.load` fails; git ignore rules stop working; `.gitignore` shows as untracked patterns not applied |
| **Cause** | Cursor `StrReplace` or Windows editor saves text as UTF-16 LE (NUL bytes between ASCII chars) |
| **Fix** | Rewrite affected files with Python `Path.write_text(..., encoding='utf-8')`; re-run `scripts/check-file-encoding.sh` |
| **Prevention** | Bulk edits on Windows via Python/PowerShell UTF-8 write; include root `.gitignore` in encoding scan |
### KB-002 — Invalid `trivy-action@0.28.0` ref

| Field | Detail |
|-------|--------|
| **Symptom** | Security Scan workflow fails at setup: action version not found |
| **Cause** | Bare semver `@0.28.0` is not a valid GitHub Action ref tag |
| **Fix** | Pin to full SHA: `aquasecurity/trivy-action@a9c7b0f06e461e9d4b4d1711f154ee024b8d7ab8 # v0.36.0` |
| **Prevention** | Run `validate-workflow-actions.sh` pre-push; use `check-workflow-action-ref-format.sh` locally |
### KB-003 — `gh api --silent` false CI failures

| Field | Detail |
|-------|--------|
| **Symptom** | `validate-workflow-actions.sh` fails in CI with unknown `gh` flag error |
| **Cause** | `gh api` has no `--silent` flag; stderr not suppressed correctly |
| **Fix** | Redirect to `/dev/null` instead: `gh api ... >/dev/null 2>&1` |
| **Prevention** | Test validation scripts in CI job with `GH_TOKEN`; avoid undocumented `gh` flags |
### KB-004 — Lighthouse performance flake on shared runners

| Field | Detail |
|-------|--------|
| **Symptom** | CI fails with performance 0.88 vs required 0.90 on a single Lighthouse run |
| **Cause** | GitHub-hosted runner CPU variance; single-run assertion is noisy |
| **Fix** | Set `numberOfRuns: 3` in `.lighthouserc.json`; LHCI uses median; keep `minScore: 0.9` |
| **Prevention** | Do not lower performance budget for CI flake; use multi-run median in `modules/web/MODULE.md` |
### KB-005 — Playwright webServer duplicate build

| Field | Detail |
|-------|--------|
| **Symptom** | E2E hangs or serves stale assets; double `vite build` in CI |
| **Cause** | `webServer` runs build while CI already built; wrong host binding |
| **Fix** | Use `vite preview` on `127.0.0.1`; CI runs `npm run build` once before Playwright |
| **Prevention** | Golden Path `examples/web/playwright.config.ts` documents preview-only webServer |
### KB-006 — TypeScript strict null in render handlers

| Field | Detail |
|-------|--------|
| **Symptom** | `tsc` / ESLint error: Object is possibly null inside `render()` callback |
| **Cause** | `strictNullChecks` + `document.getElementById` return type includes null |
| **Fix** | Assign narrowed ref at module scope: `const root = document.getElementById('root')!` or guard once |
| **Prevention** | Module-level `const root = app` pattern in `examples/web/src/main.ts` |
### KB-007 — npm/pip overrides policy for transitive CVEs

| Field | Detail |
|-------|--------|
| **Symptom** | Dependabot or `npm audit` / `uv pip audit` reports CVE in a transitive dependency with no direct upgrade path |
| **Cause** | Parent package pins or bundles a vulnerable sub-dependency; fix not yet published upstream |
| **Fix** | **npm:** add `overrides` in `package.json` to force patched semver (see `examples/web` `@lhci/cli` overrides). **Python:** prefer `uv`/`pip` constraint or bump direct dep; document in DECISION_LOG if override is temporary |
| **Prevention** | Prefer overrides over `--force` installs; remove overrides when upstream ships fix; weekly triage per `docs/SECURITY_TRIAGE.md`; see KB-007 before dismissing Dependabot alerts |
### KB-009 — Release Please `pr` output is JSON, not a PR number

| Field | Detail |
|-------|--------|
| **Symptom** | `release-please.yml` sync step fails: `Error reading JToken from JsonReader` or empty `gh pr checkout` |
| **Cause** | `steps.release.outputs.pr` is empty when `release_created == 'true'` (post-merge push) or stale PR metadata |
| **Fix** | Skip sync when `release_created`; resolve PR number in shell from `PR_JSON` or `gh pr list --head release-please--branches--main` |
| **Prevention** | Never use bare `fromJSON(steps.release.outputs.pr)` in workflow `env:` without a non-empty guard |
### KB-008 — `android-release` APK hash compare policy

| Field | Detail |
|-------|--------|
| **Symptom** | `Android - assembleRelease` fails: APK hashes differ between two clean `assembleRelease` runs on CI |
| **Cause** | Usually a reproducibility regression (non-hermetic timestamp, path, or dependency drift). Rare runner flakes are possible but treated as failures to catch real regressions early |
| **Fix** | Rebuild locally with `SOURCE_DATE_EPOCH=1700000000 ./gradlew clean assembleRelease` twice; compare `sha256sum` of release APK. Align `build.gradle.kts`, `gradle.properties`, and dependency lockfiles with `modules/android/MODULE.md` |
| **Prevention** | Keep `SOURCE_DATE_EPOCH` pinned in CI; use `scripts/verify-reproducible-apk.sh --strict` before release tags. Do not downgrade the job to WARN — strict compare is intentional (M17 P2) |
### KB-010 — Agent shell opens `.sh` files and steals editor focus

| Field | Detail |
|-------|--------|
| **Symptom** | While typing, a `.sh` tab opens and keystrokes land in the wrong file during Cursor Agent work |
| **Cause** | Agent runs `bash scripts/*.sh`; Cursor reveals script paths. `beforeShellExecution` hooks used to run `.sh` wrappers on every shell command |
| **Fix** | Use `python3 scripts/agent-run.py <name> [args]` in agent commands; hooks migrated to `.cursor/hooks/*.py`; workspace `.vscode/settings.json` sets `workbench.editor.autoReveal: false` |
| **Prevention** | Agents follow `.cursor/commands/` and `scripts/agent-run.py`; pin active editor tab; optional `<!-- cursor-hooks: off -->` in `BUILD_PLAN.md` disables hooks entirely |
### KB-012 — Cursor hooks fail-open (not a hard guarantee)

| Field | Detail |
|-------|--------|
| **Symptom** | Agent runs `git push` or another denylisted command even though `destructive-ops.mdc` says it is blocked |
| **Cause** | `before_shell_guard.py` and `after_edit_encoding.py` fail-open: parse errors, empty command, missing denylist, or `<!-- cursor-hooks: off -->` in `BUILD_PLAN.md` return allow. `/push` approval of `git push` also matches `git push --force` via substring |
| **Fix** | Treat hooks as **instructed-with-best-effort**. Require `[HUMAN]` or `/push` / `/ship` for destructive-ops. Do not label fail-open hooks as hard denies |
| **Prevention** | Honesty table in `.cursor/rules/destructive-ops.mdc` and `docs/CURSOR_INTEGRATIONS.md`; keep `shell-denylist.txt` in sync with the rule |
### KB-013 — `npm ci` fails after `@puppeteer/browsers` override

| Field | Detail |
|-------|--------|
| **Symptom** | CI `npm ci` in `examples/web`: Missing `proxy-agent@8` from lock file after overriding `@puppeteer/browsers` >=3.2.0 |
| **Cause** | Browsers 3.2.0 optional peer `proxy-agent` >=8.0.1. Local `npm install` on Node 26 can omit that tree; Actions Node 22 `npm ci` requires it |
| **Fix** | Add `"proxy-agent": ">=8.0.2"` to web overrides; run `npm ci` locally before push |
| **Prevention** | After puppeteer/LHCI overrides, verify with `npm ci` (not only `npm install`) |
### KB-014 — Windows PowerShell init hangs on Python 3.14 pyrepl

| Field | Detail |
|-------|--------|
| **Symptom** | `scripts/simulate-template-upgrade.sh` PowerShell smoke (`pwsh … init-project.ps1`) hangs; Python reports `WinError 123` from `getheightwidth` / pyrepl |
| **Cause** | CPython 3.14 enables the interactive `pyrepl` frontend. On Windows it queries console size even when stdin is not a TTY, then blocks |
| **Fix** | Set `PYTHON_BASIC_REPL=1` (and `PYTHONUNBUFFERED=1`) in `scripts/init-project.ps1`, `scripts/init-project.sh`, and the upgrade-sim `pwsh` invocation |
| **Prevention** | Keep those env vars on every Windows Python spawn used by init / `/ship` regress. Do not use the interactive REPL in non-interactive scripts |
### KB-015 — Windows upgrade-sim: `jq` CRLF and post-prune doc links

| Field | Detail |
|-------|--------|
| **Symptom** | Required **Template Upgrade Simulation (Windows)** fails. Ubuntu may pass. Logs show `281 path(s) missing` including the running `validate-template-index.sh`, or `check-doc-links` breaks on `modules/android/COMMERCIAL.md` after `--prune` |
| **Cause** | `jq.exe` under Git Bash emits CRLF, so `test -e "$ROOT/$path"` looks for `path\r`. After web prune, commercial docs still link into removed stack trees |
| **Fix** | Strip CR in `scripts/validate-template-index.sh` `check_path`. `check-doc-links` skips missing `modules/<stack>` / `examples/<stack>` targets when that stack directory is gone. Portable-purpose tests must not require `coding-agent` after child init stamps a new purpose |
| **Prevention** | Do not mark `/ship` done until both upgrade-sim jobs are green. Prefer Python path checks on Windows; keep Unreleased empty before Release Please |
### KB-016 — Git Bash PATH and inherited PYTHONPATH

| Field | Detail |
|-------|--------|
| **Symptom** | `command -v gh` fails in Git Bash even when GitHub CLI is installed. `validate-bootstrap` fails with `ModuleNotFoundError` for `env_schema` / `agent_adapters` |
| **Cause** | Git Bash does not inherit `C:\Program Files\GitHub CLI`. A parent shell `PYTHONPATH=scripts/lib` shadows repo-root imports |
| **Fix** | `scripts/lib/resolve-tools.sh` prepends Windows tool dirs and unsets `PYTHONPATH`. `agent-run` passes `child_env()` that drops `PYTHONPATH` |
| **Prevention** | Never export `PYTHONPATH=scripts/lib` in the agent shell. Source `resolve-tools.sh` before `command -v gh` |
### KB-017 — Fold empties local Unreleased only

| Field | Detail |
|-------|--------|
| **Symptom** | After `/ship` merges Release Please, `CHANGELOG.md` still has leftover `[Unreleased]` bullets under or after the new version heading |
| **Cause** | `changelog_unreleased.py --fold` writes the working tree and comments the PR; it does not commit. Release Please copies Unreleased from the last pushed commit |
| **Fix** | Empty `[Unreleased]` (and keep it first) in a `docs(release)` archive commit after merge, or empty it in the prepare commit before push |
| **Prevention** | Do not mark `/ship` done until Unreleased is first and empty on `origin/main` |
### KB-011 — Vitest jsdom `localStorage` broken on Node 25+

| Field | Detail |
|-------|--------|
| **Symptom** | `npm test` in `examples/web`: `TypeError: Cannot read properties of undefined (reading 'clear')` or `localStorage.getItem is not a function` |
| **Cause** | Node 25+ enables a global Web Storage stub without `--localstorage-file`; jsdom skips installing real Storage and the stub shadows it |
| **Fix** | Vitest `setupFiles: ["src/test/setup-localStorage.ts"]` installs in-memory Storage when `getItem` is missing |
| **Prevention** | Keep the setup file; do not rely on Node’s experimental `localStorage` in browser-unit tests |
