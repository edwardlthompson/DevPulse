# Security Triage

Weekly CVE triage playbook for Dependabot alerts and release security gates.

## Setup (one-time, [HUMAN])

1. Open GitHub -> **Settings** -> **Code security and analysis**
2. Enable **Dependabot alerts** and **Dependabot security updates** (CVE advisories on dependencies)
3. Enable **Private vulnerability reporting** (Settings -> Code security -> Private vulnerability reporting)
4. Verify `.github/dependabot.yml` exists for each active package ecosystem

**Automated setup (recommended):** run the idempotent script after clone or init:

```bash
bash scripts/setup-github-repo.sh
# Windows:
pwsh scripts/setup-github-repo.ps1

```

Requires `gh` CLI authenticated with admin access. On API `422` (plan or permission limits), the script prints a manual UI checklist. Re-run after fixing permissions.

5. Configure branch protection on `main` requiring status checks: **CI**, **Security Scan**, **CodeQL**, **Repo Hygiene**, **Feature Gate**, **Template Upgrade Simulation (Windows)** (`scripts/setup-github-repo.sh` sets these via API; verify in Settings -> Branches)

**Verify after setup:** `bash scripts/verify-branch-protection.sh` asserts required check contexts, `strict: true`, and `allow_force_pushes: false`. Override expected checks with `GITHUB_REQUIRED_CHECKS` when workflow job names differ.

**Rulesets fallback:** GitHub repos using **rulesets** instead of classic branch protection return `404` from `repos/{owner}/{repo}/branches/{branch}/protection`. In that case, confirm equivalent rules in **Settings → Rules → Rulesets** (required status checks, block force pushes, require linear history). Rulesets **Bypass list** (Add bypass → GitHub Actions) lives there — not under **Settings → Branches**. Classic branch protection on **personal** repos has no bypass list; admins bypass by default unless "Do not allow bypassing the above settings" is enabled.

**Note:** Workflow rollup names (`CI`, `Security Scan`, `CodeQL`) and CI job names (`Repo Hygiene`, `Feature Gate`, `Template Upgrade Simulation (Windows)`) must match GitHub check contexts exactly. Override with `GITHUB_REQUIRED_CHECKS` if your repo uses different names.

**Public repos:** Dependabot alerts are free.

`dependabot.yml` schedules version-update PRs; **Dependabot alerts** are a separate GitHub setting for CVE advisories - both are required.

## Weekly Triage Pass

Recommended cadence: **Monday** (aligned with scheduled security scans and `health-check.yml`).

| Step | Owner | Action |
|------|-------|--------|
| 1 | HUMAN | Open **Security -> Dependabot alerts**; sort Critical/High first |
| 2 | HUMAN | Review open Dependabot version-update PRs |
| 3 | AGENT | Apply dependency bumps, run tests locally, open PRs as needed |
| 4 | AUTO | CI (Trivy, CodeQL, matrix tests) validates merges |
| 5 | HUMAN | Merge PR or escalate deferred items |
| 6 | AUTO | Review `weekly-health-check.yml` weekly run (Monday 07:00 UTC); confirm CI + Security Scan + CodeQL green on main |
| 7 | AUTO | Run `bash scripts/check-security-triage.sh --wait-ci 300` (Dependabot + workflows + OpenSSF Scorecard) |
## OpenSSF Scorecard

- Workflow: `.github/workflows/scorecard.yml` (`name: OpenSSF Scorecard`)
- Weekly triage: `check-security-triage.sh` reports latest Scorecard run conclusion
- Pre-release: `pre-release-gate.sh` invokes `check-security-triage.sh --strict` (fails on missing/failed Scorecard)
- SARIF: Scorecard uploads findings to **Security → Code scanning**; triage open items into BUILD_PLAN `[AGENT]` rows or dismiss with rationale in DECISION_LOG.md

### SARIF triage (M35 / 2026-08-15)

| Check | Decision | Rationale |
|-------|----------|-----------|
| PinnedDependencies (GitHub-owned `@vX`) | Dismiss | Allowed by the pin policy below (`@vX.Y.Z` or SHA + comment). Mass SHA-pin conflicts with `validate-workflow-actions.sh`. Third-party scanners stay SHA-pinned. |
| TokenPermissions (workflow-level write) | Fix | Workflows default to `permissions: read-all`; write scopes live on the job that needs them. |
| VulnerabilitiesID (hono / nanoid / postcss GHSAs) | Dismiss | Already patched in v0.18.0 (`hono` ≥4.12.34, `nanoid` ≥3.3.18, `postcss` ≥8.5.23). Re-run Scorecard after lockfile merges; alert is stale vs HEAD. |
| CodeReview / Maintained / CIIBestPractices / Fuzzing | Defer | Process scores, not product CVEs. No BUILD_PLAN row. |
| BinaryArtifacts | Defer | Gradle wrapper JAR is the expected Android wrapper binary (`examples/android/gradle/wrapper/`). |
## Triage Decisions

| Decision | When | Action |
|----------|------|--------|
| **Fix** | Patch available, low risk | Merge Dependabot PR or [AGENT] applies bump |
| **Defer** | No fix yet, acceptable risk window | Open issue with expiry date; log in DECISION_LOG.md |
| **Dismiss** | False positive or not applicable | Document rationale in issue or ADR |
After triage, confirm Trivy and CodeQL workflows are green on `main`.

## GitHub Actions Pin Policy

Third-party workflow actions must use **immutable refs** to reduce supply-chain risk (see Trivy action advisory, March 2026).

| Rule | Detail |
|------|--------|
| **Allowed** | `@vX.Y.Z` (-v prefix semver) or full commit SHA with `# vX.Y.Z` comment |
| **Forbidden** | Bare semver (`@0.28.0`), floating `@v0` / `@main`, unpinned third-party actions |
| **Pre-push** | Run `scripts/validate-workflow-actions.sh` (requires `gh` + `GH_TOKEN`) |
| **Local fast guard** | `scripts/check-workflow-action-ref-format.sh` (pre-commit; no network) |
| **Post-push** | `scripts/check-github-ci.sh --wait 300` - required workflows: **CI**, **Security Scan**, **CodeQL** |
| **Missing runs** | `scripts/check-github-ci.sh --wait 600 --dispatch-if-missing` — `workflow_dispatch` CI/Security/CodeQL when HEAD has no run (covers Dependabot merges that used `GITHUB_TOKEN`) |
| **Automerge token** | Optional repo secret `AUTOMERGE_TOKEN` (PAT with `contents` + `workflow`) so Dependabot auto-merge triggers `push` workflows; without it, weekly health dispatches missing runs. Set via `scripts/setup-automerge-token.sh` (uses `AUTOMERGE_TOKEN` env or `gh auth token`) |
## Release Gate (mandatory before tag)

Before any version bump or GitHub Release:

- 🔲 Weekly triage completed within last **7 days**
- 🔲 Zero open **Critical/High** Dependabot alerts (or documented exception with [HUMAN] approval + linked issue/ADR)
- 🔲 Deferred vulnerabilities have a linked issue and [HUMAN] sign-off in release notes or DECISION_LOG.md
- 🔲 All [AUTO] security scans green on main (Trivy, CodeQL)

**Release workflow gates (`.github/workflows/release.yml`):**

| Trigger | Gate / action |
|---------|----------------|
| `workflow_dispatch` (no tag input) | Full `pre-release-gate.sh` dry-run before next release |
| `workflow_dispatch` (with `tag` input) | SBOM upload only — backfill assets on an existing release |
| `release` published | Polls full CI rollup (`check-github-ci.sh --wait 3600`) then SBOM + Winget stub upload |
| Tag push `v*` | Lightweight gate only: tag must match `.template-version`; polls **Repo Hygiene** + **Feature Gate** via `check-github-ci.sh --skip-workflows` (does **not** wait on CI/CodeQL rollup or emulator jobs) |
Release Please publishes the GitHub Release; the `release` published event attaches SBOM assets. Use `workflow_dispatch` (no tag input) for maintainer dry-runs before merging the Release Please PR.

If a Critical/High alert has no upstream fix, release may proceed only when:

1. A linked issue documents the advisory, impact, and mitigation
2. [HUMAN] explicitly approves in the release notes or DECISION_LOG.md

## OWASP LLM walk (agents / tools / MCP)

When the product exposes agents, run the compact walk in [`THREAT_MODEL.md`](THREAT_MODEL.md) (prompt injection, insecure output, supply chain, over-agency). Map findings to BUILD_PLAN `[AGENT]` rows. No new scanner — Dependabot, CodeQL, Trivy, and Gitleaks remain the automated gates.

1. **Prompt injection / insecure output** — untrusted content cannot become shell or system prompts (destructive-ops Prompt Injection Defense).
2. **Supply chain** — no unsigned marketplace plugins on the default FOSS path.
3. **Excessive agency** — honesty table; hooks fail-open is not a hard deny (KB-012).

## Related Files

| File | Purpose |
|------|---------|
| `.github/dependabot.yml` | Weekly grouped version-update PRs |
| `.github/workflows/security.yml` | Trivy filesystem scan |
| `.github/workflows/codeql.yml` | CodeQL static analysis |
| `.github/workflows/weekly-health-check.yml` | Weekly CI + Security Scan + CodeQL status on main |
| `scripts/validate-workflow-actions.sh` | Resolve action refs via GitHub API |
| `scripts/check-workflow-action-ref-format.sh` | Local bare-semver guard |
| `scripts/check-security-triage.sh` | Weekly Dependabot + workflow + Scorecard gate |
| `scripts/pre-release-gate.sh` | Pre-merge `workflow_dispatch` dry-run (`feature-gate --strict`, `check-security-triage --strict`) |
| `.github/workflows/scorecard.yml` | OpenSSF Scorecard SARIF upload |
| `scripts/setup-github-repo.sh` | One-time Dependabot + reporting + branch protection setup |
| `scripts/setup-automerge-token.sh` | Set `AUTOMERGE_TOKEN` secret from env or `gh auth token` |
| `scripts/verify-branch-protection.sh` | Post-setup branch protection + strict/force-push verification |
| `scripts/verify-reproducible-apk.sh` | Local reproducible APK hash check (also in `run-maintainer-gates.sh` full mode) |
| `docs/MAINTAINING_THE_TEMPLATE.md` | Maintainer release checklist |
| `docs/INITIALIZATION_PROMPT.md` | Section 7a pre-release gate |
