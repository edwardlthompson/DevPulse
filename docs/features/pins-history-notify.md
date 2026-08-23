# Feature: pins-history-notify

> Sprint 15. Ideas 3, 5, 6. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

| Name | Contract |
|------|----------|
| `PinRules.hideFromRedList` | Unchanged; pins persist in DataStore |
| `ScanHistory.wentQuiet(prev, next)` | Packages that became Red |
| `PulseHistory.note` | Append refresh/scan/update rows to `scan-history/pulse_log.tsv`; drop rows older than 30 days |
| `StaleCrossing.newlyCrossed(before, after, days)` | First time `daysSinceActivity` reaches 180 or 365 |
| `StaleNotifyWorker` | Weekly WorkManager; NotificationManager only; no FCM |
## Acceptance criteria

- ✅ Pin on app detail hides the row from the red/stale list
- ✅ Scan history shows how many went quiet since last scan
- ✅ Opt-in notifications for 6-month and 1-year crossings
- ✅ The same opt-in posts a local notice when fetchable updates exist
- ✅ Offline: all local; worker no-ops without inventory permission

## Smoke scenario

1. _Given_ a red app is pinned
2. _When_ stale-only is on
3. _Then_ that app is hidden until unpinned
