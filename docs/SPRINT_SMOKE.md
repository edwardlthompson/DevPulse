# Sprint smoke

Agents do not start the next sprint until every ✅ `[AGENT]` / `[AUTO]` row in the finished sprint is proven live.

## When

| Moment | Command |
|--------|---------|
| After each AGENT row | `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto` |
| Sprint (or feature) all ✅ | `python3 scripts/agent-run.py smoke-sprint --require` |
| `/gates` wrap-up | same check with `--if-complete` (skips if rows are still open) |

Fail → leave the last row 🔲 or ❌. Fix. Re-run. Do not chain `/build` to the next sprint.

## What it proves

- **No errors/crashes** on Golden Path shells (web HTTP 200, CLI `ok`, Android manifest readable)
- **Startup time** vs `scripts/sprint-smoke-budget.json` (web TTFB, CLI start)
- **Load order** (web `link`/`script` tags; Android Application then launch Activity)
- **Each ✅ row** maps to a passing probe (`docs/SPRINT_SMOKE.md` keywords in `sprint_smoke_map.py`)

Report: `.cursor/sprint-smoke.json` (gitignored).

Device TalkBack / emulator launch stays `[ADB]`. No Ollama, Play Services, or crash-proxy.

## Flags

```bash
python3 scripts/agent-run.py smoke-sprint --require --sprint M50
python3 scripts/agent-run.py smoke-sprint --if-complete
python3 scripts/agent-run.py smoke-sprint --dry-run --sprint M51
```
