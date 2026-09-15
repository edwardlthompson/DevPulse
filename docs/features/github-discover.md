# Feature: github-discover

> Sprint 36. Background leftover GitHub search after scan+update. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory` and `dev.foss.goldenpath.index.forge`. Unit tests use fixtures only — no live GitHub.

| Name | Kind | Contract |
|------|------|----------|
| `GitHubDiscover.BATCH` | const | `5` packages per worker run |
| `GitHubDiscover.queued` | fun | Store-less, unhinted, not noise, not 30-day skip |
| `GitHubDiscover.tick` | fun | Search+verify; persist hit; skip miss; leave 403/429 queued |
| `GithubSearchSkip.TTL_MS` | const | 30 days |
| `GitHubDiscoverLaunch.enqueue` | fun | WorkManager unique work; Refresh uses 15s delay; Update All replaces |
| `ReleaseRefreshProbes.github` | fun | Leftover Search only when `searchUnknowns` (Settings, default off) |

## Acceptance criteria

- ✅ User-visible: Refresh stays hint/`listReleases` only unless the user enables Search GitHub when F-Droid has no source link
- ✅ After pulse-run, a background worker searches up to 5 leftovers at 10/min
- ✅ Hits persist in `github_verified.tsv`; empty search is skipped for 30 days
- ✅ Continuum is not curated; the worker can still bind it
- ✅ Play listed still skips search; Play miss still queues
- ✅ Offline/error: 403/429 do not write skip; Wi-Fi-only pref is honoured
- ✅ Accessibility: N/A — silent worker
- ✅ i18n: no new strings

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Blank package skipped; empty Search JSON is skip (`GitHubDiscoverTest`) |
| Network timeout | Existing GitHub timeouts; unknown stays queued |
| Race conditions | Worker retries while Refresh or Update All is busy; Update All REPLACE after installs |
| Unhandled exceptions | `runCatching` in the worker; `Result.retry` |

## Smoke scenario

1. _Given_ Continuum Calendar is installed, not in the shipped TSV, Settings leftover Search off
2. _When_ Refresh then Update All finish
3. _Then_ the worker searches it, Forge lists `owner/repo` on the next Refresh via the verified TSV, and logcat has no leftover Search during Refresh

## Container map

| Layer | Path |
|-------|------|
| Logic | `GitHubDiscover`, `GithubSearchSkip`, `GitHubDiscoverLive` |
| View | none |
| Tests | `GitHubDiscoverTest`, `GithubSearchSkipTest`, `ReleaseRefreshProbesSkipTest` |
| Wiring | `ReleaseRefreshRunner`, `UpdateAllLaunch`, `GitHubDiscoverWorker` |

## Tests

- Automated: yes

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Definition of Done

Client tests with recorded fixtures. Do not hit live GitHub in unit tests.

## Notes

- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
