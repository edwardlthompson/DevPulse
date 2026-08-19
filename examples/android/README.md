<p align="center">
  <img src="../../branding/assets/logo-mark.svg" alt="Golden Path" width="64" />
</p>

# Golden Path Android (FOSS)

FOSS apps with a clear path from idea to release — FOSS-only Gradle/Kotlin skeleton (no Google Play Services or Firebase). Brand kit: [`branding/BRANDING.md`](../../branding/BRANDING.md).

## Repository layout

```text
examples/android/
  app/src/main/
    res/values/strings.xml       # user-visible strings (English default)
    res/values-{lang}/           # add when shipping translations
    java/.../ui/
      theme/                     # GoldenPathTheme, generated Color.kt / Type.kt / Dimens.kt
      components/                # ThemeToggle, etc. — labels via stringResource()
      screens/                   # GoldenPathScreen, etc.

```

**Styles and strings are separate:** theme colors and spacing live in `ui/theme/` (from `design-tokens/`). All copy lives in `strings.xml`, consumed via `stringResource(R.string.*)` in Compose — never `Text("literal")`.

See [`docs/DESIGN_GUIDE.md`](../../docs/DESIGN_GUIDE.md) and [`docs/WEB_PROJECT_LAYOUT.md`](../../docs/WEB_PROJECT_LAYOUT.md) for cross-stack conventions.

Optional task runner (not required for CI): install [just](https://github.com/casey/just), then `just test` (needs Android SDK).

## Why these tools?

Gradle + Kotlin + Compose is the FOSS-friendly Android stack. We pin the wrapper hash and `SOURCE_DATE_EPOCH` so F-Droid-style reproducible builds are possible, and we ban Play Services so the Golden Path stays redistributable.

## Structure validation (CI)

CI validates Gradle file structure and FOSS compliance markers only. Full APK builds require local Android SDK.

## Local build (ADB / HUMAN tasks)

```bash
export SOURCE_DATE_EPOCH=1700000000
cd examples/android
./gradlew assembleDebug

```

## Emulator checklist

Before running instrumented tests or manual QA:

- 🔲 Android SDK Platform 34+ installed (`sdkmanager "platforms;android-34"`)
- 🔲 Build-tools 34.x installed
- 🔲 System image with Google APIs **not** required (use AOSP image for FOSS parity)
- 🔲 `adb devices` lists emulator or hardware as `device`
- 🔲 Set `SOURCE_DATE_EPOCH` for reproducible release builds (template default: `1700000000`)
- 🔲 Accept licenses: `sdkmanager --licenses`

## FOSS compliance

- No `com.google.android.gms` dependencies
- No Firebase dependencies
- `SOURCE_DATE_EPOCH` for reproducible builds
- Pinned Gradle wrapper SHA-256 in `gradle/wrapper/gradle-wrapper.properties`

## F-Droid notes

Document dependency hashes and reproducible build verification steps in your project's `AGENT_MEMORY.md` when activating module A.
