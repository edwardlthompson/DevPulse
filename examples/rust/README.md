# Golden Path Rust

Minimal `hello` binary stub for agent-project-bootstrap.

## Commands

```bash
cargo fmt --check
cargo clippy -- -D warnings
cargo test
cargo run
```

## CI Integration

Runs in root `.github/workflows/ci.yml` **rust** job when `examples/rust/` exists and changed (path filter).
