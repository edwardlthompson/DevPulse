# Crash-report GitHub App proxy (disabled)

Default Golden Path crash filing uses a GitHub account ([PRIVACY.md](PRIVACY.md)). An anonymous GitHub App proxy is **not** enabled.

## Status

- **Off** unless a child repo sets `crash_proxy.enabled` in `bootstrap.config.json` **after** a DPIA.
- Do not put a PAT in the client. Do not enable Pages telemetry.

## [HUMAN] before enable

1. Complete the DPIA checklist in [PRIVACY.md](PRIVACY.md).
2. Record lawful basis, retention, and abuse controls in `DECISION_LOG.md`.
3. Host the proxy on infrastructure you control; rate-limit; drop PII fields already banned in the privacy report schema.

A self-hosted GlitchTip / Bugsink inbox is a separate **disabled** stub — see [CRASH_INBOX.md](CRASH_INBOX.md). That stub is not a live crash-proxy either.

Template maintainers keep this file as the named follow-up only.
