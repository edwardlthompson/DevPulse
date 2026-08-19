#!/usr/bin/env bash
# Back-compat wrapper for feature-gate.sh
exec bash "$(dirname "$0")/feature-gate.sh" "$@"
