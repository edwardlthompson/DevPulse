# Glossary

Short definitions for first-timers. Why the files exist: [`BEST_PRACTICES.md`](../BEST_PRACTICES.md). Ten-minute walk: [`TOUR.md`](TOUR.md).

## Repo words

| Term | Meaning |
|------|---------|
| **Sacred** | [`AGENTS.md`](../../AGENTS.md) is agent routing law (edit, then re-sync adapters). [`AGENT.md`](../../AGENT.md.example) is the original product brief — never overwritten by init. |
| **AGENT.md vs AGENTS.md** | `AGENT.md` = verbatim human brief (Sacred). `AGENTS.md` = stamped routing. Recovery: BUILD_PLAN **Product (do not drift)**. |
| **Canon** | The checked-in contract (`AGENTS.md`, `BUILD_PLAN.md`, `BUILD_PLAN_TEMPLATE.md`, gates). Adapters are thin pointers, not extra rules. |
| **Bootstrap vs Reference** | New child repo vs using this template as rules-only. See [`START_HERE.md`](../START_HERE.md). |
| **Golden Path** | The runnable `examples/{stack}/` slice you copy for the next feature. |
| **`/ideas` vs `/allideas`** | Ranked 5–8 next features vs a complete in-scope dump to fill BUILD_PLAN. Neither implements until you name numbers or say `board`. |
| **`/upgrade` (child)** | Compare this app to the parent template and **plan** updates. Does not overwrite your work until you name item numbers. On the template repo it runs a simulation instead. |
| **Adapters** | Generated files (`CLAUDE.md`, `.cursor/rules/main.mdc`, and others) that point at `AGENTS.md`. |
## BUILD_PLAN labels

| Term | Meaning |
|------|---------|
| **AGENT** | The coding agent may do this row. |
| **HUMAN** | Needs a person (credentials, a device, `git push`, or production). |
| **ADB** | Needs an Android device or emulator. |
| **AUTO** | A script or CI can do this. |
| **🔲** | Open |
| **✅** | Done |
| **❌** | Blocked |
Status uses those emoji. Do not convert them to GitHub `- [ ]` checkboxes. Legend: [`BUILD_PLAN.md`](../../BUILD_PLAN.md). Child model: [`BUILD_PLAN_TEMPLATE.md`](../../BUILD_PLAN_TEMPLATE.md). Both files start with a **Remaining** tally (AGENT / AUTO / HUMAN / ADB).

## Gates

| Term | Meaning |
|------|---------|
| **Gate** | A local check that matches CI (`bash scripts/verify.sh`). |
| **Feature gate** | Stack tests for the active Golden Path. `/build` uses `--scope auto` (dirty stacks); `/gates` is full multi-stack. |
| **Sprint smoke** | After a sprint is all ✅, `smoke-sprint --require` re-checks every row (no crashes; startup + load order) before the next sprint. |
| **3-strike** | After three failed auto-fixes, stop and ask a human. `/debug` reads `strikes` in `.cursor/agent-progress.json`. |
## Android test words

| Term | Meaning |
|------|---------|
| **Espresso** | AndroidX UI test library used by instrumented tests. On Android 16 (API 36) pin **3.7.0+** — older Espresso calls removed `InputManager.getInstance`. See KB-022. |
| **Instrumented** | Tests that run on a device or emulator (`connectedDebugAndroidTest`), not JVM unit tests. |
| **AVD** | Android Virtual Device — the emulator image/profile CI or `/emulator` boots for instrumented runs. |
## Destructive ops

`git push`, production deploys, and history rewrites need a human or an explicit `/push` / `/ship`. See [`.cursor/rules/destructive-ops.mdc`](../../.cursor/rules/destructive-ops.mdc).
