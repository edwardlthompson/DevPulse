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
| Optional APKMirror / APKPure package lookups | Listing, version, and dump-site date when enabled | Consent (Settings, default off) | Until next Refresh |
| Optional GitHub token | Higher forge API limits | Consent | EncryptedSharedPreferences until user removes it |
| Optional GitHub starred list | Match starred repos to apps already installed | Consent (Settings toggle; token already saved) | Requested live from GitHub; not stored; never logged |
| Scan cache and history | Degraded/offline results; history | User request | Until user clears cache |
| Pins and private notes | Keep-anyway and Develop-next | User request | Until user deletes them |
| Release notes from F-Droid what'sNew or GitHub release body | Show what changed on app detail | User request (Refresh) | Until process ends or next Refresh |
| Update-check prefs (`last_checked`, format, interval) | F-Droid-safe About stub | Legitimate interest | Local until cleared |
| Product update prefs (`last_check_at`, `last_seen_version`, `dismissed_version`) | Daily GitHub installer check and one donate note per version | Legitimate interest | Device-local SharedPreferences until cleared; not peer-synced |
## Network

Network runs when the user scans, opts into later lookups, or when the daily GitHub self-update check is due.

- Play: public details HTML, on demand or during a user-started scan
- F-Droid and extra-repo indexes: official client-style index download
- Aptoide `app/getMeta` by package name, only if the user enables it and starts Refresh
- APKMirror `app_exists` and APKPure `get_app_update` in batches, only if the user enables those outlets and starts Refresh
- Forges: documented GitHub / optional GitLab / Codeberg APIs
- Optional starred scan: `GET /user/starred` up to 5 pages when the user taps Scan in Settings (token required; star names are not logged)
- About / launch update check: GitHub `releases/latest` once per 24 hours; User-Agent `DevPulse/{version}`; 10s timeout; fail stays silent
- User-Agent: `DevPulse/0.1` plus the public GitHub URL. No browser impersonation
- Optional APK file download (user tap): APKPure `asset.url`, F-Droid/Izzy repo APK, GitHub release asset, or Aptoide `file.path`. Files stay in app cache until the user installs or clears cache.
- Optional Root install: local `su` / `pm install` only. No install traffic leaves the device.

No PII is transmitted. The optional token is an Authorization header to GitHub only and is never logged.

## Data We Do Not Collect

- No tracking, crash reporters, ads, or accounts
- No cloud sync or social features
- No sale of personal data
- No FCM or other proprietary push
- Donation taps open Venmo in the browser; DevPulse does not collect payment data

## User Rights (GDPR / CCPA)

- **Access:** Export scan reports (CSV/JSON) when that feature ships
- **Deletion:** Clear cache, revoke usage-stats, delete the token, or uninstall
- **Opt-out:** Usage stats, Aptoide / APKMirror / APKPure lookups, notifications, Play/forge lookups, the GitHub token, and starred scan are opt-in
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
