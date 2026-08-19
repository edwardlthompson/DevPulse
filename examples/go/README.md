# Golden Path Go

Minimal `hello` command stub for agent-project-bootstrap.

## Commands

```bash
gofmt -l .
go vet ./...
go test ./...
go run .
```

## Module pinning

Run `go mod tidy` before release tags. The zero-dep stub has no `go.sum`; it appears once external dependencies are added. Release SBOM for Go is gated on `go.sum` in `release.yml`.

## CI Integration

Runs in root `.github/workflows/ci.yml` **go** job when `examples/go/` exists and changed (path filter). Includes `go mod tidy`.
