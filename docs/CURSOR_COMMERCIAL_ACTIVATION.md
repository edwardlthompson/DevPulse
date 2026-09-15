# Commercial Cursor activation

Requires **Commercial** distribution tier (`--distribution-tier commercial` or `sync-cursor-features.py --tier commercial`).

## Checklist

1. Confirm `[HUMAN]` approval to swap from FOSS compliance (`foss-compliance.mdc` → `commercial-compliance.mdc`)
2. Run `python3 scripts/sync-cursor-features.py --tier commercial --copy-commercial`
3. Copy examples (if not auto-copied):
   - `.cursor/environment.json.commercial.example` → `.cursor/environment.json`
   - `.cursor/BUGBOT.md.commercial.example` → `.cursor/BUGBOT.md`
   - `.cursor/mcp.commercial.example` → `.cursor/mcp.json` (or merge servers)
   - Optionally merge `.cursor/hooks.cloud.commercial.example.json` conversation hooks with project hooks (keep FOSS shell/encoding hooks)
4. Set environment variables (`LINEAR_API_KEY`, Sentry auth, etc.) — never commit secrets
5. Restart Cursor; verify Cloud Agent / Bugbot in Cursor settings
6. Run `bash scripts/check-cursor-integrations.sh --tier commercial`
7. Review Automations recipes in [`CURSOR_AUTOMATIONS.commercial.md`](CURSOR_AUTOMATIONS.commercial.md) (CI-fail triage, nightly digest, webhook → gates)
8. Map Bugbot Autofix to local `/fix` expectations in `BUGBOT.md`
9. For multi-repo or Slack: configure a named Cloud environment in the Cursor dashboard (Slack Jul 2026 multi-repo support) — docs only; no Slack app secrets in-repo
10. Optional: create Grok Bots from [`GROK_BOTS.md`](GROK_BOTS.md) for always-on Android/R8 scouting and BUILD_PLAN weekly/monthly maintain (read-only git; no signing keys; no `git push`)

## Docs-only dry-run (FOSS template)

No secrets, no tier swap. Walk the checklist on paper:

1. Confirm this repo’s `.cursor/stack-selection.json` still says `"distribution_tier": "foss"` (stop here unless `[HUMAN]` approved commercial).
2. Read each example path in the checklist without copying: `.cursor/environment.json.commercial.example`, `BUGBOT.md.commercial.example`, `mcp.commercial.example`, `hooks.cloud.commercial.example.json`.
3. Skim [`CURSOR_AUTOMATIONS.commercial.md`](CURSOR_AUTOMATIONS.commercial.md) and [`GROK_BOTS.md`](GROK_BOTS.md) Monday-cron drill — note which items need billing.
4. Run (still FOSS): `python3 scripts/agent-run.py check-cursor-integrations -- --tier foss`
5. Do **not** run `sync-cursor-features.py --tier commercial` during this dry-run.

## Android commercial patterns

See [`modules/android/COMMERCIAL.md`](../modules/android/COMMERCIAL.md) for Play Services / Firebase guidance (not in FOSS path).

## Automations

See [`CURSOR_AUTOMATIONS.commercial.md`](CURSOR_AUTOMATIONS.commercial.md) for cron/event automations (Cursor Cloud billing applies).

## Cloud conversation hooks

See [`.cursor/hooks.cloud.commercial.example.json`](../.cursor/hooks.cloud.commercial.example.json) for `afterAgentResponse` / `stop` self-correct loops via `watch-agent-gates`. Project `.cursor/hooks.json` remains the FOSS baseline; Cloud Agents ignore IDE Run Modes — keep destructive policy in hooks.
