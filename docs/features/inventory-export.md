# Feature: inventory-export

> Share the current visible inventory as HTML, CSV, or XML. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory`. Compose adapters must not redefine them.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `InventoryExportFormat` | enum | `Html`, `Csv`, `Xml` — each has `fileName` and `mimeType` |

### Functions

| Name | Contract |
|------|----------|
| `InventoryExport.render(apps, format)` | Snapshot of `apps` only. Empty list writes headers / empty table / empty `<inventory>` |
| `InventoryExport.listingUrl(link)` | `http(s)` URL only when `listed`. Forge/GitHub is included only when listed with a real page URL |
| `InventoryShare.send(context, apps, format)` | Write cache, `ACTION_SEND` via FileProvider. Failures do not crash |

## Acceptance criteria

- ✅ User-visible behavior: top-bar Share on the main list opens HTML / CSV / XML and shares the current visible apps
- ✅ Offline/error behavior: export is local; write or share failures are swallowed
- ✅ Accessibility: Share icon and format items use `inventory_export*` strings
- ✅ i18n: `inventory_export`, `inventory_export_html`, `inventory_export_csv`, `inventory_export_xml`

## Smoke scenario

1. _Given_ the inventory list is filtered (search, stale, updates, On GitHub)
2. _When_ the user taps Share and picks HTML
3. _Then_ the file contains only those rows, with `<a href="https://github.com/...">` for Forge-listed apps

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../inventory/InventoryExport.kt`, `InventoryExportMarkup.kt` |
| View | `examples/android/.../ui/inventory/InventoryShareAction.kt` |
| Tests | `examples/android/app/src/test/.../inventory/InventoryExportTest.kt` |
| Wiring | `InventoryUiModel.onExport` one line; `GoldenPathScreen` one Share action |

## Definition of Done

Unit tests for HTML link + escape, CSV package+url, well-formed XML, empty list. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Export uses `inventory.apps` after query, filters, and sort. No marks are added to the main list.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
