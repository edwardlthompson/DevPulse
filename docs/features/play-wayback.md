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

## Smoke scenario

1. _Given_ the feature is enabled
2. _When_ the user exercises the primary path
3. _Then_ the app stays honest about missing or unknown dates

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/dev/foss/goldenpath/` |
| View | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/` |
| Tests | `examples/android/app/src/test/` |
| Wiring | `GoldenPathApp.kt` ≤10 lines |

## Tests
- Automated: yes — see Container map Tests row and `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
