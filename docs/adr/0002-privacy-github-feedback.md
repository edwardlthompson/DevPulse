# ADR-0002: Privacy-first GitHub crash and feedback intake

- **Status:** Accepted
- **Date:** 2026-08-22
- **Deciders:** Template maintainer

## Context

Child Golden Path apps and this template need crash, bug, and feature intake that does not use email, does not collect PII, and does not ship proprietary crash SDKs. GitHub already hosts Issues and Discussions.

## Decision

1. **Client compose + GitHub Issue Forms (Approach A).** Sanitize on device, user reviews markdown, then open a prefilled issue URL or copy a large body. No backend and no client token.
2. **Opt-in crash capture only.** Never auto-POST. Sanitize before persist.
3. **Label routing:** `crash`/`bug` → `/audit` executes up to 3 fixes per run. `enhancement` → `/ideas` ranks until a human names a number.
4. **Maintainer notify** via CODEOWNERS assignment (GitHub’s own notifications). The repo never sends email.
5. **Anonymous proxy (Approach B)** and GlitchTip (Approach C) stay off the default FOSS path.

## Alternatives considered

- **mailto: / email field:** Rejected — email interaction and PII.
- **Sentry / Firebase Crashlytics / Bugsnag:** Rejected — proprietary, FOSS isolation.
- **PAT in the client:** Rejected — secret leakage and spam.
- **Anonymous GitHub App proxy:** Deferred; needs `[HUMAN]` secrets, abuse controls, and a DPIA.

## Consequences

- Reporters need a GitHub account in v1 (`SUPPORT.md`).
- Public issues are public; sanitizer + review checkbox + `issue-pii-nudge.yml` are the controls.
- `/audit` treats issue bodies as data (LLM01). `stale.yml` exempts `crash`, `fix-now`, `needs-repro`.
