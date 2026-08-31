# Changelog

All notable changes to this template will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),

and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.37.1](https://github.com/edwardlthompson/DevPulse/compare/v0.37.0...v0.37.1) (2026-08-31)


### Fixed

* **scripts:** reconfigure UTF-8 encoding on stdout/stderr in changelog_unreleased ([882c997](https://github.com/edwardlthompson/DevPulse/commit/882c99727936b5a674db4f3fe30155c3c59efb22))


### Changed

* normalize CHANGELOG unreleased heading after v0.37.0 release ([5136d4a](https://github.com/edwardlthompson/DevPulse/commit/5136d4a379e40b2290fde17c146fa00a0141a8b8))


### Documentation

* record v0.37.0 session retrospective in AGENT_MEMORY ([6f2cee7](https://github.com/edwardlthompson/DevPulse/commit/6f2cee7dc84d3cd842f6e7664cdaa7d5f2cfc41b))

## [Unreleased]

## [0.37.0](https://github.com/edwardlthompson/DevPulse/compare/v0.36.0...v0.37.0) (2026-08-31)

### Added

* **ui:** add scrubbable year scrollbar, clean navigation flow, and move ideas to settings ([916f11e](https://github.com/edwardlthompson/DevPulse/commit/916f11eda54621d5aa54a201265ab83ab839eb56))
- Interactive scrubbable scrollbar on the main app inventory list with live scrubbing and animated year callout badges (`AppListScroller` and `AppYearScrubber`)
- Integrated "What to build" (Ideas) into the Settings Hub with category gap tracking and quiet app analysis
- "Select All" / "Deselect All" sources toggle action in Sources settings for streamlined repository selection
- Automatic settings and data persistence backup/restore across updates (`SettingsPersistence` and backup extraction rules)

### Changed

* normalize CHANGELOG unreleased heading after v0.36.0 release ([e5fe764](https://github.com/edwardlthompson/DevPulse/commit/e5fe764b9ed829d27d6bc227f2e88b39267b1ff7))
- Removed redundant "Back to settings" / "Back to apps" buttons across menus and app details in favor of natural Android back navigation with preserved menu history
- Removed redundant legacy Local Scan radar action in top bar in favor of full multi-source Refresh

### Fixed

- Default `auroraPlayEnabled` to `true` and prioritize direct download resolution before Play Store redirect fallback

## [0.36.0](https://github.com/edwardlthompson/DevPulse/compare/v0.35.0...v0.36.0) (2026-08-31)

### Added

* **inventory:** add APKPure parser and secondary store download fallback ([be54622](https://github.com/edwardlthompson/DevPulse/commit/be5462216f4e97fd5abe17443bf67f534aae4a6a))

### Fixed

* **build:** ensure release build type is deterministic when keystore is unset ([aba5d96](https://github.com/edwardlthompson/DevPulse/commit/aba5d96dda3ca038435bb96b86e59f4ad9167039))

## [0.35.0](https://github.com/edwardlthompson/DevPulse/compare/v0.34.2...v0.35.0) (2026-08-30)

### Added

* **inventory:** signer replace installation flow and ignored updates management ([a615863](https://github.com/edwardlthompson/DevPulse/commit/a615863a5a6bca216cee2556338a1ff4426fda5e))
* **inventory:** version history and one-tap rollback support ([1252a88](https://github.com/edwardlthompson/DevPulse/commit/1252a880c7e7f5240af94482fd542533f94b9671))

### Documentation

* record v0.34.2 ship notes and fold lessons ([b21762e](https://github.com/edwardlthompson/DevPulse/commit/b21762e34f514955da6cdfff918ed6c8737d7e05))

## [0.34.2](https://github.com/edwardlthompson/DevPulse/compare/v0.34.1...v0.34.2) (2026-08-30)

### Added

- Version history section displaying the last 5 versions per app on the app detail card with one-tap rollback support via staged APK download and installer handoff
- Daily background scan interval option in scan settings and periodic update notifications on completion

### Fixed

- Android 14+ foreground service crash when scheduling periodic background WorkManager scans by declaring `dataSync` foreground service type
- Staged APK installation after uninstall in signer-replace flow commits package installer session directly without UI blocking or losing queued downloads
- IgnoredUpdates records download and scraping failures from batch updates to prevent unserviceable listings from re-prompting
- Version delta line only displays when an actionable update exists, preventing ghost update deltas for unserviceable listings
- Play Store installed apps are restricted from offering unlisted non-Play remote update sources

### Added

- Ignored update listings view in Settings History with package, source, and version details
- One-tap Reset ignored listings action in Settings History and Settings Inventory

## [0.34.2](https://github.com/edwardlthompson/DevPulse/compare/v0.34.1...v0.34.2) (2026-08-28)

### Fixed

- GitHub Refresh reads release tags and APK assets for hinted repos so Has update matches Obtainium instead of a stale F-Droid version (or none)
- Required status checks `CI`, `Security Scan`, and `CodeQL` are published as concluding jobs so `main` merges no longer wait on names that never appear
