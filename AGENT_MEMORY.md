# Agent Memory

> Centralized index of tech stack, threat models, persistent context, and retrospectives.
> Update only at session startups, milestone boundaries, or major architectural pivots.

## Tech Stack

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| Platform | Android only (Kotlin, Compose, Material 3) | Golden Path stub 0.1.0 | Child of agent-project-bootstrap 0.21.0 |
| License | GPL-3.0-or-later | - | Init wrote MIT; Sprint 0 replaced it |
| Distribution | GitHub Releases only | - | Not listed on F-Droid; app still queries F-Droid indexes for other apps |
## Active Modules

- ✅ Android / F-Droid (`modules/android/MODULE.md`)
- Unused stacks pruned at init (web, python, node, rust, go, lightroom)

## Threat Model Checklist

- ✅ `docs/THREAT_MODEL.md` drafted (STRIDE, trust boundaries, top abuse cases, OWASP LLM Top 10 walk)
- ✅ No proprietary closed-source SDKs in production path
- ✅ Opt-in only telemetry (GDPR/CCPA compliant); see `docs/PRIVACY.md`
- ✅ Secrets excluded from VCS (Gitleaks pre-commit)
- ✅ Dependency vulnerability scanning enabled (CodeQL + Trivy + Dependabot)
- ✅ Input validation at all data boundaries
- ✅ `SECURITY.md` and private vulnerability reporting enabled

## Persistent Context

### Project Purpose

DevPulse: local-only Android pulse-check for installed apps across Play, F-Droid (official, Archive, Izzy, Guardian, Calyx), optional Aptoide / APKMirror / APKPure, and GitHub. GitHub listings come from same-object F-Droid `sourceCode` (no token). Leftover GitHub name-search and dump-site lookups are opt-in.

### Key Constraints

- Max 300 lines per static data file (UI + i18n), 150 lines per pure logic file
- Trunk-based development with Conventional Commits
- Strict type safety and test coverage budgets

## Session Retrospectives

| Date | Milestone | What worked | What to improve |
|------|-----------|-------------|-----------------|
| 2026-08-20 | v0.24.0 /ship | `feat:` prepare commit made RP propose 0.24.0; tag published on merge; SBOM dispatched; PR #5 opened with `gh` after Actions PR deny | Enable Actions "create pull requests"; do not pass `newline='\\n'` to `Path.write_text` from PowerShell (truncates the file) |
| 2026-08-20 | HUMAN/ADB device close-out | Date smoke: 0 epoch-1970 rows; Aegis F-Droid APK cached via `download_url`; README `device-inventory.jpg`; first-tag ack via published `v0.23.0` | Recurring maintenance HUMAN (next tag, Nov radar) stay 🔲; keep `human_task_android.py` thin — smoke lives in `device_smoke_run.py` |
| 2026-08-20 | /build Sprints 11–13 | Prefetch opt-in + cert/sha256 gate; Wayback recovers Play `datePublished` only on live Missing; ADB rows later completed on CPH2583 | Host-resolve F-Droid package API; skip on-device index files >2MB; do not mix adb 39/41 |
| 2026-08-20 | v0.23.0 /ship | Dump-store batches worked on device (Mirror 143/385, Pure 315/385); RP #4 opened by hand after Actions PR deny; tag+release created with `gh` after RP missed the 0.23.0 tag | Enable Actions "create pull requests"; use `feat:` not `chore(release):` if the cut is a minor; RP still leaves a phantom next-version branch |
| 2026-08-20 | v0.22.0 /ship | Pruned-stack CI + donation + About-gate + inset tests fixed; RP #3 opened by hand after Actions PR deny; admin-merge; SBOM on the tag | Enable Actions "create pull requests"; fold Unreleased on the RP branch before merge; Settings Close is below the fold on emulator |
| 2026-08-19 | DevPulse Sprint 6 | Package-id-first forge matcher + paste/token UI; no live HTTP | EncryptedSharedPreferences adapter still to swap in with live client |
| 2026-08-19 | DevPulse Sprint 3 | Local-only scan + honest badges; remotes stay unknown | Do not add Play/forge HTTP until their sprints |
| 2026-08-19 | DevPulse Sprint 2 | Inventory list, QUERY_ALL_PACKAGES gate, PackageManager catalog | Screenshots stay HUMAN; do not push without `/push` |
| 2026-08-19 | DevPulse Sprint 1 | `applicationId` `app.devpulse`; About/theme/nav green; Windows `gradlew.bat` ADB smoke | Screenshots stay HUMAN; do not push without `/push` |
| 2026-08-19 | DevPulse Sprint 0 | Init confirmed; GPL stamp; spec/plan/BUILD_PLAN; feature files; Golden Path types locked in spec | HUMAN must approve ADR-0001 and license; do not rename applicationId until Sprint 1 tests stay green |
| 2026-08-18 | v0.21.0 /ship | CI + Windows upgrade-sim green on feat and fix; RP #69 admin-merge; fold comments leftover notes | Fold is local-only — commit empty Unreleased before push or RP leaves leftovers under the version heading |
| 2026-08-17 | M39 /ideas Windows PATH + ship hygiene | Shared PATH resolver; agent-run drops PYTHONPATH; fold Unreleased onto RP; Q&A GraphQL + HUMAN line | Do not attach Environments to required-check workflows; keep Unreleased empty only after fold+comment |
| 2026-08-17 | M38 /ideas ship-hardening | Branch protection now includes Windows upgrade-sim; Python TEMPLATE_INDEX; RP wait skip; lib files ≤150 | `gh` is not on Git Bash PATH unless Program Files is exported |
| 2026-08-17 | v0.20.0 /ship | Three /ideas rounds + Windows upgrade-sim required; RP #68 admin-merge after CI green on 812a2db | Empty Unreleased before RP; jq.exe CRLF breaks template-index; wait for `release` SBOM |
| 2026-08-17 | /ideas pass 3 | Windows required check; COACH.md; dirty Unreleased notes; weekly AUTO skip; Codespaces verify; citation date; setup-python pin; build_sprint split | Allowlist leftover oversized lib modules; do not pretend they are under 150 |
| 2026-08-17 | /ideas pass 2 | Health template-vs-child; pwsh skip; Windows upgrade-sim CI; UTF-8 health; hint JSON split; root md links; Q&A category; pre-commit | Recurring 🔲 maintenance rows are the honest template next-row |
| 2026-08-17 | /ideas implement-all | Eight ranked items: Windows REPL hang, citation sync, glossary, portable stamp, verify hints, welcome hook, docs links, Discussions | Keep welcome/Discussions opt-in or best-effort; do not fail init when `gh` is missing |
| 2026-08-16 | v0.19.0 /ship | Tour + portable adapters; CI green after push of unpushed feat; RP #67 admin-merge | Hard gate cannot see CI until HEAD is on origin; wait for `release` published SBOM |
| 2026-08-16 | Portable first-run | AGENTS.md SoT + thin pointers; GEMINI.md pointer-only; /tour twin in docs/help | Do not add `.agents/agents.md` (second SoT) |
| 2026-08-16 | Coach layer | BEST_PRACTICES + FIRST_30_DAYS + /coach; justfiles optional | Keep just out of CI |
| 2026-08-16 | M37 gap close | verify.sh + env schema + commit-msg + Dockerfile; post hooks implemented but opt-in | Keep `.agent/` as indexes only |
| 2026-08-16 | M36 bootstrap standards | Extended init-project instead of a second generator; 11 engine unit tests; validate-bootstrap --quick green | Full simulate-template-upgrade still the heavy init dry-run |
| 2026-08-16 | v0.18.3 /ship | Autofix + pre-release green; Codex skip; RP #66 admin-merge; Compose BOM 2026.08.00 | Release assets start empty — wait for `release` published SBOM job |
| 2026-08-16 | v0.18.2 /push | RP #63 admin-merge after maintainer gates; HEAD CI already green; no extra prepare commit | Keep Unreleased empty before RP or notes land under `chore` |
| 2026-08-15 | M35 HUMAN open items | Job-scoped workflow tokens; dismissed 65 PinnedDependencies; merged Dependabot #58–#61; radar max 6 | Rebase Dependabot before Feature Gate on stale lockfiles; Scorecard VulnerabilitiesID lags patched HEAD |
| 2026-08-15 | v0.18.1 /push | `resolve-python.sh` now sets a single executable path so `"$PY"` works; RP #62 admin-merge after CI green | Do not set `PY="py -3"` (quoted invoke fails); keep Unreleased empty before RP or notes land under `chore` |
| 2026-08-15 | M35 /audit | Shared `resolve-python.sh` skips Store stub; About gate restores from HEAD; slim Unreleased; UTF-8 LF rules | Do not run `python3` on Windows PATH; leave Scorecard + Dependabot PRs to HUMAN |
| 2026-08-15 | v0.18.0 /ship | M34 thin steals + extract-zip High cleared via `@puppeteer/browsers` 3.2.0; lockfile needed `proxy-agent` 8 for `npm ci`; RP #56 admin-merge | Generate lockfile with Node 22 / `npm ci` locally after overrides; Windows Store `python3` hangs autofix |
| 2026-08-14 | M34 prior-art thin steals | Honesty labels + handoff + Sacred upgrade column without vendoring cousin repos | Keep fail-open hooks labeled; do not claim `/push` blocks `--force` |
| 2026-08-12 | v0.17.0 /ship | Branding kit + pitch README generator; RP #55 admin-merge; CI green on feat commit | Trigger Release workflow for SBOM if assets empty after tag |
| 2026-08-10 | v0.16.0 /ship | Codex + multi-stack autofix in `/prerelease`; fixed About-without Biome stubs; undici/ip-address/nanoid overrides cleared High alerts after push; RP #51 admin-merge | Prefer Git Bash via agent-run on Windows (System32 bash = WSL1 breaks npm); push security lockfile before expecting Dependabot zero |
| 2026-08-01 | v0.15.2 /ship | Cleared High Dependabot mid-ship (js-yaml, brace-expansion, postcss); RP #50 admin-merge after auto-merge wait | Re-check Dependabot after each push before merge-release-please |
| 2026-07-22 | v0.15.0 /ship | RP #37 merged; fixed duplicate CHANGELOG Unreleased + Node 25 vitest localStorage before CI green | Confirm single Unreleased before push; watch GH Dependabot banner vs triage script |
| 2026-07-21 | M33 Cursor feature integration | Native worktrees + permissions + 7 skills + plugin pack + CLI example; commercial docs deepened | Keep pack script globs wholesale when adding skills; residual Auto-review classifier drift |
| 2026-07-12 | v0.14.1 release | /push merged RP #36; fixed Dependabot alert API + FOSS mcp.json gate | Prefer AUTOMERGE_TOKEN over admin merge fallback for RP |
| 2026-07-12 | M32 audit | Caught GITHUB_TOKEN automerge skipping push CI; Git Bash preference for Windows agent-run | Completed via HUMAN automation; GitHub MCP enabled locally |
| 2026-06-13 | v0.6.0 design system | Cross-stack tokens + i18n scaffold | Restore optional-stack CI jobs after large merge |
| 2026-06-30 | Autonomous /build + HUMAN automation | Grouped human section keeps board readable; automation router backlogs failures only | Release Please PR #20 for 0.12.0 needs human merge |
## Template Provenance

- **Source template:** `edwardlthompson/agent-project-bootstrap` (child: `edwardlthompson/DevPulse`)
- **Template version:** `0.25.0` (see `.template-version`)
- **Last update check:** See `.template-update.json`
