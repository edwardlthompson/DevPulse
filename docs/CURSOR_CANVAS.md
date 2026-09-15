# Canvas and Design Mode walkthrough

Two Cursor surfaces. Do not mix them up.

## Gate status Canvas (`/gates`)

Always render the bootstrap status overview after local gates.

```bash
python3 scripts/agent-run.py render-gates-status
```

Skill: [`.cursor/skills/canvas-bootstrap-status/SKILL.md`](../.cursor/skills/canvas-bootstrap-status/SKILL.md). That writes `.cursor/gates-status.md`. When `CURSOR_CANVAS_DIR` is set, it also writes a Canvas file.

If Canvas tooling is unavailable, fall back to a markdown gate table in chat. Do not fail `/gates` because Canvas is missing.

## Design Mode (web/PWA only)

Product Design Mode (Agents Window browser: click / draw / voice on the live UI) applies when the active stack includes **web/PWA**. Do not use it for Android- or Python-only work. Docs: [Design Mode](https://cursor.com/docs/agent/design-mode).

Home chrome stays **Settings-only**. Theme, About, and donate live in Settings/About. Do not add a header Theme chip or donate button while sketching.

## When to use which

| Surface | Use | Skip |
|---------|-----|------|
| Gate Canvas | `/gates` pass/fail + next BUILD_PLAN row | Designing UI |
| Design Mode | Layout/copy on `examples/web` | Android/Python-only, `/feature` logic |
| Markdown table | Canvas unavailable | Nothing — this is the fallback |

See [`CURSOR_MODES.md`](CURSOR_MODES.md) and [`DESIGN_GUIDE.md`](DESIGN_GUIDE.md).
