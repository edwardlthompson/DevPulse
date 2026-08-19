#!/usr/bin/env bash
# Fail if text files use UTF-16 or NUL-byte corruption (must be UTF-8)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

python3 "$(dirname "$0")/check-file-encoding.py" "$ROOT" "$@"
