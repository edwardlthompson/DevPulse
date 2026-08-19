# Post-release regression

After merging a Release Please release PR or tagging vX.Y.Z:

1. Run `python3 scripts/agent-run.py pre-release-gate` and confirm CI + Security Scan + CodeQL green.
2. Verify GitHub Release includes SBOM assets; review @THIRD_PARTY_LICENSES.md.
3. Confirm GitHub Pages demo deployed (web stack) with no tracking scripts.
4. Run `python3 scripts/agent-run.py simulate-template-upgrade` or confirm CI upgrade-simulation job passed.
5. Append regressions to @KNOWLEDGE_BASE.md and BUILD_PLAN [AUTO] items.

Begin now.
