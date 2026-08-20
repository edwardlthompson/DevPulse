# Privacy Policy

> DevPulse is completely local. Customize only if a later feature changes this. Status: 🔲 open · ✅ done · ❌ blocked.

## Data We Collect

DevPulse does not create accounts, analytics, or telemetry. Data stays on the device unless the user starts a scan or later opts into lookups or notifications.

Local notifications are device-only (scan progress and “release dates updated”). Nothing is sent to a push vendor.

| Data | Purpose | Lawful Basis | Retention |
|------|---------|--------------|-----------|
| Installed-app inventory (package, version, timestamps, SDK levels) | Staleness picture | Legitimate interest / user request | Until uninstall or user clears cache |
| Optional usage stats (if granted) | Rank by real use; Opportunity | Consent | Until permission revoked or data cleared |
| Optional Aptoide package lookups | Last-release date when F-Droid has no record | Consent (Settings, default off) | 24h cache until cleared |
| Optional GitHub token | Higher forge API limits | Consent | EncryptedSharedPreferences until user removes it |
| Scan cache and history | Degraded/offline results; history | User request | Until user clears cache |
| Pins and private notes | Keep-anyway and Develop-next | User request | Until user deletes them |
| Update-check prefs (`last_checked`, format, interval) | F-Droid-safe About stub | Legitimate interest | Local until cleared |
## Network

Network runs only when the user scans, or when they opt into later lookups or the About update-check stub.

- Play: public details HTML, on demand or during a user-started scan
- F-Droid and extra-repo indexes: official client-style index download
- Aptoide `app/getMeta` by package name, only if the user enables it and starts Refresh
- Forges: documented GitHub / optional GitLab / Codeberg APIs
- About update check: GitHub Releases or configured manifest only
- User-Agent: `DevPulse/0.1` plus the public GitHub URL. No browser impersonation

No PII is transmitted. The optional token is an Authorization header to GitHub only and is never logged.

## Data We Do Not Collect

- No tracking, crash reporters, ads, or accounts
- No cloud sync or social features
- No sale of personal data
- No FCM or other proprietary push

## User Rights (GDPR / CCPA)

- **Access:** Export scan reports (CSV/JSON) when that feature ships
- **Deletion:** Clear cache, revoke usage-stats, delete the token, or uninstall
- **Opt-out:** Usage stats, Aptoide lookup, notifications, Play/forge lookups, and the GitHub token are opt-in
- **Portability:** Full-scan and focused Opportunity exports

## Data Minimization

- QUERY_ALL_PACKAGES is explained before the first scan and is used only to list installed apps
- PACKAGE_USAGE_STATS is never required
- Play lookup stays optional and on-demand so F-Droid anti-features stay NonFreeNet at most
- Failed lookups store last error, not guessed dates

## DPIA Checklist (`[HUMAN]`)

If processing EU personal data beyond on-device inventory:

- 🔲 Document processing purpose and legal basis
- 🔲 Assess necessity and proportionality
- 🔲 Identify risks and mitigations
- 🔲 Record in `DECISION_LOG.md` or ADR

## Contact

Privacy inquiries: see maintainers in `.github/CODEOWNERS` or `SECURITY.md`.
