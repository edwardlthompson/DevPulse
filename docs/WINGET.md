# Winget publish runbook

Use this when a **child** repo ships a native Windows installer. This template ships a committed example at `packaging/winget/example/manifest.yaml` and can generate a stub. `[HUMAN]` opens the `microsoft/winget-pkgs` PR.

## Example

`packaging/winget/example/manifest.yaml` is a singleton with an `example.com` URL and a placeholder SHA-256. Validate it without submitting:

```bash
bash scripts/validate-winget-stub.sh packaging/winget/example/manifest.yaml

```

Do not file a Winget PR for this template using that example.

## Multi-arch

Ship **one installer row per architecture**. Typical Windows children list `x64` and `arm64`.

- Each row needs its own `InstallerUrl` and `InstallerSha256`.
- Do not reuse one hash for both architectures.
- Optional `x86` only when you actually ship a 32-bit build.
- Hash the Release asset after it exists. Never invent a digest.

The committed example shows both `x64` and `arm64` with placeholder hashes.

## Generate the stub

```bash
bash scripts/generate-winget-manifest.sh Example.Publisher.App 1.2.3 packaging/winget
bash scripts/validate-winget-stub.sh packaging/winget/manifest.stub.yaml

```

`generate-winget-manifest.sh` writes `PackageIdentifier`, `PackageVersion`, `License`, and `InstallerSha256`. `validate-winget-stub.sh` fails if those keys are missing. A missing file is a skip (CI generates the stub in `release.yml` before the check).

## Fill before submit

1. Replace `Example.Publisher.App` and `Example Publisher` with the real identity.
2. Set `InstallerUrl` to the GitHub Release asset (HTTPS).
3. Set `InstallerSha256` to the SHA-256 of that asset. Never invent a hash.
4. Keep `License: MIT` unless the child repo changed `LICENSE`.
5. Re-run `bash scripts/validate-winget-stub.sh packaging/winget/manifest.stub.yaml`.

Do not commit live installer URLs that embed tokens. Do not commit `.env`.

## Submit

1. Fork [microsoft/winget-pkgs](https://github.com/microsoft/winget-pkgs).
2. Add the singleton (or versioned) manifest under `manifests/<first-letter>/<Publisher>/<Package>/<Version>/`.
3. Open a PR. Winget validation bots must pass.
4. `[HUMAN]` owns the publish click. Agents may draft the YAML only.

## Local publish loop

Hash real installer files, write a stub, and validate. The loop **does not submit**.

```bash
WINGET_INSTALLER_X64=dist/app-x64.zip WINGET_INSTALLER_ARM64=dist/app-arm64.zip \
  bash scripts/winget-publish-loop.sh
# Prefer hashing real GitHub Release assets when present:
bash scripts/winget-publish-loop.sh --dry-run --from-release
# No Windows assets yet (packs a local zip):
bash scripts/winget-publish-loop.sh --dry-run

```

`--from-release` uses `gh release download` for the latest tag’s `.zip`/`.msi`/`.exe` assets when any exist. If the latest Release has **no** installers (common on this template), it prints a NOTE and falls back to the packed dry-run zip so the loop still validates.

Output: `dist/winget-loop/manifest.stub.yaml` (gitignored). `[HUMAN]` replaces `InstallerUrl` with GitHub Release HTTPS assets, then opens the `microsoft/winget-pkgs` PR.

## Template vs child

On **this** template, the stub is an example. Do not file a Winget PR for `agent-project-bootstrap`. Child desktop apps follow this runbook after `/prerelease` and a GitHub Release with the installer attached.
