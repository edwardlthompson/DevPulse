# ADR-0001: Core Application Architecture

- **Status:** Accepted (autonomous `/build` 2026-08-19; confirm in DECISION_LOG)
- **Date:** 2026-08-19
- **Deciders:** Project team

> Child-repo architecture for DevPulse. Template baseline remains `docs/adr/0000-template-baseline.md`.

## Context

DevPulse is a local-only Android scanner. Init left MIT and a Golden Path stub. The product needs a locked stack, honest badges, and FOSS networking before any Play or forge client is written.

## Decision

**Selected pattern:** Clean / MVVM. Domain logic (especially staleness) is pure and unit-tested. Compose is a view adapter. Composition root (`GoldenPathApp.kt` or successor) stays tiny.

### Stack

- Android only, Kotlin, Jetpack Compose, Material 3 / Material You
- minSdk 26 (do not lower), targetSdk 37
- No Play Services, Firebase, FCM, or closed telemetry
- License GPL-3.0-or-later
- Planned `applicationId`: `app.devpulse` (rename in Sprint 1 only if tests stay green)
- Distribution: GitHub Releases only. Do not submit DevPulse to F-Droid. F-Droid/extra-repo indexes remain a lookup source for other apps.

### HTTP

OkHttp with a shared client, interceptors for token-bucket/spacing, and persisted backoff. User-Agent `DevPulse/0.1` plus the public GitHub URL. Do not impersonate a browser. Honour 403 and 429. Degrade to cache.

### Storage

Room for F-Droid/extra-repo indexes, scan results, pins, notes, and history. EncryptedSharedPreferences only for the optional GitHub token.

### Play parser

Isolated HTML parser plus checked-in fixtures. Unparseable or 403 → `unknown-check-manually`. Never guess a date. Modest concurrency, effectively serial. Cache per package, TTL about 24 hours. Persist last error. Do not block the whole scan on one failure.

### F-Droid and extra repos

Official signed index if verify is feasible without a huge dependency. Otherwise HTTPS plus checksum, and document the limit in `docs/features/fdroid-index.md`. Same pipeline for extra repos. Multi-day cache, manual refresh.

### Remote vs installed (badge honesty)

Badge age is the newest reliable remote among Play updated-on, F-Droid last-updated (not last-added alone), extra-repo last-updated, and forge latest release or default-branch commit. Installed `lastUpdateTime` is a separate field and cannot paint green.

- Failed lookup → unknown (not automatic red)
- Missing on every successful remote check → red
- Under 180 days green; 180–365 amber; over 365 red
- Archived or empty forge dates do not count as activity

### Compatibility

Warning when `targetSdk` is more than 3 API levels behind stub `targetSdk` 37. Warning does not change badge color.

### Pin / Opportunity

Pin hides the red list only. Opportunity includes pinned apps only when the user asks.

### Forge match

Package id first, then name. Prefer exact package or Gradle id over fuzzy title. Show match confidence on the detail screen.

### Self-pulse

Configure DevPulse package and repo so About and Opportunity do not depend on a search hit.

## Consequences

- Sprint 0/1 lock Golden Path About, theme, and navigation only
- Scanners start in Sprint 2+ one slice at a time
- `[HUMAN]` must approve this ADR and the GPL-3.0-or-later license
- Changing this ADR later requires a new ADR and BUILD_PLAN `[HUMAN]` approval

## Alternatives considered

| Pattern | Rejected because |
|---------|------------------|
| MIT remain | Product target is GPL-3.0-or-later; init could not select it |
| Guess Play dates | Would lie when HTML changes |
| Installed time paints green | Sideload updates would hide dead remotes |
| Browser User-Agent | Evades blocks and fails the ethics note |
| FCM notifications | Proprietary; closed push |
| List DevPulse on F-Droid | Human chose GitHub Releases only (2026-08-19) |
| Rename applicationId in Sprint 0 | Risks breaking Golden Path before About is proven |
