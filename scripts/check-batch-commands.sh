#!/usr/bin/env bash
# Verify batch command registry matches .cursor/commands/*.md and super chains
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ERRORS=0

ATOMIC=(
  audit cleanup debug gates triage dependabot push prerelease regress
  feature fix init prune ci docs upgrade setup plan restore compact scope
  codex-review coach tour ideas
)

SUPER=(
  bootstrap verify build ship maintain
)

declare -A SUPER_CHAINS
SUPER_CHAINS[bootstrap]="init prune setup gates"
SUPER_CHAINS[verify]="docs gates ci"
SUPER_CHAINS[build]="plan feature gates cleanup"
SUPER_CHAINS[ship]="prerelease push regress"
SUPER_CHAINS[maintain]="triage dependabot audit"

check_file() {
  local name="$1"
  local path=".cursor/commands/${name}.md"
  if [ ! -f "$path" ]; then
    echo "MISSING: $path"
    ERRORS=$((ERRORS + 1))
  fi
}

for cmd in "${ATOMIC[@]}"; do
  check_file "$cmd"
done

for cmd in "${SUPER[@]}"; do
  check_file "$cmd"
done

# Orphan .md files not in registry
for f in .cursor/commands/*.md; do
  base="$(basename "$f" .md)"
  found=0
  for cmd in "${ATOMIC[@]}" "${SUPER[@]}"; do
    if [ "$cmd" = "$base" ]; then
      found=1
      break
    fi
  done
  if [ "$found" -eq 0 ]; then
    echo "ORPHAN: $f (not in registry)"
    ERRORS=$((ERRORS + 1))
  fi
done

# Super chains reference existing atomics
for super in "${SUPER[@]}"; do
  for child in ${SUPER_CHAINS[$super]}; do
    child_path=".cursor/commands/${child}.md"
    if [ ! -f "$child_path" ]; then
      echo "SUPER_CHAIN: $super references missing child $child"
      ERRORS=$((ERRORS + 1))
    fi
  done
done

# Rule and catalog
for required in \
  .cursor/rules/batch-commands.mdc \
  docs/BATCH_COMMANDS.md \
  docs/help/BATCH_COMMANDS.md \
  CODE_REVIEW.md.example \
  RELEASE_NOTES.md.example \
  scratchpad.md.example \
  docs/features/_handoff.md
do
  if [ ! -f "$required" ]; then
    echo "MISSING: $required"
    ERRORS=$((ERRORS + 1))
  fi
done

EXPECTED=$(( ${#ATOMIC[@]} + ${#SUPER[@]} ))
ACTUAL=$(find .cursor/commands -maxdepth 1 -name '*.md' | wc -l | tr -d ' ')
if [ "$ACTUAL" -ne "$EXPECTED" ]; then
  echo "COUNT: expected $EXPECTED command files, found $ACTUAL"
  ERRORS=$((ERRORS + 1))
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS batch command check(s) failed"
  exit 1
fi

echo "Batch commands OK ($EXPECTED files: ${#ATOMIC[@]} atomic + ${#SUPER[@]} super)"
