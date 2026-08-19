# Maintaining the Template

Playbook for template maintainers optimizing agent-project-bootstrap over time.

## Semver Policy

- **MAJOR:** Breaking changes to init prompt structure or file layout
- **MINOR:** New modules, examples, CI gates, non-breaking features
- **PATCH:** Doc fixes, dependency bumps, typo corrections

**Exemplar vs template version:** Golden Path stack examples ship a stub app version (`0.1.0` in `examples/android/app/build.gradle.kts` and web `package.json`) so forks can set their own semver independently. Template releases use `.template-version` (currently aligned with Release Please tags). Do not confuse the two when filing issues or tagging releases.

## Release Checklist

1. All CI checks green on main
2. `bash scripts/check-repo-hygiene.sh` passes
3. **Dry-run:** **Actions → Release → Run workflow** (`workflow_dispatch`, no tag input) to validate SBOM/provenance **before** merge
4. Run `scripts/pre-release-gate.sh` (or `.ps1`) — CI poll, Dependabot Critical/High count, version/tag match
5. Run `scripts/run-maintainer-gates.sh` for weekly maintainer cycle (readme, fdroid metadata, feature-gate, CI jobs)
6. Bump `.template-version` (or merge Release Please PR which bumps it)
7. Update `CHANGELOG.md` (Keep a Changelog; Release Please PR covers this)
8. Update `TEMPLATE_INDEX.json` version and file entries
9. Run `scripts/validate-template-index.sh`
10. Merge Release Please PR; **release published** event attaches SBOM assets automatically
11. Update repo About if description changed
12. Weekly CVE triage completed within last 7 days (`docs/SECURITY_TRIAGE.md`)
13. Zero open Critical/High Dependabot alerts (or documented exception with linked issue)
14. `THIRD_PARTY_LICENSES.md` reviewed; SBOM attached to release
15. Move completed Sprint M* items to `COMPLETED_TASKS.md`

## Safe Edit Zones

| Zone | Risk | Notes |
|------|------|-------|
| `docs/`, `modules/` | Low | Additive changes preferred |
| `docs/CURSOR_MODES.md` | Low | Canonical Cursor mode router; avoid duplicating tables elsewhere |
| `.github/workflows/` | Medium | Document in CHANGELOG |
| `TEMPLATE_INDEX.json` schema | High | Requires migration notes |
| `INITIALIZATION_PROMPT.md` structure | High | MAJOR version bump |
## Feedback Loop

Encourage `template_improvement` issues. Triage labels:

- `agent-confusion` — agent could not self-route
- `token-waste` — unnecessary files read
- `ci-gap` — missing quality gate
- `module-request` — new ecosystem module

## Coach layer

- Why catalog: [`docs/BEST_PRACTICES.md`](BEST_PRACTICES.md). 30-day playbook: [`docs/FIRST_30_DAYS.md`](FIRST_30_DAYS.md). Slash command: `/coach`.
- Root and Golden Path `justfile`s are optional DX. CI must keep calling `uv` / `npm` / `gradlew` / `scripts/verify.sh` directly.
- Adding a slash command requires updating `scripts/check-batch-commands.sh` ATOMIC, `validate-bootstrap.sh` BATCH_COMMANDS, `.cursor/rules/batch-commands.mdc`, and both BATCH_COMMANDS docs.

## Regression

Template CI must pass before every release. The template eats its own dogfood.

## Branding pack vs template README

- Upstream template keeps `branding/product.json` `"mode": "template"`. `scripts/generate-project-readme.py` writes **only** `branding/generated/README.preview.md` — never overwrite the template [README.md](../README.md) with the product pitch template.
- Child repos set `"mode": "product"` during Sprint 0 after filling name/tagline/pitch.
- Logos and official colors: [`branding/BRANDING.md`](../branding/BRANDING.md). After token or asset edits: `python3 scripts/sync-design-tokens.py`.

## README Badges

The [README.md](../README.md) uses [shields.io](https://shields.io/) static badges (`style=flat-square`) for BUILD_PLAN owner labels and supported stacks. Badges are external images with `alt` text; no tracking scripts.

| Badge | Color | Meaning |
|-------|-------|---------|
| `AGENT` | `#2ea043` (green) | Cursor-automated work |
| `HUMAN` | `#0969da` (blue) | Human-only gates |
| `ADB` | `#bf8700` (amber) | Android device / F-Droid |
| `AUTO` | `#656d76` (gray) | CI, bots, scripts |
Stack badge colors: Web `#646cff`, Python `#3776AB`, Android `#3DDC84`, Lightroom `#31A8FF`. Optional stacks use neutral gray badges and link to `docs/OPTIONAL_STACKS.md`.

When adding a new **default** stack to the init picker:

1. Add the module to `TEMPLATE_INDEX.json` `modules`
2. Add a badge row to the README **Supported Stacks** HTML table
3. Keep `TEMPLATE_INDEX.json` as the machine-readable source of truth

Write README edits via Python `Path.write_text(..., encoding="utf-8")` on Windows to avoid UTF-16 corruption from editor tools.

**Hero badges:** Sync the `template-X.Y.Z` shields.io badge in the README hero strip with `.template-version` on every release (Release Checklist step 6).

**Table of contents:** The README `## Contents` anchor list must stay in sync when adding or renaming `##` sections.

## Roadmap Index

`TEMPLATE_INDEX.json` includes a `roadmap` object with the active maintainer board pointer and key exemplar paths. Move implemented paths into the `files` array (and `modules` when applicable) when new sprint work ships.
