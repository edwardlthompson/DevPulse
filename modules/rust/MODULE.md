# Module F: Rust Applications

> Optional stack — not in the default init stack picker. See `docs/OPTIONAL_STACKS.md` to opt in.

## Requirements (Verbatim)

- **Strict dependency locking:** Commit `Cargo.lock` for binaries and applications; pin toolchain in `rust-toolchain.toml` when reproducibility matters.
- **Static analysis:** Run `cargo clippy -- -D warnings` and `cargo fmt --check` in CI.
- **Type safety:** Prefer `#![deny(unsafe_code)]` unless an ADR documents otherwise.

## Activation Checklist

- 🔲 Copy or keep `examples/rust/` Golden Path stub
- 🔲 Set edition and MSRV in `Cargo.toml`
- 🔲 Enable `cargo test` and clippy in CI
- 🔲 Document MSRV in `AGENT_MEMORY.md`

## Golden Path Reference

See `examples/rust/` for a minimal `hello` binary (MIT).

## CodeQL

Root `.github/workflows/codeql.yml` does **not** analyze `examples/rust/` (optional stack). Use `cargo clippy` and `cargo audit` in CI when this module is active. See `docs/OPTIONAL_STACKS.md`.

## Owner Labels for This Module

| Task type | Label |
|-----------|-------|
| Scaffold crate, tests | `AGENT` |
| MSRV / edition approval | `HUMAN` |
| clippy/fmt/test CI gates | `AUTO` |
