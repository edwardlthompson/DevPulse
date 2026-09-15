# Feature: fdroid-package-api

> Sprint 15. Idea 1. Official + Archive indexes OOM Refresh. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.fdroid`.

| Name | Contract |
|------|----------|
| `FdroidIndexBudget.hostResolve(repo)` | Official and Archive skip the jar and resolve per package |
| `FdroidIndexBudget.extraHostResolve(repo)` | Izzy uses per-package HTML only when the index download is empty |
| `FdroidPackageParser.parse(pkg, json, repoId)` | `GET /api/v1/packages/{pkg}` → version name from `suggestedVersionCode` |
| `FdroidPackagePage.parse(html)` | F-Droid/Izzy package HTML → last `Added on` date, source link, category, related packages, APK file name |
| `FdroidHostResolver.resolve(repo, wanted)` | One record per installed package; cache hits inside one refresh |

## Acceptance criteria

- ✅ User-visible behavior: official/archive Refresh lists F-Droid without downloading `index-v1.jar`
- ✅ Offline/error behavior: missing package → not listed; timeout → repo fail-soft; extra repos harvest their index at any size
- ✅ Accessibility: existing F-Droid listing rows
- ✅ i18n: no new user-facing copy (refresh traces only)

## Smoke scenario

1. _Given_ official F-Droid is enabled and the device cannot hold the 80MB index
2. _When_ the user taps Refresh
3. _Then_ DevPulse does not crash; installed F-Droid apps still get a listing and version when the package API returns 200

## Definition of Done

Unit tests for API JSON, HTML date/source, and large-index harvest. Fallback: `bash scripts/feature-gate.sh --stack android`.

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
