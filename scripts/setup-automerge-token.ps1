# Set repo secret AUTOMERGE_TOKEN so Dependabot merges trigger push CI.
# Usage: scripts/setup-automerge-token.ps1
# Optional: $env:AUTOMERGE_TOKEN = "ghp_..."
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/setup-automerge-token.sh @args
exit $LASTEXITCODE
