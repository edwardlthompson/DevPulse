# Documentation checks

Docs-only validation (no full feature gate):

```bash
python3 scripts/agent-run.py check-readme-health
python3 scripts/agent-run.py check-markdown-tables
python3 scripts/agent-run.py check-file-encoding
```

Fix failures before commit. On Windows, re-run encoding check after bulk doc edits.

Begin now.
