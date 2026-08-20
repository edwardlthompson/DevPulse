# Feature: store-clients

> Settings links for every scanned store, plus APKPure file download. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.inventory` and `dev.foss.goldenpath.index.apkpure`. No live network in unit tests.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `StoreClients` | object | Catalog of Play, F-Droid, Droid-ify, Izzy/Guardian/Calyx repo pages, Aptoide, APKMirror, APKPure, GitHub |
| `ApkPureDirect` | object | `resolve` runs get_app_update for one package and keeps `asset.url` when it is an APK |

## Acceptance criteria

- ✅ User-visible: Settings → Store apps lists every scanned source with Play / F-Droid / official APK / website when that channel exists
- ✅ Aptoide Games (`aptoidegames` launcher) shows Install Aptoide Store and opens `https://en.aptoide.com/download`
- ✅ APKPure Update tries `asset.url` first (APKUpdater path); `market://details?id=` opens the APKPure app when there is no file
- ✅ Offline/error: failed fetch opens the APKPure app or website fallback
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
| Network timeout | Fetch `Result` → open APKPure app / website; no invented URL |
| Race conditions | Artifact memory still synchronized; button disables while busy |
| Unhandled exceptions | `runCatching` on `startActivity`; stage still uses cert/package identity |

## Notes

- Aptoide listing taps use `https://en.aptoide.com/app?package_name=` (Games and Store). Classic `aptoidesearch://` does not resolve on Aptoide 10 Games.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
