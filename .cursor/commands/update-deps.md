# Local-first dependency update

> Skill: `.cursor/skills/update-deps/`

Do **not** `git push`. Prefer `depsonar_*` MCP tools (`scan`, `check`, `changelog`, `audit` / `live_cve`, then `update` at patch/minor) when they are available. If a depsonar tool returns nothing in about 60 seconds, fall back to the CLI.

## Step 1 — Dry-run

```bash
python3 scripts/agent-run.py update-deps
```

Summarize outdated packages, majors, and Gradle fallback (`Gradle: Dependabot backup or enable depsonar MCP.`). Do not apply majors. Always render the dry-run canvas:

```bash
python3 scripts/agent-run.py render-upd-status
```

## Step 2 — Audit

```bash
python3 scripts/agent-run.py update-deps -- --audit
```

Halt on HIGH/CRITICAL. Optional: depsonar changelog for packages you will bump.

## Step 3 — Apply patch/minor only (after summarizing)

```bash
python3 scripts/agent-run.py update-deps -- --apply
```

Halt if Kotlin `>=2.3.30` or the Kotlin guard fails. Then `python3 scripts/agent-run.py verify`. If `examples/web`, `examples/node`, `examples/python`, or `examples/android` changed, run that stack’s tests (`npm test`, `uv run pytest`, `./gradlew test`).

Gradle plugins from depsonar must use the same cap (never write Kotlin `>=2.3.30` yourself):

```bash
python3 scripts/agent-run.py apply-depsonar-gradle -- --pins 'plugin.id=x.y.z,...'
python3 scripts/agent-run.py apply-depsonar-gradle -- --apply --pins 'plugin.id=x.y.z,...'
```

Stop for the human before commit/push unless they invoked `/ship` (which grants push after this step).

Begin now.
