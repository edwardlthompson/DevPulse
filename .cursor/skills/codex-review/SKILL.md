---
name: codex-review
description: Advanced optional Codex CLI review into CODE_REVIEW.md. Use only when the human explicitly asks for /codex-review. Not first-time. Not part of /ship or /prerelease. Beginners use Cline.
disable-model-invocation: false
---

# Codex review (read-only, advanced)

Beginners skip this. First-time path is Cline (`docs/help/CLINE.md`).

See also: `.cursor/commands/codex-review.md`, `docs/CODEX_REVIEW.md`

```bash
python3 scripts/agent-run.py run-codex-review
```

| Exit | Meaning |
|------|---------|
| 0 | `CODE_REVIEW.md` written |
| 3 | Skip — no `OPENAI_API_KEY` or `codex` CLI |
| 1 | Failure — do not append BUILD_PLAN from partial output |

After Critical/High findings: append 🔲 `[AGENT]` BUILD_PLAN rows, fix, then `watch-agent-gates --once --autofix`. Never ask Codex to apply patches.
