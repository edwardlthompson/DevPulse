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
- ✅ Root is silent only when `su` returns Success; otherwise show a failure (no website)
- ✅ Session may skip the confirm dialog on Android 12+ only for updates of apps DevPulse already installed; first installs and Play/F-Droid-owned apps still prompt
- ✅ Accessibility: chips have labels
- ✅ i18n: `install_method_*`

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
| Unhandled `su` exception | `Result` + `install_method_root_failed`; fall through is not automatic |
| Silent install without consent | Default System; Root only after the user picks it; Session skip only for installer-of-record updates |
