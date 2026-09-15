# Feature: ux-honesty

> Sprint 28. Honest fail copy, empty states, Hide vs Stop, slim Refresh bar. Pulse-check first. Do not rename Refresh.

## Public API (locked)

| Name | Kind | Contract |
|------|------|----------|
| `InventoryEmptyKind` | enum | `None`, `Search`, `Filters`, `NoApps`, `Blocked` |
| `InventoryEmpty.kind(...)` | fun | empty list → Search if query, Filters if filters on, Blocked if `!canScan`, else NoApps |
| `InventoryEmpty.res(kind)` | fun | string for that empty kind |
| `InventoryCopy.failRes` | fun | dedicated fail keys; never `about_*` or `debug_*` ids |
| Hide vs Stop | UX | Hide dismisses Update all and keeps the batch; Stop cancels. Close is not cancel. |
| Slim Refresh bar | UX | Dismissing the Refresh dialog does not cancel. A 2dp bar stays while `refreshing`. |

Do not add `InstallWhy.Abi`. Do not delete `ScanScreen`. No haptic. No `design-tokens.json` edits. Extra i18n in `update-all.xml` / `inventory-ui.xml`, not `strings.xml`.

## Acceptance criteria

- ✅ User-visible: empty search, blocked scan, and no-apps each have their own copy; fail rows say timeout / incompatible / no file / older honestly
- ✅ Offline/error: ABI mismatch copy is listing-only (not a new `InstallWhy`); listing skipped uses its own string
- ✅ Accessibility: empty and fail text is readable copy, not emoji-only; Hide/Stop labeled
- ✅ i18n: new keys in `res/values/update-all.xml` and `res/values/inventory-ui.xml`

## Smoke scenario

1. Given a search with no matches, or QUERY_ALL skipped
2. When the list is empty
3. Then the empty copy matches `InventoryEmptyKind`, not the generic no-apps line

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../inventory/InventoryEmptyKind.kt`, `InventoryCopy.kt` |
| View | `ui/inventory/`, `GoldenPathScreen.kt` |
| Tests | `InventoryCopyTest.kt`, `InventoryEmptyKindTest.kt` |
| Wiring | none in `GoldenPathApp` |

## Tests

- Automated: yes — `InventoryCopyTest`, `InventoryEmptyKindTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | `InventoryEmpty.kind` treats `visibleCount > 0` as `None`; blank query is not Search |
| Network timeout | `failRes(Timeout)` uses `install_fail_timeout`, not About/debug ids |
| Race | Dismiss Refresh/Hide Update all does not cancel in-flight work; Stop/outlet Stop does |
| Unhandled exceptions | `failRes` covers every `InstallWhy`; ABI stays listing copy, not a new why |

## Notes

- Rename poisoned ids at callers: `about_update_no_compatible` → `aptoide_games`, `about_update_restarting` → `settings_clear_listings`
- `listingMarkStatusRes` stays; do not reuse `update_cache_failed` for timeout
- Follow-ups: pulse rows (Sprint 29), detail Advanced (Sprint 30)
