# Design Guide

> Cross-stack visual contract for Golden Path UI (tokens, Settings-only chrome, i18n paths). Read after your active `modules/{stack}/MODULE.md`. **Quality bar for any screen you add:** [`ux-ui-guidelines.md`](ux-ui-guidelines.md) (construction law; `/ux-review` uses the same checklist). For website folder roles and GitHub Pages hosting, see [`docs/WEB_PROJECT_LAYOUT.md`](WEB_PROJECT_LAYOUT.md).

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
- **Android:** Settings → Appearance dropdown; persisted via DataStore (`ThemePreferences`).
- **Web:** `initTheme()` plus Settings `<select>` (`[data-settings-theme]`); persisted in `localStorage` key `gp-theme`; updates `<meta name="theme-color">`.
- **Never** put a theme control in the home app bar. Do not use `FilterChip` / chip rows for exclusive theme (or other enum) choices.

Accessibility: dropdown option labels come from i18n keys (`settings.theme.mode.*`), not hardcoded English.

## Chrome and menus

Child apps should look calm on first paint. Material 3, Apple Settings, and Nielsen Norman grouping all agree: **few chrome actions, grouped lists, one control per row**.

### Home chrome

- **One** trailing action: Settings (labeled on web; icon + `contentDescription` on Android).
- Theme, About, donate, and feedback **never** appear in the header / `TopAppBar`.
- Off home: Android shows Back only; web keeps the panel Close control and hides the header Settings button.

### Menu IA (Settings, then About)

Sort for scan, not exploration. Section headers + dividers; no chip clouds.

| Order | Settings | About |
|-------|----------|-------|
| 1 | Appearance (theme dropdown) | App (version, format, update status) |
| 2 | Privacy (switches) | Support (donate links, when enabled) |
| 3 | Data (export/import when the stack has it) | Feedback (dropdown: bug / feature) |
| 4 | About (navigation row → App info) | |
### Control vocabulary

| Need | Use | Do not use |
|------|-----|------------|
| Exclusive enum (theme, feedback kind) | Dropdown (`<select>` / `ExposedDropdownMenu`) | `FilterChip`, radio chip rows, header icons |
| Boolean | Switch on the row | Extra toolbar toggles |
| Navigate to a screen | Full-width row + short hint | Duplicate header icons |
| Filters / tags on a collection | Chips | Settings enums |
Touch targets stay ≥ 44px / 2.75rem. Section labels use title-small / uppercase label color (`onSurfaceVariant`) so rows stay the readable layer.

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

Compose 1.12 (BOM `2026.08.00`): brand colors stay in tokens — do not replace `Color.kt` with `MeshGradientPainter`. Skip `Modifier.onFirstVisible` (deprecated). Credential Manager text-field autofill is commercial-only.

## Web (CSS variables)

- Import `design-tokens.css` in `style.css`.
- Use `var(--gp-color-*)`, `var(--gp-space-*)`, `var(--gp-text-*)`.
- Layout: `margin-inline`, `padding-block`, `text-align: start` for RTL safety.
- Respect `prefers-reduced-motion: reduce` (see `style.css`) for **theme and nav transitions** — zero non-essential motion when the user prefers reduced motion.
- Contrast: `python3 scripts/agent-run.py check-token-contrast` (WCAG 2.2 AA on token pairs).
- Initialize theme with `initTheme()` before first paint when possible.

## Localization

### Android

- English seed: `res/values/strings.xml`
- Second catalog: `res/values-es/strings.xml` (Spanish). More locales: `res/values-{lang}/strings.xml`
- Plurals: `res/values/plurals.xml` when needed

### Web

- Catalogs: `src/locales/{locale}.json` (`en` + `es`; `setLocale` / `navigator.language`)
- API: `t(key)`, `setLocale(locale)`, `getLocale()` from `src/i18n/index.ts`
- Set `document.documentElement.lang` on locale change

### Shared key naming

Keep keys aligned across stacks:

```
app.title, app.greeting, app.status.online, app.status.offline
nav.back, settings.section.*, settings.about, about.section.*, about.feedback.*
settings.theme.mode.system, settings.theme.mode.light, settings.theme.mode.dark

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
- 🔲 Home chrome is Settings-only (no theme / About / donate in the header)
- 🔲 Theme is a Settings dropdown (system / light / dark); no chips for exclusive enums
- 🔲 `scripts/check-design-cohesion.sh` passes

## Branding pack

Product identity (logos, pitch copy, official color sheet) lives under [`branding/`](../branding/). See [`branding/BRANDING.md`](../branding/BRANDING.md). Parent template mark (not synced into apps): [`branding/template/`](../branding/template/). Child rebrand: `/brand`.

| Edit | Then run |
|------|----------|
| Colors / type / spacing in `design-tokens/design-tokens.json` | `python3 scripts/sync-design-tokens.py` |
| Logos / favicon / heroes in `branding/assets/` | `python3 scripts/sync-design-tokens.py` |
| Name, tagline, pitch in `branding/product.json` | `python3 scripts/generate-project-readme.py` |
Sync also writes `branding/official-colors.css`, copies web public icons, and emits Android `ic_brand_mark.xml`.

**README modes:** `"mode": "template"` (upstream default) writes only `branding/generated/README.preview.md`. Child repos set `"mode": "product"` so the generator overwrites root `README.md` with a pitch-quality README. Never set product mode on the template itself.

### Splash / first paint

- **Same mark** for favicon, app icon, and Android 12+ SplashScreen (`Theme.GoldenPath.Splash` → `ic_brand_mark`).
- Background = surface token (`splash_background` day/night); no content gated behind splash; perceived flash ≤ ~300ms.
- Web: `theme-color` + favicon match tokens so cold start does not flash a different palette.
- Full rules: [`branding/BRANDING.md`](../branding/BRANDING.md) § Splash / first paint.

## Extending the system

Add new semantic colors to `design-tokens.json` under `color`, re-run sync, then reference via `MaterialTheme` or CSS vars. For new components, copy patterns from `GoldenPathScreen` (Android) or `main.ts` + `style.css` (web) — do not introduce one-off styles.

## About screen

Cross-stack in-app About (not GitHub repo About):

| Key prefix | Purpose |
|------------|---------|
| `about.title`, `about.close`, `about.open` | Navigation |
| `about.version`, `about.format` | Installed metadata |
| `about.update.install`, `about.update.later`, `about.update.message` | Installer prompt (never includes donate) |
| `about.update.current`, `about.update.available`, `about.update.no_compatible`, `about.update.restarting` | Status copy |
| `about.donate`, `about.donate.nudge.*`, `about.not_now` | Quiet Venmo donate + once-per-version note |
| `about.donations.*` | Optional donation encouragement |
**Update rules:** compare installer filenames (`{Prefix}-X.Y.Z-x64-setup.exe` / `{prefix}-X.Y.Z-foss.apk`), not git tags; daily GitHub check; Later silences that version. PWA `applyPwaUpdate()` stays About-only.

**Platform parity:** Launch prompts are donate-or-update, never both. Web `localStorage` (`gp.update.*`); Android SharedPreferences `gp_updates` excluded from Auto Backup.

**Donations:** external Venmo (or `donations.json`) links only; hide block when disabled or empty. Never put donate on the update dialog. **Web and Android:** donate links live under Settings → App info (About) only — never in the header / TopAppBar. Walkthrough (GitHub Sponsors, Liberapay, Open Collective, PayPal, etc.): [`docs/help/DONATIONS.md`](help/DONATIONS.md).
