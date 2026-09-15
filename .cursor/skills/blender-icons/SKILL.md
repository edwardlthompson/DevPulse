---
name: blender-icons
description: Manifest-driven Cycles icon factory. Seed, camera, and lighting only. Use when /feature icon-factory or blender stack.
disable-model-invocation: false
---

# Blender icon factory

See also: `docs/features/icon-factory.md`, `modules/blender/MODULE.md`, `docs/adr/0006-blender-runtime.md`.

1. Read `AGENT.md` if present (do not substitute template About/donate).
2. Change seed, camera, and lighting **only** via the JSON manifest. Do not hand-light 4000 icons.
3. QA is `qa-report.json`. Never add a BUILD_PLAN `[HUMAN]` row to visually review the batch.
4. OptiX: `export BLENDER_CYCLES_DEVICE=OPTIX` on This Computer. CI stays CPU or skip.

```bash
python3 scripts/agent-run.py blender-icons -- --stub --limit 1
python3 scripts/agent-run.py blender-icons -- --manifest examples/blender/fixtures/icon-manifest.sample.json --resume
```
