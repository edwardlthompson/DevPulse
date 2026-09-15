# Feature: feedback

> Sprint 32. Local markdown compose. Never auto-sends.

## Acceptance criteria

- ✅ User-visible: Privacy can copy a bug/feature draft
- ✅ Offline/error: compose works without network
- ✅ Accessibility: copy control is labeled
- ✅ i18n: `feedback_*` in `feedback.xml`

## Smoke scenario

1. Given save-crashes is off
2. When the user copies a bug draft
3. Then markdown is local and GitHub does not open

## Container map

| Layer | Path |
|-------|------|
| Logic | `feedback/FeedbackCompose.kt` |
| View | `ui/settings/PrivacySettings.kt` |
| Tests | `FeedbackComposeTest.kt` |
| Wiring | none in `GoldenPathApp` |

## Tests

- Automated: yes — `FeedbackComposeTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
