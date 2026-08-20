# Feature: play-wayback

> Sprint 13. Recover a Play `datePublished` from a Wayback snapshot after live Play is missing. Never guess a date.

## Public API (locked)

| Name | Contract |
|------|----------|
| `WaybackSnapshot.snapshotUrl(json)` | Closest available archive.org URL, rewritten with `id_`; none if unavailable |
| `WaybackPlayClient.recover(pkg)` | `PlayLookup` only when archived HTML has `datePublished` |
| `PlayScan.toOffer` | Live Missing → optional recover; still `listed = false` |

## Acceptance criteria

- ✅ Delisted/404 Play can keep a recovered date from archived Play HTML
- ✅ Listed or bot-wall Play never hits Wayback
- ✅ No date from CDX/availability timestamp alone
- ✅ Unlisted Play date does not beat a listed source

### Critique

| Issue | Resolution |
|---|---|
| Null/empty snapshot | Leave delisted Play with no date |
| Network timeout | `runCatching` → no recovery |
| Race | Same `ReleaseRefreshRuntime.tryBegin()` |
| Unhandled parse | `PlayHtmlParser` already refuses relative dates |
