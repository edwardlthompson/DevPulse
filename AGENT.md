# AGENT.md — original product brief (Sacred)

Copy of the DevPulse product brief. Bootstrap stamps `AGENTS.md` only and will
not overwrite this file. Later sessions: read this before any BUILD_PLAN sprint row.

<!-- agent-brief:one-liner -->
See which of your apps still have a heartbeat.
<!-- /agent-brief:one-liner -->

<!-- agent-brief:keywords -->
staleness, inventory, local-only, sideload, heartbeat
<!-- /agent-brief:keywords -->

## Rules

- Local-only Android FOSS; no Google Play Services, Firebase, or closed telemetry.
- Honest dates: never-guessed remotes, unknown vs missing.
- GitHub Releases as primary channel. applicationId is `app.devpulse`.
- Do not uninstall `app.devpulse` without backup.

## First milestone

Scan installed apps across Play, F-Droid plus extra repos, and public forges to surface stale software.
