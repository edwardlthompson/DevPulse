$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/verify-fdroid-metadata.sh @args
exit $LASTEXITCODE
