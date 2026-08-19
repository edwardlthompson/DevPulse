# Cursor Feature Radar

Weekly maintainer workflow that diffs [Cursor docs index](https://cursor.com/llms.txt) against [`CURSOR_FEATURE_REGISTRY.json`](CURSOR_FEATURE_REGISTRY.json).

## Scoring rubric

| Factor | Weight | Question |
|--------|--------|----------|
| Agent efficiency | 3 | Reduces tokens, steps, or human interrupts? |
| Error prevention | 3 | Catches mistakes rules/scripts miss? |
| FOSS fit | 2 | Works without paid Cursor tiers? |
| Child-repo value | 2 | Helps forks, not just template maintainer? |
| Implementation cost | -2 | New scripts, CI, or breaking changes? |

**Thresholds**

- Score ≥ 7 → append to gitignored `CURSOR_RADAR_BACKLOG.md`
- Score ≥ 9 → flag **suggest BUILD_PLAN** in `CURSOR_RADAR_REPORT.md` only (never auto-edit BUILD_PLAN)

## Run locally

```bash
bash scripts/cursor-feature-radar.sh
```

Network failures write `status: fetch_failed` and exit **0** (non-blocking in CI).

## Tier gate

Commercial-only registry entries do not auto-suggest for FOSS-default repos unless `distribution_tier: commercial` in `.cursor/stack-selection.json`.

## Overrides

Edit registry fields without code changes:

- `template_status`: `shipped` | `example` | `planned` | `rejected` | `watch`
- `ignore_until`: `YYYY-MM-DD` suppresses stale warnings until that date

Report footer includes `llms_txt_sha256` for diff debugging.

See also: [`CURSOR_INTEGRATIONS.md`](CURSOR_INTEGRATIONS.md), [`/maintain`](../.cursor/commands/maintain.md).
