$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/feature-autofix.sh @args
exit $LASTEXITCODE
