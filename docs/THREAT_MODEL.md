# Threat Model

> Draft during Sprint 1 before Golden Path ships. Link security tasks in `BUILD_PLAN.md`.

## Scope

| Item | Value |
|------|-------|
| Project | [PROJECT_NAME] |
| Stack | [INSERT PLATFORM / TECH STACK HERE] |
| Methodology | STRIDE (adapt per stack: OWASP ASVS for web, MASVS for mobile) + OWASP LLM Top 10 for agent-exposed APIs |
## Trust Boundaries

```text
[User] --> [Client App / PWA] --> [API / Backend] --> [Data Store]
                |                      |
           Local storage          External services

```

Document your actual boundaries after architecture is defined.

## STRIDE Summary

| Threat | Example | Mitigation | Owner |
|--------|---------|------------|-------|
| Spoofing | Fake API client | Auth tokens, TLS | AGENT |
| Tampering | Modified local state | Integrity checks, signed updates | AGENT |
| Repudiation | Denied user action | Audit logs (no PII without consent) | AGENT |
| Information disclosure | PII in logs | Data minimization, redaction | AGENT |
| Denial of service | Oversized payloads | Input limits, rate limiting | AGENT |
| Elevation of privilege | Bypass auth | Least privilege, boundary validation | AGENT |
## Top Abuse Cases

1. _Define after Golden Path — e.g., unauthorized data access_
2. _Supply-chain compromise via malicious dependency_
3. _Secret leakage via committed credentials_
4. _Prompt injection (if agent-exposed APIs)_
5. _Telemetry opt-out bypass_

## OWASP LLM Top 10

Walk agent-exposed surfaces against [OWASP LLM Top 10 (2025)](https://owasp.org/www-project-top-10-for-large-language-model-applications/). No extra scanner — map to existing gates. Prompt injection: [`.cursor/rules/destructive-ops.mdc`](../.cursor/rules/destructive-ops.mdc).

| ID | Risk | Template control |
|----|------|------------------|
| LLM01 Prompt Injection | Untrusted text steers tools | Validate at boundaries; never execute untrusted text as system prompts |
| LLM02 Sensitive Information Disclosure | Secrets/PII in prompts or logs | No secrets in git; opt-in telemetry; `docs/PRIVACY.md` |
| LLM03 Supply Chain | Malicious model, plugin, or dep | Dependabot, CodeQL, Trivy; no default marketplace install |
| LLM04 Data/Model Poisoning | Tampered training or RAG | Treat uploads as untrusted; N/A until child adds RAG |
| LLM05 Improper Output Handling | Model output executed as code | Never eval LLM output; tool allowlists only |
| LLM06 Excessive Agency | Agent can push, deploy, or drop | Honesty labels; hooks denylist; `[HUMAN]` for destructive-ops |
| LLM07 System Prompt Leakage | Rules or secrets in prompts | Keep credentials out of rules and `AGENTS.md` |
| LLM08 Vector/Embedding | Retrieval injection | N/A until child adds a vector store |
| LLM09 Misinformation | Over-trust of model output | Critique table; gates; regression tests on bug fixes |
| LLM10 Unbounded Consumption | Token or cost DoS | Token economy 300/150; no 80k playbooks |
Weekly walk: `docs/SECURITY_TRIAGE.md`.

## Security Tasks

Link mitigations to `BUILD_PLAN.md` and `docs/SECURITY_TRIAGE.md` weekly triage.

## Review Cadence

- `[HUMAN]` Review at each milestone boundary
- `[AGENT]` Update when architecture or data flows change (append ADR reference)
