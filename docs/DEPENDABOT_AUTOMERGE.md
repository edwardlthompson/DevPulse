# Dependabot auto-merge (patch-only groups)

> Policy for `.github/workflows/dependabot-automerge.yml`. Local `/update-deps` remains primary; GitHub Dependabot is weekly backup.

## What auto-merges

| Update type | Auto-merge? | Notes |
|-------------|-------------|-------|
| `version-update:semver-patch` | Yes | Squash + auto when CI + Dependency Review pass |
| `version-update:semver-minor` | Yes | Same path as patch (workflow today) |
| `version-update:semver-major` | No | Needs a `HUMAN` label, then auto-merge may enable |

Grouped Dependabot PRs (`web-dependencies`, `node-dependencies`, `python-dependencies`, `android-dependencies`, `rust-dependencies`, `go-dependencies`, `github-actions`) inherit the **highest** semver bump in the group. A group that includes a major does **not** get patch-only treatment — treat it as major.

## Patch-only intent for optional stacks

Cargo (`rust-dependencies`) and Go (`go-dependencies`) should stay **weekly grouped**. Prefer local `python3 scripts/agent-run.py update-deps` (dry-run → `--apply` patch/minor) so optional-stack majors never land via silent auto-merge.

If you tighten the workflow to **patch-only** (drop minor from the `if:`), document that change here and in `SECURITY_TRIAGE.md`. Do not auto-merge Kotlin `>=2.3.30` (CodeQL block) — Dependabot already ignores those versions for Android.

## Required protection

Branch protection must require **CI** and **Dependency Review** before merge. Without them, auto-merge is unsafe.

## Token

- Default: `GITHUB_TOKEN` (merge may not retrigger push workflows)
- Preferred: repo secret `AUTOMERGE_TOKEN` (PAT with `contents` + `workflow`) so merges re-run push CI — see `scripts/setup-automerge-token.sh`

## Human checklist

1. Confirm groups still exist in `.github/dependabot.yml`
2. Confirm majors still require the `HUMAN` label
3. After any policy change, run `/maintain` once and watch the next Dependabot PR

See also: [`SECURITY_TRIAGE.md`](SECURITY_TRIAGE.md), [`features/dependabot-cargo-go.md`](features/dependabot-cargo-go.md), [`features/automerge-setup.md`](features/automerge-setup.md).

## `github-actions` group pin policy

The Dependabot `github-actions` group (weekly) may bump **GitHub-owned** actions on the `@vX` / `@vX.Y.Z` pin policy in `docs/SECURITY_TRIAGE.md`. Third-party actions stay **SHA-pinned** with a version comment — Dependabot PRs that rewrite third-party pins to floating tags must not auto-merge.

Local `update-deps` does not rewrite workflow actions; treat github-actions Dependabot PRs as reviewable backup, not silent `/ship` input.
