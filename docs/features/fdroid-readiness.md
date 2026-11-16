# Feature: fdroid-readiness

> F-Droid source-side metadata. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible: store listing uses root `fastlane/` (title, short, full, NonFreeNet, icon, one real screenshot)
- ✅ Offline/error: N/A — packaging metadata
- ✅ Accessibility: N/A
- ✅ i18n: en-US fastlane copy only

## Smoke scenario

1. Given `schemas/golden-path/app-version.json` is 1.0.0 / 10000
2. When Gradle reads the SoT and F-Droid scrapes root fastlane
3. Then versionCode 10000 matches changelog + tag `v1.0.0`, and LICENSE starts with canonical GPL-3.0 text

## Container map

| Layer | Path |
|-------|------|
| Logic | `schemas/golden-path/app-version.json`, `examples/android/app/build.gradle.kts` |
| View | `fastlane/metadata/android/en-US/` |
| Tests | `tests/test_fdroid_readiness.py` |
| Wiring | none |

## Tests

- Automated: yes — `tests/test_fdroid_readiness.py`
- Coverage: version SoT, LICENSE, signingConfig, NonFreeNet, screenshot dummy gate

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Definition of Done

See `docs/FEATURE_MODULES.md` per-feature checklist.
