# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · AUTO 0 · HUMAN 0 · ADB 0 · **0 open**
<!-- /remaining-tally -->

Live board. Done work: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).

**Who:** `AGENT` code · `HUMAN` person · `ADB` device · `AUTO` CI
**State:** 🔲 open · ✅ done · ❌ blocked — reason

Format: `🔲 [AGENT] Short task`. Sequential `[AGENT]` first. Parallel: [`docs/PARALLEL_AGENT_SCOPES.md`](docs/PARALLEL_AGENT_SCOPES.md). `/build` tries HUMAN/ADB after automation; failures → `HUMAN_BACKLOG.md`.

After each `[AGENT]` row: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`. After the last `[AGENT]`/`[AUTO]` row in a sprint: `python3 scripts/agent-run.py smoke-sprint --require`.

---

## Product

### Product (do not drift)

> Auto-managed from `AGENT.md`. Do not hand-edit inside markers.

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

<!-- open-prs-sync:begin -->
_No open Dependabot or Release Please PRs._
<!-- open-prs-sync:end -->

### Template gaps (synced)

<!-- template-gaps-sync:begin -->
_No template gaps; .template-version matches upstream (or template maintainer N/A)._
<!-- template-gaps-sync:end -->

---

### Sprint 37 — Daily scan reliability + updater UX

<!-- parallel_exception: sequential-only schedule + pulse-run UX; no Parallel lane -->

Sequential:

1. ✅ [AGENT] Fix ScanSchedule nextDelayMs when due/null; reschedule after successful scan; update ScanScheduleTest
2. ✅ [AGENT] ReleaseRefreshWorker: retry on Wi-Fi skip and busy; extract pure allow/retry helper + unit tests
3. ✅ [AGENT] Daily/Weekly settings: next-scan copy, POST_NOTIFICATIONS on enable, clarify scan_interval_body
4. ✅ [AGENT] PulseRunHost auto-kick Update All after scan; DECISION_LOG + pulse-run.md align
5. ✅ [AGENT] Visible Skip rows + OneClickUpdateIcon fail snackbar + sticky reopen bar while busy
6. ✅ [ADB] OP12: enable Daily, confirm WM delay, wait/force Worker, UpdatesNotify when updates exist; Refresh auto-kick smoke

## Ongoing Maintenance

Monday cron runs health, gaps, radar, and dep dry-run. `/ship` owns the release tag. Red cron: `weekly-maintain`, then [`docs/GROK_BOTS.md`](docs/GROK_BOTS.md).

## Archive

Older sprints: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).
