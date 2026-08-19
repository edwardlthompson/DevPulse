# ADR-0000: Template Baseline Architecture

- **Status:** Accepted
- **Date:** 2026-06-12
- **Deciders:** Template maintainer

## Context

This repository is a GitHub Template for bootstrapping FOSS projects with Cursor agents. It must serve two modes: **Bootstrap** (new project from template) and **Reference** (apply rules to existing codebase).

## Decision

1. Use **multi-stack Golden Path stubs** (`examples/web`, `examples/python`, `examples/android`) rather than a single monolithic app.
2. Use **owner-labeled BUILD_PLAN** (AGENT / HUMAN / ADB / AUTO) with Sequential and Parallel lanes per sprint.
3. Use **modern Cursor rules** (`.cursor/rules/*.mdc` + `AGENTS.md`) instead of legacy `.cursorrules`.
4. Use **TEMPLATE_INDEX.json** as machine-readable file catalog for agent routing.
5. Use **semver template versioning** (`.template-version`) with update checker for downstream consumers.

## Alternatives Considered

- **Single-stack template (TypeScript only):** Rejected — too narrow for multi-ecosystem audience.
- **Monolithic example app:** Rejected — high token cost and confusing for non-matching stacks.
- **Auto-sync child repos from template:** Rejected — GitHub does not support this natively.

## Consequences

- Consumers prune unused `examples/` and `modules/` during Sprint 0.
- Maintainers must update `TEMPLATE_INDEX.json` and `.template-version` on every release.
- Android CI is structure-only; full APK builds require local SDK (`ADB` tasks).
