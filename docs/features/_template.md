# Feature: {name}

> Copy this file to `docs/features/{name}.md` before Sprint 2+ work. Replace placeholders. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: _describe the happy path_
- 🔲 Offline/error behavior: _describe degraded mode_
- 🔲 Accessibility: _keyboard/screen reader expectations (web/Android)_
- 🔲 i18n: keys added under `{feature}.*` (web `locales/`, Android `strings.xml`)

## Smoke scenario

1. _Given_ app is running on the active stack
2. _When_ user _performs primary action_
3. _Then_ _expected outcome_ without console/logcat errors

## Container map

| Layer | Path |
|-------|------|
| Logic | `src/{feature}/` or stack equivalent |
| View | `src/components/{Feature}Panel.ts` / `ui/{feature}/` |
| Tests | co-located unit tests |
| Wiring | composition root ≤10 lines |
## Tests

- Automated: yes — `{path to unit/e2e tests}` (set **no** only when automation is not feasible)
- Coverage: pure logic plus one smoke path

## Fallback validation

Required when Automated is **no**. Still name the smoke command when tests exist.

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack <active>`

## Definition of Done

See `docs/FEATURE_MODULES.md` per-feature checklist and BUILD_PLAN Sprint 2+ feature template rows.

## Notes

- Reference exemplar: About screen (`examples/web/src/about/`, `examples/android/.../about/`)
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
