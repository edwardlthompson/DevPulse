# Feature: aurora-play

> Sprint 17. Opt-in Play APK download via Aurora `gplayapi`, then Play Store fallback. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.index.aurora`. No live Play or auroraoss.com in unit tests. Never invent an APK URL. Never log the auth JSON.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `AuroraPlayFile` | data class | `url`, optional version |
| `AuroraPlayFiles` | fun interface | Returns purchase files for one package |
| `AuroraPlayDirect` | object | Maps a https Play CDN URL onto `UpdateArtifact` |
| `AuroraAuth` | object | Parses anonymous AuthData JSON; empty email → no session |
| `EncryptedAuroraAuthStore` | class | EncryptedSharedPreferences; token never logged |

### Functions

| Name | Contract |
|------|----------|
| `AuroraPlayDirect.resolve(pkg, files)` | First allowed https URL or null |
| `OneClickUpdate.apply` Play kind | `resolveAurora` then download; null → `market://details?id=` |
| `AuroraAuth.emailOf` / `parse` | Missing or blank email → null |

## Acceptance criteria

- ✅ User-visible: Settings → Scan sources shows Aurora next to Google Play; off by default
- ✅ Aurora Store app is not required; Play Store page is the only fallback
- ✅ Update tries Aurora download, then opens Play Store
- ✅ Offline/error: auth or purchase fail → Play Store; no invented URL
- ✅ Accessibility: Aurora toggle and Update label have content descriptions
- ✅ i18n: `aurora_play_*`, `update_one_click_aurora`, `store_client_aurora`

## Smoke scenario

1. _Given_ a Play-listed app and a fixture Play CDN URL
2. _When_ `AuroraPlayDirect.resolve` and `OneClickUpdate.apply` run
3. _Then_ the file installs; a null resolve opens `market://details?id=`

### Critique

| Issue | Resolution |
|----|---|
| Null/empty at boundary | Blank package, blank email, or rejected URL → null; Play Store fallback (`AuroraPlayDirectTest`, `OneClickUpdateTest`) |
| Network timeout | 25s HTTP; `runCatching` → empty files → Play Store |
| Race conditions | Artifact memory stays synchronized; Update button disables while busy |
| Unhandled exceptions | Live purchase wrapped in `runCatching`; identity inspect still required before install |

## Notes

- Uses FOSS `com.auroraoss:gplayapi` (GPL-3), same anonymous `https://auroraoss.com/api/auth` path APKUpdater uses. Google can break this. Opt-in only.
- Identity checks (package + cert + sha256) still apply. Android still confirms install unless Root silent is chosen.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
