# Local-first dependency updater. Dry-run by default. Never git push.
param(
    [switch]$Apply,
    [switch]$Audit,
    [switch]$DryRun
)

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

$extra = @()
if ($Apply) { $extra += "--apply" }
elseif ($Audit) { $extra += "--audit" }
elseif ($DryRun) { $extra += "--dry-run" }

& $Bash scripts/update-deps.sh @extra
exit $LASTEXITCODE
