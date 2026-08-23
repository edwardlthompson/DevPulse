# Feature: store-status

> Truthful Play/F-Droid/Aptoide/GitHub listing marks, limited to stores the user selected. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

| Name | Contract |
|------|----------|
| `ListingMark` | `Listed`, `Missing`, `Unknown` |
| `InventoryCopy.listingMark(listed, known)` | `Listed` only when `listed && known`; `Missing` only when `!listed && known`; otherwise `Unknown` (null offer included) |
| `StoreSelection.sources(play, aptoide, forge, repoIds)` | Enabled `RemoteReleasedSource` set |
| `StoreSelection.visible(listings, enabled)` | Hide rows for stores that are off |
| `InventoryPreferences.playLookupEnabled` | Default **on** |
| `InventoryPreferences.forgeLookupEnabled` | Default **on** |
| `InventoryPreferences.aptoideLookupEnabled` | Default **off** (unchanged) |
Refresh totals stay `repos(enabled) + apps × enabled probes` via `RefreshLocations.total`. Runner passes a Play/GitHub client only when that toggle is on. F-Droid still uses `FdroidEnabledRepos`.

## Acceptance criteria

- ✅ User-visible behavior: listing rows show ✅ when listed and known, ❌ when missing and known, ❓ when unknown; subtitle distinguishes 403, parse fail, and never listed
- ✅ Offline/error behavior: timeout, scrape fail, or missing offer stays unknown — never a guess
- ✅ Accessibility: TalkBack reads “listed” / “not listed” / “unknown”, not emoji alone; store toggles have labels
- ✅ i18n: `inventory_listing_status_*`, `play_lookup_*`, `forge_lookup_*`

## Smoke scenario

1. _Given_ Play is on and Aptoide is off
2. _When_ the user opens app detail after Refresh
3. _Then_ Play can show ✅ or ❌ from evidence, Aptoide is hidden, and Refresh did not probe Aptoide

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../inventory/StoreSelection.kt`, `InventoryCopy.kt` |
| View | `ui/inventory/InventoryDetailScreen.kt`, `ui/settings/PlayLookupSettings.kt`, `ForgeLookupSettings.kt` |
| Tests | `src/test/.../inventory/StoreSelectionTest.kt`, `InventoryCopyTest.kt`, `ReleaseRefreshTest.kt` |
| Wiring | `ReleaseRefreshRunner.kt` (no `GoldenPathApp` change) |
## Notes

- Do not scan a hidden store as a fallback because another store hit
- After each AGENT step: `python scripts/agent-run.py watch-agent-gates --once --autofix`
