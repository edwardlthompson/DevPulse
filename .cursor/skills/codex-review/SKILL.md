---
name: codex-review
description: Run optional Codex CLI review into CODE_REVIEW.md and hand off Critical/High to BUILD_PLAN + /fix. Use when /codex-review or expanded /prerelease.
disable-model-invocation: false
---

# Codex review (read-only)

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
