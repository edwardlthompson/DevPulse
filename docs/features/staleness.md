# Feature: staleness

> Sprint 3 heart. FR-15 to FR-17. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Pure functions in `dev.foss.goldenpath.staleness`. Installed `lastUpdateTime` is never an activity signal.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `RemoteSource` | enum | `Play`, `Fdroid`, `ExtraRepo`, `Forge` |
| `RemoteLookup` | enum | `NotChecked`, `Failed`, `SuccessMissing`, `SuccessDated` |
| `RemoteSignal` | data class | `source`, `lookup`, `activityAtMs`, `countsAsActivity` |
| `Badge` | enum | `Green`, `Amber`, `Red`, `Unknown` |
| `StalenessInput` | data class | `remotes`, `installedLastUpdateMs`, `targetSdk`, `stubTargetSdk` (default 37) |
| `StalenessResult` | data class | `newestRemoteActivityMs`, `daysSinceActivity`, `badge`, `installedLastUpdateMs`, `compatibilityWarning` |

### Functions

| Name | Contract |
|------|----------|
| `Staleness.evaluate(input, nowMs)` | Badge from newest `countsAsActivity` dated remote only |
| `Staleness.badgeForDays(days)` | Green `< 180`; amber `180..365`; red `> 365` |
| `Staleness.compatibilityWarning(targetSdk, stubTargetSdk)` | true when `stubTargetSdk - targetSdk > 3` |
| `LocalOnlyStaleness.evaluate(app, nowMs)` | All remotes `NotChecked` → badge `Unknown`; copies installed time |

### Badge honesty

- Failed lookup and no dated success → `Unknown` (not red)
- Every successful remote is `SuccessMissing` and none failed → `Red`
- Archived or empty forge: `countsAsActivity = false`
- Compatibility warning does not change badge color

## Acceptance criteria

- ✅ User-visible behavior: badges from the newest reliable remote; installed time shown separately
- ✅ Offline/error behavior: failed lookup is unknown, not automatic red; missing on all successful remotes is red
- ✅ Accessibility: badge meaning available as content description, not color alone
- ✅ i18n: keys under `staleness_*` and `badge_*` in `strings.xml`
- ✅ Installed `lastUpdateTime` must not paint green if remotes are dead
- ✅ Green under 180 days; amber 180–365; red over 365
- ✅ Compatibility warning when targetSdk is more than 3 levels behind 37; does not change badge color
- ✅ Archived or empty forge dates do not count as activity

## Smoke scenario

1. _Given_ fixtures with a dead remote and a recent installed time
2. _When_ `daysSinceActivity` and `badgeFor` run
3. _Then_ the badge is red or unknown, never green from installed time alone

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../staleness/` |
| View | list/detail consume the model only |
| Tests | `examples/android/app/src/test/.../staleness/` |
| Wiring | none beyond scan shell |

## Definition of Done

Pure functions plus unit tests are mandatory. This is the heart. Fallback only if JVM tests cannot run: `bash scripts/feature-gate.sh --stack android` and say why.

## Notes

- ADR-0001 locks the badge math
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
