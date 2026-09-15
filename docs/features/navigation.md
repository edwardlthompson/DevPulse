# Feature: navigation

> Sprint 32. Settings-only home chrome. No bottom nav.

## Acceptance criteria

- ✅ User-visible: home has Settings, not Apps/Updates tabs
- ✅ Offline/error: chrome is local
- ✅ Accessibility: Settings gear is labeled
- ✅ i18n: existing `settings_*`

## Smoke scenario

1. Given inventory home
2. When the user looks for destinations
3. Then only the Settings gear opens chrome

## Container map

| Layer | Path |
|-------|------|
| Logic | `ui/NavigationChrome.kt` |
| View | `ui/GoldenPathScreen.kt` |
| Tests | `NavigationChromeTest.kt` |
| Wiring | none |

## Tests

- Automated: yes — `NavigationChromeTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
