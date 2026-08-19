<p align="center">
  <img src="../../branding/assets/logo-mark.svg" alt="Golden Path" width="64" />
</p>

# Golden Path Node (Hono API)

FOSS apps with a clear path from idea to release — minimal typed HTTP API (Hono, TypeScript, Vitest). Brand kit: [`branding/BRANDING.md`](../../branding/BRANDING.md).

## Commands

```bash
npm ci
npm run lint
npm test
npm run dev

```

## Routes

- `GET /health` — readiness probe
- `GET /greet/:name?` — sample JSON handler

## CI Integration

Runs in the root `.github/workflows/ci.yml` `node` job.
