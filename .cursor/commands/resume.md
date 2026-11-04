# Cloud → PC handoff after a Cloud Agent session

Run the digest first (fetch + sync Dependabot/Release Please into BUILD_PLAN + list open `cursor/*` PRs + LOCAL scope overlap):

```bash
python3 scripts/agent-run.py resume-handoff

```

Then:

1. If the digest says local `main` is behind and the tree is clean, `git pull`.
2. If open Cloud (`cursor/*`) PRs are listed, merge or close them before a new `[AGENT][LOCAL]` row when the digest reports scope overlap.
3. Continue from the **Next BUILD_PLAN row** named in the digest (prefer 🔲 `[AGENT][LOCAL]` — never claim `[CLOUD]` on This Computer).
4. Do **not** expect `.cursor-session-state.json` / `/compact` to travel from Cloud — it is gitignored and machine-local.

Begin now.
