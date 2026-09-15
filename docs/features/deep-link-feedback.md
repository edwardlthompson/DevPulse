# Feature: deep-link-feedback

> Sprint 32. `devpulse://feedback` does not auto-open GitHub.

## Acceptance criteria

- ✅ User-visible: the deep link is recognized
- ✅ Offline/error: matcher is local
- ✅ Accessibility: N/A — no auto dialog
- ✅ i18n: none

## Smoke scenario

1. Given `devpulse://feedback`
2. When MainActivity receives it
3. Then GitHub is not opened

## Container map

| Layer | Path |
|-------|------|
| Logic | `feedback/FeedbackDeepLink.kt` |
| View | none |
| Tests | `FeedbackComposeTest.kt` |
| Wiring | `MainActivity` intent filter |

## Tests

- Automated: yes — `FeedbackComposeTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
