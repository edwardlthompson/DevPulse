# Android — Commercial distribution patterns

> Not used on FOSS bootstrap (`foss-compliance.mdc`). For `--distribution-tier commercial` only.

## Allowed (when documented)

- Google Play Services (FCM, Maps) with privacy policy updates
- Firebase Analytics/Crashlytics — opt-in telemetry only
- Play Store distribution alongside or instead of F-Droid

## Trade-offs

| Choice | Benefit | Cost |
|--------|---------|------|
| Play Services push | Reliable delivery | Proprietary dependency; F-Droid exclusion |
| Firebase | Crash/analytics | Closed-source SDK; GDPR disclosures |
| Play Store | Wider reach | Store fees; review process |

## FOSS path

Default template uses UnifiedPush and reproducible FOSS builds — see `modules/android/MODULE.md`.

## ADR

Record SDK choices in `docs/adr/` before adding proprietary dependencies.
