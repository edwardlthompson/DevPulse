# Optional Stack Modules

The init script stack picker (`web` / `python` / `android` / `node` / `multi` / `none`) does **not** include optional ecosystems (rust, go, lightroom). Enable them manually when needed.

| Module | Guide | Example | When to keep |
|--------|-------|---------|--------------|
| Lightroom plugin | `modules/lightroom/MODULE.md` | `examples/lightroom/` | Adobe Lightroom Classic plugin work |
| Rust | `modules/rust/MODULE.md` | `examples/rust/` | Rust CLI, services, or libraries |
| Go | `modules/go/MODULE.md` | `examples/go/` | Go CLI, services, or libraries |
| Node API | `modules/node/MODULE.md` | `examples/node/` | Primary stack via `--stack node`; optional when pruning other stacks |

## Opt-in during init

When the init script asks to prune unused examples, answer **no** (or choose `multi`) if you need optional stacks. Then delete stacks you do **not** need:

```bash
# Example: web-only project, drop Rust/Go/Lightroom
rm -rf examples/rust examples/go examples/lightroom
rm -rf modules/rust modules/go modules/lightroom
```

### Non-interactive prune flags

`--non-interactive` alone **skips** pruning. To prune in scripts, pass `--prune` explicitly.

| Flag | Effect |
|------|--------|
| `--keep-optional` | When pruning, retain `rust` / `go` / `lightroom` examples and modules (**default**) |
| `--prune-optional` | When pruning, also remove optional stacks |

```bash
# Web-only fork; keep optional stacks for later
bash scripts/init-project.sh --non-interactive --stack web --prune --keep-optional \
  --project-name "My App" --purpose "PWA"

# Web-only fork; drop optional stacks too
bash scripts/init-project.sh --non-interactive --stack web --prune --prune-optional \
  --project-name "My App" --purpose "PWA"
```

PowerShell equivalents: `-Prune -KeepOptional` (default) or `-Prune -PruneOptional`.

`scripts/simulate-template-upgrade.sh` runs both paths in CI (`--no-prune` then `--prune --prune-optional`).

## CI behavior

Optional example CI jobs (`rust`, `go`, `lightroom`) and the instrumented Android job run when:

1. The corresponding `examples/{stack}/` directory **exists** (skipped after you delete the folder), and
2. Files under that directory **changed** on the push/PR (or on `workflow_dispatch`, which runs all present optional jobs).

The `node` example job runs on every push when `examples/node/` exists. Core jobs (web, python, android build, feature-gate) always run on every push to `main`.
