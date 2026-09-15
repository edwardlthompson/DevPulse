$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& bash scripts/feedback-inbox.sh @args
exit $LASTEXITCODE
