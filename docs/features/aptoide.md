# Feature: aptoide

> Sprint 11. Play-agnostic last-release lookup. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.aptoide`. No live network in unit tests. Never guess a date.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `AptoideLookupStatus` | enum | `Ok`, `UnknownCheckManually` |
| `AptoideLookup` | data class | `updatedOnMs`, `publishedVersion`, `status` |
| `AptoideCachePolicy` | object | 24h TTL |
| `AptoideFetchPolicy` | object | honest User-Agent; Refresh uses bounded parallel getMeta (no serial 1500 ms sleep) |
### Functions

| Name | Contract |
|------|----------|
| `AptoideMetaParser.parse(json)` | Reads `data.updated` or `data.modified` or `data.file.added` |
| Missing or unparseable date | `UnknownCheckManually`, `updatedOnMs = null` |
| `AptoideScan.toPick` | Ok + plausible ms → `RemoteReleasedSource.Aptoide` |
## Acceptance criteria

- ✅ User-visible behavior: Settings opt-in (default off); Refresh queries Aptoide for every user app when the outlet is enabled
- ✅ Offline/error behavior: 404 / timeout / bad JSON → unknown; inventory stays local
- ✅ Accessibility: toggle labeled for TalkBack
- ✅ i18n: keys under `aptoide_*`
- ✅ Never auto-download or install APKs

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
| Wiring | `rememberScanSession` starts fetch when opted in |
## Definition of Done

Fixture parser tests required. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Date is last-seen-on-Aptoide, not Play. Refresh always probes Aptoide in the bounded parallel pool when the outlet is enabled; an F-Droid hit does not skip it.
- Listing taps open `cm.aptoide.pt` when installed (`{uname}.en.aptoide.com` or `aptoidesearch://package`). Otherwise they open the Aptoide web listing. `en.aptoide.com` without a uname host is Aptoide home, not an app page.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
