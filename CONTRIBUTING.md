# Contributing

Thank you for contributing to **agent-project-bootstrap** — a FOSS project template for coding agents (Cursor, Windsurf, Antigravity, and others).

## Who contributes what

| Label | Contributor | Examples |
|-------|-------------|----------|
| `AGENT` | Coding agent | Scaffolding, tests, CI config, docs |
| `HUMAN` | Human developer | Approvals, credentials, product decisions |
| `ADB` | Human (Android) | Device testing, F-Droid submission |
| `AUTO` | CI/scripts | GitHub Actions, Dependabot, pre-commit |
## First contribution

Thank you for helping. Read [`docs/BEST_PRACTICES.md`](docs/BEST_PRACTICES.md) if you want the industry *why* behind these files. Questions vs bugs vs vulns: [`SUPPORT.md`](SUPPORT.md). Labeled starter tasks: **Good first issue**.

1. Fork the repository and create a feature branch from `main`.
2. Read `docs/START_HERE.md`, `docs/CURSOR_MODES.md`, `CODE_OF_CONDUCT.md`, and `docs/MAINTAINING_THE_TEMPLATE.md`. First-time walk: `docs/help/TOUR.md` (Cursor: `/tour`).
3. Report security issues via `SECURITY.md` (private reporting preferred).
4. Make changes; run `bash scripts/verify.sh` locally (or the VS Code **Verify** task).
5. Open a PR using the provided template.

## Recommended branching (GitHub Flow)

Short-lived branches, one concern per PR, merge to `main` when required checks are green. Do not force-push `main`. Required checks (via `scripts/setup-github-repo.sh`): **CI**, **Security Scan**, **CodeQL**, **Repo Hygiene**, **Feature Gate**, **Template Upgrade Simulation (Windows)**.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/). Enforced by a `commit-msg` hook:

```bash
pre-commit install --hook-type commit-msg

```

Subjects must match `type(scope)?: description` (`feat`, `fix`, `docs`, `chore`, `ci`, `test`, `refactor`, `perf`, `style`, `build`, `revert`). Merge and Revert subjects are allowed.

## Template improvements

Use the **Template Improvement** issue template for feedback.

## Pre-commit hooks

```bash
pip install pre-commit
pre-commit install
pre-commit install --hook-type commit-msg
pre-commit run --all-files

```

Includes repo hygiene checks (`scripts/check-repo-hygiene.sh`). See [`docs/REPO_HYGIENE.md`](docs/REPO_HYGIENE.md).

## Security triage

Maintainers run a weekly CVE triage pass per `docs/SECURITY_TRIAGE.md`. Review Dependabot alerts before each release.

## Release process (maintainers)

See `docs/MAINTAINING_THE_TEMPLATE.md` for the full semver release checklist.
