# Required vs informational CI checks

Merge-blocking contexts are listed in [`.github/required-checks.json`](../.github/required-checks.json) and applied by `scripts/setup-github-repo.sh` / branch protection. Local `/gates` is stricter than GitHub required checks (it also runs stack feature-gates and smoke).

## Merge-blocking (branch protection)

| Check name | Workflow / job | Notes |
|------------|----------------|-------|
| CI | `ci.yml` → job `ci-ok` (`name: CI`) | Aggregates core child jobs; see failing names in the `ci-ok` summary |
| Security Scan | `security.yml` | Trivy / dependency review path |
| CodeQL | `codeql.yml` | Default + security-extended |
| Repo Hygiene | `ci.yml` → `repo-hygiene` | Tracked artifacts, branch name |
| Feature Gate | `ci.yml` → `feature-gate` | Multi-stack local gates in CI |
| Template Upgrade Simulation (Windows) | `ci.yml` → Windows upgrade job | Child catch-up simulation |

## Informational (do not block merge by default)

| Check / signal | Where | Notes |
|----------------|-------|-------|
| Nix flake | `ci.yml` `nix` job | Path-filtered; skipped ≠ failure for `ci-ok` |
| SBOM diff (Dependabot) | `sbom-diff-dependabot.yml` | `continue-on-error`; comments only |
| Dependency Review on Release Please | `release-please.yml` | Published as a check on the RP head |
| Scorecard | `scorecard.yml` | Weekly / push; triage via `/triage` |
| Pages deploy | `pages.yml` | Publish only; never a required check |
| Cursor Approval / Security agents | external checks | Optional commercial; not in required-checks.json |

When adding a new **required** check: update `required-checks.json`, `settings.yml`, and this table in the same PR.

## Windows upgrade-sim flake recipe

If **Template Upgrade Simulation (Windows)** flakes once: follow KB-024 (re-run failed jobs once). Two consecutive failures = real regression — do not dismiss the required check.
