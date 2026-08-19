# Privacy Policy (Draft)

> Customize for your application. Required when collecting any user data.

## Data We Collect

| Data | Purpose | Lawful Basis | Retention |
|------|---------|--------------|-----------|
| _Example: app settings_ | _Feature functionality_ | _Legitimate interest_ | _Until user deletes_ |
| _Example: crash logs (opt-in)_ | _Debugging_ | _Consent_ | _90 days_ |

## App update checks

- Release endpoint: GitHub Releases API or configured manifest URL
- Stored locally: `last_checked`, `installed_artifact_format`, `check_interval`
- No PII transmitted

## Data We Do Not Collect

- No tracking without explicit opt-in
- No sale of personal data
- No PII in logs without user consent

## User Rights (GDPR / CCPA)

- **Access:** Users can request a copy of their data
- **Deletion:** Users can request data deletion
- **Opt-out:** Telemetry and analytics are opt-in only
- **Portability:** Export settings where technically feasible

## Data Minimization

- Collect only what each feature requires
- Use local-first storage where possible
- Anonymize or aggregate analytics data

## DPIA Checklist (`[HUMAN]`)

If processing EU personal data:

- 🔲 Document processing purpose and legal basis
- 🔲 Assess necessity and proportionality
- 🔲 Identify risks and mitigations
- 🔲 Record in `DECISION_LOG.md` or ADR

## Contact

Privacy inquiries: see maintainers in `.github/CODEOWNERS` or `SECURITY.md`.
