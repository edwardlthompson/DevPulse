$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/smoke-sprint.sh @args
exit $LASTEXITCODE
