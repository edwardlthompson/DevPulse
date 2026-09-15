# Feature: crash-inbox

> Sprint 32. GlitchTip/Bugsink stub stays off. Not a live proxy.

## Acceptance criteria

- ✅ User-visible: no inbox UI
- ✅ Offline/error: `CrashInbox.enabled` is false on the example JSON
- ✅ Accessibility: N/A
- ✅ i18n: none

## Smoke scenario

1. Given `crash-inbox.example.json`
2. When the gate runs
3. Then the stub is off (`scripts/check-crash-inbox.sh`)

## Container map

| Layer | Path |
|-------|------|
| Logic | `crashcapture/CrashInbox.kt` |
| View | none |
| Tests | `CrashInboxTest.kt` |
| Wiring | none |

## Tests

- Automated: yes — `CrashInboxTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
