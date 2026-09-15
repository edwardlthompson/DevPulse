# Optional stack “hello About” matrix

Quick index for MODULE.md owners. Each optional stack should expose a minimal About/version surface for feature-gate smoke.

| Stack | About / version entry | Gate / test |
|-------|----------------------|-------------|
| web | Settings → About panel | `feature-gate --stack web` |
| android | Settings → About | `./gradlew test` + instrumented smoke |
| node | OpenAPI About schema | `examples/node` contract tests |
| python | `hello.about` module | `uv run pytest` |
| go | HTTP About endpoint | `go test` |
| rust | CLI `--version` + crash stub | `cargo test` |
| blender | Manifest `--stub` QA; Cycles when Blender installed | `feature-gate --stack blender` |

Missing rows stay 🔲 in the stack `MODULE.md` until the Golden Path example lands.
