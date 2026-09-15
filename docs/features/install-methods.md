# Feature: install-methods

> Sprint 12. User-picked APK install backends. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. No live `su` in unit tests. Default is System confirm.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `InstallMethod` | enum | `System`, `Session`, `Root` |
| `SessionUpdateOwnership` | object | `USER_ACTION_NOT_REQUIRED` only when API 31+, the APK is already installed, and DevPulse is the installer of record |
| `InstallShell` | fun interface | `run(args) -> InstallShellResult` |
| `RootPmInstall.args` | function | `su -c pm install -r --user 0 "path"` or none if the path is unsafe |
| `ApkInstall.apply` | function | Dispatches to System / Session / Root; Root never starts an Activity |
## Acceptance criteria

- ✅ Settings chips pick the method; default System
- ✅ Root is silent only when `su` returns Success; otherwise show a failure (no website). Split APKs use `pm install-create` / `install-write` / `install-commit`.
- ✅ Session may skip the confirm dialog on Android 12+ only for updates of apps DevPulse already installed; first installs and Play/F-Droid-owned apps still prompt
- ✅ Accessibility: chips have labels
- ✅ i18n: `install_method_*`
- ✅ Home shows a tap-to-grant banner when Install unknown apps is off (API 26+)
- ✅ Session falls back to System when the app cannot request package installs
- ✅ Listing tap / Update show why install failed: permission, signing, timeout, no file, or older than installed
- ✅ A listing older than the installed version is not fetched
- ✅ A listing whose minSdk is above the device, or whose native ABI does not overlap, is not fetched
- ✅ Listing rows show known APK size and F-Droid anti-features before download
- ✅ Aptoide listing tap picks Store or Games catalog before fetch
- ✅ Install is refused before the system installer when the APK signing cert does not match the installed app
- ✅ A cert clash from a listing tap offers uninstall-then-install; Update all never uninstalls and instead lists those apps for a later replace
- ✅ Session confirm-pending falls back to root `pm install` (including splits) when `su` works, else System

## Smoke scenario

1. _Given_ a cached APK path `/data/local/tmp/app.apk`
2. _When_ `RootPmInstall.args` and `outcome` run on a fake shell that prints `Success`
3. _Then_ the command contains `pm install` and the result is ok; a bad path yields no command

### Critique

| Issue | Resolution |
|---|---|
| Null/empty APK path | `RootPmInstall.args` returns null; UI keeps the file and shows failure |
| Network timeout | N/A — install is local |
| Race (two Install taps) | Detail button disables while busy |
| Unhandled `su` exception | `ProcessInstallShell` returns exit 127; Root waits only if installed `versionCode` is below the APK, then fails closed |
| Silent install without consent | Default System; Root only after the user picks it; Session skip only for installer-of-record updates |

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/dev/foss/goldenpath/` |
| View | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/` |
| Tests | `examples/android/app/src/test/` |
| Wiring | `GoldenPathApp.kt` ≤10 lines |

## Tests
- Automated: yes — see Container map Tests row and `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
