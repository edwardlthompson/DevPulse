# Feature: inventory

> Sprint 2. FR-1 to FR-5. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Shared types live in `dev.foss.goldenpath.inventory`. Compose adapters must not redefine them. Origin resolution and usage-stats queries are later sprints.

### Types

| Name | Kind | Fields / values |
|------|------|-----------------|
| `InstalledApp` | data class | raw `lastUpdateTimeMs` / `firstInstallTimeMs`, plus `installedAtMs`, `installedAtSource`, `remoteReleasedAtMs`, `remoteReleasedSource` |
| `AppOrigin` | enum | `Unknown` (Sprint 2 default), `Play`, `Fdroid`, `ExtraRepo`, `SideloadedUnknown` |
| `UsageStatsConsent` | enum | `NotOffered`, `WalkthroughSeen`, `Granted`, `Declined` |
| `PackageCatalog` | fun interface | `listInstalled(): List<InstalledApp>` — PackageManager adapter only; no network |
| `PackageChange` | object | `shouldReload(action, package, replacing, self)` — install/uninstall/replace only |
| `InstalledAppsRevision` | object | Bumped by `PackageChangeListen` and on resume so the list re-queries PackageManager |
### Functions

| Name | Contract |
|------|----------|
| `InventoryFilter.visibleApps(apps, includeSystem)` | User apps unless `includeSystem` is true |
| `InventoryFilter.sortedByLabel(apps)` | Case-insensitive label order |
| `InstalledDateResolver.resolve(...)` | Honest local date; Sprint 11 |
| `QueryAllPackagesGate.mustExplain(sdkInt)` | true when `sdkInt >= 30` (Android 11+) |
| `QueryAllPackagesGate.canScan(acknowledged, sdkInt)` | scan allowed only after acknowledge when explanation is required |
| `UsageStatsGate.isRequiredForInventory()` | always false |
| `UsageStatsGate.canRankByUsage(consent)` | true only when `Granted` |
### Persistence (`InventoryPreferences`)

| Flow / setter | Default | Meaning |
|---------------|---------|---------|
| `queryAllPackagesAcknowledged` | false | FR-4 gate |
| `includeSystemApps` | false | FR-1 Settings toggle |
| `usageStatsConsent` | `NotOffered` | FR-5 walkthrough; never required |
### Out of scope this sprint

- Play, F-Droid, extra-repo, or forge HTTP
- Setting `origin` to anything other than `Unknown`
- `UsageStatsManager` queries (consent type only)
- Scan badges, Opportunity, alternatives

## Acceptance criteria

- ✅ User-visible behavior: list user-installed apps via PackageManager; Settings toggle includes system apps
- ✅ Offline/error behavior: inventory is local; no network required
- ✅ Accessibility: TalkBack reads label, package, and version on each row
- ✅ i18n: keys under `inventory_*` in `strings.xml`
- ✅ Show icon, label, package name, installed version, `lastUpdateTime`, minSdk, targetSdk
- ✅ Origin is Play, F-Droid, extra repo, sideloaded, or preinstalled — never left as unknown
- ✅ QUERY_ALL_PACKAGES rationale shown once on Android 11+; later launches use the splash image; Settings → Permissions re-grants access; do not scan until acknowledged
- ✅ PACKAGE_USAGE_STATS optional walkthrough; never required

## Smoke scenario

1. _Given_ the user has acknowledged QUERY_ALL_PACKAGES
2. _When_ they open the inventory list
3. _Then_ user apps appear with icon, name, package, and version, without logcat crashes

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../inventory/` |
| View | `examples/android/.../ui/inventory/` |
| Tests | `examples/android/app/src/test/.../inventory/` |
| Wiring | `GoldenPathApp.kt` ≤10 lines |
## Definition of Done

See `docs/FEATURE_MODULES.md`. Tests: PackageManager wrapper unit tests with fakes. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Parallel notes

After Sequential API lock: unit tests live under `examples/android/app/src/test/java/dev/foss/goldenpath/inventory/`; Compose view under `ui/inventory/`; wiring is `GoldenPathApp` ≤10 lines via `rememberInventoryUiModel`. `listInstalled()` runs only after `QueryAllPackagesGate.canScan`.

## Notes

- No Play, F-Droid, or forge calls in this slice
- Include-system lives in Settings. Top-bar Refresh probes every enabled outlet in parallel and counts each F-Droid index plus apps × enabled Play/Aptoide/GitHub probes. Search and sort chips stay behind icons.
- The visible list reloads when a package is installed, uninstalled, or replaced (and again on resume). Store dates for a new app stay unknown until the next Refresh.
- Origin comes from the installer package, then last-release source. targetSdk more than 3 levels behind the device SDK is shown in red.
- Scan interval: on demand, weekly, or monthly (`ScanSchedule` + WorkManager).
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
