# Decision Log

> Chronological register of major technical trade-offs, accepted architectures, and rejected alternatives.
> **Treat past entries as immutable history; append only.**

## Format

```markdown
### YYYY-MM-DD — [Title]
- **Status:** Accepted | Rejected | Superseded
- **Context:** ...
- **Decision:** ...
- **Alternatives considered:** ...
- **Consequences:** ...

```

## Entries

### 2026-08-18 — Ship v0.21.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M38+M39. Pre-release green on `f54927e`; feat `8eab392` then `df322af` after `rp_merge_status` import failed once `PYTHONPATH` was stripped.
- **Decision:** Push feat + cwd-relative import fix; wait Ubuntu + Windows upgrade-sim; admin-merge Release Please #69 to **v0.21.0**. Fold leftover Unreleased onto the PR as comments. Archive M39 and empty Unreleased after merge (fold does not rewrite the RP branch). Codex skipped (no key/CLI).
- **Alternatives considered:** Leave leftover Unreleased under `[0.21.0]` (rejected: next `/ship` fails first+empty gates). `pull_request_target` for RP checks (rejected).
- **Consequences:** Template at 0.21.0. Next `/ship` should empty Unreleased in the prepare commit so RP does not carry bullets. SBOM attaches via `release` published workflow.

### 2026-08-17 — M39 /ideas Windows PATH + ship hygiene
- **Status:** Accepted
- **Context:** Fifth `/ideas` pass. Git Bash still missed `gh`; inherited `PYTHONPATH=scripts/lib` broke validate-bootstrap; leftover Unreleased blocked RP merge; Q&A REST create often SKIP'd.
- **Decision:** Shared `resolve-tools.sh` prepends Windows tool dirs and unsets `PYTHONPATH`. `agent-run` uses `child_env()`. Fold Unreleased onto the RP PR comment then empty. No `environment:` on CI/Security/CodeQL. GraphQL list + REST/GraphQL create for Q&A with a one-line HUMAN fallback. Archive M38; name Windows check on child Sprint 0 AUTO.
- **Alternatives considered:** Document PATH-only (rejected: every `gh` script still fails). `pull_request_target` for RP checks (rejected: untrusted checkout). Fail setup when Q&A API 422s (rejected: Settings fallback is enough).
- **Consequences:** Source `resolve-tools.sh` before `command -v gh`. Never export `PYTHONPATH=scripts/lib`. `/ship` comments leftover notes then empties Unreleased locally.

### 2026-08-17 — M38 /ideas ship-hardening
- **Status:** Accepted
- **Context:** Fourth `/ideas` pass after v0.20.0. Live `main` still required only five checks; Windows `jq` and leftover Unreleased still bit `/ship`.
- **Decision:** Fail `pre-release-gate` on missing Windows upgrade-sim; apply that check via `setup-github-repo`. Python-only `TEMPLATE_INDEX`. Skip RP wait on `ACTION_REQUIRED`. Split allowlisted `scripts/lib` to ≤150. Empty-Unreleased gate before RP merge. Archive Coach/M37/M36. Token on upgrade-sim jobs.
- **Alternatives considered:** Keep jq + CR strip (rejected: two paths). Leave lib allowlist (rejected: token-economy lie).
- **Consequences:** `/ship` now fails until protection matches the script. New `scripts/lib` files stay ≤150 with an empty allowlist.

### 2026-08-17 — Ship v0.20.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after three `/ideas` implement-all rounds. First pre-release gate was green on `01e21fc`; feat `14811be` then failed upgrade-sim (stamped purpose, pruned Android link, Windows `jq` CRLF).
- **Decision:** Keep portable-purpose assert on the template repo only; ignore doc links into pruned `modules/`/`examples/`; strip CR from `jq` paths in `validate-template-index`. Push fixes, wait for Ubuntu + Windows upgrade-sim, admin-merge Release Please #68 to **v0.20.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Skip full validate after prune (rejected: hides real child-repo doc breaks). Drop Windows upgrade-sim from required checks (rejected: that was the point of the ideas pass).
- **Consequences:** Template at 0.20.0; `/ship` must treat Windows `jq.exe` CRLF and post-prune links as first-class gates. SBOM attaches via `release` published workflow.

### 2026-08-17 — Implement /ideas backlog (required Windows check, coach twin)
- **Status:** Accepted
- **Context:** Third `/ideas` pass. Windows upgrade-sim existed but `/ship` and branch protection did not name it.
- **Decision:** Add the Windows job to required checks; ship `docs/help/COACH.md`; health notes dirty Unreleased; skip weekly AUTO rows after a green weekly-health run; Codespaces `verify.sh`; citation `date-released`; pin setup-python SHA; split `build_sprint` and gate new `scripts/lib` files at 150 lines (allowlist pre-existing oversize modules).
- **Alternatives considered:** Split every lib file in one pass (rejected: too much risk for `/build`). Fail file-limits on allowlisted modules (rejected: would block unrelated work).
- **Consequences:** `/ship` waits on Windows upgrade-sim once the job has run on HEAD. New `scripts/lib` modules must stay ≤150 lines.

### 2026-08-17 — Implement /ideas backlog (health, Windows CI, links)
- **Status:** Accepted
- **Context:** Second `/ideas` pass after the first eight items shipped locally. Health still pointed at child Sprint 0 on this template.
- **Decision:** Auto lane uses maintainer board when `bootstrap.config.json` still describes this template. Skip `pwsh` when missing; add `windows-latest` upgrade-sim. Split gate hints to JSON. Extend doc-link gate to root `*.md` + pre-commit. Best-effort Q&A category after Discussions enable.
- **Alternatives considered:** Keep auto=child on the template (rejected: wrong next step). Fail upgrade-sim without `pwsh` (rejected: bash path already proved). Require Q&A API success (rejected: Settings fallback).
- **Consequences:** `/coach` and `/ideas` on this repo name Ongoing Maintenance, not init-project. Child repos with their own purpose stay on the child playbook.

### 2026-08-17 — Implement /ideas backlog (8 items)
- **Status:** Accepted
- **Context:** `/ideas` ranked eight in-scope template items after v0.19.0. User asked to implement all.
- **Decision:** Ship Windows pyrepl env, CITATION.cff version sync, glossary, portable stamp copy, verify.sh hints, opt-in welcome issue, docs link gate, and Discussions enablement from `setup-github-repo`.
- **Alternatives considered:** Cursor-only purpose (rejected: portability). Welcome issue on by default (rejected: matches other post hooks). Fail setup if Discussions API cannot toggle (rejected: [HUMAN] Settings fallback).
- **Consequences:** `post_welcome_issue` stays false; `/ship` regress should finish on This Computer; glossary is REQUIRED.

### 2026-08-16 — Ship v0.19.0 (/ship)
- **Status:** Accepted
- **Context:** Coach layer (`2f77fb9`) plus portable first-run polish were unpushed; first pre-release CI wait failed because HEAD had no Actions run.
- **Decision:** Push feat commits, wait for CI/Security/CodeQL, re-run `pre-release-gate`, merge Release Please #67 to **v0.19.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Hold for Codex (rejected: skip is allowed). Invent per-tool rulebooks (rejected: AGENTS.md SoT).
- **Consequences:** Template at 0.19.0; SBOM attaches via `release` published workflow; batch commands are 24 atomic + 5 super.

### 2026-08-16 — Portable first-run (any agent IDE)
- **Status:** Accepted
- **Context:** First-time users needed a scripted tour and readable gate failures; the template was Cursor-weighted while Windsurf, Antigravity, and others already read `AGENTS.md`.
- **Decision:** Keep `AGENTS.md` Sacred. Generate thin pointers (`GEMINI.md` pointer-only, Windsurf, Cline, Aider, Continue). Ship `/tour` plus `docs/help/TOUR.md`. Add adapter drift gate, VS Code tasks, SUPPORT.md, CITATION.cff, good-first-issue, live badges, Codespaces link.
- **Alternatives considered:** Duplicate full rules into `.windsurfrules` / `.agents/agents.md` (rejected: drift and a second SoT). Register `/why` (rejected: `/coach` synonym only).
- **Consequences:** Edit `AGENTS.md` then `--sync-adapters`. Never put real rules in `GEMINI.md`.

### 2026-08-16 — Template Excellence / Coach Layer
- **Status:** Accepted
- **Context:** The template already had gates, memory, and Golden Paths; new users still got files without the industry *why*.
- **Decision:** Add `docs/BEST_PRACTICES.md` + `docs/FIRST_30_DAYS.md`, `/coach` + Welcome Tour, init what/why summary, optional FUNDING.yml/topics, and optional `justfile`s. Do not require `just` in CI. Do not register a second `/why` command.
- **Alternatives considered:** Fold the 30-day list into BEST_PRACTICES (rejected: token bloat); husky instead of just (rejected: pre-commit already covers hooks).
- **Consequences:** Batch-command count is 23 atomic + 5 super. `/bootstrap` ends with a tour. Child product READMEs gain For humans / For agents sections.

### 2026-08-16 — M37 gap close (verify, env, hooks)
- **Status:** Accepted
- **Context:** Checklist audit found core governance/CI present; remaining gaps were docker preflight, unimplemented post-hook flags, no root verify command, no env schema, no commit-msg enforcement, no Dockerfile, and no `.agent/memory` indexes.
- **Decision:** Extend the existing engine. `scripts/verify.sh` is the harness. Env validation is stack-agnostic JSON schema. Skills/memory under `.agent/` are indexes to `.cursor/skills/` and `DECISION_LOG.md` / `KNOWLEDGE_BASE.md`. Post install/test/git-init stay opt-in. Conventional Commits via pre-commit `commit-msg`, not Node-only commitlint.
- **Alternatives considered:** Duplicate skills into `.github/skills/` (rejected); husky + lint-staged (rejected: pre-commit already covers all stacks); auto-commit after init (rejected: destructive-ops).
- **Consequences:** `validate-bootstrap` requires env schema, Dockerfile, `.agent/` indexes, and `verify.sh`. Feature-gate fails if `.env.example` drifts from `env.schema.json`.

### 2026-08-16 — M36 bootstrap standards (AGENTS.md + lifecycle)
- **Status:** Accepted
- **Context:** Audit asked for a generator-style AGENTS.md engine, SDD stubs, security-by-default, manifest, and pre/post hooks. The repo already shipped SECURITY.md, CONTRIBUTING.md, CI, Dependabot, issue/PR templates, and `init-project`.
- **Decision:** Keep the GitHub Template + `init-project` model. Expand `AGENTS.md` as the canonical spec; generate thin Cursor/Claude/Copilot adapters; add `docs/spec.md` / `docs/plan.md`; add `bootstrap.config.json` plus preflight/post hooks and `PROJECT_CHECKLIST.md`. MIT remains default; Apache-2.0 is an init option for child repos. Do not auto-commit or auto-install deps.
- **Alternatives considered:** Separate yeoman-style generator CLI (rejected: would fork the template model); GitHub `- [ ]` checkboxes on the new checklist (rejected: repo-wide 🔲/✅/❌ convention).
- **Consequences:** `validate-bootstrap` requires SDD stubs, adapters, and engine unit tests. Child `AGENTS.md` stays Sacred on upgrade; adapters are Canon via `--sync-adapters`.

### 2026-08-16 — Ship v0.18.3 (/ship)
- **Status:** Accepted
- **Context:** Dependabot #64 Compose BOM bump on main; RP #66 already open
- **Decision:** `/ship` autofix + pre-release gate, then merge Release Please #66 to **v0.18.3**. Codex skipped (no key/CLI).
- **Alternatives considered:** Hold BOM for a later patch (rejected: CI including instrumented Android already green)
- **Consequences:** Template at 0.18.3; SBOM attaches via `release` published workflow

### 2026-08-16 — Ship v0.18.2 (/push)
- **Status:** Accepted
- **Context:** M35 HUMAN Scorecard/Dependabot/radar already on `main` @ `23254e8`; CI/Security/CodeQL green; RP #63 open
- **Decision:** Merge Release Please #63 to **v0.18.2** after local maintainer + pre-release gates (no extra prepare commit)
- **Alternatives considered:** Wait for RP auto-merge (blocked: no checks on release-please branch)
- **Consequences:** Template at 0.18.2; next quarterly radar 2026-11-15; Dependabot #64 left open

### 2026-08-15 — M35 Scorecard SARIF + Dependabot + radar
- **Status:** Accepted
- **Context:** Open HUMAN items after v0.18.1: Scorecard PinnedDependencies / TokenPermissions / VulnerabilitiesID; Dependabot #58–#61; quarterly radar (last report 2026-06-30)
- **Decision:** Job-scope write tokens (`permissions: read-all` at workflow level). Keep `@vX.Y.Z` for GitHub-owned actions. Treat VulnerabilitiesID as stale (hono/nanoid/postcss already patched in 0.18.0). Merge green Dependabot PRs after rebase; rebase #61 (stale web lockfile). Radar max new score is 6 — no BUILD_PLAN row.
- **Alternatives considered:** SHA-pin every `actions/*` (rejected: conflicts with `validate-workflow-actions` + existing policy); add Design Mode / Canvas now (rejected: score 6, below ≥9 suggest threshold)
- **Consequences:** TokenPermissions should clear on next Scorecard run; PinnedDependencies remain accepted; next quarterly radar due 2026-11-15

### 2026-08-15 — Ship v0.18.1 (/push)
- **Status:** Accepted
- **Context:** M35 Windows Store `python3` hang + About-gate restore ready; first `PY="py -3"` broke `"$PY"` in Dependabot count
- **Decision:** Resolve `PY` to `sys.executable` from `py -3`; merge Release Please #62 to **v0.18.1** after CI green on `b4fca9c`
- **Alternatives considered:** Leave `PY="py -3"` and unquote all callers (rejected: `"$PY"` is the safe pattern); wait for RP auto-merge (blocked: no checks on release-please branch)
- **Consequences:** Template at 0.18.1; Scorecard SARIF and Dependabot PRs #58–#61 stay HUMAN

### 2026-08-15 — Audit M35 Windows Python resolver
- **Status:** Accepted
- **Context:** `/ship` autofix hung because `command -v python3` resolved to the Microsoft Store stub under `WindowsApps`
- **Decision:** Add `scripts/lib/resolve-python.sh` (skip Store stub; prefer `py -3`) and source it from gate/autofix scripts; restore About slice from `git checkout HEAD` if the verify-about backup is missing
- **Alternatives considered:** Document-only workaround (rejected: every Windows gate still hangs); require a `python3` symlink in PATH (rejected: Store alias still wins)
- **Consequences:** Local gates on This Computer no longer stall on the stub; HUMAN still owns Scorecard SARIF and Dependabot PRs #58–#61

### 2026-08-15 — Ship v0.18.0 (/ship)
- **Status:** Accepted
- **Context:** M34 prior-art steals ready; pre-release gate blocked on High `extract-zip` (no upstream patch) via LHCI → puppeteer-core
- **Decision:** Override `@puppeteer/browsers` >=3.2.0 (uses `modern-tar`); lock optional peer `proxy-agent` >=8.0.2 so CI `npm ci` matches; bump `hono`/`postcss`/`nanoid`; merge Release Please #56 to **v0.18.0**
- **Alternatives considered:** Dismiss extract-zip as dev-only (rejected: gate requires zero High); vendor a patched fork (rejected: no patch exists)
- **Consequences:** Template at 0.18.0; honesty labels + scratchpad/handoff ship; Codex skipped (no key/CLI)

### 2026-08-14 — Prior-art thin steals (M34)
- **Status:** Accepted
- **Context:** Compared CopperDogma, Barony, Sciensoft, and wshobson/agents against this Cursor-first template. Need mechanisms without vendoring those trees or an 80k playbook.
- **Decision:** Ship honesty labels, parallel handoff stub, Canon/Mixed/Sacred upgrade column, OWASP LLM walk, scratchpad reset, optional marketplace pointer, and bootstrap-doctor alias. Number as **M34** (plan draft said M30; that sprint is already archived).
- **Alternatives considered:** Vendor Barony/`baron` (rejected: second product + PyPI dep); install wshobson catalog by default (rejected: token bloat); Sciensoft one-file playbook (rejected: 300/150 caps).
- **Consequences:** Hooks stay fail-open and labeled; child `AGENTS.md` / init prompt stay Sacred; no new CI scanner or marketplace install on the FOSS default path.

### 2026-08-12 — Ship v0.17.0 branding kit (/ship)
- **Status:** Accepted
- **Context:** Child repos need replaceable logos/colors and pitch-quality READMEs without overwriting the template README
- **Decision:** Ship `branding/` pack + mode-gated `generate-project-readme.py` (`template` preview only; `product` writes root README); extend token sync for official-colors and asset distribution; merge Release Please #55 to **v0.17.0**
- **Alternatives considered:** Generate logos from tokens only (rejected: humans replace art files); always overwrite root README (rejected: clobbers template guide)
- **Consequences:** Sprint 0 fills `product.json` then generate; upstream keeps `mode: template`; store PNGs remain human/ADB exports

_Seed template ADR: `docs/adr/0000-template-baseline.md`. Child repos use `docs/adr/0001-core-architecture.md`._

### 2026-08-10 — Ship v0.16.0 (/ship)
- **Status:** Accepted
- **Context:** Need third-party review + broader autofix before release; `/ship` should stay one command
- **Decision:** Codex read-only reviewer (opt-in CI + `/codex-review`) feeds `CODE_REVIEW.md` → Cursor `/fix`; expand `/prerelease` with multi-stack autofix; merge Release Please #51 to **v0.16.0**
- **Alternatives considered:** Codex writes patches in CI (rejected: destructive-ops / FOSS spend control); chain Codex into every `/maintain` (rejected: API cost)
- **Consequences:** `/ship` runs autofix + optional Codex + hard gate; enable Codex CI by copying workflow example + `OPENAI_API_KEY`

### 2026-08-01 — Ship v0.15.2 (/ship)
- **Status:** Accepted
- **Context:** Plan Mode left risks as open questions; Dependabot High blocked pre-release (js-yaml, then postcss)
- **Decision:** Require Issue→Resolution Critique in always-applied rules + `/plan`; override patched npm transitive CVEs; merge Release Please #50 to **v0.15.2**
- **Alternatives considered:** Soft "list risks" Critique (rejected: humans still had to chase resolutions); defer brace-expansion/postcss (rejected: pre-release gate requires zero Critical/High)
- **Consequences:** Agents must bake mitigations into plan todos; template at 0.15.2 with SBOM release assets

### 2026-07-22 — Ship v0.15.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M33 + local-first compute; first CI failed on duplicate `## [Unreleased]`; web tests failed on Node 25+ localStorage stub
- **Decision:** Polyfill Storage in vitest setup (KB-011); collapse stale Unreleased; merge Release Please #37 to **v0.15.0**
- **Alternatives considered:** `--no-webstorage` only (rejected: may break older Node CI); leave duplicate Unreleased (rejected: gate hard-fail)
- **Consequences:** Template at 0.15.0 with Cursor worktrees/permissions/skills/plugin pack and local-first parallelism

### 2026-07-21 — Local-first compute on This Computer
- **Status:** Accepted
- **Context:** Agents defaulted toward serial work or Cloud handoff even when the desktop has many cores
- **Decision:** Ship `local-compute.mdc` + sessionStart CPU reminder; parallelize independent `validate-bootstrap` checks via `run_checks_parallel.py` (`BOOTSTRAP_CHECK_JOBS`); pytest-xdist `-n auto`; Gradle `--parallel`; document `/scope` + worktrees/`/best-of-n` as the local default over Cloud Agents
- **Alternatives considered:** Always Cloud Agents for parallelism (rejected: wastes local hardware and costs credits); unbounded bash `&` in validate-bootstrap (rejected: harder error aggregation on Windows)
- **Consequences:** Quick bootstrap checks use all cores (e.g. jobs=CPU count); agents are steered to concurrent Task/worktrees when local

### 2026-07-21 — Cursor 3.9–3.11 FOSS integration (M33)
- **Status:** Accepted
- **Context:** Cursor added native worktrees setup, Auto-review `permissions.json`, Skills direction, CLI/GHA, side chats, Design Mode, cloud conversation hooks, Automations, and plugin packaging; registry lagged at 2026-06-30
- **Decision:** Ship FOSS live `worktrees.json` + fail-soft OS setup, committed `permissions.json` (dual layer with hooks), four new skills + checker atomic update, CLI workflow under `.github/workflow-examples/` (never auto-run), plugin via pack-to-`dist/cursor-plugin` (no repo-root symlink); keep commercial as examples (cloud hooks, Automations recipes, Bugbot Autofix map)
- **Alternatives considered:** Custom plugin paths into `.cursor/` (rejected: discovery risk); whole-repo plugin symlink (rejected: double-load); `.example.yml` under `workflows/` (rejected: GHA may load it); weaken shell hook for Auto-review (rejected: hooks stay hard FOSS enforcement)
- **Consequences:** `check-cursor-integrations` requires seven skills + worktrees/permissions; `/best-of-n` documented beside parallel-lock worktrees; Cloud Agents still ignore Run Modes

### 2026-07-12 — Pre-release gate Dependabot counter + FOSS MCP check
- **Status:** Accepted
- **Context:** `/push` pre-release `--strict` failed: Dependabot alerts API used unsupported `page=` form; FOSS integrations check failed whenever gitignored `.cursor/mcp.json` existed locally
- **Decision:** Count alerts via `gh api --paginate` query string; treat live `mcp.json` as OK unless `git ls-files` shows it tracked; multi-stack `--strict` skips missing optional toolchains
- **Alternatives considered:** Require `security_events` refresh always (rejected: false failures blocked release); ban local MCP (rejected: contradicts CURSOR_INTEGRATIONS activation)
- **Consequences:** Maintainer gates pass with local MCP enabled; Release Please #36 published v0.14.1

### 2026-07-12 — Dependabot automerge CI gap (M32)
- **Status:** Accepted
- **Context:** Merges via `GITHUB_TOKEN` (`app/github-actions`) do not start `push` workflows; `main` tip after Dependabot merges had zero CI runs; weekly health failed waiting for missing runs
- **Decision:** Prefer optional `AUTOMERGE_TOKEN` PAT for Dependabot/Release Please merge; add `workflow_dispatch` to CodeQL + Security Scan; `check-github-ci.sh --dispatch-if-missing` (weekly health uses it with `actions: write`); prefer Git Bash in `agent-run.py` on Windows
- **Alternatives considered:** Require PAT only (rejected: blocks FOSS template without secrets); SHA-pin all actions for Scorecard (deferred: conflicts with documented `@vX.Y.Z` policy)
- **Consequences:** Weekly health can self-heal missing runs; post-merge CI still needs HUMAN required-status-checks + optional PAT for true push triggers

### 2026-07-02 — Quiet agent shell (hooks Python + agent-run)
- **Status:** Accepted
- **Context:** Cursor Agent shell execution opened `.sh` hook and script tabs, stealing editor focus while users typed
- **Decision:** Migrate hooks to Python; add `scripts/agent-run.py` for agent gate invocations; ship `.vscode/settings.json` anti-reveal defaults; document KB-010
- **Alternatives considered:** Disable hooks globally (rejected: loses destructive-op guard); rewrite all scripts to PowerShell (rejected: scope); `pythonw.exe` for hooks (rejected: breaks stdout JSON)
- **Consequences:** Agent-facing commands no longer contain `.sh` paths; underlying bash scripts unchanged for CI/humans

### 2026-07-01 — Cursor hook smoke isolation (M31)
- **Status:** Accepted
- **Context:** M31 audit found `check-cursor-hooks.sh --smoke` false-pass when `.cursor-session-state.json` already listed `git push` in `destructive_ops_approved`
- **Decision:** Smoke test clears session approvals before deny assertion; validate hook scripts require shebang on line 1
- **Alternatives considered:** Ignore local session state in smoke (rejected: hides real deny-path bugs); require empty session file (rejected: breaks dev workflow)
- **Consequences:** `--smoke` is deterministic in CI and locally; invalid hook scripts fail validate-bootstrap early

### 2026-06-30 — Cursor hooks as enforcement layer (M30)
- **Status:** Accepted
- **Context:** M27 rejected `beforeSubmitPrompt` hooks; rules alone cannot block destructive shell commands at runtime
- **Decision:** Ship FOSS-safe project hooks (`beforeShellExecution`, `afterFileEdit`, `subagentStart`, `sessionStart`, `beforeMCPExecution`); fail-open guards; session `destructive_ops_approved` for `/push`/`/ship`; opt-out via `<!-- cursor-hooks: off -->`
- **Alternatives considered:** Prompt-rewrite hooks (rejected per M27); broad shell blocklists (rejected: blocks legitimate agent work)
- **Consequences:** `check-cursor-hooks.sh --smoke` in validate-bootstrap; complements `destructive-ops.mdc` without token bloat

### 2026-06-20 — Repo-wide checklist status markers
- **Status:** Accepted
- **Context:** BUILD_PLAN and scattered checklists used mixed ⬜ / `- [ ]` / ✅ formats; inconsistent in Markdown Preview vs source
- **Decision:** Standardize on 🔲 open · ✅ done · ❌ blocked emoji markers repo-wide; document in `BUILD_PLAN.md` legend and agent read order
- **Alternatives considered:** GitHub `- [ ]` task lists (rejected: poor Preview readability and agent parsing); keep ⬜ white square (rejected: visually similar to ✅ in some fonts)
- **Consequences:** All new checklist rows use emoji; `agent-progress.sh` accepts legacy ⬜ for child repos during transition

### 2026-06-18 — Release automation hardening (M29)
- **Status:** Accepted
- **Context:** v0.11.0 release lacked SBOM assets (GITHUB_TOKEN cannot chain `release` → `release.yml`); Release Please skipped `extra-files`; `health-check.yml` registered as path name caused 0-job push failures
- **Decision:** `release-please.yml` runs `sync-template-version.sh` on release PR branches and dispatches `release.yml` on `release_created`; rename workflow to `weekly-health-check.yml`; fix sync script for Windows Git Bash
- **Alternatives considered:** PAT with workflow scope for release chaining (rejected: secrets management); manual SBOM backfill only (rejected: repeated human step each release)
- **Consequences:** Release Please needs `actions: write`; future releases should ship SBOM assets without manual dispatch

### 2026-06-17 — Batch instruction templates (M27)
- **Status:** Accepted
- **Context:** Agents and child-repo owners needed repeatable shortcuts for bootstrap, verify, build, ship, and maintenance workflows without re-pasting long prompts
- **Decision:** Ship 25 slash commands in `.cursor/commands/` (20 atomic + 5 super), bare-word expansion via `batch-commands.mdc`, human cheat sheet at `docs/help/BATCH_COMMANDS.md`, registry at `docs/BATCH_COMMANDS.md`; `/push` and `/ship` grant explicit push approval
- **Alternatives considered:** `beforeSubmitPrompt` hook for bare words (rejected: Cursor API cannot rewrite prompts); single mega-doc for humans and agents (rejected: overwhelms first-time users)
- **Consequences:** `alwaysApply` rule adds ~25 lines per session; `check-batch-commands.sh` prevents registry drift; child repos cherry-pick via `UPGRADING_FROM_TEMPLATE.md`

### 2026-06-30 — Autonomous /build with grouped human section
- **Status:** Accepted
- **Context:** `/build` halted on HUMAN/ADB rows; humans needed a single review block after automation; child repos need scripted attempts before manual follow-up
- **Decision:** Add `build-sprint-status.sh`, `attempt-build-plan-row.sh`, and `HUMAN_BACKLOG.md` (failure-only); restructure BUILD_PLAN with `#### Human & device (after automation)`; AGENT/AUTO runs first, then automation attempts on grouped human rows
- **Alternatives considered:** Skip human rows entirely during /build (rejected: loses automation catalog value); keep human rows interleaved in Sequential (rejected: hard to review after automation)
- **Consequences:** Child repos must place HUMAN/ADB rows in the grouped section; `<!-- no-auto-approve -->` disables autonomous ADR ack

### 2026-06-13 — @lhci/cli npm overrides for transitive CVEs
- **Status:** Accepted
- **Context:** Lighthouse CI (`@lhci/cli`) bundles transitive dependencies (`tmp`, `uuid`) with known CVEs; no patched `@lhci/cli` release available at triage time
- **Decision:** Add npm `overrides` in `examples/web/package.json` forcing `tmp >= 0.2.6` and `uuid >= 11.1.1`; document in KB-007
- **Alternatives considered:** Dismiss Dependabot alert (rejected: hides real risk); remove Lighthouse CI job (rejected: loses performance gate)
- **Consequences:** Lockfile must be regenerated after override changes; overrides should be removed when `@lhci/cli` ships fixed dependencies

### 2026-06-13 — Ship all optional ecosystem modules (M3)
- **Status:** Accepted
- **Context:** Sprint M3 asked whether to ship Lightroom, Rust, and Go optional modules in the template maintainer repo
- **Decision:** Ship all three with Golden Path stubs, MODULE.md guides, and path-gated CI jobs (`lightroom`, `rust`, `go`) that skip when child repos remove the directories
- **Alternatives considered:** Lightroom-only (rejected: Rust/Go stubs are low-cost and popular); defer all optional modules (rejected: COMPLETED_TASKS M3 work already landed)
- **Consequences:** Template CI runs more jobs on `main`; child repos can delete unused `examples/` folders to skip jobs via `hashFiles` guards
