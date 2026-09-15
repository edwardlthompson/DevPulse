# Feature: privacy-report

> Sprint 32. Local privacy summary. No DSN.

## Acceptance criteria

- ✅ User-visible: Privacy page can show the local report
- ✅ Offline/error: text is bundled, no network
- ✅ Accessibility: report is readable text
- ✅ i18n: labels in `feedback.xml`

## Smoke scenario

1. Given Settings → Privacy
2. When the user reads the report
3. Then it states local-only and no auto-send

## Container map

| Layer | Path |
|-------|------|
| Logic | `privacy/PrivacyReport.kt` |
| View | `ui/settings/PrivacySettings.kt` |
| Tests | `PrivacyReportTest.kt` |
| Wiring | none |

## Tests

- Automated: yes — `PrivacyReportTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
