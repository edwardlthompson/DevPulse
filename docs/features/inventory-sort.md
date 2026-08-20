# Feature: inventory-sort

> Sprint 11. FR-5 query, FR-21 inventory half, honest local dates. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. Compose adapters must not redefine them.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `InstalledDateSource` | enum | `LastUpdate`, `FirstInstall`, `ApkMtime`, `Unknown` |
| `RemoteReleasedSource` | enum | `Play`, `Fdroid`, `ExtraRepo`, `Izzy`, `Guardian`, `Calyx`, `Archive`, `Aptoide`, `Forge`, `None` |
| `InventorySortMode` | enum | `Oldest`, `Newest`, `Name`, `UsedAndStale` |
| `UsageSnapshot` | data class | `packageName`, `lastTimeUsedMs`, `totalTimeInForegroundMs` |
| `RemoteDate` | data class | `ms`, `source` |
| `RemoteReleasePick` | data class | `ms`, `source` |

### Functions

| Name | Contract |
|------|----------|
| `InstalledDateResolver.resolve(...)` | Max of plausible lastUpdate / firstInstall / APK mtime; else unknown. Rejects pre-2008-09-23 and future |
| `RemoteRelease.pick(...)` | Newest non-null remote date |
| `RemoteRelease.ageMs(app)` | `remoteReleasedAtMs` else `installedAtMs` |
| `InventoryFilter.matchesQuery` | Blank query returns all |
| `InventoryFilter.olderThan` | Uses `ageMs`; unknown excluded |
| `InventoryFilter.onAnyListedSource` | Empty set shows all; otherwise `listed && known` on any selected source (OR) |
| `InventorySourceFilter` | Persistable chip set: Play, F-Droid, Archive, Izzy, Guardian, Calyx, Aptoide, GitHub |
| `InventorySort.apply` | Unknown last on Oldest/Newest; UsedAndStale without usage ≡ Oldest |
| `UsagePulse.score` | `foregroundHours30d * ageDays`; unknown age = 0 |
| `UsageStatsAccess.isGranted` | AppOps only; catch SecurityException |

## Acceptance criteria

- ✅ User-visible behavior: sort Oldest/Newest/Name/Used+stale; search; older-than-180d; never show 1971
- ✅ Offline/error behavior: inventory works without network; remotes fill from Refresh
- ✅ Accessibility: chip and search labels from `strings.xml`
- ✅ i18n: `inventory_sort_*`, `inventory_filter_*`, `inventory_updated_unknown`, `inventory_last_release*`

## Smoke scenario

1. _Given_ an app whose PackageManager `lastUpdateTime` is epoch
2. _When_ the inventory list opens
3. _Then_ it shows Last release: unknown (not 1971), and Oldest does not put it first

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../inventory/` |
| View | `examples/android/.../ui/inventory/` |
| Tests | `examples/android/app/src/test/.../inventory/` |
| Wiring | `GoldenPathApp.kt` 0 new lines |

## Definition of Done

Unit tests for resolver, sort, pulse, filters. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Aptoide fetch is opt-in and Refresh probes it for every user app when enabled. See `docs/features/aptoide.md`.
- Refresh draws a determinate full-width bar (no animation APIs) so it still fills when animator duration scale is 0.
- Refresh runs in `ReleaseRefreshService` so Settings, About, and app switches do not cancel it. A local notification fires when it finishes.
- The main list is name, last-release date, and an update icon when a newer remote version exists. The app card uses an information icon for Android app details. Store rows are one line (version · date) under the store name; a live row opens that download page. The card always shows Play, F-Droid, Archive, Izzy, Guardian, Calyx, and Aptoide (Delisted until a live hit). F-Droid versions come from the newest APK in `packages`, not `suggestedVersionName`. Forge links are only real repo release pages and do not inherit the F-Droid version.
- Filters include Older than 180 days, Has update, and **On {source}** chips for Play, F-Droid, Archive, IzzyOnDroid, Guardian, Calyx, Aptoide, and GitHub (`listed && known`). Multiple source chips are OR. The main list does not add GitHub marks. Share exports the current visible list as HTML, CSV, or XML — see `docs/features/inventory-export.md`.
- Release dates, highest version, and every live listing persist in `remote_releases.json`. Last-release date and highest version can come from different sources. Play is linked only after the store page is present.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
