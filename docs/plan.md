# Implementation Plan

> DevPulse milestone map. Active work lives in `BUILD_PLAN.md`.
> Status: 🔲 open · ✅ done · ❌ blocked.

## How to run a sprint

Each sprint: Sequential lock API, then Parallel logic-plus-tests versus view-plus-i18n, then wire, then gates. Default split: Agent A pure logic plus unit tests. Agent B Compose plus `strings.xml`.

Every `[AGENT]` feature adds or updates automated tests, or this plan / the feature file documents why tests are not feasible and names the fallback command (`bash scripts/feature-gate.sh --stack android` or `bash scripts/watch-agent-gates.sh --once --autofix`).

Do not skip Golden Path to jump at scraping. A running About screen and a tested staleness function are worth more than a fragile Play parser on day one.

## Milestone map

| Sprint | Name | Outcome | Tests / fallback | Status |
|--------|------|---------|------------------|--------|
| 0 | Template customization | Init, GPL, spec, plan, branding, adapters | `verify.sh` passed locally; AUTO GitHub CI still open | ✅ |
| 1 | Golden Path | About, theme, navigation, FOSS Gradle stub runs | `./gradlew test` in `examples/android/` | ✅ |
| 2 | Inventory | User-app list, system toggle, QUERY_ALL_PACKAGES explanation, package fields | Inventory unit tests + feature-gate | ✅ |
| 3 | Staleness and scan shell | Pure staleness model, badges, local-only scan UI, detail shell | Staleness unit tests; remotes stay unknown | ✅ |
| 4 | F-Droid index | Official index download, cache, lookup, SourceCode, origin, extra-repo settings | Index parse tests + feature file if signature verify is not feasible | ✅ |
| 5 | Play lookup | Rate-limited public page parse, published vs installed, fallbacks | Fixture-based parser tests | ✅ |
| 6 | Forge lookup | GitHub search, commit/release, optional GitLab/Codeberg, paste-a-repo, token | Client tests with recorded JSON fixtures | ✅ |
| 7 | Filters, pins, history, export | Keep-anyway, sort/filter, scan history, full CSV/JSON | Logic unit tests + export snapshot tests | ✅ |
| 8 | Opportunity | Usage-stats opt-in, category gaps, Develop-next notes, self-pulse, focused export | Opportunity ranking tests | ✅ |
| 9 | Alternatives and sources | Maintained F-Droid/Izzy matches, download locations, no auto-install | Similarity tests against cached fixtures | ✅ |
| 10 | Polish and ship | Widget, local notifications, compatibility UX, GitHub Release notes, first release | `check-github-ci.sh`; widget/notify later or fallback named in feature files | ✅ AGENT / 🔲 HUMAN |
## Sprint notes

### Sprint 0 — Template customization

Stamp identity, GPL-3.0-or-later, spec, plan, BUILD_PLAN, privacy, threat model, ADR-0001, feature files. No scanners.

### Sprint 1 — Golden Path

About, theme, navigation. Optional `applicationId` rename to `app.devpulse` only if tests stay green.

### Sprint 2 — Inventory

FR-1 to FR-5. PackageManager wrapper only.

### Sprint 3 — Staleness and scan shell

FR-15 to FR-20 using installed `lastUpdateTime` only. Remote sources unknown.

### Sprint 4 — F-Droid index

FR-7, FR-8, origin parts of FR-3 and FR-11. Feature file: `docs/features/fdroid-index.md`.

### Sprint 5 — Play lookup

FR-6 and published-vs-installed. Feature file: `docs/features/play-lookup.md`. Serial or very small concurrency. 24h cache.

### Sprint 6 — Forge lookup

FR-9 to FR-13. Feature file: `docs/features/forge-lookup.md`. Package id first.

### Sprint 7 — Filters, pins, history, export

FR-21, FR-22, FR-31, FR-32. Named stubs until this sprint: keep-anyway, history, full export.

### Sprint 8 — Opportunity

FR-5 usage ranking, FR-25 to FR-28. Feature file: `docs/features/opportunity.md`.

### Sprint 9 — Alternatives and sources

FR-29, FR-30. Feature file: `docs/features/alternatives.md`.

### Sprint 10 — Polish and ship

FR-17 UX, FR-33, FR-34, FR-35. Ship a GitHub Release only. Widget and notifications are named stubs until this sprint.

## Next feature

1. Copy `docs/features/_template.md` only if a new name is required
2. Lock the public API (Sequential)
3. Add unit tests before or with the implementation
4. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

If automated tests are not feasible, write the justification and fallback command in the feature spec before marking the BUILD_PLAN row ✅.
