#!/usr/bin/env bash
# Generate a Winget manifest stub for desktop binaries (template maintainer helper).
# Usage: generate-winget-manifest.sh <PackageIdentifier> <Version> [OutputDir]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

PKG_ID="${1:-Example.Project.GoldenPath}"
VERSION="${2:-0.0.0}"
OUT_DIR="${3:-packaging/winget}"

mkdir -p "$OUT_DIR"

cat > "$OUT_DIR/manifest.stub.yaml" <<EOF
# Winget manifest stub — customize before submitting to microsoft/winget-pkgs
PackageIdentifier: $PKG_ID
PackageVersion: $VERSION
PackageLocale: en-US
Publisher: Example Publisher
PackageName: Golden Path Example
License: MIT
ShortDescription: Replace with your application summary
Installers:
  - Architecture: x64
    InstallerType: exe
    InstallerUrl: https://example.com/releases/$VERSION/setup.exe
    InstallerSha256: REPLACE_WITH_SHA256
ManifestType: singleton
ManifestVersion: 1.6.0
EOF

echo "Wrote $OUT_DIR/manifest.stub.yaml"
echo "See https://github.com/microsoft/winget-pkgs for submission guidelines."
