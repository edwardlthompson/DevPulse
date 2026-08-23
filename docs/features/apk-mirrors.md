# Feature: apk-mirrors

> Sprint 11 follow-up. Opt-in APKMirror and APKPure listing lookup. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.apkmirror` and `dev.foss.goldenpath.index.apkpure`. No live network in unit tests. Never guess a date.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `ApkMirrorBatchFetcher` | fun interface | `fetch(packageNames): Result<String>` — one JSON body per chunk |
| `ApkPureBatchFetcher` | fun interface | same |
| `ApkMirrorScan.offersFor` | function | Chunk 100, up to 4 chunks in parallel; exists+version+`publish_date` when present |
| `ApkPureScan.offersFor` | function | Chunk 200; version only; date stays null |
### Functions

| Name | Contract |
|------|----------|
| `ApkMirrorMetaParser.parseMany` | Missing/unparseable date → `ms = null`; never invent a day |
| Failed HTTP | reuse last-good dump chunk when present; else `listed = false`, `known = false` |
| Successful miss | `listed = false`, `known = true` |
## Acceptance criteria

- ✅ Settings opt-in, default off; Refresh batches enabled dump sites
- ✅ Offline/error: inventory stays local; unknown not red
- ✅ i18n: `dump_store_*`, `apkmirror_enable`, `apkpure_enable`, `inventory_source_apkmirror`, `inventory_source_apkpure`
- ✅ Listing taps download APKPure `asset.url` (including `download.cdnpure.com`) and APKMirror `download.php` when the listing page exposes it. Missing file is a failed download.
- ✅ Dump 429/403 honor Retry-After before the next Refresh chunk

## Smoke scenario

1. _Given_ saved APKMirror exists JSON and APKPure update JSON
2. _When_ the parsers run
3. _Then_ Mirror has version+date or unknown date; Pure has version and no guessed date
4. Device: `am start ... --ez refresh true --ez apk_mirror true --ez apk_pure true` persists the Settings toggles and starts Refresh. Logcat `DevPulse` should show `dump stores mirror=true` then one `apkmirror chunk` / `apkpure chunk` line per batch.

## Notes

- Dates are last-seen-on-that-site. APKPure has no date field in its update JSON. APKMirror chunks overlap (4-wide). Mirror and Pure dump threads overlap. An Aurora/Play miss still goes through both dump stores.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
