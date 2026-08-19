$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/check-readme-health.sh @args
exit $LASTEXITCODE
