# First 30 days

> Playbook after `/bootstrap`. Status: 🔲 open · ✅ done · ❌ blocked.
> Walk this with `/coach` or `/tour` (`docs/help/TOUR.md` in other IDEs). Industry **why** lives in [`BEST_PRACTICES.md`](BEST_PRACTICES.md).

<!-- bootstrap-project-card -->
**Product:** agent-project-bootstrap
**Purpose:** GitHub Template for FOSS coding-agent projects
**Stack:** multi
<!-- /bootstrap-project-card -->

## Week 1 — Make it yours

- 🔲 Run `scripts/init-project.sh` (or `.ps1`) if you have not already
- 🔲 Read `docs/START_HERE.md`, `docs/CURSOR_MODES.md`, and `docs/BEST_PRACTICES.md` (first four conventions)
- 🔲 Copy `.env.example` → `.env` (never commit `.env`)
- 🔲 `pip install pre-commit && pre-commit install --hook-type commit-msg`
- 🔲 `bash scripts/verify.sh` green locally
- 🔲 Fill `branding/product.json` if this is a product (then regenerate the README)

## Week 2 — Security and GitHub

- 🔲 `scripts/setup-github-repo.sh` (Dependabot alerts, private reporting, branch protection)
- 🔲 Confirm **CI**, **Security Scan**, **CodeQL**, and **Template Upgrade Simulation (Windows)** on the first push (`check-github-ci.sh --wait 300`; `setup-github-repo.sh` also requires **Repo Hygiene** and **Feature Gate**)
- 🔲 Paste `docs/GITHUB_ABOUT.md` into GitHub → Settings → General → About (description + topics)
- 🔲 If you have a donation URL, confirm `.github/FUNDING.yml` exists
- 🔲 Walk through `docs/help/DONATIONS.md` (GitHub Sponsors + international methods; Android donate under Settings → About only)
- 🔲 Review `SECURITY.md` reporting channel

## Week 3 — Golden Path and first feature

- 🔲 Run the active stack’s Golden Path tests (`examples/{stack}/` README)
- 🔲 Copy `docs/features/_template.md` → `docs/features/{name}.md` for the first real feature
- 🔲 Add tests with the change (or write the fallback command in the spec)
- 🔲 `/build` Sequential then Parallel; do not start feature B until A passes gates

## Week 4 — Operate like a maintained project

- 🔲 **Green CI before `/allideas`** — required checks on `main` must be green (`project-health` / `check-github-ci`) before dumping backlog ideas onto BUILD_PLAN
- 🔲 Update `AGENT_MEMORY.md` at this milestone only
- 🔲 Append one `DECISION_LOG.md` entry for the first architecture choice
- 🔲 `/maintain` or `/triage` + `/update-deps` once (local bumps; GitHub Dependabot leftover + Scorecard awareness)
- 🔲 Optional: `just local-compute` / `just linux-dev`; follow `docs/LINUX_DEV.md` on Linux (direnv, caches, inotify)
- 🔲 Optional Android on Linux: one-pager [`LINUX_DEV.md` — Apt-locked hosts + Android on Linux](LINUX_DEV.md#apt-locked-hosts-user-local-jdk--android-sdk) (SDK under `~/Android/Sdk`, udev/RSA once, then `/emulator`)
- 🔲 Optional: Ollama (`docs/LOCAL_MODELS.md`); `/emulator` if you have an Android SDK
- 🔲 Bookmark `docs/help/BATCH_COMMANDS.md` (`/verify` before every PR)

## Next recommended action

If you are unsure, type **`/coach`** (or ask any agent to read this file and `docs/BEST_PRACTICES.md`). It reads BUILD_PLAN + AGENT_MEMORY and explains the industry reason for the next row. First session: `/tour` or `docs/help/TOUR.md`. For a ranked backlog of possible next features: `/ideas` or `docs/help/IDEAS.md`. For a complete dump to fill BUILD_PLAN: `/allideas` or `docs/help/ALLIDEAS.md`.
