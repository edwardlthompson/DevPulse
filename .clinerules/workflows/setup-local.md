# /setup-local — Local Ollama agent setup

Follow `AGENTS.md` strictly. Ollama is optional.

1. Run `python3 scripts/agent-run.py recommend-model`.
2. Run `python3 scripts/agent-run.py setup-local` (dry-run). Use `-- --apply` only if the base model is already pulled.
3. Print Cline settings (Provider=Ollama, model, Context Window).
4. Point at `docs/help/LOCAL_AGENT.md` and `docs/LOCAL_MODELS.md`.

Reference: `docs/help/LOCAL_AGENT.md`.
