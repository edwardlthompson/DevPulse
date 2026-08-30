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

- ✅ User-visible: Update all appears when any visible app has a newer fetchable listing; the dialog fills the screen with gutters, lists every queued app, and shows download then install success/fail bars; Back or Close dismisses it and cancels remaining downloads and installs
- ✅ Offline/error: a failed download skips that app; remaining apps still run
- ✅ Accessibility: button and overall bar have content descriptions
- ✅ i18n: `update_all`, `update_all_busy`, `update_all_downloaded`, `update_all_installed`
- ✅ System/Session still prompt per install; Root stays silent only when the user picked it
- ✅ Play/F-Droid/APKPure/Aptoide/GitHub use the same in-app download path as a listing tap; APKMirror joins Update all only when a cached `download.php` file URL exists
- ✅ History lists failed downloads and installs; Retry failed downloads, Retry failed installs, and Reset ignored listings put those apps back in Update all
- ✅ Play files are queued only when the Aurora toggle is on; otherwise Update all uses the next fetchable source
- ✅ A successful install settles the package so it leaves the updates list across process death until Refresh finds a newer listing; a failed app stays only while a lower fetchable version remains
- ✅ Ignored versions persist in `ignored_updates.tsv` so the same false-positive listing does not return until Refresh finds a newer version
- ✅ Update all downloads up to two APKs at once, then installs the ready files one at a time
- ✅ There is no cap on how many apps Update all queues, and APK size is limited only by free disk
- ✅ A cert clash is not installed and is not ignored; it is kept on a signing list after matching-signer installs finish
- ✅ Uninstall-then-install waits until the package is actually gone before installing; a fast uninstall-UI result does not delete the staged APK
- ✅ Downloaded ok/fail counts tick as each APK finishes; the download and install bars are split green (ok) / red (failed) / gray (in progress) against the attempted total, without mixing in-flight byte progress into the overall percent
- ✅ Long-press a row with an update to include only that app in Update; empty selection still updates every listed app
- ✅ Remaining Update all packages persist across process death and resume on the next launch
- ✅ Download is skipped when free space is at or below a 64 MB reserve
- ✅ Update on a metered network asks before starting downloads
- ✅ A Play AppNotPurchased result opens the Play Store listing (or an Open Play Store action in Update all) with copy that the purchase must be verified on Google Play Store; that package is not retried on APKPure in the same batch
- ✅ Play-installed apps are not sideloaded from Aptoide/APKPure in Update all; a Play download that returns no file and has no other source offers Open Play Store
- ✅ Scan and Update all refresh the Aurora Play session once up front so Play file URLs can be fetched on the first pass
- ✅ A GitHub/F-Droid tag like `fdroid-v2.3.6` is not treated as newer than installed `2.3.6`

## Smoke scenario

1. _Given_ two newer apps (Play-listed and F-Droid-listed)
2. _When_ Update all runs
3. _Then_ first-choice APKs download in parallel, installs run one at a time, and a failed source walks the next version in a later wave; successful rows leave the list

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/dev/foss/goldenpath/inventory/UpdateAll.kt`, `UpdateAllTally.kt` |
| View | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/UpdateAllButton.kt`, `UpdateAllDialog.kt` |
| Tests | `UpdateAllTest.kt`, `UpdateAllTallyTest.kt`, `ApkStreamCopyTest.kt`, `UpdateCacheTest.kt`, `ApkSizeCapTest.kt` |
| Wiring | `InventoryScreen` one composable call |
### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | `UpdateAll.artifacts` skips apps without a Direct URL; `UpdateCache.stage` rejects empty bytes |
| Network timeout | `ApkHttpFetcher` 60s read timeout plus one retry on connection reset; `ApkStreamCopy` cancel; failed fetch increments `failedDownload` as that APK finishes |
| Race conditions | Button disables while busy; downloads use `ReleaseRefreshParallel` (`PARALLEL` 2); `InstallAwait` waits for each Session result |
| Unhandled exceptions | `runCatching` in `StoreListingIntent.open`; stage uses `Result` |
| Cache evicts mid-batch | `UpdateCache` file/byte caps are 0 (unlimited); `StorageRoom` 64 MB reserve still blocks a new download |
## Notes

- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
