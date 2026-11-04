---
name: local-models
description: Point Cursor Chat or Cline at localhost Ollama/LM Studio without cloud API keys.
disable-model-invocation: false
---

# Local models (no keys)

See also: `docs/LOCAL_MODELS.md`, `docs/help/LOCAL_AGENT.md`, `docs/help/LOCAL_TROUBLESHOOTING.md`, `scripts/check_local_compute.py`

```bash
python3 scripts/agent-run.py recommend-model
python3 scripts/agent-run.py setup-local
python3 scripts/agent-run.py check-local-compute

```

Bind 127.0.0.1 only. Do not use ngrok, CURSOR_API_KEY, or LAN bind. Cursor may require typing `ollama` in the Models GUI (not a secret; never commit). Modelfiles: `templates/ollama/`.
