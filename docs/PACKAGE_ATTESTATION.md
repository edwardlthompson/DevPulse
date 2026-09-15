# Package attestation (npm, uv, GitHub)

How this template proves what was built, and how child repos publish or verify **npm** and **uv/PyPI** attestations. This is documentation only: CI does **not** fail when a registry package has no attestation (most FOSS indexes still omit them).

## What this repo already emits

`.github/workflows/release.yml` writes SLSA provenance for the release SBOM after `release` published or `workflow_dispatch`:

```yaml
- uses: actions/attest-build-provenance@<pinned> # v2.2.0
  with:
    subject-path: sbom.cyclonedx.json

```

The job needs `id-token: write` and `attestations: write`. Verify a downloaded SBOM:

```bash
gh attestation verify sbom.cyclonedx.json --repo <owner>/<repo>

```

OpenVEX travels next to the SBOM (`docs/SECURITY_TRIAGE.md`). That is **GitHub artifact** provenance, not an npm or PyPI package attestation.

## npm (when a child publishes)

Golden Path `examples/web` and `examples/node` are **not** published to the npm registry from this template. If a child repo publishes:

1. Use GitHub Actions with `id-token: write` (OIDC). Do not put an npm classic token in git.
2. Publish with provenance:

```bash
npm publish --provenance
# or, for a scoped public package:
npm publish --access public --provenance

```

3. Consumers check registry signatures (fails closed only when the package **has** signatures):

```bash
npm audit signatures

```

Do not add `--provenance` to template CI. There is no npm publish step here.

## uv / PyPI (PEP 740)

Python Golden Path install path is **lock + frozen**, not live index trust:

```bash
cd examples/python
uv lock
uv sync --frozen

```

[PEP 740](https://peps.python.org/pep-0740/) (index digital attestations) is optional extra honesty when you consume wheels from PyPI:

- Inspect attestations on the project page or Warehouse API (`/integrity`).
- Treat missing attestations as **info**, not a gate. Many wheels have none.
- Prefer `uv.lock` + `uv sync --frozen` and `python3 scripts/agent-run.py update-deps -- --audit` over requiring PEP 740 in CI.

`upd-cli==0.6.2` via `uvx` is the local bump tool (`docs/SECURITY_TRIAGE.md`). It does not publish packages.

## Honesty

| Claim | True here? |
|-------|------------|
| GitHub provenance on `sbom.cyclonedx.json` | Yes, on release |
| npm provenance on Golden Path packages | No publish job |
| uv/PyPI attestations required in CI | No |
| Local HIGH+ audit before `/ship` | Yes (`update-deps --audit`) |
## Related

| File | Role |
|------|------|
| `.github/workflows/release.yml` | SBOM + `attest-build-provenance` |
| `scripts/wait-release-sbom.sh` | Poll SBOM + OpenVEX assets |
| `docs/SECURITY_TRIAGE.md` | Weekly CVE + release security gate |
| `docs/MAINTAINING_THE_TEMPLATE.md` | Maintainer dry-run before merge |
## Release APK / AAB vs SBOM attestation

This template attests the **CycloneDX SBOM** (`sbom.cyclonedx.json`) on GitHub Releases via `actions/attest-build-provenance`. It does **not** currently attach SLSA provenance to every APK/AAB byte stream.

| Artifact | Attested here? | How to verify |
|----------|----------------|---------------|
| `sbom.cyclonedx.json` | Yes (release workflow) | `gh attestation verify sbom.cyclonedx.json --repo <owner>/<repo>` |
| Release APK / AAB (when published) | No default APK subject | Prefer checksums in release notes + reproducible unsigned CI build (`docs/ANDROID_SIGNING.md`) |
| OpenVEX | Sibling asset, not an attestation subject | Pair with SBOM triage |
Child repos that upload APKs may add a second `attest-build-provenance` step with `subject-path` pointing at the APK; keep FOSS unsigned CI builds separate from local upload-key signing.
