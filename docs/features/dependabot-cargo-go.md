# Feature: dependabot-cargo-go

> Weekly Dependabot backup for optional Rust and Go Golden Path examples.

## Acceptance criteria

- ✅ `.github/dependabot.yml` has `cargo` at `/examples/rust` and `gomod` at `/examples/go`
- ✅ Both stay `interval: weekly` (local `/update-deps` remains primary)
- ✅ Groups `rust-dependencies` / `go-dependencies` stay present (health note in `SECURITY_TRIAGE.md`)
- ✅ Rust and Go MODULE checklists mention the ecosystems

## Smoke scenario

1. _Given_ the template `dependabot.yml`
2. _When_ `python3 -m unittest tests.test_dependabot_optional_stacks`
3. _Then_ cargo and gomod directories are present and not daily

## Container map

| Layer | Path |
|-------|------|
| Logic | N/A |
| View | N/A |
| Tests | `tests/test_dependabot_optional_stacks.py` |
| Wiring | `.github/dependabot.yml` |
## Tests

- Automated: yes — YAML phrase lock
- Coverage: missing cargo/gomod, daily interval

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack docs`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
