# Feature: update-all

> Inventory batch update. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. No live network in unit tests. Listing taps download a source APK when one exists; APKMirror follows the listing page to `download.php` when that link is present.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `UpdateAll` | object | `jobs(apps)` is every outdated app with a fetchable source; `run` keeps up to `PARALLEL` (6) APK downloads in a slot pool and installs ready files one at a time |
| `UpdateAllResult` | data class | `downloaded`, `installed`, `failedDownload`, `failedInstall` |
## Acceptance criteria

- ✅ User-visible: Update all appears when any visible app has a newer fetchable listing; the dialog fills the screen with gutters, lists every queued app, and shows download then install success/fail bars; Hide dismisses the dialog and keeps the batch (the button reopens it); Stop cancels remaining downloads and installs; Close is not cancel
- ✅ Offline/error: a failed download skips that app; remaining apps still run
- ✅ Accessibility: button and overall bar have content descriptions
- ✅ i18n: `update_all`, `update_all_busy`, `update_all_downloaded`, `update_all_installed`
- ✅ System/Session still prompt per install; Root stays silent only when the user picked it
- ✅ Play/F-Droid/APKPure/Aptoide/GitHub use the same in-app download path as a listing tap; APKMirror joins Update all only when a cached `download.php` file URL exists
- ✅ History lists failed downloads and installs; Retry failed downloads, Retry failed installs, and Reset ignored listings put those apps back in Update all
- ✅ Play files are queued only when the Aurora toggle is on; otherwise Update all uses the next fetchable source
- ✅ A successful install settles the package so it leaves the updates list across process death until Refresh finds a newer listing; a failed app stays only while a lower fetchable version remains
- ✅ Ignored versions persist in `ignored_updates.tsv` so the same false-positive listing does not return until Refresh finds a newer version
- ✅ Update all keeps up to six APK downloads in flight (slot pool, not a 2-job wave) and installs those ready files one at a time; a slow download does not block the next prepare
- ✅ A listing older than installed (or unusable ABI/`Sdk`) is persisted to `ignored_updates.tsv` with no Fail row and no failDl count; the app leaves Updates / Update N unless another source is actually newer
- ✅ Refresh scans then auto-starts Update All in the same window when work remains; Close/Hide only dismisses the window
- ✅ Each app row has a download bar and an install bar; downloading/installing rows stay above finished rows
- ✅ Play Older does not fetch APKPure/APKMirror in the same batch; a signing clash does not download other sources
- ✅ The live Update all list magnet-follows to the top until the user scrolls, then stays locked for that dialog
- ✅ There is no cap on how many apps Update all queues, and APK size is limited only by free disk
- ✅ A cert clash is not installed and is not ignored; it is kept on a signing list; other sources for that package are not fetched in the same batch
- ✅ Uninstall-then-install waits until the package is actually gone before installing; a fast uninstall-UI result does not delete the staged APK
- ✅ Downloaded ok/fail counts tick as each APK finishes; the download and install bars are split green (ok) / red (failed) / gray (in progress) against the attempted total, without mixing in-flight byte progress into the overall percent
- ✅ Long-press a row with an update to include only that app in Update; empty selection still updates every listed app
- ✅ Remaining Update all packages persist across process death and resume on the next launch
- ✅ Download is skipped when free space is at or below a 64 MB reserve
- ✅ Update on a metered network asks before starting downloads
- ✅ A Play AppNotPurchased result opens the Play Store listing (or an Open Play Store action in Update all) with copy that the purchase must be verified on Google Play Store; that package is not retried on APKPure in the same batch
- ✅ Play-installed apps are not sideloaded from Aptoide/APKPure in Update all; a Play download that returns no file and has no other source offers Open Play Store
- ✅ After a Play Session install, installer-of-record may be DevPulse; Play-listed apps that are already current are not retried from APKPure/APKMirror
- ✅ Scan and update hides Stop once the batch is idle; the window auto-closes after finished work so a signing-hold row does not require Stop
- ✅ Session install waits for confirm, then times out after `InstallAwait.TIMEOUT_MS` even if Play Protect is still showing, so the batch does not hang forever
- ✅ Scan and Update all refresh the Aurora Play session once up front so Play file URLs can be fetched on the first pass
- ✅ A GitHub/F-Droid tag like `fdroid-v2.3.6` is not treated as newer than installed `2.3.6`
- ✅ Aptoide listings resolve via `listAppsUpdates` + signing SHA-1; a resolve miss is logged as `ResolveMiss` and is not ignored
- ✅ When both installed and listed version codes are present, those codes decide “newer” (junk Aptoide names do not override)
- ✅ Listing downloads look up the installed version/code so a same-version extra does not re-download
- ✅ Play files whose Aurora `versionCode` is not newer than installed are not downloaded; an archive older than installed or whose `lib/` ABI does not overlap the device is dropped (`Older` / `Sdk`). Download fail log lines include that why. Transient DNS/connection drops retry once and stay `Timeout` (not ignored). Remembered Aptoide `lib/` ABIs persist (and survive a later listing that omits `cpus`) so a later Update all does not re-download the same unusable APK.

## Smoke scenario

1. _Given_ two newer apps (Play-listed and F-Droid-listed)
2. _When_ Update all runs
3. _Then_ first-choice APKs download up to six at a time, installs run one at a time, a slow APK does not stall later prepares, and a failed source walks the next version; successful rows leave the list

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
| Race conditions | Button disables while busy; downloads use a 6-slot pool; `InstallAwait` waits for each Session result; list magnet ignores programmatic scrolls |
| Unhandled exceptions | `runCatching` in `StoreListingIntent.open`; stage uses `Result` |
| Cache evicts mid-batch | `UpdateCache` file/byte caps are 0 (unlimited); `StorageRoom` 64 MB reserve still blocks a new download |
## Notes

- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`

## Tests

- Automated: yes — see Container map Tests row and `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
