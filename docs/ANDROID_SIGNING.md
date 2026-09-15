# Android signing and rollback

> Upload keys stay **outside git**. This template never ships a keystore.

## Upload keystore

Create one upload keystore on a machine you control (`keytool -genkeypair`). Store the `.jks` / `.keystore` / `.p12` file outside the repo (password manager or encrypted disk). **Never commit** those files — `.gitignore` already lists `*.jks`, `*.keystore`, and `*.p12`.

Play App Signing (optional commercial store) keeps the *app signing* key on Google's side. You keep only the **upload** key. F-Droid rebuilds from source and signs with the F-Droid key; your upload key is not required there.

## Environment variables

Copy `.env.example` → `.env` (gitignored). For a local signed release:

| Variable | Role |
|----------|------|
| `GOLDENPATH_UPLOAD_STORE_FILE` | Absolute path to the upload keystore |
| `GOLDENPATH_UPLOAD_STORE_PASSWORD` | Keystore password |
| `GOLDENPATH_UPLOAD_KEY_ALIAS` | Key alias (default `upload` in Gradle) |
| `GOLDENPATH_UPLOAD_KEY_PASSWORD` | Key password |

DevPulse GitHub Release CI still accepts `DEVPULSE_STORE_FILE` / `DEVPULSE_STORE_PASSWORD` / `DEVPULSE_KEY_ALIAS` / `DEVPULSE_KEY_PASSWORD` as aliases for the same files.
`examples/android/app/build.gradle.kts` applies the `upload` signing config only when `GOLDENPATH_UPLOAD_STORE_FILE` is set. Empty or missing → release stays debug-signed (CI hash compare).

## Local signed release

```bash
export SOURCE_DATE_EPOCH=1700000000
export GOLDENPATH_UPLOAD_STORE_FILE="$HOME/keys/goldenpath-upload.jks"
# plus the three password/alias variables
cd examples/android && ./gradlew assembleRelease

```

Confirm `app/build/outputs/apk/release/` is signed (`apksigner verify`). Keep that version's `mapping.txt` with the GitHub Release.

## Continuous integration

`android-release` in `.github/workflows/ci.yml` must **not** receive store passwords. It assembles twice with `SOURCE_DATE_EPOCH=1700000000` and compares APK hashes. Production signing happens on a human-held keystore or a protected GitHub Environment — never on the default `GITHUB_TOKEN` job.

## Per-ABI release APK hash compare

When shipping ABI splits (`armeabi-v7a`, `arm64-v8a`, `x86_64`), compare hashes **per output APK**, not only the universal artifact:

```bash
export SOURCE_DATE_EPOCH=1700000000
cd examples/android
./gradlew assembleRelease
sha256sum app/build/outputs/apk/release/*.apk | tee /tmp/apk-hashes-1.txt
./gradlew clean assembleRelease
sha256sum app/build/outputs/apk/release/*.apk | tee /tmp/apk-hashes-2.txt
diff -u /tmp/apk-hashes-1.txt /tmp/apk-hashes-2.txt

```

`scripts/verify-reproducible-apk.sh` checks the primary release APK twice. Extend child CI to loop ABI outputs the same way when `splits.abi.isEnable = true`. One-ABI mismatches usually mean NDK/unstripped `.so` timestamps — pin NDK and keep `SOURCE_DATE_EPOCH`.

## CI artifact retention (reproducible APK)

| Artifact | Retention policy |
|----------|------------------|
| Unsigned release APK from `android-release` | **Do not upload** by default. The job proves reproducibility via hash compare only — retaining unsigned APKs adds little value and grows Actions storage. |
| Optional debug upload (human / fork) | If you add `actions/upload-artifact` for triage, set `retention-days: 7` (max 14 for this template). Never longer than needed to bisect a flake. |
| Signed APK / AAB + `mapping.txt` | Attach to the **GitHub Release** for that tag (not CI artifacts). Releases are the long-term store. |
| SBOM / OpenVEX | Release assets; wait with `wait-release-sbom -- --require`. |
Rationale: reproducibility is a **gate**, not a binary warehouse. Ship binaries through Releases; keep CI lean.

## F-Droid and Play

| Channel | Who signs | Rollback |
|---------|-----------|----------|
| GitHub Releases | Your upload (or debug for CI proofs) | Re-attach the previous tag's APK / AAB |
| F-Droid | F-Droid builders | Previous suggested version in the recipe |
| Play (optional) | Play App Signing after upload | Play Console → prior release; keep the same upload key |
## Rollback

1. Identify the last known-good tag (GitHub Release).
2. Re-publish that APK/AAB (do not rebuild if the key or epoch changed).
3. If users already installed a bad build, ship a **higher** `versionCode` that reverts the code, signed with the **same** upload key.
4. Retrace crashes with that version's `mapping.txt` (`app/build/outputs/mapping/release/mapping.txt`).
5. Log user-impacting incidents in `DECISION_LOG.md`.

Losing the upload key **cannot** be fixed by generating a new one for the same Play listing. Rotate only via `docs/RUNBOOK.md` Secret Rotation after a leak; then enroll a new upload key in Play Console.

## Mapping files

Archive `mapping.txt` next to each signed artifact. R8 obfuscation makes unmapped Play/F-Droid stacks useless. Do not commit mappings that embed secrets; they are build outputs (`*.apk` is already gitignored).

## Unsigned CI vs local signed

| Build | Where | Signing | Purpose |
|-------|-------|---------|---------|
| CI release assemble | GitHub Actions | Unsigned (or ephemeral) | Reproducible artifact + SBOM pairing; no upload key in CI secrets by default |
| Local upload build | Maintainer machine | `GOLDENPATH_UPLOAD_*` keystore env | Play / sideload upload only |
FOSS path: keep CI **unsigned** for bit-for-bit comparison; sign locally when distributing. Do not commit keystores. See package attestation honesty in `docs/PACKAGE_ATTESTATION.md`.
