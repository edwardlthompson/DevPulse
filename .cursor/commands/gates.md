# Gates

> Skills: `.cursor/skills/validate-bootstrap/`, `.cursor/skills/check-repo-hygiene/`, `.cursor/skills/canvas-bootstrap-status/`

Run Sprint 0 / pre-push validation (Git Bash on Windows), then **always** report via skill `canvas-bootstrap-status` (Canvas beside chat, or a markdown table if Canvas is unavailable). Do not skip the status overview.

```bash
python3 scripts/agent-run.py check-local-compute
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py check-cursor-hooks -- --smoke
python3 scripts/agent-run.py feature-gate --stack multi
python3 scripts/agent-run.py smoke-sprint --if-complete
python3 scripts/agent-run.py check-repo-hygiene
python3 scripts/agent-run.py run-android-emulator-local -- --if-device
python3 scripts/agent-run.py render-gates-status

```

`check-cursor-hooks --smoke` covers destructive-ops + UTF-8 hook wiring (fail-open honesty still applies).

`check-local-compute` is INFO (exit 0 unless misconfig). Emulator `--if-device` SKIPs when no adb device — it will not download system images.

Report pass/fail per script. Fix failures in scope before marking BUILD_PLAN items complete. `smoke-sprint --if-complete` skips while AGENT rows remain; after a sprint is all ✅ it must pass (startup + load order, no crashes) or the wrap-up fails. After the scripts, follow `.cursor/skills/canvas-bootstrap-status/SKILL.md` and render the gate table (Canvas first).

**bootstrap-doctor** (alias): same gates — `python3 scripts/agent-run.py validate-bootstrap --quick` (Sprint 0 / local) or `python3 scripts/agent-run.py run-maintainer-gates` (weekly; omit `--quick` for full CI wait). No separate script.

Begin now.
