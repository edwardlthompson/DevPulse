# Feature: github-leftover

> Sprint 35. Store-less apps get an unauthenticated GitHub leftover search. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.forge`. Unit tests use fixtures only — no live GitHub.

| Name | Kind | Contract |
|------|------|----------|
| `GitHubSearchPace.PER_MINUTE` | const | `10` — GitHub anonymous search quota. A token does not raise leftover search above this so Refresh never waits on 403 |
| `GitHubSearchQuery.repositories` | fun | `"package" OR label` (quoted label when it has spaces). Same-string label omitted |
| `ForgeSlug.matches` | fun | Compact repo name vs label and package segments length ≥ 6 |
| `GitHubReleasePick.leftover` | fun | Package evidence, else slug/tail match then first `.apk` across the release page (walks SBOM-only latest tags) |
| `PackageIdAliases.encoded` | fun | `io.github.Owner.Repo` → `Owner/Repo` hint; listReleases; no search |
| `GitHubLeftoverNoise.skip` | fun | `app.devpulse` and `org.chromium.webapk.*` |
| `ListingMiss.Searched` | enum | Empty leftover search. Codec flag `s`. 7-day miss TTL. Pre-change `0` Never misses are searched again |

## Acceptance criteria

- ✅ User-visible: apps with no store listing (Play miss is not a listing) are GitHub-searched in the background after scan+update without a token and without a curated Continuum row. Refresh leftover Search stays off unless Settings enables it
- ✅ Play-listed apps still skip GitHub name-search (`github $pkg skip search (listed)`)
- ✅ Leftover query is one search per package (`package OR label`); hits persist in `github_verified.tsv`; later Refresh is hint `listReleases` only
- ✅ Latest GitHub tag with no APK does not hide an older tag that has an APK
- ✅ Offline/error: 403/429 stay unknown and retry; empty worker search is a 30-day skip (`GithubSearchSkip`)
- ✅ Accessibility: N/A — no new UI
- ✅ i18n: no new strings (`strings.xml` cap)

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Blank package skipped; empty search JSON is `Searched` miss (`GitHubScanLeftoverTest`) |
| Network timeout | Existing 10s/15s GitHub timeouts; 403/429 unknown not cached (`GitHubScanTest`) |
| Race conditions | Store outlets still finish before the GitHub wave (`ReleaseRefreshWaves.storeThenForge`) |
| Unhandled exceptions | `runCatching` on search/verify; fail → unknown (`GitHubScan.fail`) |

## Smoke scenario

1. _Given_ Continuum Calendar is installed and not on Play/F-Droid, with no curated alias
2. _When_ Refresh then the leftover discovery worker run
3. _Then_ Forge lists `edwardlthompson/continuum-calendar` from leftover search, using an APK-bearing tag if v1.2.0 has none, and Refresh logcat has no leftover Search for that package unless Settings search-unknowns is on

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../index/forge/` (`ForgeSlug`, `GitHubScanVerify`, `GitHubLeftoverNoise`, `PackageIdAliases.encoded`) + `ReleaseRefreshProbes` |
| View | none |
| Tests | `GitHubScanLeftoverTest`, `ForgeSlugTest`, `ReleaseRefreshProbesSkipTest` |
| Wiring | existing GitHub outlet; `PackageIdAliases.expand` |

## Tests

- Automated: yes — see Container map Tests row

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Definition of Done

Client tests with recorded fixtures. Do not hit live GitHub in unit tests.

## Notes

- GitHub does not raise anonymous `search/repositories` above 10/min. Core `listReleases` is a different 60/hour bucket and is what hinted apps already use. No HTML scraping.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
