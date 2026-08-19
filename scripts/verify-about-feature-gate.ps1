$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/verify-about-feature-gate.sh @args
exit $LASTEXITCODE
