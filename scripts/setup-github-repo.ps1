# Idempotent GitHub repo security setup (PowerShell wrapper).
# Usage: scripts/setup-github-repo.ps1 [-Repo owner/name]
param(
    [string]$Repo = ""
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root
foreach ($dir in @(
        "${env:ProgramFiles}\GitHub CLI",
        "${env:ProgramFiles}\Git\bin",
        "${env:ProgramFiles}\nodejs",
        "${env:LOCALAPPDATA}\Programs\GitHub CLI"
    )) {
    if ($dir -and (Test-Path $dir)) {
        $env:PATH = "$dir;$env:PATH"
    }
}

$Bash = "bash"
if (-not (Get-Command $Bash -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Git\bin\bash.exe") {
        $Bash = "C:\Program Files\Git\bin\bash.exe"
    } else {
        Write-Host "ERROR: bash or Git for Windows required for setup-github-repo.sh"
        exit 1
    }
}

$bashArgs = @("scripts/setup-github-repo.sh")
if ($Repo) { $bashArgs += $Repo }

& $Bash @bashArgs
exit $LASTEXITCODE
