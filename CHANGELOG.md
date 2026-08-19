# Changelog

All notable changes to this template will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),

and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.21.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.20.0...v0.21.0) (2026-08-18)


### Added

* harden Windows PATH, Unreleased fold, and ship gates ([8eab392](https://github.com/edwardlthompson/agent-project-bootstrap/commit/8eab392edff967bbc0cfabb89aff0902a6b8ac0e))


### Fixed

* import rp_merge_status from cwd-relative scripts/lib ([df322af](https://github.com/edwardlthompson/agent-project-bootstrap/commit/df322af79d3302457f6dad26fac0d2e424544a15))


### Documentation

* **release:** archive v0.20.0 ship memory and decisions ([f54927e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f54927eb378a5e413f6b8346f2699ba0ef19ea58))

## [0.20.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.19.0...v0.20.0) (2026-08-17)

### Added

* add first-run backlog and Windows upgrade-sim gates ([14811be](https://github.com/edwardlthompson/agent-project-bootstrap/commit/14811be2f4290841e8bf99bbd002facc50ff8e6e))

### Fixed

* ignore pruned stack links in the docs gate ([d7a3ba0](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d7a3ba099736f2194e08ec5d4d75ae6c4704935c))
* keep portable-purpose test valid after child init stamp ([5084b4b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5084b4b07542c583371e2e4e0277ac81731f42c1))
* strip CR from jq paths in template-index gate ([812a2db](https://github.com/edwardlthompson/agent-project-bootstrap/commit/812a2db71bca3929334d913848826bedffb4d80f))

### Documentation

* **release:** archive v0.19.0 ship memory and decisions ([3e55a0b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/3e55a0b2b1a896cea8c699a4ba612274b91729d5))
* rewrite template README as a first-run pitch ([01e21fc](https://github.com/edwardlthompson/agent-project-bootstrap/commit/01e21fc0f559a56e09b69172e0d06b763ed11d75))

## [0.19.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.18.3...v0.19.0) (2026-08-17)

### Added

* add coach layer and complete bootstrap defaults ([2f77fb9](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2f77fb9a65ad1f1f963e69e276d43972e57b54e5))
* add portable first-run tour and multi-agent adapters ([7944f1b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7944f1b2a76322c5b96dc666922fa0c88d0ce628))

### Documentation

* **release:** archive v0.18.3 ship memory and decisions ([3b572d8](https://github.com/edwardlthompson/agent-project-bootstrap/commit/3b572d823820e4ed2bd0a4c6729da98ae925b573))

## [0.18.3](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.18.2...v0.18.3) (2026-08-16)

### Changed

* **android:** Compose BOM 2026.06.01 → 2026.08.00 (Dependabot #64)
* **deps:** Bump the android-dependencies group across 1 directory with 3 updates ([5ea63fd](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5ea63fd32634365566d5b5b4460848c3ce8b512d))
* **release:** prepare v0.18.3 release ([afbea39](https://github.com/edwardlthompson/agent-project-bootstrap/commit/afbea39e98346091d9524f04ba1ae0e85b8ed389))

### Documentation

* **release:** archive v0.18.2 ship memory and decisions ([9ce74d1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/9ce74d177ab0dce65170df2ece370142907227e0))

## [0.18.2](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.18.1...v0.18.2) (2026-08-16)

### Fixed

* **ci:** move write `permissions` to the job that needs them (Scorecard TokenPermissions)

### Changed

* **deps-dev:** Bump the web-dependencies group across 1 directory with 5 updates ([40f1ff8](https://github.com/edwardlthompson/agent-project-bootstrap/commit/40f1ff879ec039beeff60e666e6c0ced5d582705))
* **deps:** Bump the android-dependencies group across 1 directory with 2 updates ([cec8ce1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/cec8ce1704a4fa7e235ebb02eb06ca2d9b32fcf2))
* **deps:** Bump the github-actions group across 1 directory with 3 updates ([a1910d1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a1910d121b0839548902968c0bbacdb76ce19c8e))
* **deps:** Bump the node-dependencies group across 1 directory with 4 updates ([dbba532](https://github.com/edwardlthompson/agent-project-bootstrap/commit/dbba532ac842ea5cccc9e9d93bdba3c621b7156a))
* **security:** triage Scorecard SARIF and archive M35 HUMAN items ([132612d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/132612d8b2005e0c80437695fccd868f48c2c861))

### Documentation

* **security:** record M35 Scorecard SARIF triage (pin policy, stale hono/nanoid/postcss GHSAs)
* **release:** archive v0.18.1 ship memory and decisions ([83daf53](https://github.com/edwardlthompson/agent-project-bootstrap/commit/83daf53c2a403514f673d95f9fb30b2341dc73dd))

## [0.18.1](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.18.0...v0.18.1) (2026-08-15)

### Fixed

* **windows:** resolve Python via `scripts/lib/resolve-python.sh` (skip Store stub; `PY` is a single executable so `"$PY"` works)
* **gates:** restore About slice from `git checkout HEAD` if `verify-about-feature-gate` backup is missing

### Changed

* **release:** prepare v0.18.1 release ([b4fca9c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/b4fca9c1fb7014b96d95649e285916f23d9cd2e0))

### Documentation

* **changelog:** slim `[Unreleased]` after v0.18.0 so Release Please does not re-list shipped notes
* **kb:** record npm ci lockfile miss after browsers override ([33e961f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/33e961f857764ffd8f0f78f0b7a7e44c8073ede5))
* **release:** archive v0.18.0 ship memory and decisions ([54909bc](https://github.com/edwardlthompson/agent-project-bootstrap/commit/54909bc8447bc767de70c297361771eff50592d4))

## [0.18.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.17.0...v0.18.0) (2026-08-15)

### Added

* **agent:** add prior-art thin steals and clear High Dependabot ([80dc2a6](https://github.com/edwardlthompson/agent-project-bootstrap/commit/80dc2a60caab73a9b1c097e97a5ee9dce4d54c20))

### Fixed

* **web:** lock proxy-agent 8 so npm ci matches browsers override ([be4333b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/be4333baa5dcf65044d03bc365aead8b939172e6))

### Documentation

* **build-plan:** archive human branding visual review for v0.17.0 ([f3f7e00](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f3f7e00a0726dce75bd989a5732bd40c1f059c31))
* **release:** archive v0.17.0 branding kit ship memory and decisions ([7b5fa0d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7b5fa0dfce8893d86610f205e695ce6a62b587f7))

## [0.17.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.16.0...v0.17.0) (2026-08-12)

### Added

* **branding:** add branding kit and pitch README generator ([2488b09](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2488b0962f0921e855c0c5afe6ed5af15a1337a1))

### Documentation

* **release:** archive v0.16.0 ship memory and decisions ([90ce3db](https://github.com/edwardlthompson/agent-project-bootstrap/commit/90ce3db8b678ec43cf53701d0c81a31afbe3345a))

## [0.16.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.15.2...v0.16.0) (2026-08-10)

### Added

* **quality:** Codex review, multi-stack autofix, expanded /prerelease ([13cbb5d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/13cbb5d5d4c1bbb84f08032bc692d292869e4485))

### Documentation

* **release:** archive v0.15.2 and update memory after /ship ([619e456](https://github.com/edwardlthompson/agent-project-bootstrap/commit/619e45675ec288c6067d0487f0289f6b53039f16))

## [0.15.2](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.15.1...v0.15.2) (2026-08-01)

### Fixed

* **security:** clear High npm alerts and harden plan critiques ([2b119a7](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2b119a78f088f91077ff96f3657c622bc9755395))
* **security:** override postcss &gt;=8.5.18 in examples/node ([981ee64](https://github.com/edwardlthompson/agent-project-bootstrap/commit/981ee64ae64560394f4cb8e0614ab6589610e60b))

### Documentation

* **changelog:** note postcss override in Unreleased security fix ([96c8fd0](https://github.com/edwardlthompson/agent-project-bootstrap/commit/96c8fd07892051df0e8ccff0ebcb7932b59c9ba3))

## [0.15.1](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.15.0...v0.15.1) (2026-07-22)

### Fixed

* **security:** clear Dependabot npm High/Moderate alerts ([647e4ed](https://github.com/edwardlthompson/agent-project-bootstrap/commit/647e4ede6fecfcd4278e3cb5b4708fdd9b621069))

### Changed

* **deps-dev:** Bump vite ([#40](https://github.com/edwardlthompson/agent-project-bootstrap/issues/40)) ([2f0c4ab](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2f0c4ab69684620cbab9b7a8fb3326b3506c486b))
* **deps:** Bump com.android.application ([#38](https://github.com/edwardlthompson/agent-project-bootstrap/issues/38)) ([dfa93c9](https://github.com/edwardlthompson/agent-project-bootstrap/commit/dfa93c97a9cc73504d8a2a684b737938143e33cb))
* **deps:** Bump the github-actions group with 2 updates ([#41](https://github.com/edwardlthompson/agent-project-bootstrap/issues/41)) ([2dfa0c1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2dfa0c15361a9e29a568e6b8d45c61a9183ff8c1))
* **deps:** Bump the node-dependencies group ([#39](https://github.com/edwardlthompson/agent-project-bootstrap/issues/39)) ([ad302c9](https://github.com/edwardlthompson/agent-project-bootstrap/commit/ad302c96fbe34edbd489938c25a2bdeafcb2d17c))

### Documentation

* **release:** archive M33 and v0.15.0 after /ship ([69de879](https://github.com/edwardlthompson/agent-project-bootstrap/commit/69de879396fb6387decffd5184e5bbd700fb7f40))

## [0.15.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.14.1...v0.15.0) (2026-07-22)

### Added

* **cursor:** integrate 3.9-3.11 surfaces and local-first compute ([5d2d129](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5d2d12920f68dc23e02071cfe2e43f04bd924f31))

### Fixed

* **changelog:** keep a single Unreleased section ([dcbea94](https://github.com/edwardlthompson/agent-project-bootstrap/commit/dcbea948a8cd0f3b49891bb1add66aa796906808))

### Documentation

* **release:** archive v0.14.1 and update memory after /push ([4cf495b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/4cf495b99f9a3f4a99ccf8a65eb8faf7c63e15df))
* restore DECISION_LOG seed line order after v0.14.1 archive ([22cada9](https://github.com/edwardlthompson/agent-project-bootstrap/commit/22cada93c8105712e2f9286c05424e642268f044))

## [0.14.1](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.14.0...v0.14.1) (2026-07-12)

### Fixed

* **ci:** heal Dependabot merge CI gap and automate M32 human gates ([4d195a2](https://github.com/edwardlthompson/agent-project-bootstrap/commit/4d195a2ed8944610fbdb011313f2fd9692c7445c))
* **ci:** index setup-automerge-token in TEMPLATE_INDEX ([e532c20](https://github.com/edwardlthompson/agent-project-bootstrap/commit/e532c209a03e562db051588af2e1fc0193381850))

### Changed

* **deps-dev:** Bump the web-dependencies group ([#34](https://github.com/edwardlthompson/agent-project-bootstrap/issues/34)) ([c3b88a6](https://github.com/edwardlthompson/agent-project-bootstrap/commit/c3b88a697782dab40beba72019146ad743ff9d31))
* **deps-dev:** Bump vite ([#32](https://github.com/edwardlthompson/agent-project-bootstrap/issues/32)) ([e1aca21](https://github.com/edwardlthompson/agent-project-bootstrap/commit/e1aca21ea6d515840ad00a73054dbff5883b514a))
* **deps:** Bump androidx.compose:compose-bom ([#30](https://github.com/edwardlthompson/agent-project-bootstrap/issues/30)) ([a0a4ff0](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a0a4ff0ee23d22f44f3911c2b635d4bacf58689f))
* **deps:** Bump the github-actions group with 2 updates ([#35](https://github.com/edwardlthompson/agent-project-bootstrap/issues/35)) ([97f98cf](https://github.com/edwardlthompson/agent-project-bootstrap/commit/97f98cfa4ff6f1844498c953bba70fe6e7b0ee45))
* **deps:** Bump the node-dependencies group ([#31](https://github.com/edwardlthompson/agent-project-bootstrap/issues/31)) ([829a79a](https://github.com/edwardlthompson/agent-project-bootstrap/commit/829a79a3bbb9f8a153f15379f7af31010fadf334))
* **deps:** Bump the node-dependencies group ([#33](https://github.com/edwardlthompson/agent-project-bootstrap/issues/33)) ([edc6127](https://github.com/edwardlthompson/agent-project-bootstrap/commit/edc61273e77328854cdfe8e191f8f30432216ae4))
* **release:** prepare v0.14.1 release ([6cad442](https://github.com/edwardlthompson/agent-project-bootstrap/commit/6cad442d9d91ff0ca92a37ad05bf6217229edd21))

### Documentation

* **build-plan:** archive M32 audit sprint after CI green ([2157d0b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2157d0bde69ebda696035ea198d4bc40f03bc16f))

## [0.14.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.13.2...v0.14.0) (2026-07-02)

### Added

* **cursor:** quiet agent shell with Python hooks and agent-run ([7fc8e4b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7fc8e4b455655acb68c5037e364ada7454177d01))

### Fixed

* **changelog:** remove duplicate Unreleased section for CI gate ([4c82890](https://github.com/edwardlthompson/agent-project-bootstrap/commit/4c8289072c7db231063be65e2a263be05f4af0bc))

### Changed

* **deps:** Bump the github-actions group across 1 directory with 3 updates ([#22](https://github.com/edwardlthompson/agent-project-bootstrap/issues/22)) ([a118247](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a118247a43a3a698315885423d13803ac2d40a71))

### Documentation

* **build-plan:** archive v0.13.2 release and slim active board ([2b708ca](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2b708ca7313076b19524cf1d989ea3b8f6ed7cd7))

## [0.13.2](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.13.1...v0.13.2) (2026-07-01)

### Fixed

* **ci:** re-pin Kotlin 2.3.20 for CodeQL after Dependabot [#26](https://github.com/edwardlthompson/agent-project-bootstrap/issues/26) ([42f4659](https://github.com/edwardlthompson/agent-project-bootstrap/commit/42f4659d373b8bd07172c6d091ebf02b6bebfb11))
* **cursor:** harden hook validation and complete M31 audit ([cd21e5a](https://github.com/edwardlthompson/agent-project-bootstrap/commit/cd21e5a70e8d2848bcbdf9d22b1e62daccff55c1))

### Changed

* **release:** prepare v0.13.2 release ([9932753](https://github.com/edwardlthompson/agent-project-bootstrap/commit/9932753e34b7ace26fa1aab1cd671c7b6b281896))

## [0.13.1](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.13.0...v0.13.1) (2026-07-01)

### Fixed

* **ci:** index release automerge paths; correct bypass docs ([5d4682d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5d4682d0ab6307005a584761534233d212104e2b))
* **ci:** pin Kotlin for CodeQL and repair weekly health workflow ([66d58f6](https://github.com/edwardlthompson/agent-project-bootstrap/commit/66d58f62767adc51b4b10c749d324baf1b2509e0))

### Changed

* **deps-dev:** Bump the web-dependencies group ([#25](https://github.com/edwardlthompson/agent-project-bootstrap/issues/25)) ([62278a1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/62278a14710173c01194c4a386f26bfc568f2b19))
* **deps:** Bump org.jetbrains.kotlin.plugin.compose ([#26](https://github.com/edwardlthompson/agent-project-bootstrap/issues/26)) ([84952c2](https://github.com/edwardlthompson/agent-project-bootstrap/commit/84952c26ceb365a412acfe22a3468679bd104055))
* **deps:** Bump the node-dependencies group across 1 directory with 5 updates ([#17](https://github.com/edwardlthompson/agent-project-bootstrap/issues/17)) ([5bb9730](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5bb973038e2ee3ec9d89dbad859e68651c3bb4a7))

## [0.13.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.12.0...v0.13.0) (2026-06-30)

### Added

* **release:** auto-merge Release Please PRs via workflow and script ([0e817c3](https://github.com/edwardlthompson/agent-project-bootstrap/commit/0e817c370bc44a01320815ee9cd266573b99b507))

### Fixed

* **release:** guard empty release-please pr output on merge push ([1d1b56c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/1d1b56c3010a7e048e9ca4fe197eb2b284d3df26))

## [0.12.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.11.1...v0.12.0) (2026-06-30)

### Added

* **agents:** autonomous /build with HUMAN/ADB automation ([cdea429](https://github.com/edwardlthompson/agent-project-bootstrap/commit/cdea429beecea4c9cf2e099d4fbea7b4e873f9f7))
* **agents:** parallel-first BUILD_PLAN and auto multi-agent dispatch ([f0a0b2f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f0a0b2fd0b93aa3a6aebdf38992c0337265cd7a2))
* **android:** add system bar insets and 3-button nav compensation ([cc946af](https://github.com/edwardlthompson/agent-project-bootstrap/commit/cc946af6eb590db574815de69e2ad955e993b250))
* **commands:** add /cleanup to archive BUILD_PLAN rows after build ([d7ab7fc](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d7ab7fc96d411be36ead3f1d31dc433041c30bb2))
* **cursor:** integrate FOSS hooks, skills, subagents, and feature radar (M30) ([1cd2b72](https://github.com/edwardlthompson/agent-project-bootstrap/commit/1cd2b726e8ac6a63cadfa5e63851e935fe1f53ac))
* **limits:** adopt static data 300L and pure logic 150L taxonomy ([49c7407](https://github.com/edwardlthompson/agent-project-bootstrap/commit/49c740719cfe8eb760a7d3cb4e675769cb296d27))
* **python:** enforce pytest in CI and workflow gates ([ec3939f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/ec3939f8569f95083643e20fba8970b13e0e180a))

### Fixed

* **ci:** use stack-presence job for conditional Python matrix ([ce6bfb3](https://github.com/edwardlthompson/agent-project-bootstrap/commit/ce6bfb3aa839fff8b75088e1c98f874ded2d89de))
* **deps:** cap undici override at 7.x for jsdom compat ([24b240f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/24b240f325b6b42363f638a2067c8a005b7c798a))

### Documentation

* log autonomous /build milestone in memory and decision log ([508a541](https://github.com/edwardlthompson/agent-project-bootstrap/commit/508a54103349d5b91b193913636afffaf3be6562))

## [0.11.1](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.11.0...v0.11.1) (2026-06-21)

### Fixed

* **release:** automate version sync and SBOM dispatch (M29) ([8b7d62e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/8b7d62ecbe5ee0fa4d1572fd870495bf51936460))
* **release:** parse Release Please PR number from JSON output ([57201aa](https://github.com/edwardlthompson/agent-project-bootstrap/commit/57201aa03595979196dff79c5c0327e7e70f4725))

### Documentation

* **build-plan:** unify repo-wide checklist status markers ([2370c0e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2370c0e68d0871a3b646644d588d5779c944f0b1))

## [0.11.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.10.0...v0.11.0) (2026-06-18)

### Added

* **android:** add F-Droid device dry-run scripts (M28) ([fb90878](https://github.com/edwardlthompson/agent-project-bootstrap/commit/fb9087846d424b088d37ed4e77ee13c672e9c0b6))

### Fixed

* **android:** Robolectric-safe UpdateApplierTest on Windows ([38635a3](https://github.com/edwardlthompson/agent-project-bootstrap/commit/38635a3d7948d0e592833c63612a76c3069b4314))
* **docs:** index template version sync scripts in TEMPLATE_INDEX ([367b650](https://github.com/edwardlthompson/agent-project-bootstrap/commit/367b650a1cd490f3ca181c42aaa4fe79020278cf))
* **release:** SBOM backfill from main; add template version sync gate ([d3b4c05](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d3b4c057db7470593244f46ed5d3dc9c04468a99))

### Changed

* **release:** sync version files to 0.10.0 ([36a02e4](https://github.com/edwardlthompson/agent-project-bootstrap/commit/36a02e4569d4d26bcacbc95b8e27a991a634d5b4))

### Documentation

* archive M28 weekly maintain audit ([d6de099](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d6de09991776cd54298fec2358a2534ba825f477))
* record v0.10.0 SBOM backfill complete ([f78dd18](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f78dd185fed55d2e5b225c7c5a55f5f8e637eaeb))

## [0.10.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.9.0...v0.10.0) (2026-06-17)

### Added

* **agents:** execute Sprint M15 P2 backlog and archive M14 ([a5f3199](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a5f3199255d5f897aa960da92cdcf6b09c0579df))
* **agents:** finish Sprint M15 instrumented CI and SBOM release wiring ([4d5993d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/4d5993df6602186f479de0fff0b2395ecb5860c3))
* **commands:** add batch instruction templates (M27) ([4fe713a](https://github.com/edwardlthompson/agent-project-bootstrap/commit/4fe713a51335c576058fc0e435bd419a272e8b8e))
* **docs:** add Cursor mode routing and repo sanity gates (M19-M25) ([9782e75](https://github.com/edwardlthompson/agent-project-bootstrap/commit/9782e757d95d06a27b1d03db27faf2a978e3c9c9))
* **m16-p2:** prune-optional smoke, a11y, i18n test fix, lightroom prune ([0212a19](https://github.com/edwardlthompson/agent-project-bootstrap/commit/0212a194012e884490e44d1f0194ed48756e9754))
* **m18-p2:** complete post-sequential backlog — tests, gates, CI, SW cache ([d2e088d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d2e088de9d7f97813a0ca66d4c4b3335455bf853))
* **m18:** remediate post-P2 review — Pages base path, Android apply, init config ([9105ebb](https://github.com/edwardlthompson/agent-project-bootstrap/commit/9105ebb129e660c288c65f6bef3ee7a67aeec4dd))
* **p2:** complete M17 P2 backlog — a11y, updates, CI, docs ([64ec578](https://github.com/edwardlthompson/agent-project-bootstrap/commit/64ec578a352d6d771c8d774d68da93f0fd01467d))

### Fixed

* **android:** ApkDownloadHelperTest uses file-as-parent path on Unix CI ([be237e7](https://github.com/edwardlthompson/agent-project-bootstrap/commit/be237e7d99778624ca3fe0c3a9b54ef8db74a856))
* **android:** pin androidx.test runner/rules to published 1.6.1 ([5195c46](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5195c463cbb219d490915a901bf350687b2dd98d))
* **android:** read INTERNET permission from PackageInfo in test ([5d9be3e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/5d9be3e9f3144468f84e2bdca0121788582e930c))
* **android:** test UpdateApplier intent build without startActivity ([27259c6](https://github.com/edwardlthompson/agent-project-bootstrap/commit/27259c65261fc44eac696565d01022e6f49c5ccd))
* **android:** write test APK under FileProvider cache-path ([f59023c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f59023cc342d456a653d27643a250ce9957b6abf))
* **ci:** move optional job existence checks into path-changes job ([f7213ec](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f7213eca78069907a08f181de996f516f9fa5341))
* **ci:** run android instrumented gradle in single shell script ([976a87f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/976a87ffda747d952a024f82623d8622cbce89dd))
* **ci:** scope design cohesion checks to active example stacks ([5130309](https://github.com/edwardlthompson/agent-project-bootstrap/commit/513030942117f1b3f93f8402ed40e12e1c2ae095))
* **ci:** stabilize PWA apply e2e and about-feature-gate for appBootstrap ([8946089](https://github.com/edwardlthompson/agent-project-bootstrap/commit/8946089ed2c265c1a5e4cd7996dea328b1a8c6e5))
* **docs:** index cursor-modes rule and align START_HERE paths (M26) ([8bbf689](https://github.com/edwardlthompson/agent-project-bootstrap/commit/8bbf68973260799ff6292416509590e37991246d))
* **e2e:** stabilize PWA apply test — assert button visibility only ([bda25d2](https://github.com/edwardlthompson/agent-project-bootstrap/commit/bda25d25cfed80f16ad7835df56c68a5eac4e458))
* **init:** skip design-token sync for pruned example stacks ([1844483](https://github.com/edwardlthompson/agent-project-bootstrap/commit/184448395220b7dc104dcf85c81b8f1f3e313bba))
* **m16:** tag-gate jobs-only poll, SBOM version assert, CI parity ([3f071cb](https://github.com/edwardlthompson/agent-project-bootstrap/commit/3f071cb9608910cb765ba5b465b3dfa6743df3dc))
* **m17:** INTERNET permission, update timing, prune index, release CI gate ([96829ae](https://github.com/edwardlthompson/agent-project-bootstrap/commit/96829aea0af3ff24a6942a9add8f9fb4c2e2364f))
* **release:** skip pre-release gate for SBOM backfill dispatch ([ac1377c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/ac1377c15840f428c6ec36095a4f7ac820d4f88b))
* **scripts:** stub settings preferences when verifying About removal ([d6b92a2](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d6b92a2c31c464bdd6595dcd49adf7fe305020e1))
* treat OWNER/REPO placeholder as unset for update checks ([2721c01](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2721c01d0cf4adb36ecf123aeb42c0c6429a5e80))
* **upgrade-sim:** skip full bootstrap validation after prune pass ([1634917](https://github.com/edwardlthompson/agent-project-bootstrap/commit/1634917f35ee9137ffb56b5c3a6e763025819ed7))
* **web:** override js-yaml to patch Dependabot alert [#9](https://github.com/edwardlthompson/agent-project-bootstrap/issues/9) ([609165c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/609165c6321cb2498c39541b5315ff19a533f7fb))
* **web:** use hasAttribute for disabled check in panelDialog ([f4d082e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f4d082ea1331446f45baeabd77f55818ec5a8cdf))

### Documentation

* **build-plan:** add Sprint M16 from post-M15 code review ([88050db](https://github.com/edwardlthompson/agent-project-bootstrap/commit/88050db8ce8c74a6bbee825ec3f4aab0ace60d36))
* **build-plan:** add Sprint M17 from post-M16 code review ([7cdc92b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7cdc92b6cc95f5961e46c37234bd5c2a8aada9e3))
* **build-plan:** archive M18 sprint body; slim active board ([2e3a4dd](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2e3a4ddea0aab5dffdd01cb44c6e76f335c562d3))
* **build-plan:** archive Sprint M16 and slim active board ([7e4a50e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7e4a50e24efa48c984f6d905af070a5e890825e0))
* **build-plan:** archive Sprint M17 and slim active board ([047e8e7](https://github.com/edwardlthompson/agent-project-bootstrap/commit/047e8e7e3cca2c3ab24155ff92cfeb27f85e0a23))
* **build-plan:** mark M15 CI sign-off complete on a5f3199 ([2ab8bca](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2ab8bca6a192ca421fb12b715bfd1e43c32f5ca9))
* **build-plan:** mark M16 row 10 CI sign-off complete ([7846d96](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7846d9604d84dd3c2d916482e5fabeeb7e0bafeb))
* **build-plan:** mark M18 sequential complete after CI green @ 2721c01 ([d4f7b82](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d4f7b82a8c8200e626ed21575cb7734c2a9a9b9b))
* **build-plan:** record v0.9.0 human release approval ([40e6eee](https://github.com/edwardlthompson/agent-project-bootstrap/commit/40e6eee079a9e8469993cb68f14d60d5329ca14c))
* log M27 batch commands decision at ship milestone ([17318ea](https://github.com/edwardlthompson/agent-project-bootstrap/commit/17318ea250d191b5eb9b477c2239d992a809a04a))

## [Historical — pre–Release Please consolidation]
- `scripts/check-cursor-hooks.sh`, `scripts/check-cursor-integrations.sh`, `scripts/sync-cursor-features.py`, `scripts/cursor-feature-radar.sh`
- `docs/CURSOR_INTEGRATIONS.md`, `docs/CURSOR_FEATURE_REGISTRY.json`, `docs/CURSOR_FEATURE_RADAR.md`
- `--distribution-tier foss|commercial` on `init-project.sh` / `.ps1`; commercial `.example` configs + `commercial-compliance.mdc`
- Weekly Cursor feature radar wired into `weekly-health-check.yml` and `/maintain`

### Documentation

- `docs/FILE_SIZE_GUIDE.md` — industry guidance on file size limits and app responsiveness (code splitting, locale namespaces, Lighthouse gates)
- Unified repo-wide checklist status markers: 🔲 open · ✅ done · ❌ blocked across `BUILD_PLAN.md`, module guides, PR template, feature specs, and security triage (readable in Markdown source and Preview)
- Agent docs (`START_HERE`, `FOR_AGENTS`, `INITIALIZATION_PROMPT`, `core-directives.mdc`) document emoji-only checklists; `scripts/agent-progress.sh` parses 🔲

### Added

- Autonomous `/build` — HUMAN/ADB automation router, failure backlog, and sprint status manifest
- `scripts/attempt-build-plan-row.sh` + `scripts/lib/human_task_automation.py` — pattern registry for HUMAN/ADB automation during `/build`
- `scripts/build-backlog.sh` + `HUMAN_BACKLOG.md` — failure-only backlog for unautomatable human tasks (dedupe on sprint|task)
- `scripts/build-sprint-status.sh` + `scripts/lib/build_sprint.py` — next-row manifest (`automate_human`, `automate_adb`, `execute`, `parallel_dispatch`)
- `agent-progress.sh set-parallel-sprint-done` — track parallel dispatch completion per sprint
- `scripts/check-build-plan-parallel.sh` — gate requiring ≥ 2 AGENT Parallel rows per sprint (or documented exception)
- `scripts/setup-agent-worktrees.sh` — optional git worktrees for parallel agent isolation
- Parallel-first BUILD_PLAN protocol: decomposition checklist, `### Parallelization` in Plan Mode, `/scope` auto Task dispatch
- `agent-progress.sh set-step` — track parallel completion for idempotent `/build`
- `scripts/check-python-pytest-workflow.sh` — require pytest in deps, pyproject config, CI, and feature-gate when Python stack is present
- M27 batch instruction templates: 26 slash commands (21 atomic + 5 super) in `.cursor/commands/`
- `.cursor/rules/batch-commands.mdc` — bare-word triggers for batch commands
- `docs/help/BATCH_COMMANDS.md` — human cheat sheet; `docs/BATCH_COMMANDS.md` — agent registry
- `CODE_REVIEW.md.example` and `RELEASE_NOTES.md.example` (ephemeral outputs gitignored)
- `scripts/check-batch-commands.sh` — registry ↔ filesystem validation
- README **Agent shortcuts** section; Child Playbook step 2b; PROMPT_LIBRARY Entries 22–46
- `docs/CURSOR_MODES.md` — Batch commands row (`/audit` ≠ Debug Mode)
- `.cursor/rules/cursor-modes.mdc` — alwaysApply mode routing rule
- PROMPT_LIBRARY Entries 18–21 (Ask, Plan, Debug, Agent execute prompts)
- Sprint M15: init `--keep-optional` / `--prune-optional`; `simulate-template-upgrade.sh` non-interactive init smoke; Playwright About update-status e2e
- CI `android-instrumented` job for `connectedDebugAndroidTest`

### Fixed

- **cursor:** line-1 shebang gate in `check_cursor_hooks.py`; deterministic smoke clears session approvals before deny test (M31 audit)
- **ci:** re-pin Kotlin compose plugin to 2.3.20 after Dependabot #26 regressed CodeQL; tighten dependabot ignore for plugin id
- Android bottom buttons and snackbars overlapping 3-button navigation bar on edge-to-edge layouts — `bottomInsetPadding()` with 48dp fallback
- `examples/web/package.json` — npm override `undici >=7.28.0` (Dependabot GHSA-vmh5-mc38-953g high, GHSA-pr7r-676h-xcf6 medium; transitive via jsdom)
- v0.11.0 SBOM backfill via Release workflow dispatch (M29 audit F-001)

### Changed

- `/build` runs AGENT/AUTO and Parallel work first; HUMAN/ADB grouped under **Human & device (after automation)** in BUILD_PLAN for human review after automation
- Autonomous `/build` sprint loop — chains sprints without approval pauses; never halts on HUMAN/ADB labels
- Python CI and weekly health check run pytest when `examples/python/` is present; feature-gate job steps are stack-conditional
- File size taxonomy: **static data 300 lines** (UI + i18n) and **pure logic 150 lines** — replaces 250-line view limit; enforced in `scripts/check-file-limits.sh`
- `release-please.yml` — auto-sync template version files on release PR; dispatch SBOM workflow on publish (use `fromJSON(pr).number` for checkout)
- Rename `health-check.yml` → `weekly-health-check.yml`; add `actions: read` for CI poll
- INITIALIZATION_PROMPT §7 split into §7a (pre-release audit, Agent) and §7b (defect investigation, Debug)
- START_HERE, AGENTS.md, FOR_AGENTS, init scripts, and README Quick Start synced with CURSOR_MODES
- Child Repo Playbook Sprint 0 step 2a — pick Cursor mode before AGENT init
- `validate-bootstrap.sh` requires `docs/CURSOR_MODES.md` and `.cursor/rules/cursor-modes.mdc`
- `scripts/check-changelog-unreleased.sh` — fail on duplicate CHANGELOG [Unreleased] sections
- `simulate-template-upgrade.sh` AREAS — CURSOR_MODES, cursor-modes rule, changelog check
- `MAINTAINING_THE_TEMPLATE.md` — blank line before Feedback Loop table
- `UPGRADING_FROM_TEMPLATE.md` — cherry-pick row for `check-changelog-unreleased.sh`
- `TEMPLATE_INDEX.json` — index `.cursor/rules/cursor-modes.mdc` (bootstrap REQUIRED)
- `START_HERE.md` — consistent `docs/` paths in repo-mode bullets
- `examples/web/package.json` — npm override for transitive `js-yaml` CVE (Dependabot #9)
- `MainActivitySmokeTest` uses `ActivityScenarioRule`; CodeQL rust/go exclusion documented in workflow + module guides
- `release.yml` splits tag gate vs SBOM upload; Release Please dispatches SBOM assets on publish

## [0.9.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.8.0...v0.9.0) (2026-06-15)

### Added

* **agents:** execute Sprint M13 human-gate automation ([4fddec8](https://github.com/edwardlthompson/agent-project-bootstrap/commit/4fddec8aa677ab5f873a86e88dbb56bb5082bc46))
* **agents:** execute Sprint M14 post-M13 review remediation ([fc71433](https://github.com/edwardlthompson/agent-project-bootstrap/commit/fc71433fcbfeae3c15703adedde7970106c80c90))

### Documentation

* **build-plan:** mark M14 CI sign-off complete on fc71433 ([0419e62](https://github.com/edwardlthompson/agent-project-bootstrap/commit/0419e621a9a99e7f34fc31f32b5dd19a30437345))

## [0.8.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.7.1...v0.8.0) (2026-06-15)

### Added

* add in-app About screen with format-locked update checker ([29278cc](https://github.com/edwardlthompson/agent-project-bootstrap/commit/29278ccad8f1e7ee623e3c761026bbc4711a83a3))
* add industry-standard repo hygiene automation ([136bfe3](https://github.com/edwardlthompson/agent-project-bootstrap/commit/136bfe336e3f868c9f32c12e531ea26517765cc2))
* **agents:** automate BUILD_PLAN human gates and Sprint M8 CI ([810e259](https://github.com/edwardlthompson/agent-project-bootstrap/commit/810e259ceb69994800372d2f66df09c918465738))
* **agents:** execute Sprint M9 sequential rows 1-7 ([e8e6831](https://github.com/edwardlthompson/agent-project-bootstrap/commit/e8e6831f3367f41fce5cb22f25c7d07347034e98))
* **agents:** execute Sprint M9-8 and M10 code review remediation ([9163dab](https://github.com/edwardlthompson/agent-project-bootstrap/commit/9163dab255d4a7552ea1d0d4f169f2b78f569593))
* **agents:** incremental feature assembly and autonomous agent gates ([3d47485](https://github.com/edwardlthompson/agent-project-bootstrap/commit/3d4748595589a642db0373947ae2152e5e3cd1de))

### Fixed

* **agents:** execute Sprint M11 post-M10 hardening ([daa9d85](https://github.com/edwardlthompson/agent-project-bootstrap/commit/daa9d858afe0eed78e95913107af704e652fb964))
* **agents:** execute Sprint M12 post-M11 polish ([0f859fd](https://github.com/edwardlthompson/agent-project-bootstrap/commit/0f859fd5211e06d06647760cccde452402254614))
* **android:** pin Kotlin compose plugin 2.3.20 for CodeQL ([7055255](https://github.com/edwardlthompson/agent-project-bootstrap/commit/70552559df7dd57d8f9ad7d2ee334ee4f6759c04))
* **ci:** repair M12 gate failures (TS tests, Kotlin pin) ([cb1c5d5](https://github.com/edwardlthompson/agent-project-bootstrap/commit/cb1c5d5a51793daa0377c9f82ea84affc7b9d222))
* **web:** assign narrowed HTMLDivElement for AppShell root ([f1dad83](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f1dad832e03f2adda807a99fadc63e314d0d833c))
* **web:** narrow app root type for AppShell render ([53406b1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/53406b1b84736d145ebf620a5852a6d6eb23c087))
* **web:** resolve CI lint and file-size budget failures ([1759a8f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/1759a8f54b80f54564318e4174f56388d20047ef))

### Changed

* sync template version to 0.7.1 and fix CHANGELOG encoding ([e7ebf2d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/e7ebf2dadc367ceec14f701bd927a20b7c4dcde0))

### Documentation

* **build-plan:** mark M12 CI sign-off complete on 7055255 ([3189747](https://github.com/edwardlthompson/agent-project-bootstrap/commit/31897474dec6b30b1ee7f95701f3cebecc9c249b))
* mark automated BUILD_PLAN gates complete after CI green ([f3013a0](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f3013a06f9ff1c9e7f5d5a85e96f495065a0610a))
* mark Sprint M6 repo-hygiene CI gate complete ([7ed666b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/7ed666ba8c4b482f948b22a8e29e87dad2cef2e1))
* refresh README with badges, TOC, and collapsible sections ([008140e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/008140ed6eed951a788cfdb1832f5b035bb7a582))

## [0.7.1](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.7.0...v0.7.1) (2026-06-13)

### Fixed

* **android:** bump compileSdk to 37 for androidx 1.19 dependencies ([3a74f0c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/3a74f0c08d32aff7a14d6fb7eff829578cbe73da))
* **android:** migrate Golden Path example to AGP 9 built-in Kotlin ([a84a16c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a84a16c8fa7e67d3179b820962aead03add7be34))
* **android:** rewrite Gradle Kotlin DSL files as UTF-8 ([11bc782](https://github.com/edwardlthompson/agent-project-bootstrap/commit/11bc78233b804ccaaca318f630f9eb1501cf10b8))
* **ci:** gofmt Go example and harden PR coverage comment parsing ([0423990](https://github.com/edwardlthompson/agent-project-bootstrap/commit/0423990b4ece9559a42db6c6bc66cec0eab518c8))
* **ci:** remove job-level hashFiles guards from ci.yml ([36fdbc1](https://github.com/edwardlthompson/agent-project-bootstrap/commit/36fdbc118f25e5865fb18aee653728ee87613f09))
* **ci:** repair corrupted template literals in coverage comment job ([a64ad04](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a64ad04b1fd5ec301110925d8783ed8a1653a6ab))

### Changed

* **deps:** Bump the github-actions group across 1 directory with 12 updates ([c70ce00](https://github.com/edwardlthompson/agent-project-bootstrap/commit/c70ce00d5d2bd89525aa928ef279ffe66d82e720))
* **deps:** Bump the github-actions group across 1 directory with 12 updates ([c242785](https://github.com/edwardlthompson/agent-project-bootstrap/commit/c2427853e0aec0c0680b68beb0497cbed4da7e8c))
* **deps:** Bump the node-dependencies group in /examples/node with 2 updates ([42b87eb](https://github.com/edwardlthompson/agent-project-bootstrap/commit/42b87eb548942b4197fe67194698d1eaf49028e4))
* sync v0.7.0 and complete BUILD_PLAN automation pass ([369e17e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/369e17ea198e671a4e0cec5943282e6f5e1f2786))

## [0.7.0](https://github.com/edwardlthompson/agent-project-bootstrap/compare/v0.6.0...v0.7.0) (2026-06-13)

### Added

* design system, web layout docs, and Golden Path UI refresh ([912ebbe](https://github.com/edwardlthompson/agent-project-bootstrap/commit/912ebbe2c57fb2d47223c49b3332f3037cc9c80f))
* initial agent-project-bootstrap template v0.1.0 ([d71c23c](https://github.com/edwardlthompson/agent-project-bootstrap/commit/d71c23c22dd97b96f3ef91435319d5df04bb28b6))
* template v0.2.0 with UTF-8 gates, lockfiles, and build verification ([2317440](https://github.com/edwardlthompson/agent-project-bootstrap/commit/2317440eeecec0ef961bb9cf54ea3830c183d8cf))

### Fixed

* add gradle.properties with android.useAndroidX for assembleDebug ([ea87f7d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/ea87f7dfd40500a7550759fb4466418de5ed2aae))
* Android CI ΓÇö google maven for AGP and FOSS grep comment false positive ([b907c46](https://github.com/edwardlthompson/agent-project-bootstrap/commit/b907c46628f05275ecefb149bc230ea08ac47845))
* bind vite preview to 127.0.0.1 for Playwright CI and extend pre-commit encoding ([9a3f935](https://github.com/edwardlthompson/agent-project-bootstrap/commit/9a3f9357ab4ca2d2afcdf8250ce955a6f46e9c60))
* **ci:** repair workflow action SHAs, e2e selectors, and shell line endings ([38ce003](https://github.com/edwardlthompson/agent-project-bootstrap/commit/38ce003771c04cdc043ffc79bf8ee2296fac19aa))
* **ci:** use full git history for Gitleaks on Release Please PRs ([01d585b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/01d585bcc1183633139cf46d4ca4932c3dbd8ee3))
* correct gh api calls in validate-workflow-actions.sh ([100df56](https://github.com/edwardlthompson/agent-project-bootstrap/commit/100df5649aee5724b22a70c84742453a106f1f1a))
* **deps:** override transitive tmp and uuid for @lhci/cli alerts ([222fb59](https://github.com/edwardlthompson/agent-project-bootstrap/commit/222fb59f5aa2d11b66c79496c23ad0980108b620))
* extend encoding scan to Android kts/kt/xml/properties ([8f52c55](https://github.com/edwardlthompson/agent-project-bootstrap/commit/8f52c550f52231520c76dd0a12326c0838db07e4))
* improve CI configs for python packaging and vitest ([36fbb39](https://github.com/edwardlthompson/agent-project-bootstrap/commit/36fbb394c5c1aa414c812334aa8628abfc47f178))
* normalize Android example UTF-16 files for Gradle CI ([39a78d3](https://github.com/edwardlthompson/agent-project-bootstrap/commit/39a78d36844739a9b5b77cb1f9876feb229accca))
* normalize UTF-16 index.html and extend encoding scan to html/css ([e16272e](https://github.com/edwardlthompson/agent-project-bootstrap/commit/e16272eded9901380c086dad4da6189e548209f4))
* playwright preview server host and timeout for CI e2e ([99188fa](https://github.com/edwardlthompson/agent-project-bootstrap/commit/99188faff52638196366aa8f967cfa832900e37e))
* repair Security Scan workflow and add GH CI automation ([80f9fc0](https://github.com/edwardlthompson/agent-project-bootstrap/commit/80f9fc03a5ada9ef795b39111d2de6b508a982c6))
* resolve CI failures in web lint, python format, and android grep ([444ad7b](https://github.com/edwardlthompson/agent-project-bootstrap/commit/444ad7b4c713257bb749371c7c9882b0e883bd19))
* stabilize Lighthouse CI with 3-run median ([f41c48d](https://github.com/edwardlthompson/agent-project-bootstrap/commit/f41c48daff6d8a148d86ba5cfabf800d015c806d))

### Changed

* **deps:** Bump the github-actions group across 1 directory with 10 updates ([#3](https://github.com/edwardlthompson/agent-project-bootstrap/issues/3)) ([648f5d2](https://github.com/edwardlthompson/agent-project-bootstrap/commit/648f5d202b1b2820e10b59c680b6980109290f77))
* release v0.2.1 full bootstrap hardening ([a2749a3](https://github.com/edwardlthompson/agent-project-bootstrap/commit/a2749a30cfac1b3bf3f2d450666592453ca3aca2))

### Documentation

* harden initialization prompt against CI failure patterns ([ec5ee91](https://github.com/edwardlthompson/agent-project-bootstrap/commit/ec5ee914332e7697e6168676c88eaca341cff643))
* mark v0.2.1 About and release human tasks complete ([33dc47f](https://github.com/edwardlthompson/agent-project-bootstrap/commit/33dc47fa934728a4b1b0247e14af95e95d031db0))

## [0.6.0] - 2026-06-12

### Added

- Cross-stack design system: `design-tokens/design-tokens.json` + `scripts/sync-design-tokens.py`
- Android Jetpack Compose Material 3 Golden Path with system/light/dark theme toggle (DataStore)
- Web CSS design tokens, theme toggle (`theme.ts`), and i18n scaffold (`locales/`, `t()`)
- `docs/DESIGN_GUIDE.md` and `.cursor/rules/design-system.mdc`
- `scripts/check-design-cohesion.sh` / `.ps1` wired into `validate-bootstrap.sh`
- Sprint M4 in BUILD_PLAN.md (template maintainer v0.6.0)

### Changed

- `examples/android/` migrated from XML Views to Compose M3
- `examples/web/` uses CSS variables, logical properties, `prefers-reduced-motion`
- `modules/android/MODULE.md` and `modules/web/MODULE.md` design system sections
- `docs/START_HERE.md`, `docs/FOR_AGENTS.md` read order includes DESIGN_GUIDE
- `.template-version` bumped to `0.6.0`

## [0.5.0] - 2026-06-13

### Added

- `examples/lightroom/` stub (`Info.lua`, README with SDK version table) per `modules/lightroom/MODULE.md`

- Optional `modules/rust/MODULE.md` + `examples/rust/` hello stub (Cargo.toml, clippy/fmt/test CI)

- Optional `modules/go/MODULE.md` + `examples/go/` hello stub (vet/fmt/test CI)

- `docs/OPTIONAL_STACKS.md` ΓÇö Rust/Go/Lightroom/Node opt-in outside default init stack picker

- CI `stack-filters` job; `lightroom`, `node`, `rust`, `go` jobs gated on directory existence and path changes

- F-Droid submission dry-run checklist in `modules/android/MODULE.md` (`[ADB]`)

### Changed

- `TEMPLATE_INDEX.json` ΓÇö `modules.lightroom.example` ΓåÆ `examples/lightroom/`; added `rust` and `go` modules

- `.template-version` ΓåÆ `0.5.0`

- README Supported Stacks table includes Lightroom example path and optional stacks note
- README (M5.1): hero badges, table of contents, GitHub alert callouts, collapsible detail sections, audience dividers
- README (M5): shields.io badges + HTML definition lists/tables for What's Included, BUILD_PLAN Labels, Template Update Checker, and Supported Stacks
- `scripts/normalize-markdown-whitespace.py`: table-aware blank-line collapse
- `scripts/check-markdown-tables.sh`: fail on broken GFM table rows; wired into `validate-bootstrap.sh`
- `docs/MAINTAINING_THE_TEMPLATE.md`: README badge conventions and hero/TOC sync notes

## [0.2.2] - 2026-06-13

### Added

- `scripts/setup-github-repo.sh` and `scripts/setup-github-repo.ps1` - idempotent gh api setup for Dependabot alerts, private vulnerability reporting, branch protection

- `scripts/pre-release-gate.sh` and `scripts/pre-release-gate.ps1` - CI poll, Dependabot Critical/High count, template version check, release workflow reminder

- `scripts/check-file-encoding.py` - cross-platform UTF-8/UTF-16 BOM check; `check-file-encoding.sh` delegates to Python

- `.cursor/rules/windows-encoding.mdc` - Python UTF-8 write guidance for Windows

- Gitleaks CI job in `.github/workflows/security.yml` (SHA-pinned `gitleaks/gitleaks-action@v3.0.0`)

- Pre-commit hooks: `file-limits`, `validate-bootstrap --quick`

- KNOWLEDGE_BASE KB-007 npm/pip overrides policy; DECISION_LOG entry for `@lhci/cli` overrides

- PROMPT_LIBRARY entries 10 (pre-release gate) and 11 (GitHub repo setup)

### Changed

- `scripts/validate-bootstrap.sh` - `--quick` flag skips `validate-workflow-actions`

- `.github/workflows/health-check.yml` - `npm audit` for `examples/web`, `uv pip audit` for `examples/python`

- `docs/SECURITY_TRIAGE.md` - documents `setup-github-repo.sh` in Setup section

- `init-project` scripts remind to run `setup-github-repo` after init

- `AGENT_MEMORY.md` template version synced to `0.2.2`; em-dash corruption fixed to ASCII hyphen

- README CI gate section mentions `setup-github-repo` and `pre-release-gate`

## [0.3.0] - 2026-06-13

### Added

- Stack picker `web/python/android/multi/none` in `init-project` scripts; `none` keeps all examples

- `scripts/init-stack-sync.py` - sync `AGENT_MEMORY.md` checkboxes and `.cursor/stack-selection.json`

- `.cursor-session-state.example.json` - session restore schema

- `docs/adr/0001-core-architecture.md` - MVVM / Clean / Hexagonal choice template for child repos

- `android-release` CI job with `SOURCE_DATE_EPOCH=1700000000` and APK hash flake check

- Semantic PR title job (`amannn/action-semantic-pull-request`, SHA-pinned)

- `scripts/check-bundle-size.sh` - Vite dist JS gzip budget (200 KB)

- Playwright visual snapshot and service-worker offline e2e tests

- Optional `pyright` CI job for Python example

- `.cursor/rules/testing.mdc` and `.cursor/rules/ci-gates.mdc`

- `docs/PARALLEL_AGENT_SCOPES.md` and `scripts/check-parallel-scope.sh`

- PROMPT_LIBRARY entries 12-14

- `.github/workflows/scorecard.yml` - weekly OpenSSF Scorecard (SHA-pinned)

- Android Fastlane `short_description.txt` stub and emulator checklist in README

### Changed

- `docs/FOR_AGENTS.md` - failure playbook (CI poll, GH_TOKEN, Dependabot, 3-strike, parallel scope)

- Python CI enforces `pytest --cov-fail-under=90` explicitly

- `.template-version` bumped to `0.3.0`; TEMPLATE_INDEX and README updated

## [0.2.1] - 2026-06-13

### Added

- `scripts/check-workflow-action-ref-format.sh` ΓÇö local pre-commit guard against bare-semver action refs

- `.github/workflows/health-check.yml` ΓÇö weekly Monday 07:00 UTC poll of CI + Security Scan + CodeQL on main

- CI `android-build` job ΓÇö `./gradlew assembleDebug` smoke for `examples/android/`

- Gradle wrapper binaries (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) in `examples/android/`

- `KNOWLEDGE_BASE.md` ΓÇö six structured entries from v0.2.0 CI/security fix round

- `PROMPT_LIBRARY.md` entries 8ΓÇô9 ΓÇö workflow action validation and post-push GitHub gate

- Devcontainer `github-cli` feature; postStart runs encoding check + CI gate reminder

- README GitHub CI Gate section; init scripts run `validate-workflow-actions` and remind `check-github-ci`

### Changed

- Normalized root `.gitignore` from UTF-16 to UTF-8; added to encoding scan and pre-commit hook

- SHA-pinned `release.yml` actions: `anchore/sbom-action`, `softprops/action-gh-release`, `actions/attest-build-provenance`

- `docs/SECURITY_TRIAGE.md` ΓÇö GitHub Actions pin policy, health-check in weekly triage table

- `modules/web/MODULE.md` ΓÇö Lighthouse 3-run median policy documented

- `modules/android/MODULE.md` ΓÇö CI assembleDebug documented; fixed corrupted path characters

- `docs/INITIALIZATION_PROMPT.md` ΓÇö root `.gitignore` in encoding extension list

- `PROMPT_LIBRARY.md` entries 4 and 6 ΓÇö validate-workflow-actions, three-workflow sign-off

### Fixed

- CI: Lighthouse CI uses 3 runs with median assertion to reduce shared-runner flake while keeping 0.9 performance budget

- Security Scan: pin `aquasecurity/trivy-action` to SHA `a9c7b0f` (v0.36.0); invalid `@0.28.0` ref caused workflow setup failure

- Automation: `scripts/validate-workflow-actions.sh` and `scripts/check-github-ci.sh` (+ `.ps1`) to catch bad action refs and poll required GH workflows before sign-off

- CI: Web TS null narrowing in main.ts, MIT license on web package, scoped Android FOSS grep to Gradle files

- Python: ruff format on greet.py

- Index/pre-commit: CONTRIBUTING.md in TEMPLATE_INDEX; encoding hook covers .ts/.tsx/.toml

- License script: --excludePrivatePackages for private stub packages

- Encoding: normalize UTF-16 index.html and style.css; extend encoding scan to .html/.css

## [0.2.0] - 2026-06-12

### Added

- `scripts/check-file-encoding.sh` ΓÇö UTF-8 enforcement in CI and pre-commit

- `.env.example` ΓÇö documented environment variable stub

- `examples/web/package-lock.json` and `examples/python/uv.lock` ΓÇö reproducible locked installs

- Build Verification Gate in `INITIALIZATION_PROMPT.md` Section 7a (Sprint 0 + release)

- `PROMPT_LIBRARY.md` entries: bootstrap verification, security triage, SBOM audit, build verification

- Secret rotation procedure in `docs/RUNBOOK.md`

- Android operations checklist in `modules/android/MODULE.md`

- Release workflow `workflow_dispatch` for maintainer dry-run

- Web Vitest coverage budget (90%) matching Python example

### Changed

- Normalized ~46 UTF-16 corrupted files to UTF-8

- `scripts/validate-bootstrap.sh` ΓÇö encoding, index, lockfile, and LICENSE checks

- `scripts/check-license-compliance.sh` ΓÇö strict fail on disallowed licenses; stack-scoped CI steps

- `TEMPLATE_INDEX.json` ΓÇö added LICENSE, scripts, dependency-review, destructive-ops, `.env.example`; version 0.2.0

- `.github/CODEOWNERS` ΓÇö `@[PROJECT_OWNER]` placeholder; init scripts replace during Sprint 0

- `docs/SECURITY_TRIAGE.md` ΓÇö private vulnerability reporting in setup

- `docs/UPGRADING_FROM_TEMPLATE.md` ΓÇö cherry-pick rows for new scripts/workflows

- `BUILD_PLAN.md` ΓÇö encoding, lockfiles, Build Verification Gate in Sprint 0 and Milestone Gates

- `README.md` ΓÇö links THREAT_MODEL, PRIVACY, RUNBOOK, THIRD_PARTY_LICENSES, LICENSE

- CI: license check after locked installs; `uv sync --locked`; encoding-check job first

- `docs/MAINTAINING_THE_TEMPLATE.md` ΓÇö release dry-run steps

- Init scripts ΓÇö CODEOWNERS replacement, GITHUB_ABOUT.md draft, update checker config

### Human-only (not automated)

- Enable Dependabot alerts + private vulnerability reporting on GitHub

- Branch protection on `main` with required CI checks (`encoding-check`, `validate-bootstrap`)

- Replace `@[PROJECT_OWNER]` in CODEOWNERS with real GitHub username

- Paste GitHub About description from `docs/GITHUB_ABOUT.md`

## [0.1.0] - 2026-06-12

### Added

- Verbatim Project Initialization Prompt (`docs/INITIALIZATION_PROMPT.md`)

- Agent routing: `docs/START_HERE.md`, `docs/FOR_AGENTS.md`, `TEMPLATE_INDEX.json`

- Workspace memory files: `AGENT_MEMORY.md`, `DECISION_LOG.md`, `KNOWLEDGE_BASE.md`, `BUILD_PLAN.md`

- Multi-stack Golden Path stubs: Web (Vite PWA), Python (uv CLI), Android (FOSS Gradle skeleton)

- Ecosystem module guides: Android, Web, Python, Lightroom

- CI/CD guardrails: matrix CI, CodeQL, Trivy, Dependabot, release workflow

- Template update checker with configurable intervals (`off`, `daily`, `weekly`, `monthly`, `on_session`)

- Maintainer and consumer docs: `MAINTAINING_THE_TEMPLATE.md`, `UPGRADING_FROM_TEMPLATE.md`

- Devcontainer, pre-commit hooks, init scripts (bash + PowerShell)

- `SECURITY.md`, `CODE_OF_CONDUCT.md`, `.github/CODEOWNERS` ΓÇö community health and responsible disclosure

- `docs/THREAT_MODEL.md`, `docs/PRIVACY.md`, `docs/RUNBOOK.md` ΓÇö threat model, privacy-by-design, operations

- `THIRD_PARTY_LICENSES.md` + `scripts/check-license-compliance.sh` ΓÇö license compliance

- `scripts/validate-bootstrap.sh` ΓÇö Sprint 0 artifact verification in CI

- `.github/workflows/dependency-review.yml` ΓÇö PR dependency review (fail on High/Critical)

- Release workflow: SBOM (CycloneDX) + SLSA build provenance attestation

- `.cursor/rules/destructive-ops.mdc` ΓÇö human-in-the-loop gates for destructive agent operations

[0.2.0]: https://github.com/edwardlthompson/agent-project-bootstrap/releases/tag/v0.2.0

[0.2.1]: https://github.com/edwardlthompson/agent-project-bootstrap/releases/tag/v0.2.1

[0.1.0]: https://github.com/edwardlthompson/agent-project-bootstrap/releases/tag/v0.1.0
