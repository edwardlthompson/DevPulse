# Feature: signer-replace

> Play-signed app vs F-Droid/GitHub APK. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. No live uninstall in unit tests. Update all never uninstalls.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `SignerClash` | object | `offer` is true only when package ids match, both cert sets are non-empty, they do not overlap, and the app is not a system app |
| `SignerReplaceNext` | enum | `Install` if the package is gone and files remain; Host waits through a fast uninstall-UI result while the app is still present; `Cancelled` only if Keep current app; `MissingFile` if gone and files are missing |
| `UninstallIntent` | object | `PackageInstaller.uninstall` (same OEM prompt path as install); `ACTION_DELETE` fallback; blank package is ignored |
| `SessionApkInstall` | object | `commit` writes staged APKs and commits a session; `PENDING_USER_ACTION` opens the system installer with `NEW_TASK` |
| `SignerReplaceHold` | data class | Package, label, source, staged file paths under app files |

## Acceptance criteria

- ✅ User-visible: a listing tap that downloaded a cert-mismatched APK shows a replace warning; Cancel leaves the installed app untouched; confirming opens the system uninstall screen, then installs the kept file only if that package is gone
- ✅ Offline/error: missing staged APK after uninstall shows a recovery message and does not claim success; not enough space to copy the APK never opens uninstall
- ✅ Accessibility: dialog title, body, and both buttons are readable text
- ✅ i18n: `signer_replace_*` in `res/values/signer-replace.xml`
- ✅ System apps are never offered replace
- ✅ Wrong package id or empty Play keep is not a signing clash and is not ignored as Signing
- ✅ A process death after uninstall still offers “install the new file” when the staged APKs exist; confirming uninstall does not drop the staged hold when the warning dialog closes
- ✅ The uninstall system screen returning while the app is still present waits for PACKAGE_REMOVED; it does not treat that as cancel or delete the APK
- ✅ Update all does not uninstall; a replace tap closes that dialog so the system uninstall screen is in front, not under a wait overlay
- ✅ After Update all, matching-signer installs finish first; remaining cert clashes appear as Signing replacements
- ✅ Finishing or cancelling one replace deletes only that package’s staged APK; the next queued app still installs
- ✅ After uninstall, replace uses the same PackageInstaller session as uninstall (`NEW_TASK` confirm UI). DevPulse does not show a wait dialog until the user has left that installer.

## Smoke scenario

1. _Given_ fixture `app.devpulse.signtest` installed with cert A and a cert-B APK staged
2. _When_ the user confirms replace and system uninstall
3. _Then_ cert B is installed and cert A is gone; Cancel leaves cert A installed

## Container map

| Layer | Path |
|-------|------|
| Logic | `SignerClash.kt`, `UninstallIntent.kt`, `SignerReplaceStore.kt`, `SignerReplaceQueue.kt` |
| View | `ui/inventory/SignerReplaceHost.kt`, `SignerReplaceInbox.kt` |
| Tests | `SignerClashTest.kt`, `UninstallIntentTest.kt`, `SignerReplaceStoreTest.kt`, `SignerReplaceQueueTest.kt` |
| Wiring | `InventoryScreen` one composable; listing row / one-click offer a hold |

### Critique

| Issue | Resolution |
|----|---|
| Null/empty package or APK | `SignerClash.offer` false; `UninstallIntent.forPackage` null; no uninstall |
| Network timeout | N/A — uninstall/install are local after the APK is already on disk |
| Race (uninstall UI returns early) | Host waits for `PACKAGE_REMOVED` while the app is still installed; staged files stay; Keep current app clears the hold without deleting files |
| Unhandled exceptions | `runCatching` on copy and on starting uninstall; install is the existing `ListingInstallLive` path |
| Uninstall succeeds, install fails | Hold stays; next launch offers install only (no second uninstall) |
| Cache evicts APK during uninstall | Copy into `filesDir/signer-replace/` before any uninstall UI |
| Batch Update all | Cert clashes skip the installer, keep the APK, and join `SignerReplaceQueue`; Update all never uninstalls |
| Wrong-package file delete | `SignerReplaceStore.clear` takes the finished package name; Host resets `installingPkg` so the next gone APK can start |
| Install UI buried | Replace ignores the Settings install-method toggle and always uses `SessionApkInstall.commit`; wait dialog only after returning from the installer |
