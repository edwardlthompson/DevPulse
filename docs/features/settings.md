# Feature: settings

> Second vertical-slice exemplar (Sprint 2) for child repos after About (Sprint 1).

## Acceptance criteria

- ✅ User can open a Settings panel/screen from the main navigation
- ✅ Theme preference (light/dark/system) persists across restarts
- ✅ Update-check toggle respects opt-in telemetry policy (`docs/PRIVACY.md`)
- ✅ Offline: settings load last persisted values; no network required for display
- ✅ i18n: all user-visible strings under `settings.*` keys
- ✅ Settings can wipe cached remotes, dump chunks, and the F-Droid index without clearing ignores or pasted repos

## Smoke scenario

1. Given the app is running with default theme
2. When the user opens Settings and switches theme to dark
3. Then the UI applies dark theme immediately and still uses dark theme after cold restart

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| Logic | `examples/web/src/settings/` | `examples/android/.../settings/` |
| View | `examples/web/src/components/SettingsPanel.ts` | `examples/android/.../ui/settings/` |
| Tests | `settings/*.test.ts` | `src/test/.../settings/` |
| Wiring | `appBootstrap.ts` + `AppShell.ts` | `GoldenPathApp.kt` (composition root) |
## Out of scope (Sprint 2)

- Account sync, cloud backup, analytics
- Donation URL editing (stays in About / `donations.json`)

## Notes

- Theme, About, and Share/Export live in Settings. The inventory top bar is Refresh, Search, Filters, and Settings only
- About opens on top of Settings; system back closes About first, then Settings, then returns to inventory. Close settings must not finish the activity
- Reuse `ThemePreferences` patterns from `examples/android/.../ui/theme/` where applicable
- Gate after each AGENT BUILD_PLAN step: `bash scripts/watch-agent-gates.sh --once --autofix --step <scaffold|tests|wire>`
