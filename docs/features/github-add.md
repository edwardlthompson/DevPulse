# Feature: github-add

> Sprint 22–23. Replace Obtainium as an updater, not as a store. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.forge`. Unit tests use fixtures only — no live GitHub.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `PackageIdAliases` | object | Allowlist suffixes `.fdroid`, `.debug`, `.nightly` only. Lookup: exact, `pkg+suffix`, then strip those suffixes. Never strip an arbitrary last segment. Conflicting `owner/repo` → no auto-bind |
| `ForgeLibraryMatch` | object | `bind` ranks ExactPackage, SuffixVariant, ReleaseEvidence. `autoBind` persists Exact and SuffixVariant only when the repo is unique |
| `WatchedRepoStore` | TSV | Unmatched `owner/repo`. Not inventory rows. Re-bind on `PACKAGE_ADDED` |
| `GithubAdd` | object | GitHub `owner/repo` from a paste URL; persist pasted last-wins then verified |
| `GitHubStarredScan` | object | Max 5 pages of `/user/starred`. Zero `listReleases` / `searchRepos` |
| `ObtainiumImport` | object | Every GitHub `apps[].url` → watched. `id`+url bind when present. Nested JSON; never tokens |
| `GithubAppOpt` | data | Per-app prerelease flag; APK filename regex capped at 64 characters |
| `DirectApkCodec` | object | `ApkDownloadUrl.httpsFile` only (https, public host, no XAPK) |
## Acceptance criteria

- ✅ User-visible: inventory FAB pastes a GitHub URL; Exact/SuffixVariant auto-bind; picker on conflict; unmatched stays watched
- ✅ GitHub-only apps such as Obtainium list from the `.fdroid` library row without copying F-Droid version/ms
- ✅ Re-probe uses the same alias lookup so a paste does not vanish after `storeSettled`
- ✅ Opt-in starred scan (token required, Settings, off Refresh) auto-binds Exact/SuffixVariant and shows matched K of N
- ✅ Obtainium JSON import skips unknown sources and never writes tokens
- ✅ Nested Obtainium backups import every GitHub watch into `WatchedRepoStore`; file picker in Sources
- ✅ Per-app include-prereleases and filename regex; over-long or invalid pattern ignored; ABI/minSdk refuse unchanged
- ✅ Direct HTTPS APK URL installs still run package + cert + sha256 gates
- ✅ Offline/error: blank/ftp/non-GitHub FAB rejected; starred 403/429 snackbar; truncated import does not crash
- ✅ Accessibility: FAB content description and labeled URL field
- ✅ i18n: new copy in `res/values/forge.xml` (`strings.xml` is at the 300-line cap)

## Smoke scenario

1. _Given_ `dev.imranr.obtainium` is installed and the library has `dev.imranr.obtainium.fdroid` → `ImranR98/Obtainium`
2. _When_ Refresh or FAB paste / starred bind runs
3. _Then_ Forge lists `https://github.com/ImranR98/Obtainium/releases` with null version until listing tap, and `searchRepos` stays at 0

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../index/forge/` (`PackageIdAliases`, `GithubAdd`, `GitHubStarredScan`, `ObtainiumImport`) |
| View | `ui/forge/AddRepoDialog.kt`, `ui/inventory/DetailGithubOpts.kt`, `ui/settings/GithubStarredSettings.kt` |
| Tests | `src/test/.../index/forge/` + `ReleaseRefreshForgeTest` / `ReleaseRefreshProbesTest` |
| Wiring | FAB on `GoldenPathScreen`; Sources settings groups |
## Definition of Done

Alias, re-probe, starred, import, regex, and direct-APK unit tests above, or fallback `bash scripts/feature-gate.sh --stack android`. After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`.

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty URL or package | `ForgeUrl.downloadPage` + `GithubAdd.ownerRepo`; reject blank, ftp, and non-GitHub in the FAB. Tests: empty, `ftp://`, `https://github.com/orgs/foo` |
| Network timeout | Existing GitHub connect/read timeouts and `ForgeBackoff` for 403/429. Starred failure is a snackbar; Refresh is not aborted. Test: empty pages → 408, zero binds |
| Race with Refresh | Store locks on TSV files. FAB writes pasted last-wins, then `forgetFetched` + one-package re-probe. Harvest cannot clobber a pasted row because `githubHints` applies pasted last. Test: pasted `owner/repo` wins |
| Unhandled exceptions | `runCatching` on starred/release/import JSON parse; skipped count; no crash. Test: truncated Obtainium JSON |
| Alias false positive | Allowlist suffixes only; unique `owner/repo` required; conflict → picker. Test: `org.mozilla.firefox` unchanged; two repos → no auto-bind |
| Wrong update from sibling F-Droid flavor | SuffixVariant copies `ownerRepo` only, not version/ms. Cert mismatch still refuses install. Test: aliased Obtainium `versionName` null until listing tap |
| Re-probe restamps Forge miss | `AppReprobeLive` uses `GithubHintFiles.hint`; hint short-circuits `storeSettled`. Test: Play known-miss + aliased hint → Forge listed, zero `searchRepos` |
| Starred quota / privacy | Token required; 5-page cap; no per-star HTTP; opt-in Settings; `docs/PRIVACY.md`; never log token or star list |
| Obtainium import secrets | Parse url+id only; skip tokens. Test fixture with a dummy PAT that must not reach stores |
| Nested `]` truncates backup | Bracket-match `apps` array and objects; skip strings. Test: `apkUrls` + `categories` + escaped `]` in additionalSettings |
| Huge pasted backup OOM | OpenDocument picker; `readUtf8` 2MB cap returns null. Test: oversize stream |
| Uninstalled Obtainium apps vanish | `persist` always `watched.add` after bind. Test: two GitHub rows in watched store |
| Regex ReDoS | 64-char cap, filename only, invalid pattern ignored. Test: over-long pattern rejected |
| Direct APK SSRF | `ApkDownloadUrl.httpsFile` public-host allowlist; no http IP. Test: `http://169.254.0.1/x.apk` rejected |
| Watched repos becoming a catalog | `WatchedRepoStore` never appears as inventory rows; bind only when an installed package matches. Test: unmatched URL → store size 1 |
| `strings.xml` 300-line cap | New copy in `values/forge.xml` |
| File size | New files; do not grow `GitHubScan.kt` |
## Notes

- Do not browse F-Droid/Droidify catalogs or import rumboalla’s GitHub map.
- Do not open GitHub name-search for every Play miss (`storeSettled` unchanged). Aliases + paste + stars cover GitHub-only apps. See `DECISION_LOG.md`.
- Update all stays user-triggered. Root remains opt-in.
- `[ADB]` smoke: GitHub Obtainium lists on Refresh; FAB paste binds; cert refuse still blocks a mismatched APK.
