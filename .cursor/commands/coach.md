# Coach (why)

Output: **one next action** + optional one Why link. No health novels. Twin: `docs/help/COACH.md`.

```bash
python3 scripts/agent-run.py project-health
python3 scripts/agent-run.py feedback-inbox

```

Rules:

- Fixes in inbox → `/audit`
- Large dirty tree or many unpushed commits → **commit now**, then ask the human to `/push` (never silent `git push`; offer `/split-to-prs` if the diff is huge)
- Open AGENT/AUTO → `/build` (name `next_row`); after each ✅ row, nudge commit + `/push`
- No AGENT + dirty Unreleased → `/ship` or `/prerelease`
- Both empty → `/allideas` (or `/ideas` for a short backlog, or `/maintain`)
- Child with newer template → offer `/upgrade` (gap plan; or `/upgrade --deep` to reformat beyond file gaps)

Begin now.
