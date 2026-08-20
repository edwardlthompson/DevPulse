# Feature: update-all

> Inventory batch update. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. No live network in unit tests. Play and APKMirror stay page-only.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `UpdateAll` | object | `artifacts(apps)` is Direct-only; `run` downloads every file then installs sequentially |
| `UpdateAllResult` | data class | `downloaded`, `installed`, `failedDownload`, `failedInstall` |

## Acceptance criteria

- ✅ User-visible: Update all appears when any visible app has a newer direct APK
- ✅ Offline/error: a failed download skips that app; remaining files still install
- ✅ Accessibility: button has a content description
- ✅ i18n: `update_all`, `update_all_busy`
- ✅ System/Session still prompt per install; Root stays silent only when the user picked it
- ✅ APKPure page-only rows download `asset.url` when get_app_update returns an APK; otherwise they open the APKPure app

## Smoke scenario

1. _Given_ two F-Droid artifacts in memory and two newer installed apps
2. _When_ `UpdateAll.run` fetches both then installs
3. _Then_ download callbacks finish before any install; both files are staged

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/dev/foss/goldenpath/inventory/UpdateAll.kt` |
| View | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/UpdateAllButton.kt` |
| Tests | `UpdateAllTest.kt`, `StoreListingIntentTest.kt`, `ApkPureLinkTest.kt` |
| Wiring | `InventoryScreen` one composable call |

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | `UpdateAll.artifacts` skips apps without a Direct URL; `UpdateCache.stage` rejects empty bytes |
| Network timeout | `ApkHttpFetcher` / `Result` — failed fetch increments `failedDownload` and continues |
| Race conditions | Button disables while busy; installs stay sequential after the download phase |
| Unhandled exceptions | `runCatching` in `StoreListingIntent.open`; stage uses `Result` |
| Cache evicts mid-batch | `UpdateAll` passes `maxFiles` (40) into `UpdateCache.stage` |

## Notes

- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
