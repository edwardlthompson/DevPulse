# Feature: ux-pulse

> Sprint 29. Pulse mark on home rows. Filters in a sheet. No listing emoji. No haptic. No bottom nav.

## Public API (locked)

| Name | Kind | Contract |
|------|------|----------|
| `InventoryCopy.listingMarkPrefix` | fun | always `""` (words via `listingMarkStatusRes`) |
| Tokens | typography | `titleMedium` + `labelSmall` in `design-tokens.json` |

## Acceptance criteria

- ✅ User-visible: home rows show a pulse mark for updates; filters open in a bottom sheet; listing status is words, not emoji
- ✅ Offline/error: filters still work with an empty result (`InventoryEmptyKind.Filters`)
- ✅ Accessibility: TalkBack can read pulse without color alone; reduce-motion skips scrubber fade
- ✅ i18n: no new `strings.xml` keys unless a dedicated XML file is required

## Smoke scenario

1. Given the inventory list
2. When the user opens filters
3. Then chips appear in a sheet and do not cover the whole list on a small window

## Container map

| Layer | Path |
|-------|------|
| Logic | `InventoryCopy.kt` |
| View | `ui/inventory/InventoryRow.kt`, `InventoryScreen.kt`, `AppIcon.kt` |
| Tests | `InventoryCopyTest.kt` |
| Wiring | none in `GoldenPathApp` |

## Tests

- Automated: yes — `InventoryCopyTest` prefix empty

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Prefix `""` is never null; status res still covers every `ListingMark` |
| Network timeout | N/A — no network I/O |
| Race | Filter sheet state is `showFilters`; dismissing the sheet calls `onToggleFilters` |
| Unhandled exceptions | AppIcon bitmap load stays `runCatching` |

## Notes

- Accent (primary) is for primary actions (Update), not decorative chrome
- Follow-up: detail Advanced (Sprint 30)
