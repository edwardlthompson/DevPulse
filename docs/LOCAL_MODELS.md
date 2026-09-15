# Local models (no cloud keys)

Use a local OpenAI-compatible server so Cursor Chat can stay on this machine. The template never stores keys and never writes Cursor `settings.json`. This template repo does **not** require Ollama (maintainer declined 2026-09-10). The recipe below is for child repos that want a local model.

## What this is (and is not)

- **Chat / inline edit** can talk to `http://127.0.0.1:11434/v1` (Ollama) or `http://127.0.0.1:1234/v1` (LM Studio).
- **Tab completion and Agent/tool quality** may still use Cursor’s own models depending on the product version. A local 7B model is not a drop-in for `/ship`.
- Do **not** set `CURSOR_API_KEY`, create OpenAI dashboard keys, or use tunnels.

## Ollama (preferred)

1. Install [Ollama](https://ollama.com) (local binary; no account required for localhost).
2. Pull a coder model that fits RAM/VRAM, for example `ollama pull qwen2.5-coder:7b` (8G machines: stay at 7B; 16G+ can try larger).
3. Keep the server on loopback only: `ollama serve` listening on **127.0.0.1:11434**.

### Cursor Settings (GUI)

1. Cursor Settings → Models.
2. Override the OpenAI-compatible base URL to `http://127.0.0.1:11434/v1`.
3. The form may refuse an empty key. **type this in the GUI** (not a secret; never commit; never put in `.env`):

```
ollama
```

4. Add the model name exactly as `ollama list` shows it. Select only that local model.

If Cursor reports a CORS error: keep Ollama on localhost; update Cursor; **do not** bind the LAN or open a tunnel.

## LM Studio (optional)

Same GUI steps with base URL `http://127.0.0.1:1234/v1` and the same GUI dummy string.

## Probe

```bash
python3 scripts/agent-run.py check-local-compute
# or: just local-compute / just linux-dev
```

`ollama=up` means the loopback API responded. `/coach` may point here when that line is up. Broader Linux DX (caches, inotify, direnv): [`LINUX_DEV.md`](LINUX_DEV.md).

## Never

- GitHub Actions secrets for this path
- Copying the GUI dummy string into `.env`, `.env.example`, or `.cursor/mcp.json`
- Starting a second Ollama per `/best-of-n` worker (one server is enough)
