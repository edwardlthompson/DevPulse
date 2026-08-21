# Feature: forge-lookup

> Sprint 6. FR-9 to FR-13. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.forge`. Unit tests use fixtures only — no live GitHub/GitLab/Codeberg.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `ForgeHost` | enum | `GitHub`, `GitLab`, `Codeberg` |
| `MatchConfidence` | enum | `ExactPackage`, `GradleId`, `TitleFuzzy` |
| `ForgeCandidate` | data class | `host`, `ownerRepo`, `packageId`, `title`, `latestCommitMs`, `latestReleaseMs`, `archived` |
| `ForgeMatch` | data class | `candidate`, `confidence` |
| `PastedRepo` | data class | `packageName`, `url` |
| `ForgeTokenStore` | interface | `getToken()` / `setToken()` — never log |
| `GitHubSearchClient` | fun interface | `searchRepos(query)` — kept so existing fakes compile |
| `GitHubReleaseClient` | fun interface | `listReleases(ownerRepo)` — `GitHubSearchHttp` implements both |
| `GithubHint` | data class | `ownerRepo`, `ms`, `versionName` — winning F-Droid GitHub `sourceCode` (or persisted row) |
### Functions

| Name | Contract |
|------|----------|
| `ForgeMatcher.rank(packageName, label, candidates)` | Exact `packageId` wins; then Gradle id; then `ForgePackageEvidence` (full package or `.`→`-`/`_` in ownerRepo/title/description/packageId). Title/owner tail is not enough |
| `ForgeBackoff.nextDelayMs(statusCode, attempt)` | Honour 403 and 429; other codes no backoff |
| `ForgeSearchParser.parse(json)` | Recorded search fixture → candidates |
| `GitHubSearchQuery.repositories` | Trimmed **app label** for `search/repositories` (quoted if it has spaces). Blank label → quoted package for discovery only |
| `GitHubRepoParser.parse` | GitHub search API `items` → `ForgeCandidate` |
| `GitHubReleaseParser.parse` | `GET /repos/{owner}/{repo}/releases` array → haystacks (`name`, `tag_name`, `body`, `assets[].name`) + `published_at` |
| `ForgePackageEvidence.inText` | Full package or `.`→`-`/`_` variant in a release/repo haystack |
| `FdroidGithubHints.hints` | F-Droid `sourceCode` URLs that `ForgeUrl.downloadPage` accepts as GitHub → `package → GithubHint` (`ownerRepo` + record `lastUpdatedMs` + `suggestedVersionName`). Per package, first GitHub URL by repo rank: official → Izzy → Archive → Guardian → Calyx → other. Ignores GitLab/Codeberg/example.com. `map` is `ownerRepo` only |
| `FdroidGithubHints.harvest` | Same-object walk of a full index (not just installed apps). Wipe Files `sourceCode`-before-`packageName` stays on that app; the next app’s repo is not used |
| `FdroidGithubHints.mergeLibrary` | Ranked merge of persisted TSV + harvested official/Izzy/Archive/Guardian/Calyx maps. Official wins; Izzy fills Archive/Guardian/Calyx-only gaps |
| `GithubVerifiedStore` | Persisted TSV `package → owner/repo` grown from the full harvested library (`filesDir/github_verified.tsv`) |
| `GitHubScan.toOffer` | Hint-first: if a `GithubHint` (F-Droid `sourceCode` ∪ persisted map) is set, **list with zero GitHub HTTP**. `pageUrl` = `ForgeUrl.downloadPage` of that GitHub URL. Dates/version come from the F-Droid record; persisted-only rows may omit them. Success → persist. No hint + `searchUnknowns` false (default) → unknown, log `github $pkg skip search (no hint)`, **do not name-search**. No hint + `searchUnknowns` true → name search + release verify |
### Token storage

Production uses `EncryptedForgeTokenStore` (EncryptedSharedPreferences, ADR-0001) with a one-way migrate from the DataStore adapter. Tokens are never logged. GitLab/Codeberg leftover search runs after a GitHub miss (`LeftoverForgeScan`).

## Acceptance criteria

- ✅ User-visible behavior: GitHub **hint-first** (F-Droid `sourceCode` on the same app object ∪ persisted map) lists without `listReleases` / `searchRepos`. Leftover name search is **opt-in** (`forge_lookup_search_unknowns`, default off). When search is on, list only if a release asset/title/tag/body contains the full package (or `-`/`_` variant)
- ✅ Offline/error behavior: honour 403 and 429 on opt-in search; persist backoff; show unknown
- ✅ Accessibility: match confidence announced on the detail screen
- ✅ i18n: keys under `forge_*` in `strings.xml`
- ✅ Prefer exact package or Gradle id over fuzzy title
- ✅ Optional GitLab and Codeberg
- ✅ User can paste a missed repo URL; DevPulse tracks it thereafter
- ✅ Optional GitHub token in EncryptedSharedPreferences; never log the token
- ✅ F-Droid GitHub `sourceCode` on the same app object is enough to list GitHub (Izzy Wipe Files lists `peterhearty/WipeFiles` with no token). Do not list from a neighbor’s `sourceCode`

## Smoke scenario

1. _Given_ an Izzy record `uk.org.platitudes.wipefiles` with `sourceCode=https://github.com/peterhearty/WipeFiles`
2. _When_ `GitHubScan.toOffer` receives that hint
3. _Then_ the app is listed, `searchRepos` and `listReleases` stay at 0, and the offer uses the F-Droid `lastUpdatedMs` / `suggestedVersionName`

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../index/forge/` |
| View | `examples/android/.../ui/forge/` + `ForgeLookupSettings` |
| Tests | recorded JSON fixtures in `src/test` |
| Wiring | scan orchestrator ≤10 lines |
## Definition of Done

Client tests with recorded fixtures. Do not hit live GitHub in unit tests. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Refresh always probes every enabled outlet for each user app: parallel F-Droid indexes, then a bounded pool for Play (~4), Aptoide (~8), and GitHub (~2). An F-Droid or Play hit does not skip later outlets. After each index download, **harvest every GitHub `sourceCode`** on the same app object (official, Izzy, Archive, Guardian, Calyx — not only installed apps) into `github_verified.tsv`. GitHub lookup (one progress tick) is **hint-first**: ranked F-Droid `sourceCode` (official → Izzy → Archive → Guardian → Calyx → other) that `ForgeUrl` accepts as GitHub overwrites a persisted verified `package → owner/repo` row. A hint **lists immediately** — no `GET /repos/{owner}/{repo}/releases`. Persist the `owner/repo`. No hint defaults to GitHub **❓** (`listed=false, known=false`) and skips name search (`github $pkg skip search (no hint)`). Settings opt-in “Search GitHub when F-Droid has no source link” (`forge_lookup_search_unknowns`, default false) restores leftover name search + release verify. Extra HTTP stays inside the existing GitHub probe. A GitHub offer **replaces** any existing Forge offer for that package. Progress total is repos + apps × enabled probes (Play / Aptoide / GitHub).
- APKUpdater (`rumboalla/apkupdater`) is faster because it uses a curated package→repo map. DevPulse does **not** import that list. Opt-in Aurora Play downloads are a separate toggle (`docs/features/aurora-play.md`). We grow our own verified map from F-Droid, IzzyOnDroid, Archive, Guardian, and Calyx `sourceCode` (same-object parse, Wipe Files lesson).
- Self-pulse for DevPulse uses a configured repo, not this search
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
