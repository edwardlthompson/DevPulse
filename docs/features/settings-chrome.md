# Feature: settings-chrome

> Sprint 32. Keep product Settings hub. Add Privacy (save-crashes default off).

## Acceptance criteria

- ✅ User-visible: Privacy row under Settings; save-crashes default off
- ✅ Offline/error: toggle is local
- ✅ Accessibility: Privacy row has title + summary
- ✅ i18n: `settings_section_privacy` in `feedback.xml`

## Smoke scenario

1. Given Settings
2. When the user opens Privacy
3. Then save-crashes is off and About is unchanged

## Container map

| Layer | Path |
|-------|------|
| Logic | `settings/SettingsLogic.kt` |
| View | `ui/settings/PrivacySettings.kt` |
| Tests | `SettingsLogicTest.kt` |
| Wiring | none in `GoldenPathApp` |

## Tests

- Automated: yes — `SettingsLogicTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
