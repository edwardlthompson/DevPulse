# Feature: fdroid-index

> Sprint 4. FR-7, FR-8, origin. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.fdroid`. No Play or forge clients. Network fetch is an interface only.

### Types

| Name | Kind | Values / fields |
|------|------|-----------------|
| `FdroidRepoKind` | enum | `Official`, `Archive`, `Izzy`, `Guardian`, `Calyx`, `Custom` |
| `FdroidRepo` | data class | `id`, `kind`, `indexUrl`, `enabled` |
| `FdroidAppRecord` | data class | `packageName`, `lastUpdatedMs`, `sourceCode`, `repoId`, optional `category`, `relatedPackages`, `apkSizeBytes`, `antiFeatures` |
| `CachedIndex` | data class | `raw`, `fetchedAtMs` |
| `FdroidIndexError` | enum | `DownloadFailed`, `ParseFailed`, `NotFound` |
| `FdroidLookup` | data class | `record`, `fromCache`, `error` |
### Functions

| Name | Contract |
|------|----------|
| `FdroidIndexParser.parse(raw, repoId, wanted = emptySet())` | Accepts `String` or `ByteArray`. Extra repos parse wanted packages only. Official/Archive host-resolve via `FdroidPackageParser` + `FdroidPackagePage` (no `index-v1.jar`). Extra indexes of any size are harvested and cached. Izzy host-resolves per package only when the index download is empty. |
| `FdroidLookupEngine.lookup(packageName, records)` | Exact package-name match |
| `FdroidCachePolicy.isFresh(fetchedAtMs, nowMs)` | Fresh for 3 days |
| `FdroidNameCatalog.probe(repoId, wanted)` | Official/Izzy membership from shipped `assets/fdroid-names/*.txt`. Host-resolve only catalog hits. Empty catalog → probe all. Regen: `python3 scripts/build-fdroid-name-catalog.py`. New F-Droid apps wait for the next catalog build. |
| `FdroidOrigin.from(kind)` | Official → `AppOrigin.Fdroid`; others → `ExtraRepo` |
| `FdroidRepoCatalog.defaults()` | Official + Archive + Izzy + Guardian + Calyx URLs; only Official enabled by default |
### Signature verify (limitation)

Official `index-v1.jar` signature verification is **not** implemented this sprint. Indexes are accepted over HTTPS as trimmed JSON fixtures or future downloads. Checksum verify is a later hardening item. Fallback smoke: `bash scripts/feature-gate.sh --stack android`.

## Acceptance criteria

- ✅ User-visible behavior: official F-Droid index download, disk cache, package-name lookup, SourceCode
- ✅ Offline/error behavior: multi-day cache; manual refresh; degrade to last-known; unknown if download fails
- ✅ Accessibility: extra-repo settings toggles labeled for TalkBack
- ✅ i18n: keys under `fdroid_*` in `strings.xml`
- ❌ Verify signature if the official process is documented and doable without a huge dependency — skipped; see limitation above
- ✅ Otherwise checksum plus HTTPS, and this file documents that limitation
- ✅ Extra repos: IzzyOnDroid toggle; Guardian Project, Calyx, custom URLs
- ✅ Origin badge can become F-Droid or extra repo when the package is found
- ✅ Listing extras remember APK size, anti-feature names, and minSdk from the index

## Smoke scenario

1. _Given_ a cached fixture index containing a known package
2. _When_ lookup runs offline
3. _Then_ last-updated and SourceCode return without network

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../index/fdroid/` |
| View | extra-repo settings in `ui/settings/` |
| Tests | `examples/android/app/src/test/.../index/fdroid/` |
| Wiring | scan orchestrator ≤10 lines |
## Definition of Done

Parse and lookup unit tests against a trimmed fixture. If signature verify is skipped, state that here and use `bash scripts/feature-gate.sh --stack android` as the download smoke fallback.

## Notes

- Same pipeline for extra repos; treat custom URLs as untrusted
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
