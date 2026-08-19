# Repo Hygiene

> What syncs to git vs what stays local. Automation map for template and child repos.

## Track vs ephemeral

| Track in git | Never track (gitignored) |
|--------------|--------------------------|
| Source, tests, docs, CI | `node_modules/`, `.venv/`, caches |
| Lockfiles (`package-lock.json`, `uv.lock`) | `dist/`, `build/`, `target/`, `coverage/` |
| `*.example` configs | `.env`, live `.app-update.json`, `donations.json`, `scratchpad.md`, `.cursor/handoff.md` |
| Design tokens source JSON + `branding/` SVGs / product.json | APK/AAB binaries, Playwright/Lighthouse reports, store PNG exports (unless intentionally committed) |
## Industry standards — automation map

| Practice | Automated where |
|----------|-----------------|
| `.gitignore` / `.gitattributes` / `.editorconfig` | Repo config; linted by `check-repo-hygiene.sh` |
| Secret scanning | Pre-commit + CI (`gitleaks`) |
| Vulnerability scanning | CI (`security.yml`: Trivy, CodeQL) |
| Dependency updates | Dependabot weekly + PR dependency-review |
| Large files (>500 KB) | Pre-commit (staged) + `check-large-tracked-files.sh` (full tree) |
| Tracked build artifacts | `check-tracked-artifacts.sh` |
| UTF-8 encoding | Pre-commit + CI `encoding-check` |
| Merge conflicts / private keys | Pre-commit hooks |
| Lockfile presence | `validate-bootstrap.sh` |
| Ephemeral purge | `scripts/purge-ephemeral.sh` (`git clean -fdX`) |
## Commands

```bash
# Full hygiene gate (same as CI repo-hygiene job)
bash scripts/check-repo-hygiene.sh

# Reclaim disk from ignored caches (dry-run default)
bash scripts/purge-ephemeral.sh
bash scripts/purge-ephemeral.sh --apply   # ignored untracked only

# Install local hooks
pip install pre-commit && pre-commit install
pre-commit run --all-files

```

Windows: use `scripts/purge-ephemeral.ps1` and `scripts/check-repo-hygiene.ps1`.

## Purge safety

`purge-ephemeral.sh --apply` runs `git clean -fdX` — **only gitignored untracked** files. Tracked source is never deleted.

## Policies

- **Git LFS:** avoid unless `[HUMAN]` approves binary asset requirements
- **Submodules:** avoid; prefer lockfiles and vendoring policy in `THIRD_PARTY_LICENSES.md`
- **Commits:** explicit `git add <path>`; review `git status` before commit

## Anti-patterns

| Do not | Why |
|--------|-----|
| Commit `node_modules/` or `dist/` | CI guard fails; bloats history |
| Commit `.env` | Secret leak; gitleaks may catch |
| `git add -A` without review | Accidental artifact commits |
| `git clean -fd` without human review | Deletes non-ignored untracked work |
| Enable Git LFS for source code | FOSS template avoids LFS by default |
## Related

- [`docs/WEB_PROJECT_LAYOUT.md`](WEB_PROJECT_LAYOUT.md) — dist/ not in git
- [`docs/SECURITY_TRIAGE.md`](SECURITY_TRIAGE.md) — Dependabot triage
- [`.cursor/rules/repo-hygiene.mdc`](../.cursor/rules/repo-hygiene.mdc) — agent guardrails
