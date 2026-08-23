# Feature: play-lookup

> Sprint 5. FR-6. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.play`. No live network in unit tests. Never guess a date.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `PlayLookupStatus` | enum | `Ok`, `UnknownCheckManually` |
| `PlayLookup` | data class | `updatedOnMs`, `publishedVersion`, `status` |
| `PlayCachePolicy` | object | 24h listed TTL; **7-day** delist TTL (`MISS_TTL_MS`) |
### Functions

| Name | Contract |
|------|----------|
| `PlayHtmlParser.parse(html)` | Reads `itemprop="datePublished"`, `itemprop="softwareVersion"`, and developer website when present |
| Missing or unparseable date | `status = UnknownCheckManually`, `updatedOnMs = null` — never invent a day |
### Rate limit (types only)

`PlayFetchPolicy.minIntervalMs = 1_500` — serial/modest. Fetcher interface is unused in tests.

## Acceptance criteria

- ✅ User-visible behavior: public Play details HTML yields Updated-on and published version
- ✅ Offline/error behavior: 24h listed cache honored by Refresh (`ProbeCache` + `fetchedAtMs`); confirmed 404/410/not-found stays delisted for 7 days and is not re-fetched; persist last error; 403 → HTTP 403 copy; parse fail → could-not-parse copy; never guess a date; unknown rows are not reused. Aurora **missing** is a Play miss (no HTML). Play HTML runs only when Aurora is unknown or unwired.
- ✅ Accessibility: unknown state is announced as text, not an empty badge
- ✅ i18n: keys under `play_*` in `strings.xml`
- ✅ Public HTML only; modest concurrency, effectively serial
- ✅ One failure must not block the whole scan
- ✅ Fixture-based parser tests ship with the feature
- ✅ Optional and on-demand so F-Droid anti-features stay NonFreeNet at most
- ✅ App detail can re-probe Play / Aptoide / GitHub for that package without a full Refresh
- ✅ Play developer website that is a public forge URL becomes a GitHub hint (FR-11)
- ✅ HTTP 429/403 honor Retry-After on the Play HTML fetch

## Smoke scenario

1. _Given_ a saved HTML fixture
2. _When_ the parser runs
3. _Then_ it returns a date or unknown, never a guessed value

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../index/play/` |
| View | detail source row |
| Tests | `examples/android/app/src/test/.../index/play/` plus HTML fixtures |
| Wiring | scan orchestrator ≤10 lines |
## Definition of Done

Isolated parser tests with fixtures are mandatory. Fallback for live Play: manual unknown-check, not a CI live scrape.

## Notes

- Honest User-Agent. Do not impersonate a browser
- Refresh probes Play via Aurora `gplayapi` bulk details first (same catalog as Play). A listed/missing Aurora result is a Play listed/miss. First-walk misses get one second Aurora pass. Auth or transport failure is unknown and falls back to the public HTML page. A Play miss does not skip F-Droid, Aptoide, APKMirror, or APKPure. Confirmed misses are reused for 7 days. Update can still open the Play Store.
- After delisting, `docs/features/play-wayback.md` may recover `datePublished` from archived HTML. The listing stays unlisted.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
