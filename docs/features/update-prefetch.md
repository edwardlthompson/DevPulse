# Feature: update-prefetch

> Sprint 12. Cache trusted update APKs and show expandable notes. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory` and `dev.foss.goldenpath.update`. No live network in unit tests. Never guess a date or a changelog.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `UpdateNotes` | data class | `text`, `source` — only text that came from that source |
| `UpdateArtifact` | data class | `packageName`, `source`, `downloadUrl`, optional version/sha256/`localPath`/nativeCodes |
| `ApkIdentity` | object | sha256 + package + signer + ABI overlap before a cache file is ready |
| `UpdatePrefetch` | object | Opt-in, unmetered-only candidate list; never installs |
| `UpdateNotesMemory` | object | In-memory notes by package for the current process |
| `UpdateArtifactMemory` | object | Direct file URLs by package; best source first |
### Functions

| Name | Contract |
|------|----------|
| `FdroidWhatsNew.parse(chunk)` | `localized.*.whatsNew` or none; never invent text |
| `GitHubReleaseParser` notes | Release `body` when present; missing body → no notes |
| `ApkPureMetaParser` | `asset.url` (APKUpdater); skip `/XAPK`; no invented URL |
| `FdroidApkUrl.of` | `{repo}/` + `apkName` from index-v1 `packages` |
| `FdroidApkFiles.namesIn` | Highest `apkName` plus 64-hex `hash` and `nativecode` when present |
| `AptoideMetaParser` | `file.path` when it is an https APK URL |
| Failed HTTP | No file on disk; listing stays as today |
## Acceptance criteria

- ✅ User-visible: detail page expands notes when F-Droid what'sNew or GitHub release body exists
- ✅ Offline: missing notes stay hidden (not “unknown changelog”); APK cache is the next row
- ✅ Accessibility: expand/collapse is a button with a content description
- ✅ i18n: `update_notes_*` (cache strings wait for the APK row)
- ✅ User never has to open a website to fetch an APK we already have a direct URL for
- ✅ Prefetch is opt-in. Identity-safe candidate only (same cert + ABI/locale). Silent install is Root-only after the user picks it
- ✅ APKMirror stays page-only; Play downloads only when the opt-in Aurora toggle is on
- ✅ i18n: `update_cache_*`

## Smoke scenario

1. _Given_ an F-Droid app object with `whatsNew` and a GitHub release JSON with `body`
2. _When_ the parsers run
3. _Then_ notes text matches the fixture; empty/missing fields yield no notes

## Notes

- Play has no public FOSS file URL. Opt-in Aurora (`gplayapi`) can fetch a Play CDN URL; failure opens the Play Store (`market://details?id=`). Off by default. See `docs/features/aurora-play.md`.
- APKPure uses the same `get_app_update` `asset.url` APKUpdater downloads (https, not XAPK).
- Also direct: F-Droid/Izzy `apkName` from a small index **or** the package HTML we already fetch, plus repo APK base; GitHub `browser_download_url` ending in `.apk`; Aptoide `file.path`.
- APKMirror stays last-resort web; no partner download in this slice.
- Settings: `update_prefetch_*`. Prefetch runs after Refresh when the toggle is on and the network is unmetered.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`

### Critique

| Issue | Resolution |
|---|---|
| Null/empty notes or APK URL | Hide notes; skip prefetch; no invented changelog |
| Network timeout | Leave previous cache; do not delete a good file |
| Race (two Refresh jobs) | Existing `ReleaseRefreshRuntime.tryBegin()` |
| Unhandled download exception | `Result` + ignore that sha256; user can retry |
| Wrong-market APK | Cert + locale inspect before the file is marked ready |
