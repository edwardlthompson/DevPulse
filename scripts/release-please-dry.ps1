# Preview next Release Please version/changelog. Never publishes.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

$Bash = "bash"
if (-not (Get-Command $Bash -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Git\bin\bash.exe") {
        $Bash = "C:\Program Files\Git\bin\bash.exe"
    } else {
        Write-Host "ERROR: bash or Git for Windows required"
        exit 1
    }
}

& $Bash scripts/release-please-dry.sh
exit $LASTEXITCODE
