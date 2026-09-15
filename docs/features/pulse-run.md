# Feature: pulse-run

> One-tap scan then update in a single window. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. No live network in unit tests.

| Name | Kind | Contract |
|------|------|----------|
| `UpdateAllSkip` | object | After Play `Older`, drop APKPure/APKMirror fallbacks. If Play is listed and already current, do not queue unofficial APKPure/APKMirror builds. After a signing hold, clear remaining sources. `dlLine` is `skip Older`/`skip Sdk` instead of `fail` |
| `UpdateAllRowFill` | object | Per-app download and install bar fill from `UpdateAllSnap` |
| `ScanUpdateCta` | object | `autoStart(complete, count)` is true when scan finished with work |

## Acceptance criteria

- ✅ Play Older does not fetch APKPure or APKMirror for that package in the same batch; a genuinely newer F-Droid/GitHub fallback still runs
- ✅ When Play is listed and not newer than installed, unofficial APKPure/APKMirror “newer” builds are not queued (Play Session install does not flip the app into sideload)
- ✅ A signing clash remembers the APK and does not download other sources for that package
- ✅ Older/Sdk log as `update all dl skip …`, not `fail`
- ✅ Refresh opens one window titled Scan and update: a compact scan bar (count + current location) above the live app list; source outlet rows do not take over the dialog
- ✅ Known update rows appear as soon as Refresh starts; download/install bars fill after scan completes and Update All auto-starts
- ✅ Each app row has a download bar and an install bar; rows that are downloading or installing stay above finished rows
- ✅ Hide still dismisses the window without cancelling, including while the scan is still running; Stop cancels remaining updates and is shown only while work is still running
- ✅ After scan and Update All finish, the window closes by itself even if a signing hold remains (holds stay on the signing inbox; Hide is enough)

## Smoke scenario

1. _Given_ ignored updates are empty and listings exist
2. _When_ Refresh runs
3. _Then_ scan completes in the same dialog, Update All starts without tapping Update N, and logcat has `dl skip Older` with no APKPure try after Play Older

## Container map

| Layer | Path |
|-------|------|
| Logic | `UpdateAllSkip.kt`, `UpdateAllRowFill.kt`, `UpdateAllQueue.kt`, `UpdateAllTally.kt` |
| View | `UpdateAllDialog.kt`, `UpdateAllRow.kt`, `GoldenPathScreen.kt` |
| Tests | `UpdateAllSkipTest.kt`, `UpdateAllRowFillTest.kt`, `UpdateAllOlderTest.kt`, `UpdateAllStayTest.kt`, `UpdateAllTallyTest.kt`, `UpdateAllFollowTest.kt` |
| Wiring | `GoldenPathScreen` auto-kick; `UpdateAllSession` for the shared dialog |

## Tests

- Automated: yes — see Container map
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
