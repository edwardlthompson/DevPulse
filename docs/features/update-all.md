# Feature: update-all

> Inventory batch update. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. No live network in unit tests. Listing taps download a source APK when one exists; APKMirror follows the listing page to `download.php` when that link is present.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `UpdateAll` | object | `jobs(apps)` is every outdated app with a fetchable source; `run` downloads up to `PARALLEL` APKs at once, then queues installs |
| `UpdateAllResult` | data class | `downloaded`, `installed`, `failedDownload`, `failedInstall` |
## Acceptance criteria

- ✅ User-visible: Update all appears when any visible app has a newer fetchable listing; a dialog shows overall and per-app progress; Back or Close dismisses it and cancels remaining downloads and installs
- ✅ Offline/error: a failed download skips that app; remaining apps still run
- ✅ Accessibility: button and overall bar have content descriptions
- ✅ i18n: `update_all`, `update_all_busy`
- ✅ System/Session still prompt per install; Root stays silent only when the user picked it
- ✅ Play/F-Droid/APKPure/Aptoide/GitHub use the same in-app download path as a listing tap; APKMirror joins Update all only when a cached `download.php` file URL exists
- ✅ A failed source+version is ignored (⚠️ on that listing) and Update all walks remaining newer versions; Has update clears when none remain
- ✅ A successful install settles the package so it leaves the updates list; a failed app stays only while a lower fetchable version remains
- ✅ Ignored versions persist in `ignored_updates.tsv` so the same false-positive listing does not return until Refresh finds a newer version
- ✅ Update all downloads up to four APKs at once, then installs the ready files one at a time
- ✅ Long-press a row with an update to include only that app in Update; empty selection still updates every listed app
- ✅ Remaining Update all packages persist across process death and resume on the next launch
- ✅ Download is skipped when free space is at or below a 64 MB reserve
- ✅ Update on a metered network asks before starting downloads
- ✅ Listings whose minSdk is above the device, or whose ABI does not overlap, are not queued

## Smoke scenario

1. _Given_ two newer apps (Play-listed and F-Droid-listed)
2. _When_ Update all runs
3. _Then_ first-choice APKs download in parallel, installs run one at a time, and a failed source walks the next version in a later wave; successful rows leave the list

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
| Race conditions | Button disables while busy; downloads use `ReleaseRefreshParallel` (`PARALLEL` 4); `InstallAwait` waits for each Session result |
| Unhandled exceptions | `runCatching` in `StoreListingIntent.open`; stage uses `Result` |
| Cache evicts mid-batch | `UpdateAll` passes `maxFiles` (40) into `UpdateCache.stage` |
## Notes

- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
