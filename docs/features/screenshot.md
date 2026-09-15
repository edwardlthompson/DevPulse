# Feature: screenshot

> Sprint 32. Local screenshot file name helper. No upload.

## Acceptance criteria

- ✅ User-visible: share name is `devpulse-{ms}.png`
- ✅ Offline/error: naming is local
- ✅ Accessibility: N/A — helper only
- ✅ i18n: none

## Smoke scenario

1. Given a share helper
2. When a timestamp is 1
3. Then the name is `devpulse-1.png`

## Container map

| Layer | Path |
|-------|------|
| Logic | `share/ScreenshotShare.kt` |
| View | none |
| Tests | `ScreenshotShareTest.kt` |
| Wiring | none |

## Tests

- Automated: yes — `ScreenshotShareTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
