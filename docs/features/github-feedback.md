# Feature: github-feedback

> Sprint 32. Issue URL only after an explicit tap. Never auto-open GitHub.

## Acceptance criteria

- ✅ User-visible: Open GitHub is a tap, not a launch side effect
- ✅ Offline/error: URL is still composed offline
- ✅ Accessibility: the open action is labeled
- ✅ i18n: `feedback_open_github` in `feedback.xml`

## Smoke scenario

1. Given a composed draft
2. When the user does not tap Open GitHub
3. Then no browser/issue is started

## Container map

| Layer | Path |
|-------|------|
| Logic | `feedback/FeedbackGithub.kt` |
| View | `ui/settings/PrivacySettings.kt` |
| Tests | `FeedbackComposeTest.kt` |
| Wiring | none in `GoldenPathApp` |

## Tests

- Automated: yes — `FeedbackComposeTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
