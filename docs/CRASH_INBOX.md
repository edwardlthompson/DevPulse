# Self-hosted crash inbox stub (GlitchTip / Bugsink)

> **stub only until DPIA** — disabled on the FOSS template; do not enable without `docs/PRIVACY.md` DPIA.

Golden Path crash filing stays on GitHub Issues ([PRIVACY.md](PRIVACY.md), [ADR-0002](adr/0002-privacy-github-feedback.md)). A self-hosted GlitchTip or Bugsink inbox is a **disabled** stub. It is **not a live crash-proxy**.

## Status

- **Off** on this template. Copy [`schemas/golden-path/crash-inbox.example.json`](../schemas/golden-path/crash-inbox.example.json) only after a `[HUMAN]` DPIA.
- The GitHub App proxy is a separate follow-up ([CRASH_PROXY.md](CRASH_PROXY.md)) and also stays off.
- Do not add Sentry, Firebase Crashlytics, Bugsnag, or any proprietary crash SDK on the FOSS path.
- Do not ship a DSN, endpoint, or client token in git.

## Allowed providers (after DPIA)

| Provider | Notes |
|----------|--------|
| `none` | Template default |
| `glitchtip` | Self-hosted FOSS inbox (Sentry-compatible protocol) |
| `bugsink` | Self-hosted FOSS crash inbox |
## [HUMAN] before enable

1. Complete the DPIA checklist in [PRIVACY.md](PRIVACY.md).
2. Record lawful basis, retention, and abuse controls in `DECISION_LOG.md`.
3. Host GlitchTip or Bugsink on infrastructure you control; rate-limit.
4. Set `crash_inbox.enabled` in `bootstrap.config.json` only after those steps.
5. Keep capture opt-in and sanitized ([crash-capture](features/crash-capture.md)).

`scripts/check-crash-inbox.sh` fails if the stub is enabled or a live DSN is present.
