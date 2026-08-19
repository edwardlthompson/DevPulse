# Design Guide

> Cross-stack visual contract for Golden Path UI. Read after your active `modules/{stack}/MODULE.md`. For website folder roles and GitHub Pages hosting, see [`docs/WEB_PROJECT_LAYOUT.md`](WEB_PROJECT_LAYOUT.md).

## Principles

1. **Single token source** — edit colors, spacing, and typography only in [`design-tokens/design-tokens.json`](../design-tokens/design-tokens.json), then run `scripts/sync-design-tokens.py`.
2. **Styles and strings are separate** — never put user-visible copy in CSS, Kotlin string literals, or TypeScript markup. Use `strings.xml` (Android) or `locales/*.json` (web).
3. **No raw hex in UI code** — use generated CSS variables or Compose `MaterialTheme` colors.
4. **Layout survives translation** — flexible widths, logical properties, no fixed-height text containers.

## Token workflow

```bash
# Edit design-tokens/design-tokens.json, then:
python3 scripts/sync-design-tokens.py

```

Generated outputs (do not hand-edit):

| Output | Stack |
|--------|-------|
| `examples/web/src/design-tokens.css` | Web |
| `examples/web/src/theme-meta.json` | Web PWA meta |
| `examples/android/.../ui/theme/Color.kt` | Android |
| `examples/android/.../ui/theme/Type.kt` | Android |
| `examples/android/.../ui/theme/Dimens.kt` | Android |
## Theme modes (system / light / dark)

Both UI stacks support three modes. Default is **system** (follow OS preference).

| Mode | Android | Web |
|------|---------|-----|
| System | `isSystemInDarkTheme()` | `data-theme="system"` + `prefers-color-scheme` |
| Light | `LightGoldenPathColors` | `data-theme="light"` |
| Dark | `DarkGoldenPathColors` | `data-theme="dark"` |
- **Android:** `ThemeToggle` in top app bar; persisted via DataStore (`ThemePreferences`).
- **Web:** `ThemeToggle` button; persisted in `localStorage` key `gp-theme`; updates `<meta name="theme-color">`.

Accessibility: toggle labels come from i18n keys (`theme.toggle.label`, `theme.mode.*`), not hardcoded English.

## Android (Compose Material 3)

- Wrap screens in `GoldenPathTheme(themeMode) { ... }`.
- Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` — not hardcoded colors or `sp` in composables.
- All text via `stringResource(R.string.*)`.
- Spacing via `SpacingMd`, `RadiusMd`, etc. from generated `Dimens.kt`.
- Alignment: `Alignment.Start` / `End`, not `Left` / `Right`.
- Buttons: `Modifier.widthIn(min = 48.dp)` minimum touch target; avoid fixed widths for labels.

### Android system bars (edge-to-edge)

- Call `enableEdgeToEdge()` in `MainActivity`; keep status and navigation bar colors **transparent** via `ApplySystemBarStyle`.
- Use `GoldenPathScaffold` (not raw `Scaffold`) — sets `contentWindowInsets = WindowInsets.safeDrawing` and an inset-aware `SnackbarHost`.
- Bottom-fixed actions and snackbars: `Modifier.bottomInsetPadding()` from `ui/insets/` (includes 48dp fallback when 3-button nav reports zero inset).
- Wrap the app in `NavigationModeProvider`; verify detected mode on About (`about.debug_navigation_mode` string).
- **Do not use `Toast`** for in-app feedback — it ignores Compose insets and renders under the nav bar. Use `SnackbarHost` on the scaffold.
- Automated verification: `bash scripts/verify-android-insets.sh` (adb sets 3-button/gesture, runs `NavBarInsetUiTest`).

Allowed FOSS dependencies: `androidx.compose.material3`, `androidx.compose.material:material-icons-extended`, `androidx.datastore`. **Never** add `com.google.android.gms` or Firebase.

## Web (CSS variables)

- Import `design-tokens.css` in `style.css`.
- Use `var(--gp-color-*)`, `var(--gp-space-*)`, `var(--gp-text-*)`.
- Layout: `margin-inline`, `padding-block`, `text-align: start` for RTL safety.
- Respect `prefers-reduced-motion: reduce` (see `style.css`).
- Initialize theme with `initTheme()` before first paint when possible.

## Localization

### Android

- English seed: `res/values/strings.xml`
- Additional locales: `res/values-{lang}/strings.xml` (add when you ship translations)
- Plurals: `res/values/plurals.xml` when needed

### Web

- Catalogs: `src/locales/{locale}.json`
- API: `t(key)`, `setLocale(locale)`, `getLocale()` from `src/i18n/index.ts`
- Set `document.documentElement.lang` on locale change

### Shared key naming

Keep keys aligned across stacks:

```
app.title, app.greeting, app.status.online, app.status.offline
theme.toggle.label, theme.mode.system, theme.mode.light, theme.mode.dark

```

### Layout rules for long strings / RTL

- Use `max-width` + natural text wrap; avoid `height` on text blocks.
- Do not size buttons to English-only copy — use `min-width` / padding.
- Web: `dir="auto"` on `<html>`; Android: `android:supportsRtl="true"` in manifest.

## Agent checklist (before UI PR)

- 🔲 Tokens changed only in `design-tokens/design-tokens.json` with sync run
- 🔲 Branding assets updated under `branding/assets/` when the mark changes; sync run
- 🔲 No `#RRGGBB` literals in UI source (except generated files and `branding/assets/*.svg`)
- 🔲 No string literals in composables or `main.ts` markup
- 🔲 Theme toggle still cycles system → light → dark
- 🔲 `scripts/check-design-cohesion.sh` passes

## Branding pack

Product identity (logos, pitch copy, official color sheet) lives under [`branding/`](../branding/). See [`branding/BRANDING.md`](../branding/BRANDING.md).

| Edit | Then run |
|------|----------|
| Colors / type / spacing in `design-tokens/design-tokens.json` | `python3 scripts/sync-design-tokens.py` |
| Logos / favicon / heroes in `branding/assets/` | `python3 scripts/sync-design-tokens.py` |
| Name, tagline, pitch in `branding/product.json` | `python3 scripts/generate-project-readme.py` |
Sync also writes `branding/official-colors.css`, copies web public icons, and emits Android `ic_brand_mark.xml`.

**README modes:** `"mode": "template"` (upstream default) writes only `branding/generated/README.preview.md`. Child repos set `"mode": "product"` so the generator overwrites root `README.md` with a pitch-quality README. Never set product mode on the template itself.

## Extending the system

Add new semantic colors to `design-tokens.json` under `color`, re-run sync, then reference via `MaterialTheme` or CSS vars. For new components, copy patterns from `GoldenPathScreen` (Android) or `main.ts` + `style.css` (web) — do not introduce one-off styles.

## About screen

Cross-stack in-app About (not GitHub repo About):

| Key prefix | Purpose |
|------------|---------|
| `about.title`, `about.close`, `about.open` | Navigation |
| `about.version`, `about.format` | Installed metadata |
| `about.update.interval.*` | Check interval selector (`off`, `daily`, `weekly`, `monthly`, `on_session`) |
| `about.update.current`, `about.update.available`, `about.update.no_compatible`, `about.update.restarting` | Status copy |
| `about.donations.*` | Optional donation encouragement |
**Update rules:** persist `installed_artifact_format` on first run; `selectReleaseAsset()` exact match only; seamless apply + single restart with `pending_restart` guard.

**Platform parity:** Web applies updates via `applyUpdate.ts` and shows `about.update.restarting` during the restart guard. Android persists `pending_restart` in DataStore and surfaces `about_update_restarting` in `GoldenPathApp` (UI stub only — no in-app APK apply in the exemplar).

**Donations:** external links only; hide block when `donations.json` disabled or empty.
