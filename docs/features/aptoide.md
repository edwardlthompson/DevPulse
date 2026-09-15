# Feature: aptoide

> Sprint 11. Play-agnostic last-release lookup. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.aptoide`. No live network in unit tests. Never guess a date.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `AptoideLookupStatus` | enum | `Ok`, `UnknownCheckManually` |
| `AptoideLookup` | data class | `updatedOnMs`, `publishedVersion`, `status`, optional `uname`, `fileUrl`, `versionCode` |
| `AptoideCachePolicy` | object | 24h TTL |
| `AptoideFetchPolicy` | object | honest User-Agent; Refresh batches `listAppsUpdates` (100/chunk, vercode 0 + signing SHA-1). Successful-chunk omissions are known misses (7-day TTL). getMeta only if a chunk fails or the APK has no signing SHA-1 |
### Functions

| Name | Contract |
|------|----------|
| `AptoideMetaParser.parse(json)` | Reads `data.updated` or `data.modified` or `data.file.added`; file URL from `path` then `path_alt`; `vercode` when present |
| Missing or unparseable date | `UnknownCheckManually`, `updatedOnMs = null` |
| `AptoideScan.toPick` | Ok + plausible ms → `RemoteReleasedSource.Aptoide`; stores download URL + versionCode |
| `AptoideScan.lookupForInstall` | Signed APK → one-shot `listAppsUpdates`; getMeta only when signing SHA-1 is missing |
## Acceptance criteria

- ✅ User-visible behavior: Settings opt-in (default off); Refresh queries Aptoide for every user app when the outlet is enabled
- ✅ Offline/error behavior: 404 / timeout / bad JSON → unknown; inventory stays local
- ✅ Accessibility: toggle labeled for TalkBack
- ✅ i18n: keys under `aptoide_*`
- ✅ Never auto-download or install APKs on Refresh; Update All / listing tap resolve via `listAppsUpdates` + SHA-1 (store `getMeta` only if unsigned)

## Smoke scenario

1. _Given_ a saved getMeta fixture with `updated`
2. _When_ the parser runs
3. _Then_ it returns that date or unknown, never a guessed value

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../index/aptoide/` |
| View | Settings toggle |
| Tests | `src/test/.../index/aptoide/` plus JSON fixtures |
| Wiring | `ReleaseRefreshRunner` when Aptoide is enabled |
## Definition of Done

Fixture parser tests required. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Date is last-seen-on-Aptoide, not Play. Refresh POSTs installed package + signing SHA-1 to public `listAppsUpdates` (same endpoint Aptoide Store uses). Matching-signer hits get `updated`/`modified` and `file.path` (or `path_alt`). A successful chunk that omits a package is a **known miss** for 7 days — no leftover `getMeta` (Play-signed apps rarely match Aptoide’s cert). A failed chunk does not mark misses and still `getMeta`. Unsigned APKs stay on getMeta. An F-Droid hit does not skip Aptoide.
- Update All / listing taps reuse `listAppsUpdates` + the installed SHA-1 to resolve the APK URL. Store-scoped `getMeta` is not used for signed apps. Download URLs persist in `update_artifacts.tsv` across process death. A resolve miss (`InstallWhy.ResolveMiss`) is not ignored. `file.hardware.cpus` is stored as native codes so a listing that cannot run on this ABI is refused before download; a downloaded APK whose `lib/` ABI does not overlap the device is still refused as `Sdk`.
- Listing taps open `cm.aptoide.pt` when installed (`https://en.aptoide.com/app?package_name=`). Aptoide Games does not handle `aptoidesearch://`. Official Store APK: `https://en.aptoide.com/download`. HTTPS uname pages are the web fallback only.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`

## Tests

- Automated: yes — see Container map Tests row and `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
