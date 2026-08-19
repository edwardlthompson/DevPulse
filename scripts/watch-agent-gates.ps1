$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/watch-agent-gates.sh @args
exit $LASTEXITCODE
