# Module A: Android / F-Droid Pure Compliance

> Activate when your stack includes Android or F-Droid distribution.

## Requirements (Verbatim)

- **Absolute FOSS Isolation:** No commercial or proprietary closed-source SDKs are permitted (e.g., no Google Play Services, Firebase, or closed telemetry trackers). Rely exclusively on open alternatives (e.g., UnifiedPush or native OS providers).
- **Reproducible Build Environment:** Lock all compiler toolchains and build dependencies using cryptographic hashes or strict versioning. Enforce determinism by eliminating compilation timestamps (using SOURCE_DATE_EPOCH or platform-equivalent) to ensure byte-for-byte reproducible binaries matching F-Droid verification targets.

## Activation Checklist

- 🔲 Confirm no proprietary SDKs in `build.gradle.kts` / `build.gradle` dependencies
- 🔲 Set SOURCE_DATE_EPOCH in build scripts and CI
- 🔲 Pin Gradle wrapper (`gradlew`, `gradle-wrapper.jar`, `gradle-wrapper.properties`) and dependency versions
- 🔲 Review `examples/android/` Golden Path stub
- 🔲 Add [ADB] tasks to BUILD_PLAN for device/emulator verification
- 🔲 Document F-Droid metadata path (Fastlane or manual) — validate with `bash scripts/verify-fdroid-metadata.sh`

## Operations Checklist

- 🔲 Crash reporting via FOSS channel only (no proprietary trackers)
- 🔲 UnifiedPush or native OS notification provider configured
- 🔲 Reproducible build verified locally (`bash scripts/verify-reproducible-apk.sh` or CI `android-release`)
- 🔲 Signing keys stored outside repo; CI uses protected secrets
- 🔲 Rollback procedure documented in docs/RUNBOOK.md
- 🔲 F-Droid submission checklist reviewed before release

## Design system

- 🔲 Read docs/DESIGN_GUIDE.md before UI work
- 🔲 Use Jetpack Compose Material 3 via GoldenPathTheme (see examples/android/)
- 🔲 Edit tokens in design-tokens/design-tokens.json; run scripts/sync-design-tokens.py
- 🔲 Branding: `branding/assets/` + generated `res/drawable/ic_brand_mark.xml` (store PNGs still `[ADB]`/`[HUMAN]`)
- 🔲 Theme toggle: system / light / dark (DataStore persistence)
- 🔲 Edge-to-edge: `GoldenPathScaffold`, `bottomInsetPadding()`, inset-aware `SnackbarHost`
- 🔲 FOSS only: androidx.compose.* and androidx.datastore (no Play Services / Firebase)

## Localization

Strings are separate from styles. Theme colors and spacing live in `ui/theme/`; all user-visible copy lives in resource files.

| Layer | Path | API |
|-------|------|-----|
| Strings | `res/values/strings.xml` | `stringResource(R.string.*)` in Compose |
| Styles | `ui/theme/` (generated `Color.kt`, `Type.kt`, `Dimens.kt`) | `MaterialTheme.colorScheme`, `Dimens.kt` |
| Forbidden | Kotlin string literals in composables | Use `stringResource`, not `Text("...")` |
Default locale: English only (`res/values/strings.xml`). Add `res/values-{lang}/strings.xml` when shipping translations. Plurals: `res/values/plurals.xml` when needed.

Shared key naming with web: `app.title`, `theme.toggle.label`, `theme.mode.*` — see [`docs/DESIGN_GUIDE.md`](../../docs/DESIGN_GUIDE.md). For website folder conventions in multi-stack repos, see [`docs/WEB_PROJECT_LAYOUT.md`](../../docs/WEB_PROJECT_LAYOUT.md).

- ✅ In-app AboutScreen with format-locked APK update stub and donations
## Golden Path Reference

See `examples/android/` for FOSS Gradle/Kotlin skeleton. CI runs `./gradlew assembleDebug` on every push to `main`.

## Instrumented tests (CI)

Optional emulator job **Android - connectedDebugAndroidTest** in `.github/workflows/ci.yml` runs `MainActivitySmokeTest` via `reactivecircus/android-emulator-runner` (API 34, x86_64, **AOSP `default` target** — no Google APIs). Runs when `examples/android/**` changes (or on `workflow_dispatch`). Local equivalent:

```bash
cd examples/android
./gradlew connectedDebugAndroidTest

```

Requires an AVD or USB device (`[ADB]`). Robolectric unit tests remain the default fast path in `feature-gate.sh`.

**System bar / nav mode verification:** `bash scripts/verify-android-insets.sh` — sets 3-button and gesture nav via adb, runs `NavBarInsetUiTest` bounds checks. Optional `--screencap`. Manual Settings only if adb `settings put` fails on OEM.

## Feature gate (Sprint 2+)

After each feature step, `scripts/feature-gate.sh` runs (via `watch-agent-gates.sh`):

| Stage | Command |
|-------|---------|
| Unit + compile | `./gradlew test` in `examples/android/` |
Requires `JAVA_HOME` locally; gate exits `2` when Java is missing.

## Owner Labels for This Module

| Task type | Label |
|-----------|-------|
| Scaffold Gradle, Kotlin code, tests | AGENT |
| Emulator/device testing, F-Droid submit | ADB |
| FOSS dependency audit approval | HUMAN |
| CI Gradle compile / structure validation | AUTO |
## F-Droid Submission Dry-Run Checklist

`[ADB]` dry-run before first F-Droid release. Full metadata lives under `examples/android/metadata/` when present.

### Build reproducibility

- 🔲 Set `SOURCE_DATE_EPOCH` (fixed Unix timestamp) in release build scripts and CI
- 🔲 Run `bash scripts/verify-reproducible-apk.sh` locally (or rely on CI `android-release` job; CI fails on hash drift)
- 🔲 Confirm no proprietary SDK grep failures match CI (`android-structure` job)
- 🔲 Verify Gradle wrapper and dependency lockfiles committed

### Metadata and policy

- 🔲 Complete F-Droid `metadata/` (`summary`, `description`, `license`, `sourceCode`, `build` blocks)
- 🔲 Screenshots and feature graphic paths valid (Fastlane or manual `metadata/en-US/`)
- 🔲 Version code/name align with `CHANGELOG` and tag
- 🔲 Anti-feature flags accurate (ads, tracking, non-free network services)

### Device verification (ADB)

- 🔲 Install release APK on physical device or emulator: `adb install -r app/build/outputs/apk/release/*.apk`
- 🔲 Smoke test cold start, core flow, offline behavior, and notification path (if applicable)
- 🔲 Capture `adb logcat` during smoke test; confirm no crash stack traces
- 🔲 Uninstall/reinstall upgrade path from previous release version

### Submission dry-run

- 🔲 Open draft merge request to [fdroiddata](https://gitlab.com/fdroid/fdroiddata) or run `fdroid lint` locally if using repomaker workflow
- 🔲 Child repos: copy `examples/android/metadata/` text blocks; add `build` recipe YAML in fdroiddata MR (template documents handoff only)
- 🔲 Record maintainer notes in `BUILD_PLAN.md`; mark blockers ❌ [ADB] with reason
- 🔲 `[HUMAN]` sign off before tagging store release
