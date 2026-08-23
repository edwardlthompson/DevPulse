# Feature: scan-orchestrator

> Sprint 3 shell. FR-18 to FR-20 local-only. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.scan`. No Play or forge clients.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `ScanPhase` | enum | `Idle`, `Running`, `Paused`, `Completed` |
| `ScanProgress` | data class | `phase`, `completed`, `total` |
| `ScanItem` | data class | `app`, `staleness`, `repoFound` (false until later sprints) |
| `ScanDetail` | data class | `item`, `remoteDates` (empty map this sprint), `notes` |
### Functions

| Name | Contract |
|------|----------|
| `ScanMachine.start(total)` | `Running` at `0 / total` |
| `ScanMachine.pause` / `resume` / `advance` | Pause only from Running; resume only from Paused; advance only while Running; `completed == total` → `Completed` |
| `LocalScan.run(apps, nowMs)` | One `ScanItem` per app via `LocalOnlyStaleness`; `repoFound = false` |
## Acceptance criteria

- ✅ User-visible behavior: one-tap full scan with progress, pause, and resume
- ✅ Refresh dialog can pause and resume in-flight outlet work without dismissing
- ✅ Settings can restrict Refresh to Wi-Fi / unmetered networks
- ✅ Home shows the last successful Refresh time per outlet
- ✅ Home shows the last skipped/failed outlet time in error color
- ✅ Refresh resumes unfinished outlets after process death
- ✅ GitHub/GitLab 429 waits the Retry-After seconds when the header is present
- ✅ Play HTML and APKMirror/APKPure 429 also honor Retry-After
- ✅ Refresh progress text is a polite TalkBack live region
- ✅ Settings can wipe remotes, dump chunks, and the F-Droid index cache
- ✅ Manual Refresh probes only the apps in the current home filter set
- ✅ Settings Sources shows remaining per-host backoff after 403/429
- ✅ Offline/error behavior: first implementation uses installed `lastUpdateTime` only; remotes stay unknown
- ✅ Accessibility: progress and pause/resume announced to TalkBack
- ✅ i18n: keys under `scan_*` in `strings.xml`
- ✅ Play and GitHub must not be hammered (no remote clients in this slice)
- ✅ List or card results: icon, name, package, installed version, badges, repo-found indicator (repo false until later)
- ✅ Detail shell: installed time, empty remote dates, notes field

## Smoke scenario

1. _Given_ inventory is populated
2. _When_ the user starts a scan and pauses, then resumes
3. _Then_ progress continues without crashing and remotes remain unknown

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../scan/` |
| View | `examples/android/.../ui/scan/` |
| Tests | `examples/android/app/src/test/.../scan/` |
| Wiring | `GoldenPathApp.kt` ≤10 lines |
## Definition of Done

Unit tests for pause/resume state machine. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Do not implement Play HTML or forge search here
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
