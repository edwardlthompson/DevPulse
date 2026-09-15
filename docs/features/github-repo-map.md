# Feature: github-repo-map

> Sprint 36. Shipped package→GitHub repo catalog plus daily pull. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.forge` and `dev.foss.goldenpath.inventory`. Unit tests use fixtures only — no live GitHub.

| Name | Kind | Contract |
|------|------|----------|
| `GithubShippedCatalog.parse` | fun | TSV `package TAB owner/repo` via `GithubVerifiedCodec` |
| `GithubShippedCatalog.fromAssets` | fun | `assets/github-repos/verified.tsv` |
| `GithubCatalogSync.rawUrl` | fun | `raw.githubusercontent.com/{ProductUpdate.RELEASE_REPO}/main/examples/android/app/src/main/assets/github-repos/verified.tsv` |
| `GithubCatalogSync.due` | fun | Same daily window as product self-update |
| `GithubCatalogSync.merge` | fun | `pulled + local` so device hits win |
| `GithubCatalogSync.apply` | fun | 304 keeps local; 200 merges; other status does not save |
| `ReleaseRefreshHints.github` | fun | Merge order: shipped, local verified, F-Droid records, paste (right wins) |

Regen: `python3 scripts/build-github-repo-catalog.py` (official F-Droid wins over Izzy).

## Acceptance criteria

- ✅ User-visible: F-Droid GitHub apps list from the shipped map on first Refresh without leftover Search
- ✅ Paste and local `github_verified.tsv` beat shipped rows
- ✅ Daily pull from DevPulse raw TSV is GET-only; the device inventory is never uploaded
- ✅ Offline/error: missing asset is empty map; 404/403/429 skip until tomorrow; other failures retry next Refresh
- ✅ Accessibility: N/A — no new UI
- ✅ i18n: no new strings

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Blank TSV lines ignored (`GithubShippedCatalogTest`) |
| Network timeout | 10s/15s GitHub timeouts; fail stays local (`GithubCatalogSyncTest`) |
| Race conditions | Catalog pull runs before Refresh merge; local verified overlay wins |
| Unhandled exceptions | `runCatching` on asset load and GET |

## Smoke scenario

1. _Given_ NewPipe is installed and in the shipped TSV
2. _When_ Refresh runs with GitHub on
3. _Then_ Forge uses `listReleases` for that owner/repo and leftover Search stays 0 for it

## Container map

| Layer | Path |
|-------|------|
| Logic | `GithubShippedCatalog`, `GithubCatalogSync`, `GithubCatalogLive`, `ReleaseRefreshHints` |
| View | none |
| Tests | `GithubCatalogSyncTest`, `GithubHintFilesTest`, `ReleaseRefreshGithubAddTest` |
| Wiring | `ReleaseRefreshRunner` load + pull |

## Tests

- Automated: yes

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Definition of Done

Client tests with recorded fixtures. Do not hit live GitHub in unit tests.

## Notes

- Architecture stolen from APKUpdater (`GitHubApps` + `listReleases`). Do not vendor rumboalla’s list.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
