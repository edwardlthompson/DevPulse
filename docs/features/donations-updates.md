# Feature: donations-updates

> Sprint 18. Continuum Calendar donate + self-update method. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Public API (locked)

Types in `dev.foss.goldenpath.about`. Prefs stay in `devpulse_updates` SharedPreferences on this device. No peer sync. No donate copy on the update dialog.

### Types

| Name | Kind | Contract |
|------|------|----------|
| `ProductUpdate.NamedAsset` | data class | Release asset `name` + download `url` |
| `ProductUpdate.ProductAsset` | data class | Installer `version` from the filename + `url` |
| `ProductUpdatePrefs` | class | `last_check_at`, `last_seen_version`, `dismissed_version` |
| `ProductReleaseFetcher.Parsed` | data class | `htmlUrl` + named assets |

### Functions

| Name | Contract |
|------|----------|
| `shouldCheckDaily(last, now)` | True when last is null/negative or ≥24h elapsed |
| `parseApkVersion(name)` | `DevPulse-X.Y.Z.apk` or `devpulse-X.Y.Z-foss.apk`; git tags → null |
| `isNewerVersion(current, latest)` | Semver compare on three numeric parts |
| `shouldPromptUpdate` | Newer filename version and not dismissed |
| `shouldNudgeDonate` | False on first run (null last seen); true only after a version change |
| `ProductReleaseFetcher.fetchLatest` | GitHub latest; User-Agent; 10s timeout; fail → null |

## Acceptance criteria

- ✅ User-visible: **Donate via Venmo** in About and Settings. Never on the update dialog
- ✅ First run records the installed version. No donate popup
- ✅ After a version change, one optional note: Development is still going. Either button records seen
- ✅ Daily GitHub check compares installer filenames, not git/template tags
- ✅ Install opens the asset URL (fallback: release page). Later silences that version
- ✅ Failed fetch, timeout, empty assets, or same version: stay silent
- ✅ Offline/error: no block, no retry storm
- ✅ Accessibility: dialogs are `AlertDialog`; donate buttons have content descriptions
- ✅ i18n: `about_donate*`, `about_not_now`, `about_later`, `about_install`, `about_update_prompt_*`

## Smoke scenario

1. _Given_ a later launch after `0.26.0` recorded `last_seen_version`
2. _When_ the installed version is `0.27.0`
3. _Then_ the donate note appears once; a newer `DevPulse-0.28.0.apk` can prompt Install / Later on a later launch, never in the same dialog

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Blank version, empty assets, or missing filename semver → no prompt (`ProductUpdateTest`, `ProductReleaseFetcherTest`) |
| Network timeout | 10s connect/read; catch → null; app continues |
| Race conditions | Donate returns before the daily fetch; dialogs are exclusive |
| Unhandled exceptions | Fetch and URI opens wrapped; invalid JSON → null |

## Notes

- Venmo URL is public, not a secret: `https://venmo.com/code?user_id=1857304970395648420`
- Repo: `edwardlthompson/DevPulse`
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
