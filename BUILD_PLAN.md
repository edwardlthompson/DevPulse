# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 8 · AUTO 0 · HUMAN 2 · ADB 4 · **14 open**
<!-- /remaining-tally -->

> Prioritized task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.
> **Active board:** DevPulse child app. Template-maintainer history is archived at the bottom.

### Product (do not drift)

> Auto-managed from `AGENT.md` after init. Do not hand-edit inside markers. Read `AGENT.md` before any sprint row.

<!-- product-brief-sync:begin -->
> Read `AGENT.md` before any sprint row.

**One-liner:** See which of your apps still have a heartbeat.
**Do not drift:** staleness, inventory, local-only, sideload, heartbeat

**Rules:**
- Local-only Android FOSS; no Google Play Services, Firebase, or closed telemetry.
- Honest dates: never-guessed remotes, unknown vs missing.
- GitHub Releases as primary channel. applicationId is `app.devpulse`.
- Do not uninstall `app.devpulse` without backup.

**First milestone:** Scan installed apps across Play, F-Droid plus extra repos, and public forges to surface stale software.
<!-- product-brief-sync:end -->

### Open PRs (synced)

> Auto-managed. Do not hand-edit rows inside the markers.

<!-- open-prs-sync:begin -->
_No open Dependabot or Release Please PRs._
<!-- open-prs-sync:end -->

### Template gaps (synced)

> Auto-managed Monday cron + `sync-template-gaps-build-plan`. Do not hand-edit inside markers. Plan-only — run `/upgrade` then name item numbers.

<!-- template-gaps-sync:begin -->
_No template gaps; .template-version matches upstream (or template maintainer N/A)._
<!-- template-gaps-sync:end -->

## Owner Label Legend

| Label   | Owner           | When to use                                                |
| ------- | --------------- | ---------------------------------------------------------- |
| `AGENT` | Cursor Agent    | Code, docs, scaffolding, tests, CI config                  |
| `HUMAN` | Human developer | Approvals, credentials, GitHub settings, product decisions |
| `ADB`   | Human (Android) | Android SDK, emulator/device testing, F-Droid submission   |
| `AUTO`  | CI/scripts/bots | GitHub Actions, Dependabot, pre-commit, update checker     |
## Status markers

Use **emoji markers** (not `- [ ]` GitHub checkboxes) so task state reads clearly in Markdown source and Preview. **Applies repo-wide** — `BUILD_PLAN.md`, module checklists, PR template, feature specs, and security triage.

| Marker | State   | Agent action                                                          |
| ------ | ------- | --------------------------------------------------------------------- |
| 🔲     | Open    | Default for new tasks; work or leave queued                           |
| ✅      | Done    | Replace 🔲 when complete; archive sprint rows to `COMPLETED_TASKS.md` |
| ❌      | Blocked | Replace 🔲 when blocked; add brief reason after the description       |
**Task format:** `🔲 [OWNER] Description` · done: `✅ [OWNER] Description` · blocked: `❌ [OWNER] Description — reason`

```bash
grep '\[AGENT\]' BUILD_PLAN.md
grep '\[HUMAN\]' BUILD_PLAN.md
grep '\[ADB\]' BUILD_PLAN.md
grep '\[AUTO\]' BUILD_PLAN.md

```

**Agent rule:** Execute all `[AGENT]` **Sequential** items first, then dispatch **Parallel** agents with isolated file scopes (`docs/PARALLEL_AGENT_SCOPES.md`). Shared schema/types are Sequential-only.

### Parallel dispatch protocol (orchestrator)

| Step | Action                                                                                                                                                                     |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Finish all `[AGENT]` **Sequential** items for the active sprint/feature (shared schema/types locked)                                                                       |
| 2    | **Discover** parallelizable work using the decomposition checklist below; add Parallel table rows with non-overlapping ``path/**`` scopes                                  |
| 3    | Run `bash scripts/plan-parallel-dispatch.sh` → read **agent_count**                                                                                                        |
| 4    | If `agent_count >= 2`, run `/scope` (auto Task dispatch); if `1`, execute inline; if `0`, run `--suggest` and expand the Parallel table (or document `parallel_exception`) |
| 5    | Sequential owner merges results, runs `watch-agent-gates.sh`, updates BUILD_PLAN (Parallel agents never edit BUILD_PLAN)                                                   |
**Decomposition checklist** (apply before finalizing Sequential items):

| Heuristic                     | Split into Parallel agents                                                                  |
| ----------------------------- | ------------------------------------------------------------------------------------------- |
| Multi-stack repo              | One agent per active module (`examples/{stack}/`**)                                         |
| Feature container (Sprint 2+) | Agent A: pure logic + unit tests; Agent B: view/Composable + i18n                           |
| Tests vs production code      | Separate `**/*.test.*`, `e2e/**`, `androidTest/**` when paths do not overlap implementation |
| Docs vs code                  | Agent A: `examples/**`; Agent B: `docs/**`, `modules/**`, `.cursor/rules/**`                |
| CI/gates vs app code          | Agent A: `scripts/**`, `.github/workflows/**`; Agent B: stack example tree                  |
**Default rule:** If a Sequential `[AGENT]` item touches two or more non-overlapping directory prefixes, **split it** — leave only schema-lock work Sequential.

**Planning (Plan Mode):** Every BUILD_PLAN proposal must include `### Parallelization` with `agent_count_target`, decomposition table, and dry-run from `plan-parallel-dispatch.sh`. Run `check-build-plan-parallel.sh` before human approval.

**Autonomous `/build`:** Runs all `[AGENT]`/`[AUTO]` and Parallel work first, then attempts the grouped **Human & device (after automation)** section via `scripts/attempt-build-plan-row.sh`. Success marks ✅; failure appends `HUMAN_BACKLOG.md` and continues — never halts on human labels. Humans review the grouped section (and backlog) after automation finishes. Status: `bash scripts/build-sprint-status.sh --json`.

---

## Child Repo Playbook

When **Sprint 0** ends: stop re-reading `docs/INITIALIZATION_PROMPT.md` as the daily driver. Copy `scratchpad.md.example` → `scratchpad.md` and reset it on sprint change. Do not implement Play scraping, GitHub search, or Opportunity UI in Sprint 0 or 1.

> **Sprint 0** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 1** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 2** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 3** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 4** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 5** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 6** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 7** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 8** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 9** archived in COMPLETED_TASKS.md @ `0b047aa`.
> **Sprint 10** AGENT/AUTO archived in COMPLETED_TASKS.md @ `0b047aa`.
> **v0.22.0** tag archived in COMPLETED_TASKS.md @ `e53283a`.
> **v0.24.0** tag archived in COMPLETED_TASKS.md @ `36d12cc`.
> **v0.25.0** tag archived in COMPLETED_TASKS.md @ `90b6249`.
> **v0.26.0** tag archived in COMPLETED_TASKS.md @ `a8f2c44`.
> **v0.27.0** tag archived in COMPLETED_TASKS.md @ `a2663a3`.
> **v0.34.0** tagged via RP #16 @ `eef4eb9`.
> **v0.34.1** tagged via RP #17 @ `f3e22f0`.
> **v0.34.2** tagged via RP #18 @ `068f210` (RP missed the tag; `gh release create` + published event uploaded APK/SBOM).

### Archived Sprints

| Sprint | Complete | Archive |
|--------|----------|---------|
| Sprint 0 — Template customization | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 1 — Golden Path | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 2 — Inventory | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 3 — Staleness and scan shell | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 4 — F-Droid index | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 5 — Play lookup | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 6 — Forge lookup | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 7 — Filters, pins, history, export | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 8 — Opportunity | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 9 — Alternatives and sources | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 10 — Polish and ship (AGENT) | 2026-08-19 | COMPLETED_TASKS.md @ `0b047aa` |
| Sprint 11 — Inventory sort and honest dates | 2026-08-20 | COMPLETED_TASKS.md @ `fd20edd` |
| Sprint 12 — Prefetch updates and notes | 2026-08-20 | COMPLETED_TASKS.md @ `fd20edd` |
| Sprint 13 — Play HTML recovery | 2026-08-20 | COMPLETED_TASKS.md @ `fd20edd` |
| Sprint 14 — Store clients and settings hub | 2026-08-20 | COMPLETED_TASKS.md @ `90b6249` |
| Sprint 15 — Host-resolve, Opportunity, leftover forges | 2026-08-21 | COMPLETED_TASKS.md @ `a2663a3` |
| Sprint 16 — Alternatives, repo fingerprints, Opportunity finish | 2026-08-21 | COMPLETED_TASKS.md @ `a2663a3` |
| Sprint 17 — Aurora Play download | 2026-08-21 | COMPLETED_TASKS.md @ `a2663a3` |
| Sprint 18 — Donations and self-update | 2026-08-21 | COMPLETED_TASKS.md @ `a2663a3` |
| Sprint 19 — Ideas backlog (after v0.29.0) | 2026-08-23 | COMPLETED_TASKS.md @ `92c860a` |
| Sprint 20 — Ideas backlog (after Sprint 19) | 2026-08-23 | COMPLETED_TASKS.md @ `2b6d448` |
| Sprint 21 — Ideas backlog (after Sprint 20) | 2026-08-23 | COMPLETED_TASKS.md @ `2b6d448` |
| Sprint 22 — GitHub add (uninstall Obtainium) | 2026-08-26 | COMPLETED_TASKS.md @ `29040a3` |
| Sprint 23 — Obtainium migrate and filters | 2026-08-26 | COMPLETED_TASKS.md @ `29040a3` |
| Sprint 24 — Obtainium import all GitHub watches | 2026-08-26 | COMPLETED_TASKS.md @ `a1fc775` |
| Sprint 25 — GitHub release versions on Refresh | 2026-08-26 | COMPLETED_TASKS.md @ `4894cda` |
| Sprint 26 — Required-check rollups | 2026-08-26 | COMPLETED_TASKS.md @ `4894cda` |
| Sprint 27 — Store update detection, APKPure parser, and download fallback | 2026-08-31 | COMPLETED_TASKS.md @ `4894cda` |
> **Sprint 11** archived in COMPLETED_TASKS.md @ `fd20edd`.
> **Sprint 12** archived in COMPLETED_TASKS.md @ `fd20edd`.
> **Sprint 13** archived in COMPLETED_TASKS.md @ `fd20edd`.

> **Sprint 11** ADB date smoke archived in COMPLETED_TASKS.md.
> **Sprint 12** ADB download smoke archived in COMPLETED_TASKS.md.
> **Sprint 10** screenshots archived in COMPLETED_TASKS.md.

---

> **Sprint 14** archived in COMPLETED_TASKS.md (v0.25.0 prepare).
> **Sprint 15** archived in COMPLETED_TASKS.md @ `a2663a3`.
> **Sprint 16** archived in COMPLETED_TASKS.md @ `a2663a3`.
> **Sprint 17** archived in COMPLETED_TASKS.md @ `a2663a3`.
> **Sprint 18** archived in COMPLETED_TASKS.md @ `a2663a3`.
> **Sprint 19** archived in COMPLETED_TASKS.md @ `92c860a`.
> **Sprint 20** archived in COMPLETED_TASKS.md @ `2b6d448`.
> **Sprint 21** archived in COMPLETED_TASKS.md @ `2b6d448`.
> **Sprint 22** archived in COMPLETED_TASKS.md @ `29040a3`.
> **Sprint 23** archived in COMPLETED_TASKS.md @ `29040a3`.
> **Sprint 24** archived in COMPLETED_TASKS.md @ `a1fc775`.
> **Sprint 25** archived in COMPLETED_TASKS.md @ `4894cda`.
> **Sprint 26** archived in COMPLETED_TASKS.md @ `4894cda`.
> **Sprint 27** archived in COMPLETED_TASKS.md @ `4894cda`.

## Sprint 28 — UX honesty (fail copy, empty states, Hide vs Stop)

<!-- agent_count_target: 2 | sequential_lock_step: 3 -->

Pulse-check first. Do not retitle Refresh to “Check for updates.” New i18n only in `res/values/` files **not** named `strings.xml`. Rename poisoned keys at their real callers. Do not add `InstallWhy.Abi`. Do not delete `ScanScreen`. No haptic. No `design-tokens.json` edits.

### Sequential (must complete in order)

1. 🔲 [AGENT] Feature spec `docs/features/ux-honesty.md` (locked: `InventoryEmptyKind`, `failRes` map, Hide vs Stop, slim Refresh bar). Patch `docs/features/update-all.md` so Close no longer means cancel. Note follow-ups in `docs/features/inventory.md`.
2. 🔲 [AGENT] Schema lock: `InventoryEmptyKind`; `InventoryCopy.failRes` + `listingMarkStatusRes` skipped; rename `about_update_no_compatible` → `aptoide_games`, `about_update_restarting` → `settings_clear_listings`; add `update-all.xml` / `inventory-ui.xml` keys (timeout, incompatible, metered, Root wait, empty search/blocked, ABI mismatch, listing skipped, Hide/Stop); tests in `InventoryCopyTest` + empty-kind test. Keep `strings.xml` ≤300 lines.
3. 🔲 [AGENT] `GoldenPathScreen.kt`: remove FAB; 2dp Refresh indicator while `refreshing` (dismiss dialog does not cancel); top-bar Settings uses `settings_close` while open; delete `ui/components/ThemeToggle.kt`; long-press one-shot snackbar.

### Parallel (safe after Sequential step 3)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Empty/welcome/listing CD, one-click spinner + 48dp, unknown-apps Surface, Update-all Hide/Stop + Root wait, fail rows use `failRes` | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/` |
| Settings Close button gone; Add-repo on Sources | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/settings/` |

### Human & device (after automation)

- 🔲 [ADB] OP13: search/filter empty copy; Welcome skip vs blocked empty; Update-all Hide keeps batch and button reopens; Stop cancels; ABI fail ≠ “No APK file”; dismiss Refresh with slim bar still moving.

## Sprint 29 — UX pulse rows and filters

<!-- agent_count_target: 2 | sequential_lock_step: 2 -->

After Sprint 28. Pulse mark on home rows; filters in a sheet; cut listing emoji; tokenize scrubber; clip icons to `RadiusMd`. No haptic. No bottom nav.

### Sequential (must complete in order)

1. 🔲 [AGENT] Feature spec `docs/features/ux-pulse.md`. Add `titleMedium` + `labelSmall` to `design-tokens/design-tokens.json` and run `python3 scripts/sync-design-tokens.py`. Check `scripts/check-design-cohesion.sh`.
2. 🔲 [AGENT] `InventoryCopy.listingMarkPrefix` returns empty; `listingMarkStatusRes` keeps words; `InventoryCopyTest` updated.

### Parallel (safe after Sequential step 2)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| ListItem row + pulse dot/CD + primary Update chip; filter `ModalBottomSheet`; search expand; scrubber tokens + reduce-motion; clip `AppIcon`; tinted listing mark (no emoji) | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/` |
| DESIGN_GUIDE do/don’t (accent = primary actions only); pulse Notes on `docs/features/inventory.md` and `docs/features/ux-pulse.md` | AGENT | `docs/` |

### Human & device (after automation)

- 🔲 [ADB] TalkBack reads pulse without relying on color; filters no longer eat the list on a small window; reduce-motion skips scrubber fade.

## Sprint 30 — UX detail Advanced and ScanScreen cleanup

<!-- agent_count_target: 2 | sequential_lock_step: 1 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Feature spec `docs/features/ux-detail.md`: detail = identity + one status sentence + listings; paste/regex/direct APK under Advanced. Confirm no test imports `ScanScreen`.

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Collapse `InventoryDetailScreen` Advanced; keep listings + primary update | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/` |
| Remove unused `ScanScreen` / `ScanDetailScreen` if tests and wiring stay green | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/scan/` |

### Human & device (after automation)

- 🔲 [ADB] Detail still reaches paste-repo and GitHub regex under Advanced; inventory list unchanged.

## Sprint 31 — UX destinations (only if home still crowded)

<!-- agent_count_target: 3 | sequential_lock_step: 3 -->

Bigger bets from the UX audit. Do not start until Sprints 28–30 are ✅ and home is still cramped.

### Sequential (must complete in order)

1. 🔲 [HUMAN] Confirm bottom nav (Apps / Updates / Settings) and optional dynamic color (default off, pulse red stays brand). Skip this sprint if home is calm after 28–30.

2. 🔲 [AGENT] Feature spec `docs/features/ux-destinations.md` after HUMAN yes.
3. 🔲 [AGENT] `GoldenPathScreen.kt` Apps / Updates / Settings destinations (empty shells). Not `GoldenPathApp.kt`.

### Parallel (safe after Sequential step 3)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Updates destination + list chrome; Opportunity stays in Settings | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/` |
| Appearance dynamic-color toggle (default off); Settings hub labels | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/settings/` |
| Optional Material You in `GoldenPathTheme` | AGENT | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/theme/` |

### Human & device (after automation)

- 🔲 [ADB] Nav destinations and theme toggle on OP13 light/dark.

## Ongoing Maintenance

Not a checklist. GitHub Monday cron (`.github/workflows/weekly-health-check.yml`) already runs CI wait, security triage, parent template-gap BUILD_PLAN sync, radar, `update-deps` dry-run, Dependabot leftover list, open-PR BUILD_PLAN sync, and latest-release SBOM. Upgrade-sim stays on the template maintainer repo. `/ship` owns pre-release and the release tag.

If Monday cron is red: Cursor Automation `weekly-maintain`, then Grok Bot 4–5. Do not put those chores back on this board. [`docs/GROK_BOTS.md`](docs/GROK_BOTS.md)

- 🔲 [HUMAN] Approve release tag when product-ready (`/ship`)

---

## Template Maintainer Archive (inactive)

> Upstream `agent-project-bootstrap` history. Not the DevPulse active board. See `COMPLETED_TASKS.md` for M5–M39 and v0.9.0–v0.21.0.

| Note | Detail |
|------|--------|
| Template version at clone | 0.21.0 @ `1525cd6` |
| Child origin | `edwardlthompson/agent-project-bootstrap` |
| Do not resume | Template maintainer sprints on this child repo |
