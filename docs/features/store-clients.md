# Feature: store-clients

> Settings links for every scanned store, plus APKPure file download. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory` and `dev.foss.goldenpath.index.apkpure`. No live network in unit tests.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `StoreClients` | object | Catalog of Play Store fallback and APKMirror only |
| `FdroidRepoLink` | object | `fdroidrepo://` add-repo URIs with SHA-256 fingerprints (not accounts) |
| `ApkPureDirect` | object | `resolve` runs get_app_update for one package and keeps `asset.url` when it is an APK |
## Acceptance criteria

- ✅ User-visible: Settings → External pages lists Play Store fallback and APKMirror only
- ✅ F-Droid family toggles download indexes in DevPulse; they do not open F-Droid or Droid-ify
- ✅ APKPure Update uses `asset.url` in-app; missing file is a failed download (no APKPure app)
- ✅ Offline/error: failed APKPure resolve does not open a store app
- ✅ Accessibility: each row and extra link has a content description
- ✅ i18n: `store_clients_*`, `store_client_*`

## Smoke scenario

1. _Given_ APKPure `update-ok.json` and an APKPure-listed app with no artifact yet
2. _When_ `ApkPureDirect.resolve` runs
3. _Then_ memory has `https://d.apkpure.com/...` and OneClick downloads that file

### Critique

| Issue | Resolution |
|----|---|
| Null/empty package | `ApkPureDirect.resolve` returns null; `StoreClients.open` ignores blank URLs |
| Network timeout | Fetch `Result` → failed download; no invented URL |
| Race conditions | Artifact memory still synchronized; button disables while busy |
| Unhandled exceptions | `runCatching` on `startActivity`; stage still uses cert/package identity |
## Notes

- Aptoide listing taps use `https://en.aptoide.com/app?package_name=` (Games and Store). Classic `aptoidesearch://` does not resolve on Aptoide 10 Games.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
