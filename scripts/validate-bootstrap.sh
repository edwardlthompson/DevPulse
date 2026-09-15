#!/usr/bin/env bash
# Verify required bootstrap artifacts exist and pass delegated checks
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

QUICK=false
for arg in "$@"; do
  case "$arg" in
    --quick) QUICK=true ;;
  esac
done

REQUIRED=(
  README.md
  LICENSE
  CONTRIBUTING.md
  SECURITY.md
  CODE_OF_CONDUCT.md
  BUILD_PLAN.md
  BUILD_PLAN_TEMPLATE.md
  AGENTS.md
  AGENT.md.example
  AGENT_MEMORY.md
  docs/START_HERE.md
  docs/CURSOR_MODES.md
  docs/INITIALIZATION_PROMPT.md
  .cursor/rules/cursor-modes.mdc
  .cursor/rules/product-brief.mdc
  docs/DESIGN_GUIDE.md
  docs/WEB_PROJECT_LAYOUT.md
  docs/SECURITY_TRIAGE.md
  docs/THREAT_MODEL.md
  docs/PRIVACY.md
  docs/RUNBOOK.md
  docs/FEATURE_MODULES.md
  .github/dependabot.yml
  .github/CODEOWNERS
  THIRD_PARTY_LICENSES.md
  .env.example
  design-tokens/design-tokens.json
  branding/BRANDING.md
  branding/product.json
  branding/assets/logo-mark.svg
  branding/official-colors.css
  branding/generated/README.preview.md
  docs/help/BATCH_COMMANDS.md
  docs/help/batch-commands-print.html
  docs/help/UPGRADE.md
  docs/BATCH_COMMANDS.md
  .cursor/rules/batch-commands.mdc
  CODE_REVIEW.md.example
  RELEASE_NOTES.md.example
  scratchpad.md.example
  docs/features/_handoff.md
  schemas/features/feature-spec.schema.json
  schemas/features/feature-spec.contract.json
  docs/spec.md
  docs/plan.md
  docs/BEST_PRACTICES.md
  docs/FIRST_30_DAYS.md
  docs/first-30-days.json
  docs/WINGET.md
  docs/GROK_BOTS.md
  docs/CURSOR_MARKETPLACE.md
  docs/AGENT_PORTABILITY.md
  docs/help/TOUR.md
  docs/help/IDEAS.md
  docs/help/ALLIDEAS.md
  docs/help/GLOSSARY.md
  docs/help/COACH.md
  docs/help/DEBUG.md
  docs/help/ADR.md
  scripts/check-doc-links.sh
  bootstrap.config.json.example
  PROJECT_CHECKLIST.md
  CLAUDE.md
  GEMINI.md
  CONVENTIONS.md
  .clinerules
  .github/copilot-instructions.md
  .cursor/rules/main.mdc
  .windsurf/rules/agents-pointer.md
  .continue/rules/agents.md
  templates/licenses/Apache-2.0.txt
  env.schema.json
  .devcontainer/Dockerfile
  .devcontainer/devcontainer.json
  .agent/memory/decisions.md
  .agent/memory/pitfalls.md
  .agent/skills/README.md
  scripts/verify.sh
  scripts/check-agent-adapters.sh
  SUPPORT.md
  CITATION.cff
  .vscode/tasks.json
  .vscode/extensions.json
)

BATCH_COMMANDS=(
  audit cleanup debug gates triage dependabot push prerelease regress
  feature fix init prune ci docs upgrade setup plan restore compact scope
  bootstrap verify build ship maintain coach tour ideas allideas
  codex-review update-deps best-of-n emulator
)

for cmd in "${BATCH_COMMANDS[@]}"; do
  REQUIRED+=(".cursor/commands/${cmd}.md")
done

ERRORS=0

run_check() {
  if ! "$@"; then
    ERRORS=$((ERRORS + 1))
  fi
}

for f in "${REQUIRED[@]}"; do
  if [ ! -e "$f" ]; then
    echo "MISSING: $f"
    ERRORS=$((ERRORS + 1))
  fi
done

if [ -f LICENSE ] && [ ! -s LICENSE ]; then
  echo "EMPTY: LICENSE"
  ERRORS=$((ERRORS + 1))
fi

if [ -f examples/web/package.json ] && [ ! -f examples/web/package-lock.json ]; then
  echo "MISSING: examples/web/package-lock.json (required when web example present)"
  ERRORS=$((ERRORS + 1))
fi

if [ -f examples/node/package.json ] && [ ! -f examples/node/package-lock.json ]; then
  echo "MISSING: examples/node/package-lock.json (required when node example present)"
  ERRORS=$((ERRORS + 1))
fi

if [ -f examples/python/pyproject.toml ] && [ ! -f examples/python/uv.lock ]; then
  echo "MISSING: examples/python/uv.lock (required when python example present)"
  ERRORS=$((ERRORS + 1))
fi

run_check bash scripts/check-python-pytest-workflow.sh

if ! grep -q '\[AGENT\]' BUILD_PLAN.md && ! grep -q '\[HUMAN\]' BUILD_PLAN.md; then
  echo "MISSING: BUILD_PLAN.md owner labels"
  ERRORS=$((ERRORS + 1))
fi

# Writes first (must stay sequential)
run_check bash scripts/sync-exemplar-config.sh

# Independent read-only checks — use local CPU (BOOTSTRAP_CHECK_JOBS overrides)
if ! python3 scripts/lib/run_checks_parallel.py \
  check-file-encoding.sh \
  check-design-cohesion.sh \
  check-markdown-tables.sh \
  check-changelog-unreleased.sh \
  check-repo-hygiene.sh \
  check-batch-commands.sh \
  check-cursor-hooks.sh \
  check-build-plan-parallel.sh \
  check-build-plan-tally.sh \
  check-template-version-sync.sh \
  validate-template-index.sh \
  check-project-card-index.sh \
  check-agent-brief.sh \
  check-bootstrap-engine.sh \
  check-agent-adapters.sh \
  check-env.sh \
  check-doc-links.sh \
  check-pre-commit-hooks.sh \
  check-workflow-action-ref-format.sh \
  check-feature-specs.sh \
  check-i18n-parity.sh \
  check-token-contrast.sh \
  check-glossary-links.sh \
  check-action-workflows.sh \
  check-shellcheck.sh \
  check-psscriptanalyzer.sh \
  check-hadolint.sh \
  check-md-yaml-lint.sh \
  check-reuse.sh \
  check-openvex.sh \
  check-package-attestation-docs.sh \
  check-github-settings-yml.sh \
  check-merge-queue-docs.sh \
  check-pages-analytics.sh \
  check-pages-demo-link.sh \
  check-web-import-hygiene.sh \
  check-readme-badges.sh \
  check-playwright-cache.sh \
  check-android-cmdline-tools.sh \
  check-android-sdk-licenses.sh \
  check-fdroid-metadata-links.sh \
  check-nix-flake.sh \
  check-auto-review.sh \
  check-gitleaks-baseline.sh \
  check-android-sdk-secrets.sh \
  check-semgrep.sh \
  check-mcp-allowlist.sh \
  check-crash-payload-allowlist.sh \
  check-crash-inbox.sh \
  check-sanitize-fixtures.sh \
  check-first-30-days.sh \
  check-contributing-agent.sh \
  check-template-upgrade-form.sh \
  check-ideas-discussion.sh \
  check-adr-command.sh \
  check-adr-architecture.sh \
  check-ci-gaps.sh \
  check-ci-refs.sh \
  check-readme-mermaid.sh \
  check-social-preview.sh \
  check-fdroid-screenshots.sh \
  check-winget-runbook.sh \
  check-cursor-marketplace.sh \
  check-cursor-automations.sh \
  check-cursor-cloud-hooks.sh \
  check-cursor-canvas.sh \
  check-cursor-cli.sh \
  check-tour-coach-chrome.sh
then
  ERRORS=$((ERRORS + 1))
fi

TIER="foss"
if [ -f .cursor/stack-selection.json ]; then
  TIER="$(python3 -c "import json;print(json.load(open('.cursor/stack-selection.json')).get('distribution_tier','foss'))" 2>/dev/null || echo foss)"
fi
# Writes manifest — before integrations check
python3 scripts/sync-cursor-features.py --root "$ROOT" --tier "$TIER"
run_check bash scripts/check-cursor-integrations.sh --tier "$TIER"

if [ "$QUICK" = false ]; then
  run_check bash scripts/validate-workflow-actions.sh
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS bootstrap check(s) failed"
  exit 1
fi

if [ "$QUICK" = true ]; then
  echo "Bootstrap validation passed (--quick: skipped GitHub API action resolve; format check ran)"
else
  echo "Bootstrap validation passed"
fi
