# Build Plan

> Prioritized task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.
> **Active board:** DevPulse child app. Template-maintainer history is archived at the bottom.

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
> **Sprint 11** archived in COMPLETED_TASKS.md @ `fd20edd`.
> **Sprint 12** archived in COMPLETED_TASKS.md @ `fd20edd`.
> **Sprint 13** archived in COMPLETED_TASKS.md @ `fd20edd`.

> **Sprint 11** ADB date smoke archived in COMPLETED_TASKS.md.
> **Sprint 12** ADB download smoke archived in COMPLETED_TASKS.md.
> **Sprint 10** screenshots archived in COMPLETED_TASKS.md.

---

> **Sprint 14** archived in COMPLETED_TASKS.md (v0.25.0 prepare).

## Sprint 15 — Host-resolve, Opportunity, leftover forges (2026-08-20)

> Ideas 1–7. Official F-Droid index harvest OOMs Refresh on device (sideloaded GitHub APK). Host-resolve per package instead.

### Sequential (lock types first)

- ✅ [AGENT] Host-resolve official/archive F-Droid via package API + page; never harvest indexes over 2MB
- ✅ [AGENT] Wire Opportunity (“What to build”) from cached stale inventory
- ✅ [AGENT] Pin loved stale apps (persist + hide from red list)
- ✅ [AGENT] Store the optional GitHub token in EncryptedSharedPreferences
- ✅ [AGENT] Opt-in stale-crossing notifications (180 / 365 days, WorkManager, no FCM)
- ✅ [AGENT] Persist scan history and show apps that went quiet
- ✅ [AGENT] GitLab and Codeberg leftover lookup when F-Droid has no GitHub source

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Package API/page parsers return null on blank JSON/HTML; leftover search skips empty queries; pin/history ignore blank package ids (`FdroidPackageParserTest`, `LeftoverForgeParserTest`, `ScanHistoryTest`) |
| Network timeout | Package HTTP uses 15s connect / 20s read; leftover forges reuse `GitHubFetchPolicy` timeouts; refresh already catches per-repo/per-app `Throwable` |
| Race conditions | Host-resolve caches per refresh instance; pin/history writes are DataStore/file replace; leftover offer replace stays inside `ReleaseRefreshWaves.commit` |
| Unhandled exceptions | Catalog snapshot, LocalScan, and Scan start wrap `runCatching`; huge-index path never materializes 80MB JSON |
### Parallelization

| Slice | Scope |
|----|---|
| F-Droid host-resolve | `examples/android/.../index/fdroid/**`, `ReleaseRefreshRepos.kt` |
| Opportunity + pins + history UI | `examples/android/.../opportunity/**`, `query/**`, `ui/**` |
| Token + leftover forges | `examples/android/.../index/forge/**` |
| Stale notifications | `examples/android/.../notify/**` |
`agent_count_target`: 2 after Sequential API lock. This session implements Sequential inline (shared `ReleaseRefresh` signature).
<!-- parallel_exception: Sequential inline this session; shared ReleaseRefresh signature -->

## Sprint 16 — Alternatives, repo fingerprints, Opportunity finish (2026-08-20)

> Ideas 1–8 plus in-app Izzy/Guardian/Calyx fingerprint help. No official `index-v1.jar`. No FCM.

### Sequential (lock types first)

- ✅ [AGENT] Repo-add help: Guardian/Calyx `fdroidrepo` chips and fingerprint explainer
- ✅ [AGENT] Parse and persist F-Droid categories from package HTML; Opportunity uses category then origin
- ✅ [AGENT] Wire Alternatives + Sources on app detail from cached neighbors
- ✅ [AGENT] Persist pasted forge URLs; Develop-next notes; focused Opportunity export; DevPulse self-pulse
- ✅ [AGENT] Izzy host-resolve when index missing or over 2MB; red-count home widget
- ✅ [AGENT] Parse APK file name from F-Droid/Izzy package HTML so Update stays in-app

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Category/related parse returns empty; Alternatives empty copy; paste URL ignored if blank or not `http(s)`; widget shows 0 if no snapshot (`FdroidPackagePageTest`, `AlternativesFromCacheTest`, `PastedRepoStoreTest`, `WidgetRedCountTest`) |
| Network timeout | Category/alternatives use cached package HTML first; extra Izzy page fetch uses existing 15s/20s HTTP timeouts and fail-soft |
| Race conditions | Category map and pasted-repo TSV are replace-writes; widget reads last scan badges only |
| Unhandled exceptions | Detail Alternatives wrap `runCatching`; widget `onUpdate` catches and shows 0 |
### Parallelization

| Slice | Scope |
|----|---|
| Logic+tests | `index/fdroid`, `alternatives`, `opportunity`, `index/forge` |
| View+i18n | `ui/inventory`, `ui/opportunity`, `ui/forge`, `ui/settings`, widget XML |
| Docs | `docs/features/alternatives.md`, `opportunity.md`, `store-clients.md` |
`agent_count_target`: 2 after Sequential type lock (`category` / `relatedPackages` / pasted-repo store).
<!-- parallel_exception: Sequential inline this session; shared category and pasted-repo types -->

## Sprint 17 — Aurora Play download (2026-08-20)

> Opt-in Aurora/`gplayapi` next to Google Play. Play Store `market://` fallback. No FCM. No official `index-v1.jar`.

### Sequential (lock types first)

- ✅ [AGENT] Opt-in Aurora Play APK download with Play Store fallback

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Blank package/email/URL → null; Update opens Play Store (`AuroraPlayDirectTest`, `OneClickUpdateTest`) |
| Network timeout | 25s HTTP; live purchase `runCatching` → Play Store |
| Race conditions | Encrypted auth replace-write; artifact memory synchronized |
| Unhandled exceptions | Auth/purchase wrapped; cert/package inspect before install |
### Parallelization

| Slice | Scope |
|----|---|
| Logic+tests | `examples/android/.../index/aurora/**`, `OneClickUpdate.kt` |
| View+i18n | `ui/settings`, `ui/inventory`, `strings.xml` |
| Docs | `docs/features/aurora-play.md` |
`agent_count_target`: 1 this session (shared `OneClickUpdate.apply` signature).
<!-- parallel_exception: Sequential inline; shared OneClickUpdate.apply signature -->

## Sprint 18 — Donations and self-update (2026-08-20)

> Same method as Continuum Calendar. Quiet Venmo. One donate note per version. Daily GitHub installer check.

### Sequential (lock types first)

- ✅ [AGENT] Device-local donate nudge + 24h GitHub filename update prompt

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Blank version or empty assets stay silent (`ProductUpdateTest`, `ProductReleaseFetcherTest`) |
| Network timeout | 10s GitHub fetch; fail → null; app never blocks |
| Race conditions | Donate dialog returns before the update fetch; prompts never share a dialog |
| Unhandled exceptions | Fetch and URI opens catch; invalid JSON → null |
### Parallelization

| Slice | Scope |
|----|---|
| Logic+tests | `examples/android/.../about/ProductUpdate*.kt` |
| View+i18n | `ui/about`, `ui/settings/SettingsHub.kt`, `strings.xml` |
| Docs | `docs/features/donations-updates.md` |
`agent_count_target`: 1 this session (shared `ProductUpdate` API used by launch host).
<!-- parallel_exception: Sequential inline; shared ProductUpdate launch API -->

## Ongoing Maintenance (recurring)

### Weekly

- 🔲 [AUTO] `cursor-feature-radar.sh` (non-blocking)
- 🔲 [AUTO] `check-security-triage.sh --wait-ci 300`
- 🔲 [AGENT] Apply Dependabot bumps; triage Scorecard findings
- 🔲 [AUTO] CI matrix + Repo Hygiene + Feature Gate green on `main`

### Monthly

- 🔲 [AUTO] `simulate-template-upgrade.sh`
- 🔲 [AUTO] `check-license-compliance.sh` + SBOM on latest release

### Human (after automation)

- 🔲 [HUMAN] Approve release tag when product-ready
- 🔲 [HUMAN] Quarterly Cursor feature radar backlog review (next due 2026-11-15)

---

## Template Maintainer Archive (inactive)

> Upstream `agent-project-bootstrap` history. Not the DevPulse active board. See `COMPLETED_TASKS.md` for M5–M39 and v0.9.0–v0.21.0.

| Note | Detail |
|------|--------|
| Template version at clone | 0.21.0 @ `1525cd6` |
| Child origin | `edwardlthompson/agent-project-bootstrap` |
| Do not resume | Template maintainer sprints on this child repo |
