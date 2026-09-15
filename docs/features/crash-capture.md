# Feature: crash-capture

> Opt-in local crash queue. Never auto-sends. Sanitize before persist.

## Acceptance criteria

- ✅ User-visible behavior: after a captured crash, one review dialog; never auto-open GitHub
- ✅ Offline/error behavior: write failure drops the record; handler errors do not re-enter
- ✅ Accessibility: same dialog contract as Feedback
- ✅ i18n: uses `feedback.*` / `feedback_*`

## Smoke scenario

1. Given the save-crashes setting is off
2. When an unhandled error occurs
3. Then nothing is persisted
4. When the setting is on, at most one sanitized record is stored; turning the setting off deletes it

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| Logic | `examples/web/src/crash-capture/` | `examples/android/.../crashcapture/` |
| Tests | `pendingCrash.test.ts` | `PendingCrashTest.kt` |
| Wiring | `appBootstrap.ts` ≤10 lines | `GoldenPathApp.kt` / `MainActivity` ≤10 lines |
## Tests

- Automated: yes — `pendingCrash.test.ts` and `PendingCrashTest.kt`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack <active>`

## Definition of Done

Unit tests for queue-at-most-one, sanitize-before-persist, opt-in false, no re-entry. Fallback: `[HUMAN]` manual throw smoke.

## Notes

- Web: `sessionStorage` unless save-crashes is on (`localStorage`)
- Android: one app-internal file; chain previous `UncaughtExceptionHandler`

## WorkManager-free crash trim

Golden Path Android crash capture stays **in-process** (sanitize + optional local pending file). Do **not** add WorkManager / JobScheduler upload workers on the FOSS path — that expands background network surface and conflicts with “stub only until DPIA”. Trim stacks in the capture path; upload stays opt-in after DPIA (`docs/CRASH_INBOX.md`).
