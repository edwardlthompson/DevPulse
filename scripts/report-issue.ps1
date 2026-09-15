$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/report-issue.sh @args
exit $LASTEXITCODE
