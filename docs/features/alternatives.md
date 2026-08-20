# Feature: alternatives

> Sprint 9. FR-29, FR-30. Not Sprint 0 or 1. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: Alternatives search cached F-Droid plus Izzy and other enabled repos by category, name similarity, and tags
- 🔲 Offline/error behavior: cache only; empty list if no maintained match
- 🔲 Accessibility: links announced as external destinations
- 🔲 i18n: keys under `alternatives_*` and `sources_*` in `strings.xml`
- 🔲 Show only actively maintained matches with last-update dates and links
- 🔲 List every known download location: official F-Droid, IzzyOnDroid, other enabled repos, and forge Releases if an APK asset exists
- 🔲 Do not auto-download or auto-install

## Smoke scenario

1. _Given_ a stale app and a cached maintained F-Droid neighbor in the same category
2. _When_ the user opens Alternatives and Sources
3. _Then_ the neighbor appears with a date and link, and no APK is fetched

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../alternatives/` |
| View | `examples/android/.../ui/sources/` |
| Tests | `src/test/.../alternatives/` |
| Wiring | detail screen ≤10 lines |

## Definition of Done

Similarity tests against cached fixtures. Fallback: `bash scripts/feature-gate.sh --stack android`.

## Notes

- Linking to stores and releases is fine; silent install is not
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
