# Pre-release gate (used by `/ship`)

> Skill: `.cursor/skills/update-deps/`

`/ship` runs `/update-deps` first, then this command. Autofix happens here so release stays one simple super command. Release works with no extra API keys.

## Step 0 — Dependencies and Release Please preview

If `/ship` already ran `/update-deps` this session, **audit-only** (do not apply again):

```bash
python3 scripts/agent-run.py update-deps -- --audit

```

If this command was invoked **standalone** (not after `/update-deps`), execute @.cursor/commands/update-deps.md fully first.

Then preview the next Release Please version (never publishes; skip/WARN if no GitHub token):

```bash
python3 scripts/agent-run.py release-please-dry

```

## Step 1 — Mechanical autofix + gate loop

```bash
python3 scripts/agent-run.py prerelease-autofix

```

On exit `2` (env/3-strike): halt — do not `/push`.
On exit `1`: apply semantic fixes in feature scope, re-run step 1 (max 3 cycles), then continue.

## Step 2 — Hard pre-release gate (local)

```bash
python3 scripts/agent-run.py pre-release-gate -- --local

```

Local: feature-gate, `update-deps --audit`, template version, license. No GitHub CI wait, Dependabot API, or Scorecard here. `/push` and `/regress` still run the full GitHub gate after push. Do not tag until those pass. See @docs/MAINTAINING_THE_TEMPLATE.md Release Checklist for maintainers.

Begin now.
