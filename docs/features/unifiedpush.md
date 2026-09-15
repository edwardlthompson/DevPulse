# Feature: unifiedpush

> Sprint 32. UnifiedPush stays off on the FOSS path.

## Acceptance criteria

- ✅ User-visible: no distributor prompt
- ✅ Offline/error: gate is a constant false
- ✅ Accessibility: N/A — no extra UI
- ✅ i18n: none

## Smoke scenario

1. Given a fresh install
2. When the app starts
3. Then UnifiedPush is not registered

## Container map

| Layer | Path |
|-------|------|
| Logic | `notify/UnifiedPushGate.kt` |
| View | none |
| Tests | `UnifiedPushGateTest.kt` |
| Wiring | none |

## Tests

- Automated: yes — `UnifiedPushGateTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
