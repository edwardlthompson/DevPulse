#!/usr/bin/env bash
# Verify batch command registry matches .cursor/commands/*.md and super chains
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ERRORS=0

ATOMIC=(
  audit cleanup debug gates triage dependabot push prerelease regress
  feature fix init prune ci docs upgrade setup plan restore compact resume scope
  codex-review coach tour ideas allideas update-deps best-of-n emulator adr
)

SUPER=(
  bootstrap verify build ship maintain
)

declare -A SUPER_CHAINS
SUPER_CHAINS[bootstrap]="init prune setup gates"
SUPER_CHAINS[verify]="docs gates ci"
SUPER_CHAINS[build]="plan feature gates cleanup"
SUPER_CHAINS[ship]="update-deps prerelease push regress"
SUPER_CHAINS[maintain]="triage update-deps dependabot audit"

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
  docs/help/batch-commands-print.html \
  schemas/batch-commands-print.json \
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

# Portable recipes (Cursor slash command ↔ docs/help twin for other IDEs)
PORTABLE=(tour coach ideas allideas debug upgrade adr)
for name in "${PORTABLE[@]}"; do
  cmd=".cursor/commands/${name}.md"
  twin="docs/help/$(echo "$name" | tr '[:lower:]' '[:upper:]').md"
  if [ ! -f "$cmd" ]; then
    echo "MISSING: $cmd (portable twin $twin)"
    ERRORS=$((ERRORS + 1))
  fi
  if [ ! -f "$twin" ]; then
    echo "MISSING: $twin (portable twin of $cmd)"
    ERRORS=$((ERRORS + 1))
  fi
done

EXPECTED=$(( ${#ATOMIC[@]} + ${#SUPER[@]} ))
ACTUAL=$(find .cursor/commands -maxdepth 1 -name '*.md' | wc -l | tr -d ' ')
if [ "$ACTUAL" -ne "$EXPECTED" ]; then
  echo "COUNT: expected $EXPECTED command files, found $ACTUAL"
  ERRORS=$((ERRORS + 1))
fi

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
if ! "$PY" "$ROOT/scripts/lib/batch_commands_print.py" --check "${ATOMIC[@]}" "${SUPER[@]}"; then
  ERRORS=$((ERRORS + 1))
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS batch command check(s) failed"
  exit 1
fi

echo "Batch commands OK ($EXPECTED files: ${#ATOMIC[@]} atomic + ${#SUPER[@]} super)"
