# Best-of-N local model race

> Skill: `.cursor/skills/best-of-n/`

Compare models on a flaky or hard fix using **native Cursor worktrees** on This Computer. Do **not** `git push`. Do **not** start multiple Ollama servers (one local server is enough).

## 1. Budget N

```bash
python3 scripts/agent-run.py check-local-compute

```

Set **N** = `min(3, slots)` from that output. If `ram_gb` is under 16, use **N = 2**. Do not spawn more agents than N.

## 2. Run native best-of-n

Use Cursor’s `/best-of-n` (or the models picker) with N worktrees. Setup scripts: `.cursor/worktrees.json` (`setup-worktree-unix.sh` / `setup-worktree-windows.ps1`). They copy `*.env.example` only.

Models come from the **Cursor picker** (cloud or the user’s local Ollama override). See `docs/LOCAL_MODELS.md` — do not invent API keys.

## 3. Android serial isolation

When racing Android instrumented work across worktrees, set a **distinct** `ANDROID_SERIAL` (or `ANDROID_SERIAL_<worktree>`) per runner so `adb` never targets another agent’s emulator. Example: worktree A → `emulator-5554`, worktree B → `emulator-5556`. Do not share one serial across N runners. Skip `/emulator` races when only one KVM slot exists (`check-local-compute`).

## 4. Apply the winner

Keep one worktree. Remove extras when done. Parallel-lock leftovers under `.cursor/worktrees/` (not Cursor native worktrees): `python3 scripts/agent-run.py gc-worktrees -- --apply`. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto` on the winner.

Begin now.
