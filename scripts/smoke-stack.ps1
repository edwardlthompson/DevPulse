$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/smoke-stack.sh @args
exit $LASTEXITCODE
