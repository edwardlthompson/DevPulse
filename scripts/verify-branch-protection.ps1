$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/verify-branch-protection.sh @args
exit $LASTEXITCODE
