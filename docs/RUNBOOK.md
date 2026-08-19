# Runbook

> Operational guide for deploy, rollback, and incident response.

## Health Checks

For services and APIs, expose:

| Endpoint | Purpose | Expected |
|----------|---------|----------|
| `/health` | Liveness | `200` when process is up |
| `/ready` | Readiness | `200` when dependencies are reachable |

Static PWAs and CLIs may skip HTTP endpoints; document stack-specific checks instead.

## Structured Logging

- JSON or key-value format in production
- Include correlation/request ID per request
- **Never** log passwords, tokens, or PII without explicit consent
- Log levels: `ERROR` for user-visible failures, `WARN` for recoverable, `INFO` for lifecycle events

## Deploy

1. `[AUTO]` CI green on `main`
2. `[HUMAN]` Approve release (Milestone Gates in `BUILD_PLAN.md`)
3. `[AUTO]` Tag and publish via GitHub Release workflow
4. `[HUMAN]` Verify deployment on target platform

## Rollback

1. Revert to previous release tag or artifact
2. Confirm health checks pass
3. Log incident in `DECISION_LOG.md` if user-impacting

## Common Failures

| Symptom | Check | Fix |
|---------|-------|-----|
| CI failing on lint | Local `pre-commit run --all-files` | Fix and push |
| Dependabot alert | `docs/SECURITY_TRIAGE.md` | Merge bump PR |
| State lost after upgrade | Migration tests | Fix schema migration |

## Backup & Restore

| Target | RPO | RTO | Procedure |
|--------|-----|-----|-----------|
| User data | _Define_ | _Define_ | _Document per stack_ |
| Repository | N/A (git) | Immediate | `git clone` |

## SLOs (`[HUMAN]` defines)

| Service | SLI | Target |
|---------|-----|--------|
| _Example: API availability_ | Uptime | _99.9%_ |
| _Example: page load_ | p95 latency | _< 2s_ |

## Escalation

1. Check `BUILD_PLAN.md` Ongoing Maintenance
2. Review `docs/SECURITY_TRIAGE.md` for security issues
3. Contact maintainers in `.github/CODEOWNERS`

## Secret Rotation

When credentials leak or a team member with access leaves:

1. **`[HUMAN]`** Revoke compromised tokens/keys in the provider console immediately
2. **`[AGENT]`** Rotate secrets in GitHub Environments and local `.env` (never commit)
3. **`[AGENT]`** Update `.env.example` if variable names changed
4. **`[AUTO]`** Re-run CI with new secrets; confirm deploy health checks pass
5. **`[HUMAN]`** Log incident in `DECISION_LOG.md`; link advisory if CVE-related
