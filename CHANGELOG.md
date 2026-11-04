# Changelog

All notable changes to this template will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),

and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0](https://github.com/edwardlthompson/DevPulse/compare/v1.0.0...v1.1.0) (2026-11-04)


### Added

* **inventory:** add APKPure parser and secondary store download fallback ([be54622](https://github.com/edwardlthompson/DevPulse/commit/be5462216f4e97fd5abe17443bf67f534aae4a6a))
* **inventory:** signer replace installation flow and ignored updates management ([a615863](https://github.com/edwardlthompson/DevPulse/commit/a615863a5a6bca216cee2556338a1ff4426fda5e))
* **inventory:** version history and one-tap rollback support ([1252a88](https://github.com/edwardlthompson/DevPulse/commit/1252a880c7e7f5240af94482fd542533f94b9671))
* prepare v0.22.0 DevPulse product release ([8943815](https://github.com/edwardlthompson/DevPulse/commit/89438154641d45d56b7062ee6352e78b7f4c36ef))
* prepare v0.24.0 DevPulse product release ([7588adc](https://github.com/edwardlthompson/DevPulse/commit/7588adc5fc7fcccce2cc1edaacfc4b1baa7ea21d))
* prepare v0.25.0 DevPulse product release ([9853d4f](https://github.com/edwardlthompson/DevPulse/commit/9853d4fbf524f93efd5ce303217fbbbe6eff6804))
* prepare v0.26.0 DevPulse product release ([fafdbed](https://github.com/edwardlthompson/DevPulse/commit/fafdbed225fdce32328c434046ffd97130a8440f))
* prepare v0.27.0 DevPulse product release ([1848373](https://github.com/edwardlthompson/DevPulse/commit/1848373c7afa606498c81816c1287884fb62396f))
* prepare v0.28.0 DevPulse product release ([3ba27f0](https://github.com/edwardlthompson/DevPulse/commit/3ba27f0a93917fa3048d6c3ce509ffbb71a0f22a))
* prepare v0.29.0 DevPulse product release ([ac36304](https://github.com/edwardlthompson/DevPulse/commit/ac363040953609ab0062dde943e98430b694a11c))
* prepare v0.30.0 DevPulse product release ([25d33b1](https://github.com/edwardlthompson/DevPulse/commit/25d33b154cf901a6f994a492d8135e2b428089b6))
* prepare v0.31.0 DevPulse product release ([20f470d](https://github.com/edwardlthompson/DevPulse/commit/20f470d9772c0adf7c00122fca0349dc975fe012))
* prepare v0.32.0 DevPulse product release ([7f7dcf7](https://github.com/edwardlthompson/DevPulse/commit/7f7dcf7a80a183c98713c616c629255c806e4ff3))
* prepare v0.33.0 DevPulse product release ([9f3bdf8](https://github.com/edwardlthompson/DevPulse/commit/9f3bdf892db55ee907a436acf9c9a5a53527fd67))
* prepare v0.34.0 DevPulse product release ([ac6945f](https://github.com/edwardlthompson/DevPulse/commit/ac6945fa224989b917e721de66bdfdbf6cdeaf97))
* ship one-tap scan and update with leftover GitHub discovery ([ad6a690](https://github.com/edwardlthompson/DevPulse/commit/ad6a6908266a611c833188b430b3835468cbdb93))
* **ui:** add scrubbable year scrollbar, clean navigation flow, and move ideas to settings ([916f11e](https://github.com/edwardlthompson/DevPulse/commit/916f11eda54621d5aa54a201265ab83ab839eb56))


### Fixed

* **android:** add F-Droid changelog for 0.38.3 (versionCode 3803) ([52b0af2](https://github.com/edwardlthompson/DevPulse/commit/52b0af29ee700073ed3dee506009dc2ccdbaf8a1))
* **android:** add Fastlane changelogs for 0.37.2 and 0.38.0 ([29e9d6a](https://github.com/edwardlthompson/DevPulse/commit/29e9d6a46672b5fe62cb69ca9c665f763a941b6d))
* **android:** refresh honesty, curated GitHub hints, About-only Venmo ([5d0d3c6](https://github.com/edwardlthompson/DevPulse/commit/5d0d3c6f0f7f61f497c927b1e22c7373abe1b3d4))
* **android:** run Daily scans when due and unclunk Scan and update ([6538f32](https://github.com/edwardlthompson/DevPulse/commit/6538f328bb1db65f1084a15eeaefbeec3e0d9d3f))
* **android:** schedule scan intervals without auto-start ([5c93faa](https://github.com/edwardlthompson/DevPulse/commit/5c93faa7ccfb9e20656ab13b465e2eae8a6f2b31))
* **build:** ensure release build type is deterministic when keystore is unset ([aba5d96](https://github.com/edwardlthompson/DevPulse/commit/aba5d96dda3ca038435bb96b86e59f4ad9167039))
* **ci:** skip obsolete Android SDK tools package on setup-android ([0713455](https://github.com/edwardlthompson/DevPulse/commit/07134556f146e5cf7a67ede416833ed82366bb5f))
* make instrumented settings and About tests match the grown UI ([4b818af](https://github.com/edwardlthompson/DevPulse/commit/4b818afaaf8f3e9b17b462aeddd91b21ab31bf4c))
* prepare v0.34.1 DevPulse product release ([4819da6](https://github.com/edwardlthompson/DevPulse/commit/4819da67df2e4cdc40d3deb13c0de9ff9571a77b))
* read GitHub release tags for hinted repos on Refresh ([ac7c777](https://github.com/edwardlthompson/DevPulse/commit/ac7c777c5bcf89acd84706370ac1628b6790fcb6))
* read GitHub release tags on Refresh ([69a5e06](https://github.com/edwardlthompson/DevPulse/commit/69a5e0601c0dd950cf5331da9a38d289c75a802b))
* **scripts:** reconfigure UTF-8 encoding on stdout/stderr in changelog_unreleased ([882c997](https://github.com/edwardlthompson/DevPulse/commit/882c99727936b5a674db4f3fe30155c3c59efb22))
* skip CodeQL JS after pruning web and node stacks ([8fbe9ae](https://github.com/edwardlthompson/DevPulse/commit/8fbe9aef85193d4777751bcb1ec835c2561de3e5))
* skip pruned stack CI and stabilize donation tests ([7884f61](https://github.com/edwardlthompson/DevPulse/commit/7884f617a366163622d13afd604705c0210e8d8e))
* skip web About gate and scroll settings inset tests ([f8b8ebe](https://github.com/edwardlthompson/DevPulse/commit/f8b8ebe20adb5632ba0734202d76eb2f90298a30))
* walk the Settings hub in instrumented smoke tests ([4bf97e9](https://github.com/edwardlthompson/DevPulse/commit/4bf97e9781ef551ac83408572207eb8e20b4b173))


### Changed

* archive Sprint 37 after v0.38.3 ship ([c570cf1](https://github.com/edwardlthompson/DevPulse/commit/c570cf158aa01b698e9358863b396c106c80a8c6))
* **bootstrap:** catch up child template machinery to v1.5.0 ([d3684b5](https://github.com/edwardlthompson/DevPulse/commit/d3684b55308e2f8742b51b8da35f4f863f9ae730))
* catch up child template to agent-project-bootstrap 1.12.0 ([9eaaad7](https://github.com/edwardlthompson/DevPulse/commit/9eaaad7b296f00f5f6525472fadaf326dfa59ef6))
* **deps:** bump nix-action, scorecard-action, and ruff-pre-commit ([d13da16](https://github.com/edwardlthompson/DevPulse/commit/d13da16365eec68b8ae3ac023be24a9817a0b21a))
* **deps:** bump the android-dependencies group across 1 directory with 5 updates ([#14](https://github.com/edwardlthompson/DevPulse/issues/14)) ([939540c](https://github.com/edwardlthompson/DevPulse/commit/939540c7a0a9ecd8a2ec575e6e1bdbf6360ee2dc))
* **deps:** bump the android-dependencies group across 1 directory with 8 updates ([#42](https://github.com/edwardlthompson/DevPulse/issues/42)) ([1234212](https://github.com/edwardlthompson/DevPulse/commit/123421278f37f086623ec627461384291a0c0319))
* **deps:** bump the github-actions group across 1 directory with 2 updates ([#28](https://github.com/edwardlthompson/DevPulse/issues/28)) ([e87f728](https://github.com/edwardlthompson/DevPulse/commit/e87f72858eb6bf5f4b0aec9598280fb8b4287938))
* **deps:** bump the github-actions group across 1 directory with 4 updates ([#43](https://github.com/edwardlthompson/DevPulse/issues/43)) ([b6ba666](https://github.com/edwardlthompson/DevPulse/commit/b6ba666e776fef0343446ca7218486dd28962d59))
* fold product notes into v0.22.0 changelog ([10334b5](https://github.com/edwardlthompson/DevPulse/commit/10334b55557dea18705ab84c1da4be52c06387e5))
* fold product notes into v0.23.0 changelog ([a0c4b56](https://github.com/edwardlthompson/DevPulse/commit/a0c4b565d7495055fd3e30b38024fa2f109d8d6b))
* fold product notes into v0.24.0 changelog ([39c2030](https://github.com/edwardlthompson/DevPulse/commit/39c20300b6b9b834de77424d0f0d2fa52bc3cecb))
* fold product notes into v0.25.0 changelog ([ecf55ea](https://github.com/edwardlthompson/DevPulse/commit/ecf55ea9845b63129fd223fba9b06bf309db5efc))
* fold product notes into v0.26.0 changelog ([624cc41](https://github.com/edwardlthompson/DevPulse/commit/624cc41d1c97b4845d815acadc603040c636151e))
* fold product notes into v0.27.0 changelog ([e6386a5](https://github.com/edwardlthompson/DevPulse/commit/e6386a56c6dcf276da1f47bc47b61f0e375f8f4f))
* fold product notes into v0.28.0 changelog ([cf24f64](https://github.com/edwardlthompson/DevPulse/commit/cf24f64e7cf58e55e24994ae87cbe93f36bd987c))
* fold product notes into v0.29.0 changelog ([25fac9b](https://github.com/edwardlthompson/DevPulse/commit/25fac9bfc88698056f2078405b1224e5e740762a))
* fold product notes into v0.30.0 changelog ([bf3b512](https://github.com/edwardlthompson/DevPulse/commit/bf3b512df8422a7e3e6f55c10d30986d858504ec))
* fold product notes into v0.31.0 changelog ([5f7eaf0](https://github.com/edwardlthompson/DevPulse/commit/5f7eaf0c3d2e13a05dc89a53304bff2e6b4bc7aa))
* fold product notes into v0.32.0 changelog ([a5e0d3e](https://github.com/edwardlthompson/DevPulse/commit/a5e0d3ee61d00486aa1e2612c8d887221abd5f64))
* fold product notes into v0.33.0 changelog ([2a12891](https://github.com/edwardlthompson/DevPulse/commit/2a12891315b4b827bbd69e2a18b03885d59b0655))
* fold product notes into v0.34.0 changelog ([42010c9](https://github.com/edwardlthompson/DevPulse/commit/42010c95a124c3f7a71df523338c5bec659c82d1))
* fold product notes into v0.34.1 changelog ([a1075d8](https://github.com/edwardlthompson/DevPulse/commit/a1075d8c39a3310b706c3469131f8ba851169f0d))
* fold product notes into v0.34.2 changelog ([579a73b](https://github.com/edwardlthompson/DevPulse/commit/579a73b1d6ece5c9f938e54e3cf92b41c4b4d495))
* **main:** release 0.22.0 ([e53283a](https://github.com/edwardlthompson/DevPulse/commit/e53283aa5c70a782f1b85bd98953e4b825eeaa01))
* **main:** release 0.22.0 ([d7d51d9](https://github.com/edwardlthompson/DevPulse/commit/d7d51d9af269e74a397fe335d22a93695329cb81))
* **main:** release 0.22.1 ([f24cc0c](https://github.com/edwardlthompson/DevPulse/commit/f24cc0cdbb4159f796468d1e4a5f51e3fa5bca8b))
* **main:** release 0.23.0 ([d24324b](https://github.com/edwardlthompson/DevPulse/commit/d24324b78253eeff8ad9d642fe0dc2202d01b83d))
* **main:** release 0.24.0 ([36d12cc](https://github.com/edwardlthompson/DevPulse/commit/36d12cc8ef670cc3529a60a1346ad70b4c7bdd42))
* **main:** release 0.24.0 ([cb97ecf](https://github.com/edwardlthompson/DevPulse/commit/cb97ecf7d93995bee510b10367de4ae10433bfdb))
* **main:** release 0.25.0 ([90b6249](https://github.com/edwardlthompson/DevPulse/commit/90b624996c6cc764f6e12a53a9802d377f9c438a))
* **main:** release 0.25.0 ([e8b85d3](https://github.com/edwardlthompson/DevPulse/commit/e8b85d354b79719da26dc2a152ee3fcd74ddd924))
* **main:** release 0.26.0 ([a8f2c44](https://github.com/edwardlthompson/DevPulse/commit/a8f2c44a8872eb5c70d1800aeab3b3e78af6bdec))
* **main:** release 0.26.0 ([abf1796](https://github.com/edwardlthompson/DevPulse/commit/abf17965a090ee2f31504cc24306526fded8a9e9))
* **main:** release 0.27.0 ([a2663a3](https://github.com/edwardlthompson/DevPulse/commit/a2663a389e3ef5c147cf3cae8b09c6ee3e6ec227))
* **main:** release 0.27.0 ([e8a4bba](https://github.com/edwardlthompson/DevPulse/commit/e8a4bbaeebf57a1bdd99e1aee2bdf0e4c3d85f6e))
* **main:** release 0.28.0 ([7c0185e](https://github.com/edwardlthompson/DevPulse/commit/7c0185e77db55ad0b38f17f8cfab40123424555e))
* **main:** release 0.28.0 ([1ad5f79](https://github.com/edwardlthompson/DevPulse/commit/1ad5f795e8ceb701f37f4f9d0b1942ca41e2b316))
* **main:** release 0.29.0 ([3603cde](https://github.com/edwardlthompson/DevPulse/commit/3603cdec91cd6c252e6490fca035a361905884bd))
* **main:** release 0.29.0 ([7d82683](https://github.com/edwardlthompson/DevPulse/commit/7d826836114b8e1eb15ce7c69d09ace3a48da8f4))
* **main:** release 0.30.0 ([2b6d448](https://github.com/edwardlthompson/DevPulse/commit/2b6d4486b04de2d98e1a8e7298b411c3793ba5e9))
* **main:** release 0.30.0 ([19731ea](https://github.com/edwardlthompson/DevPulse/commit/19731ea356ec0ccd981d73acb6ecaff9c723914b))
* **main:** release 0.31.0 ([47e1359](https://github.com/edwardlthompson/DevPulse/commit/47e13596d057e3915355387c747a3e742305250e))
* **main:** release 0.31.0 ([13a0ddf](https://github.com/edwardlthompson/DevPulse/commit/13a0ddf0399e0acbafe9370e966c8e3fca8b3c91))
* **main:** release 0.32.0 ([29040a3](https://github.com/edwardlthompson/DevPulse/commit/29040a3aaaa667e5c078e7b468b28b1be39ca6f8))
* **main:** release 0.32.0 ([6dfcf58](https://github.com/edwardlthompson/DevPulse/commit/6dfcf58c355e6c46d6bbb461035a6634c5091c2e))
* **main:** release 0.33.0 ([a1fc775](https://github.com/edwardlthompson/DevPulse/commit/a1fc7754bc93a120c13d01bb030bfe0938bcf818))
* **main:** release 0.33.0 ([c769023](https://github.com/edwardlthompson/DevPulse/commit/c769023579530fd6d92269211d78839e6aba8440))
* **main:** release 0.34.0 ([eef4eb9](https://github.com/edwardlthompson/DevPulse/commit/eef4eb9bd176304f0610d8301d381df7980fdd58))
* **main:** release 0.34.0 ([58872bb](https://github.com/edwardlthompson/DevPulse/commit/58872bb8ed43a99ed56541a023186aafe3e9febc))
* **main:** release 0.34.1 ([f3e22f0](https://github.com/edwardlthompson/DevPulse/commit/f3e22f0ffa05f6270df0f757c558ab963f2d0072))
* **main:** release 0.34.1 ([8047be2](https://github.com/edwardlthompson/DevPulse/commit/8047be26d86f86378d49d829c30d86a12d8122f4))
* **main:** release 0.34.2 ([068f210](https://github.com/edwardlthompson/DevPulse/commit/068f2103dc39edcd653cbd935767bc4b7912d98c))
* **main:** release 0.34.2 ([dd0e4e0](https://github.com/edwardlthompson/DevPulse/commit/dd0e4e04b863f7e6f09a7b28ae451b785ef6f988))
* **main:** release 0.35.0 ([#23](https://github.com/edwardlthompson/DevPulse/issues/23)) ([4894cda](https://github.com/edwardlthompson/DevPulse/commit/4894cdacfa2c211b5789cc9bb78d483f76a052d0))
* **main:** release 0.36.0 ([f9190eb](https://github.com/edwardlthompson/DevPulse/commit/f9190eb19d64ce529348c39d581a9479c45cdbec))
* **main:** release 0.36.0 ([eb51a33](https://github.com/edwardlthompson/DevPulse/commit/eb51a339633e0ea7b323d8c8bf22a0126b6be2b9))
* **main:** release 0.37.0 ([3cb28bb](https://github.com/edwardlthompson/DevPulse/commit/3cb28bb18445dcc9f84fed5c39d3373d14dda64c))
* **main:** release 0.37.0 ([70b3ddb](https://github.com/edwardlthompson/DevPulse/commit/70b3ddb439cc828434a6017f4a956c3b367c30ba))
* **main:** release 0.37.1 ([56f4374](https://github.com/edwardlthompson/DevPulse/commit/56f4374eb9d12a76d03b49da098053fe3fb73941))
* **main:** release 0.37.1 ([c23e564](https://github.com/edwardlthompson/DevPulse/commit/c23e564f91784e1174b235b5f5c07cdc00267e3a))
* **main:** release 0.37.2 ([2b6eb1e](https://github.com/edwardlthompson/DevPulse/commit/2b6eb1e6d2ff02c985949937f7adc2e5a90c6e5a))
* **main:** release 0.37.2 ([949c531](https://github.com/edwardlthompson/DevPulse/commit/949c5315c1e46468193eaedb458865df6f4b9632))
* **main:** release 0.38.0 ([4bd1193](https://github.com/edwardlthompson/DevPulse/commit/4bd1193420cc8787f92449ddd9405a8a2e839bde))
* **main:** release 0.38.0 ([3dd5a4a](https://github.com/edwardlthompson/DevPulse/commit/3dd5a4a4c333d438762d573938c63fda0644a702))
* **main:** release 0.38.1 ([079cf3c](https://github.com/edwardlthompson/DevPulse/commit/079cf3c73421cabe28a19a39059f89aa204450d0))
* **main:** release 0.38.1 ([d283979](https://github.com/edwardlthompson/DevPulse/commit/d28397901cc12faeaf22fd51571ad9fe3bcaeb85))
* **main:** release 0.38.2 ([72e3242](https://github.com/edwardlthompson/DevPulse/commit/72e32427bb76cfffdbd77d5a9079c8c0428d2720))
* **main:** release 0.38.2 ([da50b24](https://github.com/edwardlthompson/DevPulse/commit/da50b242b0e3c552100c3908af929e86c2627e3b))
* **main:** release 0.38.3 ([02a5770](https://github.com/edwardlthompson/DevPulse/commit/02a5770c0d38cfc8794b9f0605381058ebd5559d))
* **main:** release 0.38.3 ([c00f1ad](https://github.com/edwardlthompson/DevPulse/commit/c00f1ad3aa004f7dc33eb8bac565d474f8c83f14))
* merge main before 0.38.0 ship ([b3cf81c](https://github.com/edwardlthompson/DevPulse/commit/b3cf81cf0a6419a0adb28f1f9924caed8bd67fcb))
* normalize CHANGELOG unreleased heading after v0.36.0 release ([e5fe764](https://github.com/edwardlthompson/DevPulse/commit/e5fe764b9ed829d27d6bc227f2e88b39267b1ff7))
* normalize CHANGELOG unreleased heading after v0.37.0 release ([5136d4a](https://github.com/edwardlthompson/DevPulse/commit/5136d4a379e40b2290fde17c146fa00a0141a8b8))
* portfolio pass for DevPulse 1.0.0 ([#44](https://github.com/edwardlthompson/DevPulse/issues/44)) ([1a34f37](https://github.com/edwardlthompson/DevPulse/commit/1a34f37cd86d0a9c11b5a57270dc1ef78d8e9c65))
* **release:** prepare v0.23.0 release ([ab50ba5](https://github.com/edwardlthompson/DevPulse/commit/ab50ba582b93108e0125e5ae6cd9374048db6b82))
* sync template version files for release PR ([df98eca](https://github.com/edwardlthompson/DevPulse/commit/df98eca96ebe821e42de86b04e8bd7d19e0b692f))
* sync template version files for release PR ([6c5e70f](https://github.com/edwardlthompson/DevPulse/commit/6c5e70fdaa2ad23fa8342ca79566f2084b392a9a))
* sync template version files for release PR ([8462745](https://github.com/edwardlthompson/DevPulse/commit/8462745c69240f52698d76a4e286af90d83c221b))
* sync template version files for release PR ([0ec633d](https://github.com/edwardlthompson/DevPulse/commit/0ec633d98fe0d75457f51ee193f9f96259ccc59f))
* sync template version files for release PR ([bf6c494](https://github.com/edwardlthompson/DevPulse/commit/bf6c4943a0663d482a5f24cba4887222bd4fc2ae))
* sync template version files for release PR ([b343400](https://github.com/edwardlthompson/DevPulse/commit/b34340070ea1f54e1c2cafe288b6e1e1d919dc48))
* sync template version files for release PR ([f592aba](https://github.com/edwardlthompson/DevPulse/commit/f592aba7688afb0de0b4a456dfddf64e1e4edcb2))
* sync template version files for release PR ([30eb82d](https://github.com/edwardlthompson/DevPulse/commit/30eb82dc791bd242b32a0e6ff1891c8046bbcb91))
* sync template version files for release PR ([3bbcc95](https://github.com/edwardlthompson/DevPulse/commit/3bbcc95bc65a124dff1bbed2f90147f1856025ad))
* sync template version files for release PR ([8978043](https://github.com/edwardlthompson/DevPulse/commit/8978043247c00168a88ff9005c205da2b706aaa0))
* sync template version files for release PR ([003e122](https://github.com/edwardlthompson/DevPulse/commit/003e122066f022bfd1c291d70a0d135f945f4d43))
* sync template version files for release PR ([07cbfe6](https://github.com/edwardlthompson/DevPulse/commit/07cbfe6798b64757479f0c41352bd8556b2c6747))
* sync template version files for release PR ([a5f2fb2](https://github.com/edwardlthompson/DevPulse/commit/a5f2fb210450efbc1e5ebeeac3bf6cdbc9c3104e))
* sync template version files for release PR ([a655b62](https://github.com/edwardlthompson/DevPulse/commit/a655b6265ea9d34710ce940ce3f95755c63e6329))
* sync template version files for release PR ([c3e523d](https://github.com/edwardlthompson/DevPulse/commit/c3e523d023cc069506c0d5c32e7022587b934275))
* sync template version files for release PR ([305ab59](https://github.com/edwardlthompson/DevPulse/commit/305ab597543c44a4003de6e20db8b5c9b81d1fca))
* sync template version files for release PR ([5fb6a86](https://github.com/edwardlthompson/DevPulse/commit/5fb6a86c9bf5d62f2e07f0f3874732a0c8948537))
* sync template version files for release PR ([7876aee](https://github.com/edwardlthompson/DevPulse/commit/7876aeec22779ec0ed68023e5e674f1b8f5b27f1))
* sync template version files for release PR ([4d30648](https://github.com/edwardlthompson/DevPulse/commit/4d3064890091d2afe0bd91dda3565abf92dd2de9))


### Documentation

* note OpenVEX gap for release SBOM wait (KB-035) ([d7af61f](https://github.com/edwardlthompson/DevPulse/commit/d7af61f1e07e04ea1d141345332677ee218e428e))
* record /ship block on main branch protection ([4a1f6ab](https://github.com/edwardlthompson/DevPulse/commit/4a1f6ab0fe70451b85eeec05ee08b6f5cf259249))
* record KB-030 cancelled CI failing SBOM poll ([f0faba5](https://github.com/edwardlthompson/DevPulse/commit/f0faba5e87c3a0e5d61dc27c32e2e8518b886128))
* record v0.22.0 ship notes and pruned-stack CI lessons ([2a77e6d](https://github.com/edwardlthompson/DevPulse/commit/2a77e6d4b2c71471ff989e9d206ef1c3d86aa929))
* record v0.23.0 ship notes and dump-store lessons ([fd20edd](https://github.com/edwardlthompson/DevPulse/commit/fd20edd9a120129b544fde5a12eb168003ad3beb))
* record v0.24.0 ship notes and fold lessons ([5f96a88](https://github.com/edwardlthompson/DevPulse/commit/5f96a88956aa36e915ae81459c4e6cf13401ef26))
* record v0.25.0 ship notes and fold lessons ([cd674ab](https://github.com/edwardlthompson/DevPulse/commit/cd674abd2b52a8080c8048bb49a663007f2e3e5b))
* record v0.26.0 ship notes and fold lessons ([4447349](https://github.com/edwardlthompson/DevPulse/commit/44473491b72838bfdd09e2fb29d33542016b6df9))
* record v0.27.0 ship notes and fold lessons ([0804566](https://github.com/edwardlthompson/DevPulse/commit/0804566e05a2e622143232ca27bc6f8ce4c36da6))
* record v0.27.0 ship regressions ([cc6b97b](https://github.com/edwardlthompson/DevPulse/commit/cc6b97b1c3dd3781a80b873db6c918e8f752f0e0))
* record v0.28.0 ship notes and fold lessons ([a7697d3](https://github.com/edwardlthompson/DevPulse/commit/a7697d39008822f10a05d338752d115d54f3bbfc))
* record v0.29.0 ship notes and fold lessons ([92c860a](https://github.com/edwardlthompson/DevPulse/commit/92c860ad9c118145fd09d444bef6ee17357d08d4))
* record v0.30.0 ship notes and fold lessons ([943c891](https://github.com/edwardlthompson/DevPulse/commit/943c8913747e2a66a334d394ca6c03b000d7df44))
* record v0.31.0 ship notes and fold lessons ([fdd5903](https://github.com/edwardlthompson/DevPulse/commit/fdd59035bbd858cd56ea313d1655527fdb01ba8f))
* record v0.32.0 ship notes and fold lessons ([1537c42](https://github.com/edwardlthompson/DevPulse/commit/1537c429939728c565153d5636ce71ebe710fb3c))
* record v0.33.0 ship notes and fold lessons ([51bbc2b](https://github.com/edwardlthompson/DevPulse/commit/51bbc2b7313b81d13c9b8b8b37bb6d6cbca13767))
* record v0.34.0 ship notes and fold lessons ([37cacfb](https://github.com/edwardlthompson/DevPulse/commit/37cacfbd7e2c98badc9cafbdb70bcc8209bd414c))
* record v0.34.1 ship notes and fold lessons ([efa452b](https://github.com/edwardlthompson/DevPulse/commit/efa452b9687bf7ac02f8f1d9606f579b43f0703c))
* record v0.34.2 ship notes and fold lessons ([b21762e](https://github.com/edwardlthompson/DevPulse/commit/b21762e34f514955da6cdfff918ed6c8737d7e05))
* record v0.37.0 session retrospective in AGENT_MEMORY ([6f2cee7](https://github.com/edwardlthompson/DevPulse/commit/6f2cee7dc84d3cd842f6e7664cdaa7d5f2cfc41b))
* record v0.38.0 ship notes and sync README template badge ([39dcb57](https://github.com/edwardlthompson/DevPulse/commit/39dcb57347505484774760781712312643d58a94))
* sync README template badge to 0.38.2 and record KB-036 ([a168a4d](https://github.com/edwardlthompson/DevPulse/commit/a168a4db8c4ab02e83aacc48bbe107cc8a2ca2f2))

## [Unreleased]

## [1.0.0](https://github.com/edwardlthompson/DevPulse/compare/v0.38.3...v1.0.0) (2026-11-02)

### Added

* **android:** ScanPerf instrumentation and `docs/PERF.md` for 200-app cold start, duration, and peak heap
* **android:** Foreground refresh cancel action, 20s per-source timeout, 12-hour index cache, last-scanned timestamp, resume-after-kill
* **android:** First-run sheet clarifying installed-apps permission and on-device-only scans
* **docs:** README “What this is not” (not a security audit / malware scanner)
* **chore:** Linguist attribution block and Fastlane en-US metadata with `NonFreeNet` plus screenshot placeholders

### Fixed

* **android:** Restore ❓ unknown mark with inline explanation; replace heartbeat-style copy with last updated / looks abandoned

### Changed

* **chore:** Move living agent docs into `.agent/`; GitHub About description/topics refresh for GPL FOSS discovery
* **android:** Bump app `versionName` to 1.0.0 (versionCode 10000)

## [0.38.3](https://github.com/edwardlthompson/DevPulse/compare/v0.38.2...v0.38.3) (2026-10-21)


### Fixed

* **android:** run Daily scans when due and unclunk Scan and update ([6538f32](https://github.com/edwardlthompson/DevPulse/commit/6538f328bb1db65f1084a15eeaefbeec3e0d9d3f))


### Changed

* **deps:** bump nix-action, scorecard-action, and ruff-pre-commit ([d13da16](https://github.com/edwardlthompson/DevPulse/commit/d13da16365eec68b8ae3ac023be24a9817a0b21a))


### Documentation

* sync README template badge to 0.38.2 and record KB-036 ([a168a4d](https://github.com/edwardlthompson/DevPulse/commit/a168a4db8c4ab02e83aacc48bbe107cc8a2ca2f2))

## [0.38.2](https://github.com/edwardlthompson/DevPulse/compare/v0.38.1...v0.38.2) (2026-10-05)

### Fixed

* **android:** refresh honesty, curated GitHub hints, About-only Venmo ([5d0d3c6](https://github.com/edwardlthompson/DevPulse/commit/5d0d3c6f0f7f61f497c927b1e22c7373abe1b3d4))

### Documentation

* note OpenVEX gap for release SBOM wait (KB-035) ([d7af61f](https://github.com/edwardlthompson/DevPulse/commit/d7af61f1e07e04ea1d141345332677ee218e428e))

## [0.38.1](https://github.com/edwardlthompson/DevPulse/compare/v0.38.0...v0.38.1) (2026-09-16)

### Fixed

* **android:** schedule scan intervals without auto-start ([5c93faa](https://github.com/edwardlthompson/DevPulse/commit/5c93faa7ccfb9e20656ab13b465e2eae8a6f2b31))

### Documentation

* record v0.38.0 ship notes and sync README template badge ([39dcb57](https://github.com/edwardlthompson/DevPulse/commit/39dcb57347505484774760781712312643d58a94))

## [0.38.0](https://github.com/edwardlthompson/DevPulse/compare/v0.37.2...v0.38.0) (2026-09-15)

### Added

* ship one-tap scan and update with leftover GitHub discovery ([ad6a690](https://github.com/edwardlthompson/DevPulse/commit/ad6a6908266a611c833188b430b3835468cbdb93))

### Fixed

* **android:** add Fastlane changelogs for 0.37.2 and 0.38.0 ([29e9d6a](https://github.com/edwardlthompson/DevPulse/commit/29e9d6a46672b5fe62cb69ca9c665f763a941b6d))
* **ci:** skip obsolete Android SDK tools package on setup-android ([0713455](https://github.com/edwardlthompson/DevPulse/commit/07134556f146e5cf7a67ede416833ed82366bb5f))

### Changed

* merge main before 0.38.0 ship ([b3cf81c](https://github.com/edwardlthompson/DevPulse/commit/b3cf81cf0a6419a0adb28f1f9924caed8bd67fcb))

## [0.37.2](https://github.com/edwardlthompson/DevPulse/compare/v0.37.1...v0.37.2) (2026-09-15)

### Changed

* **deps:** bump the android-dependencies group across 1 directory with 5 updates ([#14](https://github.com/edwardlthompson/DevPulse/issues/14)) ([939540c](https://github.com/edwardlthompson/DevPulse/commit/939540c7a0a9ecd8a2ec575e6e1bdbf6360ee2dc))
* **deps:** bump the github-actions group across 1 directory with 2 updates ([#28](https://github.com/edwardlthompson/DevPulse/issues/28)) ([e87f728](https://github.com/edwardlthompson/DevPulse/commit/e87f72858eb6bf5f4b0aec9598280fb8b4287938))

## [0.37.1](https://github.com/edwardlthompson/DevPulse/compare/v0.37.0...v0.37.1) (2026-08-31)

### Fixed

* **scripts:** reconfigure UTF-8 encoding on stdout/stderr in changelog_unreleased ([882c997](https://github.com/edwardlthompson/DevPulse/commit/882c99727936b5a674db4f3fe30155c3c59efb22))

### Changed

* normalize CHANGELOG unreleased heading after v0.37.0 release ([5136d4a](https://github.com/edwardlthompson/DevPulse/commit/5136d4a379e40b2290fde17c146fa00a0141a8b8))

### Documentation

* record v0.37.0 session retrospective in AGENT_MEMORY ([6f2cee7](https://github.com/edwardlthompson/DevPulse/commit/6f2cee7dc84d3cd842f6e7664cdaa7d5f2cfc41b))

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
