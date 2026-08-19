#!/usr/bin/env bash
# Remove gitignored untracked files only (git clean -fdX). Safe: never deletes tracked files.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

APPLY=false
for arg in "$@"; do
  case "$arg" in
    --apply) APPLY=true ;;
    -h|--help)
      echo "Usage: $0 [--apply]"
      echo "  default: dry-run (list ignored untracked paths)"
      echo "  --apply: delete ignored untracked files (git clean -fdX)"
      exit 0
      ;;
  esac
done

if ! git rev-parse --is-inside-work-tree &>/dev/null; then
  echo "Not a git repository"
  exit 1
fi

if [ "$APPLY" = true ]; then
  echo "Applying purge (ignored untracked files only)..."
  git clean -fdX
  echo "Purge complete."
else
  echo "Dry-run — would remove these ignored untracked paths:"
  if ! git clean -fdXn | grep -q .; then
    echo "Nothing to purge."
  else
    git clean -fdXn
    echo ""
    echo "Tip: run with --apply to reclaim disk space."
  fi
fi
