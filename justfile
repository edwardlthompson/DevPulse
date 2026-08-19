# Optional DX. CI does not require `just`.
# https://github.com/casey/just

verify:
    bash scripts/verify.sh

gates:
    python3 scripts/agent-run.py validate-bootstrap --quick

health:
    bash scripts/project-health.sh
