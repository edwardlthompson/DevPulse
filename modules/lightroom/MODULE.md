# Module D: Adobe Lightroom Classic Plugins

> Activate when your stack includes an Adobe Lightroom Classic plugin.

## Requirements (Verbatim)

- **Lightroom SDK Compliance:** Code written for Adobe Lightroom Classic must conform strictly to the Adobe Lightroom SDK object-oriented Lua API framework. Do not import generic Lua modules or attempt direct OS system actions without routing through the explicit Lr naming boundaries (e.g., `LrTasks`, `LrDialogs`, `LrLogger`, `LrView`).

## Activation Checklist

- 🔲 Verify all code uses `Lr*` SDK namespaces only
- 🔲 No generic Lua module imports (stdlib exceptions documented in ADR)
- 🔲 No direct OS system calls outside SDK boundaries
- 🔲 Configure `Info.lua` with correct `.lrplugin` wrapper parameters
- 🔲 Set up `LrLogger` for structured debug output
- 🔲 Document SDK version compatibility in AGENT_MEMORY.md
- 🔲 Add Lua lint rules if applicable

## Golden Path Reference

See `examples/lightroom/` for `Info.lua` metadata stub and SDK version documentation. Adobe SDK is proprietary; CI checks Lr* namespace compliance only.

## Feature gate (Sprint 2+)

Lightroom plugins are optional; when active, `scripts/feature-gate.sh` runs repo hygiene + encoding gates. Lua lint/SDK grep remains a milestone CI gate (`lightroom` job), not per-feature smoke.

| Stage | Command |
|-------|---------|
| Hygiene + encoding | `bash scripts/feature-gate.sh --stack multi` |
| SDK compliance | CI Lightroom namespace grep on `examples/lightroom/` |

## Owner Labels for This Module

| Task type | Label |
|-----------|-------|
| Scaffold plugin Lua structure | `AGENT` |
| SDK version/target approval | `HUMAN` |
| Plugin load testing in Lightroom | `HUMAN` |
