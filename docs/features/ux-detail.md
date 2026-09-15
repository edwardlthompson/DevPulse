# Feature: ux-detail

> Sprint 30. App detail shows identity, one heartbeat status, and listings. Paste-repo, GitHub regex, and direct APK sit under Advanced.

## Public API (locked)

| Name | Kind | Contract |
|------|------|----------|
| `InventoryDetailChrome.slot` | fun | `last_release` → Status; `package`/`icon`/`label`/`pin` → Identity; `listings` → Listings; else Advanced |
| Advanced | UX | Default collapsed. Paste, APK regex, and direct APK stay reachable when expanded |

Do not add bottom nav. Extra i18n in `inventory-ui.xml`, not `strings.xml`.

## Acceptance criteria

- ✅ User-visible: detail identity + last-release status + listings; paste/regex/APK behind Advanced
- ✅ Offline/error: Advanced still opens with no listings
- ✅ Accessibility: Advanced is a labeled control; listings stay on the surface
- ✅ i18n: `inventory_advanced` in `inventory-ui.xml`

## Smoke scenario

1. Given an installed app with a GitHub listing
2. When the user opens detail and expands Advanced
3. Then paste-repo and GitHub regex are there; the home list is unchanged

## Container map

| Layer | Path |
|-------|------|
| Logic | `inventory/InventoryDetailChrome.kt` |
| View | `ui/inventory/InventoryDetailScreen.kt`, `InventoryDetailAdvanced.kt` |
| Tests | `InventoryDetailChromeTest.kt` |
| Wiring | none in `GoldenPathApp` |

## Tests

- Automated: yes — `InventoryDetailChromeTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Unknown last-release still uses `inventory_last_release_unknown`; Advanced does not require listings |
| Network timeout | N/A — no network I/O in this change |
| Race | Advanced expand is local `remember` state; leaving detail drops it |
| Unhandled exceptions | Paste/regex/APK keep existing validation (`SourceFieldValidate`, regex codec) |

## Notes

- Unused `ScanScreen` / `ScanDetailScreen` removed; `ScanSession` stays for local-scan helpers
- Follow-up: destinations only if home is still cramped (Sprint 31)
