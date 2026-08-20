# Feature: opportunity

> Sprint 8. FR-25 to FR-28. Not Sprint 0 or 1. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: group stale apps by Play or F-Droid category; show how many have gone quiet
- 🔲 Offline/error behavior: uses cached scan model; no live scrape
- 🔲 Accessibility: category counts readable by TalkBack
- 🔲 i18n: keys under `opportunity_*` in `strings.xml`
- 🔲 Personal Develop-next / Fork-this list with private notes
- 🔲 Focused export of most-used stale titles plus category gaps as CSV and JSON
- 🔲 DevPulse shows its own GitHub activity via configured package and repo
- 🔲 Pinned apps appear only when the user asks

## Smoke scenario

1. _Given_ a cached scan with two stale apps in one category
2. _When_ the author opens Opportunity
3. _Then_ that category shows a quiet count and DevPulse self-pulse does not depend on search

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../opportunity/` |
| View | `examples/android/.../ui/opportunity/` |
| Tests | `src/test/.../opportunity/` |
| Wiring | `GoldenPathApp.kt` ≤10 lines |

## Definition of Done

Ranking unit tests required. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Usage-stats opt-in from FR-5 feeds ranking when granted
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
