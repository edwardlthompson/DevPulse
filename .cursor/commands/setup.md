# GitHub repo setup

1. Run `scripts/setup-github-repo.sh` (or `.ps1`) with `gh` authenticated as repo admin.
2. Enable Dependabot alerts, private vulnerability reporting, and branch protection (CI, Security Scan, CodeQL).
3. If API returns 422, follow the printed manual checklist in Settings ([HUMAN]).
4. **Install local pre-commit hooks** (closes CI-red / local-green gaps):

```bash
pip install pre-commit
pre-commit install
pre-commit run --all-files

```

On Windows without `pip` on PATH, use `python -m pip install pre-commit` then the same `pre-commit` commands.
Re-run until green or checklist complete.

5. Copy the sandbox allowlist if missing:

```bash
python3 scripts/agent-run.py copy-sandbox-config
python3 scripts/agent-run.py install-commit-msg-hook

```

Begin now.
