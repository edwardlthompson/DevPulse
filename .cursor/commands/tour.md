# Tour (10 minutes)

Walk a first-time human (or any coding agent) through the repo. Do not dump whole files. After each stop, wait for a question or say “ready” before the next.

1. Read @docs/START_HERE.md — name the repo mode (Bootstrap vs Reference) and point at `docs/AGENT_PORTABILITY.md` (one sentence: edit `AGENTS.md`, then re-sync adapters). Point at @docs/help/GLOSSARY.md for [**Sacred**](../../docs/help/GLOSSARY.md) / [**Canon**](../../docs/help/GLOSSARY.md) / [**AGENT**](../../docs/help/GLOSSARY.md) / [**HUMAN**](../../docs/help/GLOSSARY.md) / [**ADB**](../../docs/help/GLOSSARY.md) / [**AUTO**](../../docs/help/GLOSSARY.md) labels / 🔲 status.
2. Read @docs/BEST_PRACTICES.md — cover only LICENSE, SECURITY.md, and BUILD_PLAN labels (What / Why / How). Skip the rest unless asked.
3. Open the active Golden Path README: read `.cursor/stack-selection.json` `stack` and use `examples/{stack}/README.md`. If the stack is `multi`, `none`, or the file is missing, use `examples/web/README.md`.
4. Read @docs/FIRST_30_DAYS.md **Week 1 only**. Offer `/coach` (or “read `docs/help/TOUR.md`”) for later sessions.
5. Run the local harness and interpret the first failure (do not dump the whole log):

```bash
python3 scripts/agent-run.py tour-verify

```

`tour-verify` runs `verify.sh --quick`. On pass, say Week 1 gates are green. On fail, quote the `Tour verify: first failure` block and point at `/fix` (or `/debug` if strikes ≥ 3).

If the user’s tool has no slash commands, the same walk is `docs/help/TOUR.md`.

Begin now.
