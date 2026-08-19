# Module E: Node.js API Services

> Activate when your stack includes a Node.js HTTP API or backend service.

## Requirements (Verbatim)

- **Dependency Locking:** Commit `package-lock.json` and install with `npm ci` in CI and locally.
- **Typed TypeScript:** Strict `tsconfig` with `noEmit` lint gate; runtime via `tsx` or compiled output.
- **Testing:** Vitest unit tests with coverage; HTTP handlers tested without binding ports when possible.
- **License:** MIT-compatible dependencies only; run `check-license-compliance.sh node` before release.

## Activation Checklist

- 🔲 Create `package.json` with `"license": "MIT"` and lockfile
- 🔲 Enable `tsc --noEmit` and Vitest in CI
- 🔲 Review `examples/node/` Golden Path stub (Hono minimal API)
- 🔲 Add health/readiness route per `docs/RUNBOOK.md`
- 🔲 Wire OpenAPI or schema-first contracts if exposing public API
- 🔲 Add stack to `.github/dependabot.yml`

## Golden Path Reference

See `examples/node/` for Hono + TypeScript + Vitest minimal API stub.

## Feature gate (Sprint 2+)

After each feature step, `scripts/feature-gate.sh` runs (via `watch-agent-gates.sh`):

| Stage | Command |
|-------|---------|
| Lint | `npm run lint` in `examples/node/` |
| Unit | `npm test` |

## Owner Labels for This Module

| Task type | Label |
|-----------|-------|
| Scaffold routes, types, tests | `AGENT` |
| Dependency audit approval | `HUMAN` |
| lint/test CI gates | `AUTO` |
