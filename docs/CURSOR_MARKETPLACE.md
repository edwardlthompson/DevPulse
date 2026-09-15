# Cursor plugin marketplace runbook

FOSS default is the **local plugin pack**. Do not publish this template to a marketplace. Do not install a third-party marketplace by default.

## Local pack (default)

```bash
python3 scripts/agent-run.py pack-cursor-plugin
```

Symlink `dist/cursor-plugin` → `~/.cursor/plugins/local/agent-project-bootstrap` and Reload Window. Manifest: [`.cursor-plugin/plugin.json`](../.cursor-plugin/plugin.json). Do **not** symlink the repo root (double-loads rules). Details: [`CURSOR_INTEGRATIONS.md`](CURSOR_INTEGRATIONS.md).

## Marketplace (opt-in, `[HUMAN]`)

Child repos **may** add [wshobson/agents](https://github.com/wshobson/agents). Do **not** install it on this template — 200+ agents drown context and fight one-feature-per-agent plus local-compute-first.

If a child opts in, pick plugins that **complement** shipped commands:

| Marketplace plugin | Complements | Do not replace |
|--------------------|-------------|----------------|
| `debugging-toolkit` | `/debug` | Debug Mode in `docs/CURSOR_MODES.md` |
| `git-pr-workflows` | `/push` | Destructive-ops + hook denylist |
| Language plugins | Active stack only | Golden Path examples / MODULE.md |

Never vendor the catalog into `.cursor/`. Never install unsigned marketplace plugins on the FOSS default path ([THREAT_MODEL.md](THREAT_MODEL.md) LLM03).

## Publish (not this repo)

`[HUMAN]` owns any later marketplace listing for a **child** product plugin. This template does not ship a marketplace listing, signing key, or store listing YAML.
