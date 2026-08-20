# Threat Model

> MASVS-minded model for DevPulse. Link security tasks in `BUILD_PLAN.md`.

## Scope

| Item | Value |
|------|-------|
| Project | DevPulse |
| Stack | Android (Kotlin, Jetpack Compose) |
| Methodology | STRIDE + MASVS for mobile. OWASP LLM Top 10 only for agent-exposed repo tooling |
## Trust Boundaries

```text
[User] --> [DevPulse app]
              |-- PackageManager / optional usage stats (device)
              |-- EncryptedSharedPreferences (optional GitHub token)
              |-- Room cache (indexes, scan results)
              |-- HTTPS: F-Droid indexes, Play public HTML, forge APIs
         No backend. F-Droid build server is an operator, not a runtime host.

```

## STRIDE Summary

| Threat | Example | Mitigation | Owner |
|--------|---------|------------|-------|
| Spoofing | Fake GitHub or index host | HTTPS; verify F-Droid signature when feasible; else checksum plus documented limit | AGENT |
| Tampering | Poisoned extra-repo index or HTML | Signature/checksum; fixture tests; unknown on parse fail; never guess dates | AGENT |
| Repudiation | User denies a scan | Local scan history only; no remote audit log | AGENT |
| Information disclosure | Token in logcat; inventory uploaded | EncryptedSharedPreferences; never log token; no analytics | AGENT |
| Denial of service | Play/forge 429; huge index | Token bucket, persisted backoff, degrade to cache | AGENT |
| Elevation of privilege | Abuse QUERY_ALL_PACKAGES or usage stats | Explain-before-ask; usage stats optional; no export off-device by default | AGENT |
## MASVS-minded controls

- **QUERY_ALL_PACKAGES:** sensitive. In-app rationale before scan (FR-4). Used only to build inventory.
- **Optional usage stats:** settings walkthrough; never required (FR-5).
- **Token storage:** EncryptedSharedPreferences only. Never log (FR-13).
- **Scrape or API abuse by us:** honest User-Agent, serial-ish Play, honour 403/429, persist backoff. Do not impersonate a browser.
- **Scrape or API abuse against us:** no backend; local cache only. Widget and notify stay on-device.
- **Cache poisoning:** treat extra-repo and Play HTML as untrusted. Failed or unsigned parse → unknown. Room is not a trust root.

## Top Abuse Cases

1. A malicious extra-repo index paints a malware APK as freshly maintained
2. Play HTML change or 403 silently becomes a guessed "updated" date
3. GitHub title search binds the wrong repo and a dead app looks green
4. A leaked GitHub token in logs or backups
5. QUERY_ALL_PACKAGES used without an honest rationale, failing store review

## OWASP LLM Top 10

Walk agent-exposed surfaces against [OWASP LLM Top 10 (2025)](https://owasp.org/www-project-top-10-for-large-language-model-applications/). The Android app has no LLM runtime. Controls below apply to repo agents only.

| ID | Risk | Template control |
|----|------|------------------|
| LLM01 Prompt Injection | Untrusted text steers tools | Validate at boundaries; never execute untrusted text as system prompts |
| LLM02 Sensitive Information Disclosure | Token or inventory in prompts | No secrets in git; token never logged; `docs/PRIVACY.md` |
| LLM03 Supply Chain | Malicious dep | Dependabot, CodeQL, Trivy; FOSS Gradle/TOML grep |
| LLM04 Data/Model Poisoning | Tampered index treated as truth | Unknown on verify fail; fixtures for Play HTML |
| LLM05 Improper Output Handling | Model output executed as code | Never eval LLM output |
| LLM06 Excessive Agency | Agent can push or deploy | Honesty labels; `[HUMAN]` for destructive-ops |
| LLM07 System Prompt Leakage | Rules or secrets in prompts | Keep credentials out of rules and `AGENTS.md` |
| LLM08 Vector/Embedding | Retrieval injection | N/A |
| LLM09 Misinformation | Over-trust of model output | Critique table; gates; fixture tests |
| LLM10 Unbounded Consumption | Token or cost DoS | File budgets 300/150 |
## Security Tasks

Mitigations land in Sprints 2–6 feature files and `docs/SECURITY_TRIAGE.md` weekly triage. `[HUMAN]` enables Dependabot alerts and private reporting.

## Review Cadence

- `[HUMAN]` Review at each milestone boundary
- `[AGENT]` Update when architecture or data flows change (append ADR reference)
