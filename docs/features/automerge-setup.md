# Feature: automerge-setup

> Optional `AUTOMERGE_TOKEN` wiring in GitHub repo setup. Never invent a live PAT.

## Acceptance criteria

- ✅ `setup-github-repo.sh` calls `setup-automerge-token.sh` when `AUTOMERGE_TOKEN` or `SETUP_AUTOMERGE_TOKEN=1` is set
- ✅ Missing token prints a NOTE and does not fail repo setup
- ✅ Manual checklist names the helper and the optional env flags

## Smoke scenario

1. _Given_ `AUTOMERGE_TOKEN` is unset
2. _When_ `setup-github-repo.sh` reaches the automerge step
3. _Then_ it prints a NOTE pointing at `scripts/setup-automerge-token.sh` and continues

## Container map

| Layer | Path |
|-------|------|
| Logic | `scripts/setup-github-repo.sh` `maybe_setup_automerge_token` |
| View | N/A |
| Tests | `tests/test_setup_github_automerge.py` |
| Wiring | `scripts/setup-github-repo.ps1` already delegates to the bash script |

## Tests

- Automated: yes — script mentions helper, env flags, and non-failing NOTE
- Coverage: missing token path; helper invocation condition

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack docs`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
