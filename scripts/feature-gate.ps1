$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/feature-gate.sh @args
exit $LASTEXITCODE
