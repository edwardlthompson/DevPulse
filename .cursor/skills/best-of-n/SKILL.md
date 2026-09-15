---
name: best-of-n
description: Race N local worktrees/models on a hard fix. Use when /best-of-n or flaky gate comparison.
disable-model-invocation: false
---

# Best-of-N

See also: `.cursor/commands/best-of-n.md`, `.cursor/worktrees.json`, `docs/LOCAL_MODELS.md`

Cap N by `check-local-compute` slots (max 3; 2 if RAM < 16G). One Ollama server only. Never `git push`.

Android races: isolate with per-worktree `ANDROID_SERIAL` (never share one emulator across runners).
