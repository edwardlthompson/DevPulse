$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/verify-reproducible-apk.sh @args
exit $LASTEXITCODE
