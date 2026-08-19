$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/check-security-triage.sh @args
exit $LASTEXITCODE
