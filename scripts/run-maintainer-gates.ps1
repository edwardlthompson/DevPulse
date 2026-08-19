$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/run-maintainer-gates.sh @args
exit $LASTEXITCODE
