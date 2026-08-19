# Glossary

Short definitions for first-timers. Why the files exist: [`BEST_PRACTICES.md`](../BEST_PRACTICES.md). Ten-minute walk: [`TOUR.md`](TOUR.md).

## Repo words

| Term | Meaning |
|------|---------|
| **Sacred** | [`AGENTS.md`](../../AGENTS.md) is the only project law. Edit it, then re-sync adapters. Do not invent a second source of truth. |
| **Canon** | The checked-in contract (`AGENTS.md`, `BUILD_PLAN.md`, gates). Adapters are thin pointers, not extra rules. |
| **Bootstrap vs Reference** | New child repo vs using this template as rules-only. See [`START_HERE.md`](../START_HERE.md). |
| **Golden Path** | The runnable `examples/{stack}/` slice you copy for the next feature. |
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

Status uses those emoji. Do not convert them to GitHub `- [ ]` checkboxes. Legend: [`BUILD_PLAN.md`](../../BUILD_PLAN.md).

## Gates

| Term | Meaning |
|------|---------|
| **Gate** | A local check that matches CI (`bash scripts/verify.sh`). |
| **Feature gate** | Stack tests for the active Golden Path. |
| **3-strike** | After three failed auto-fixes, stop and ask a human. |

## Destructive ops

`git push`, production deploys, and history rewrites need a human or an explicit `/push` / `/ship`. See [`.cursor/rules/destructive-ops.mdc`](../../.cursor/rules/destructive-ops.mdc).
