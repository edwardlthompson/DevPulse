# Pre-release gate (expanded — used by `/ship`)

> Docs: @docs/CODEX_REVIEW.md

`/ship` runs this command first. Autofix + optional Codex happen here so release stays one simple super command.

## Step 1 — Mechanical autofix + gate loop

```bash
python3 scripts/agent-run.py prerelease-autofix

```

On exit `2` (env/3-strike): halt — do not `/push`.
On exit `1`: apply semantic fixes in feature scope, re-run step 1 (max 3 cycles), then continue.

## Step 2 — Optional Codex third-party review

```bash
python3 scripts/agent-run.py run-codex-review

```

- Exit `3` (`SKIP: Codex review (no key/CLI)`): print the skip and **continue** (do not block release).
- Exit `1`: leave prior `CODE_REVIEW.md` untouched; halt until fixed or `[HUMAN]` defers.
- Exit `0`: if `CODE_REVIEW.md` has Critical/High findings, append 🔲 `[AGENT]` rows to @BUILD_PLAN.md, implement them, then:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --step none

```

Repeat until Critical/High cleared or 3-strike halt (do not `/push` on halt).

## Step 3 — Hard pre-release gate

```bash
python3 scripts/agent-run.py pre-release-gate

```

Confirm CI + Security Scan + CodeQL green, zero Critical/High Dependabot alerts, `.template-version` present.
Do not tag or `/push` until this gate passes. See @docs/MAINTAINING_THE_TEMPLATE.md Release Checklist for maintainers.

Begin now.
