# Post-release regression

After merging a Release Please release PR or tagging vX.Y.Z:

1. Run `python3 scripts/agent-run.py pre-release-gate` and confirm CI + Security Scan + CodeQL green.
2. Verify GitHub Release includes SBOM + OpenVEX assets (`python3 scripts/agent-run.py wait-release-sbom -- --require --wait 300`). After wait, optional one-shot smoke: `… wait-release-sbom -- --require --once`. Review @THIRD_PARTY_LICENSES.md.
3. Confirm GitHub Pages demo deployed (web stack) with no tracking scripts. **Hard fail:** `python3 scripts/agent-run.py check-pages-analytics` (or `bash scripts/check-pages-analytics.sh`) must exit 0.
4. Run `python3 scripts/agent-run.py simulate-template-upgrade` or confirm CI upgrade-simulation job passed.
5. Append regressions to @KNOWLEDGE_BASE.md and BUILD_PLAN [AUTO] items.

Begin now.
