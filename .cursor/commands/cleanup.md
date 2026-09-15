# BUILD_PLAN archive cleanup

Run after BUILD_PLAN execution when local gates pass. Moves finished work off the active board into @COMPLETED_TASKS.md.

**Do not archive** while any `[AGENT]` or `[AUTO]` row in the active sprint/feature block is still 🔲 or ❌. Rows auto-completed by `/build` automation (✅ HUMAN/ADB) may be archived with AGENT/AUTO work. Items in `HUMAN_BACKLOG.md` stay 🔲 on the board until a human clears them.

## Step 1 — Confirm completion

- All executed `[AGENT]` and `[AUTO]` rows in the active block are ✅
- Per-row gates passed (`watch-agent-gates.sh`)
- Sprint wrap `python3 scripts/agent-run.py smoke-sprint --require` passed (every ✅ row smoked; no errors/crashes)
- Replace 🔲 → ✅ only for rows verified done **this session**; never mark complete while gates are red

## Step 2 — Archive to COMPLETED_TASKS.md

Keep the **Archived sprints** index at the top of @COMPLETED_TASKS.md. Prepend the new dated section immediately after that table (or after the file header if the table is missing):

```markdown
## {Sprint or feature name} ({YYYY-MM-DD})

- ✅ [OWNER] Original description

```

Copy every ✅ row from the finished block verbatim (keep owner labels and descriptions). Add the sprint to the index table (Sprint, Complete, SHA).

## Step 3 — Slim BUILD_PLAN.md

Remove the archived ✅ rows from the active board.

**Finished sprint (audit, maintainer, release):**

- Delete the finished sprint section from `BUILD_PLAN.md` entirely (no leftover stub, no archive table on the live board)
- Prepend the ✅ rows (and a skipped-sprint note if needed) to `COMPLETED_TASKS.md`
- Add or update the **Archived sprints** index table in `COMPLETED_TASKS.md` (Sprint, Complete, SHA)

**Finished feature (Sprint 2+ per-feature block):**

- Remove the completed feature's ✅ sequential rows and its Parallel table
- Reset the per-feature template to 🔲 defaults for the next feature, or duplicate a fresh block with the next feature name

**Playbook templates** (Child Repo Sprint 0/1/2+ boilerplate): leave 🔲 template rows in place — only archive rows that were actually executed.

## Step 4 — Stale parallel lock

```bash
python3 scripts/agent-run.py gc-parallel-lock
python3 scripts/agent-run.py gc-worktrees -- --apply

```

## Step 5 — Verify

```bash
python3 scripts/check-file-encoding.py BUILD_PLAN.md COMPLETED_TASKS.md

```

Active board should contain no ✅ rows except backlogged `[HUMAN]`/`[ADB]` items explicitly left open (see `HUMAN_BACKLOG.md`).

Begin now.
