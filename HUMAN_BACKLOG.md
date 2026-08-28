# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

## 2026-08-28 — After PR #19 automerge (KB-031)

- ✅ [AUTO] https://github.com/edwardlthompson/DevPulse/pull/19 merged by `ready-pr-automerge.yml` (`github-actions[bot]`, `69a5e06`)
- 🔲 [HUMAN] Cloud `ghs_` cannot `workflow_dispatch`. Either set repo secret `AUTOMERGE_TOKEN` (PAT so future merges start Actions) or run `gh workflow run` for CI, Security Scan, CodeQL, and Release Please on `main`
- 🔲 [HUMAN] After those runs exist, re-run `/ship` to cut `v0.34.2`
