# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

## 2026-08-28 — `/ship` blocked on main merge

- ❌ [HUMAN] Merge https://github.com/edwardlthompson/DevPulse/pull/19 (admin bypass). Cloud `ghs_` token cannot push `main` or `gh pr merge --admin`: GitHub returns `2 of 6 required status checks are expected`. Same pattern as v0.34.1 (merged by `edwardlthompson`). After merge, re-run `/ship` to cut `v0.34.2`.
