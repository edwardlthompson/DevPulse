# Feature: play-lookup

> Sprint 5. FR-6. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.play`. No live network in unit tests. Never guess a date.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `PlayLookupStatus` | enum | `Ok`, `UnknownCheckManually` |
| `PlayLookup` | data class | `updatedOnMs`, `publishedVersion`, `status` |
| `PlayCachePolicy` | object | 24h TTL |

### Functions

| Name | Contract |
|------|----------|
| `PlayHtmlParser.parse(html)` | Reads `itemprop="datePublished"` and `itemprop="softwareVersion"` only |
| Missing or unparseable date | `status = UnknownCheckManually`, `updatedOnMs = null` — never invent a day |

### Rate limit (types only)

`PlayFetchPolicy.minIntervalMs = 1_500` — serial/modest. Fetcher interface is unused in tests.

## Acceptance criteria

- ✅ User-visible behavior: public Play details HTML yields Updated-on and published version
- ✅ Offline/error behavior: 24h cache; persist last error; 403 or parse fail → unknown-check-manually; never guess a date
- ✅ Accessibility: unknown state is announced as text, not an empty badge
- ✅ i18n: keys under `play_*` in `strings.xml`
- ✅ Public HTML only; modest concurrency, effectively serial
- ✅ One failure must not block the whole scan
- ✅ Fixture-based parser tests ship with the feature
- ✅ Optional and on-demand so F-Droid anti-features stay NonFreeNet at most

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
- Refresh probes the public details page. HTTP 404/410 or a not-found page without version/date is not listed and never becomes an update link
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
